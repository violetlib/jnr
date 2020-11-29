/*
 * Copyright (c) 2015-2020 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.aqua.impl;

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.security.PrivilegedAction;

import org.violetlib.jnr.aqua.*;
import org.violetlib.jnr.impl.BasicRenderer;
import org.violetlib.jnr.impl.JNRPlatformUtils;
import org.violetlib.jnr.impl.Renderer;
import org.violetlib.jnr.impl.RendererDebugInfo;
import org.violetlib.jnr.impl.RendererDescription;
import org.violetlib.vappearances.VAppearance;

import org.jetbrains.annotations.*;

/**
  A painter that renders Aqua widgets by creating and rendering native views. This class supports only the UI for
  Yosemite (OS X 10.10) and later.

  Although using native views should mean that the rendering will be consistent with whatever release of the operating
  system is being used, there are some platform dependencies in this code (mostly layout related constants) such that
  changes to this code will be needed to support the UIs of future releases of OS X. There are also some renderings that
  cannot be supported using native views. Simulations of these renderings will not automatically update to reflect UI
  changes in not OS releases.

  This painter does not support animation frames.

  This painter does not support drawing focus rings.
*/

public class AquaNativePainter
  extends AquaUIPainterBase
  implements AquaUIPainter
{
    /*
      This painter is not completely consistent with the CoreUI painter. Some things that CoreUI can do are not
      supported here, and vice versa. Same for the JRS painter.
    */

    // NSBezelStyle
    protected static final int NSRoundedBezelStyle           = 1;
    protected static final int NSRegularSquareBezelStyle     = 2;
    protected static final int NSThickSquareBezelStyle       = 3;    // seems the same as NSRegularSquareBezelStyle
    protected static final int NSThickerSquareBezelStyle     = 4;    // seems the same as NSRegularSquareBezelStyle
    protected static final int NSDisclosureBezelStyle        = 5;
    protected static final int NSShadowlessSquareBezelStyle  = 6;
    protected static final int NSCircularBezelStyle          = 7;
    protected static final int NSTexturedSquareBezelStyle    = 8;    // seems to produce rounded corners
    protected static final int NSHelpButtonBezelStyle        = 9;
    protected static final int NSSmallSquareBezelStyle       = 10;
    protected static final int NSTexturedRoundedBezelStyle   = 11;
    protected static final int NSRoundRectBezelStyle         = 12;
    protected static final int NSRecessedBezelStyle          = 13;
    protected static final int NSRoundedDisclosureBezelStyle = 14;
    protected static final int NSInlineBezelStyle            = 15;

    // The following are internal bezel styles. They indicate that the button is on a toolbar.
    protected static final int NSCircularBezelStyle_Toolbar = 1000 + NSCircularBezelStyle;
    protected static final int NSTexturedRoundedBezelStyle_Toolbar = 1000 + NSTexturedRoundedBezelStyle;

    // NSButtonType
    protected static final int NSMomentaryLightButton         = 0;    // illuminates when pressed
    protected static final int NSPushOnPushOffButton          = 1;    // illuminated in the On state
    protected static final int NSToggleButton                 = 2;    // displays an alternate image in the On state
    protected static final int NSSwitchButton                 = 3;
    protected static final int NSRadioButton                  = 4;
    protected static final int NSMomentaryChangeButton        = 5;    // displays an alternate image when pressed
    protected static final int NSOnOffButton                  = 6;    // highlighted in the On state
    protected static final int NSMomentaryPushInButton        = 7;    // may be the same as NSMomentaryLightButton?
    protected static final int NSAcceleratorButton            = 8;
    protected static final int NSMultiLevelAcceleratorButton  = 9;

    // NSGradientType (used by NSButtonCell but not by NSButton?)
    protected static final int NSGradientNone          = 0;
    protected static final int NSGradientConcaveWeak   = 1;
    protected static final int NSGradientConcaveStrong = 2;
    protected static final int NSGradientConvexWeak    = 3;
    protected static final int NSGradientConvexStrong  = 4;

    // Text field types
    protected static final int TextFieldNormal                     = 0;
    protected static final int TextFieldRound                      = 1;
    protected static final int TextFieldSearch                     = 2;
    protected static final int TextFieldSearchWithCancel           = 3;
    protected static final int TextFieldSearchWithMenu             = 4;
    protected static final int TextFieldSearchWithMenuAndCancel    = 5;

    // The following are internal types, they indicate that the text field is on a toolbar
    protected static final int TextFieldRound_Toolbar                   = 1000 + TextFieldRound;
    protected static final int TextFieldSearch_Toolbar                  = 1000 + TextFieldSearch;
    protected static final int TextFieldSearchWithCancel_Toolbar        = 1000 + TextFieldSearchWithCancel;
    protected static final int TextFieldSearchWithMenu_Toolbar          = 1000 + TextFieldSearchWithMenu;
    protected static final int TextFieldSearchWithMenuAndCancel_Toolbar = 1000 + TextFieldSearchWithMenuAndCancel;

    // NSSegmentStyle
    protected static final int NSSegmentStyleAutomatic = 0;            // determined by window type and position in window
    protected static final int NSSegmentStyleRounded = 1;              // the default style for controls in the content area
    protected static final int NSSegmentStyleTexturedRounded = 2;      // obsolete: use NSSegmentStyleTexturedSquare
    protected static final int NSSegmentStyleRoundRect = 3;            // bordered; shorter; rounded corners - probably was used for scope options in the past
    protected static final int NSSegmentStyleTexturedSquare = 4;       // borderless; corners are rounded (recommended for window frame use)
    protected static final int NSSegmentStyleCapsule = 5;              // obsolete: use NSSegmentStyleTexturedSquare
    protected static final int NSSegmentStyleSmallSquare = 6;          // bordered; square corners; not sure how it should be used
    protected static final int NSSegmentStyleSeparated = 8;            // the generic style (not used here)
    protected static final int NSSegmentStyleSeparated_Rounded = 80;   // like rounded, but each button is separate
    protected static final int NSSegmentStyleSeparated_Textured = 81;  // like textured, but each button is separate
    protected static final int NSSegmentStyleSlider = 82;              // the macOS 11 version of Rounded for select one

    // The following are internal types, they indicate that the segmented button is on a toolbar
    protected static final int NSSegmentStyleTexturedSquare_Toolbar = 1000 + NSSegmentStyleTexturedSquare;
    protected static final int NSSegmentStyleSeparated_Toolbar = 1000 + NSSegmentStyleSeparated_Textured;

    // NSSegmentSwitchTracking
    protected static final int NSSegmentSwitchTrackingSelectOne = 0;
    protected static final int NSSegmentSwitchTrackingSelectAny = 1;

    // NSTitlePosition
    protected static final int NSNoTitle     = 0;
    protected static final int NSAboveTop    = 1;
    protected static final int NSAtTop       = 2;
    protected static final int NSBelowTop    = 3;
    protected static final int NSAboveBottom = 4;
    protected static final int NSAtBottom    = 5;
    protected static final int NSBelowBottom = 6;

    // NSSliderType
    protected static final int NSLinearSlider            = 0;
    protected static final int NSCircularSlider          = 1;
    protected static final int NSLinearSliderRightToLeft = 1002;
    protected static final int NSLinearSliderUpsideDown  = 1003;

    // NSTickMarkPosition
    protected static final int NSTickMarkBelow = 0;
    protected static final int NSTickMarkAbove = 1;
    protected static final int NSTickMarkLeft = NSTickMarkAbove;
    protected static final int NSTickMarkRight = NSTickMarkBelow;

    // NSSplitViewDividerStyle
    protected static final int NSSplitViewDividerStyleThick = 1;
    protected static final int NSSplitViewDividerStyleThin = 2;
    protected static final int NSSplitViewDividerStylePaneSplitter = 3;

    // Internal codes for control state
    protected static final int ActiveState = 0;
    protected static final int InactiveState = 1;
    protected static final int DisabledState = 2;
    protected static final int PressedState = 3;
    protected static final int DefaultState = 4;
    protected static final int RolloverState = 5;
    protected static final int DisabledInactiveState = 6;

    // Internal codes for window type
    protected static final int DocumentWindowType = 0;
    protected static final int UtilityWindowType = 1;

    // Internal codes for scroll bar type
    protected static final int LegacyScrollBar = 0;           // when the display of the scroll bar does not depend upon user gestures
    protected static final int OverlayScrollBar = 1;          // the initial rendering of an overlay scroll bar
    protected static final int RolloverOverlayScrollBar = 2;  // the rendering of an overlay scroll bar after the mouse moves over it

    static {
        java.security.AccessController.doPrivileged(new PrivilegedAction<Void>() {
            public Void run() {
                NativeSupport.load();
                return null;
            }
        });
    }

    protected static @Nullable TitleBarLayoutInfo titleBarLayoutInfo;

    public static @NotNull TitleBarLayoutInfo getTitleBarLayoutInfo()
    {
        if (titleBarLayoutInfo == null) {
            titleBarLayoutInfo = obtainTitleBarLayoutInfo();
        }
        return titleBarLayoutInfo;
    }

    private static final @NotNull ViewRendererDescriptions rendererDescriptions = new ViewRendererDescriptions();

    protected final @NotNull AquaNativeSegmentedControlPainter segmentedControlPainter;

    public AquaNativePainter()
    {
        super(rendererDescriptions);

        segmentedControlPainter = new AquaNativeSegmentedControlPainter();
    }

    @Override
    public @NotNull AquaNativePainter copy()
    {
        return new AquaNativePainter();
    }

    @Override
    public void configureAppearance(@NotNull VAppearance appearance)
    {
        super.configureAppearance(appearance);

        configureNativeAppearance(appearance);
    }

    @Override
    protected @NotNull Renderer getButtonRenderer(@NotNull ButtonConfiguration g)
    {
        ButtonWidget widget = g.getButtonWidget();
        State st = g.getState();
        ButtonState bs = g.getButtonState();

        if (widget == ButtonWidget.BUTTON_TOOLBAR_ITEM) {
            // A tool bar item button only paints a background when ON
            if (bs != ButtonState.ON) {
                return NULL_RENDERER;
            }
            ToolBarItemWellConfiguration tg = new ToolBarItemWellConfiguration(st, false);
            return getToolBarItemWellRenderer(tg);
        }

        RendererDescription rd = rendererDescriptions.getButtonRendererDescription(g);

        if (widget == ButtonWidget.BUTTON_COLOR_WELL) {
            int state = toState(st);
            BasicRenderer r = (data, rw, rh, w, h) -> nativePaintColorWell(data, rw, rh, w, h, state);
            return Renderer.create(r, rd);
        }

        if (widget == ButtonWidget.BUTTON_RECESSED) {
            // Avoid painting a background for a button in the OFF state.
            // Not sure why that would happen, but it does.
            if (!shouldPaintRecessedBackground(st, bs)) {
                return NULL_RENDERER;
            }
        }

        final ButtonWidget bw = toCanonicalButtonStyle(widget);
        int size = toSize(g.getSize());
        int state = toState(st);
        int value = toButtonValue(bs);
        int buttonType = toButtonType(bw);
        int bezelStyle = toBezelStyle(bw);
        int uiLayoutDirection = toUILayoutDirection(g.getLayoutDirection());

        BasicRenderer r = (data, rw, rh, w, h) -> nativePaintButton(data, rw, rh, w, h,
          buttonType, bezelStyle, size, state, g.isFocused(), value, uiLayoutDirection);
        return Renderer.create(r, rd);
    }

    @Override
    protected @NotNull Renderer getScrollColumnSizerRenderer(@NotNull ScrollColumnSizerConfiguration g)
    {
        RendererDescription rd = rendererDescriptions.getScrollColumnSizerRendererDescription(g);

        // obsolete
        return Renderer.createCompositeRenderer();
    }

    @Override
    protected @NotNull Renderer getScrollBarRenderer(@NotNull ScrollBarConfiguration g)
    {
        RendererDescription rd = rendererDescriptions.getScrollBarRendererDescription(g);

        // The native painter is incorrect in all cases for the overlay style. The problem is that the overlay scroll
        // bar is animated. All we can do using the public API is paint the first frame of the animation.

        // Currently, the native painter cannot paint a track without a thumb.

        ScrollBarWidget bw = g.getWidget();
        if (bw == ScrollBarWidget.OVERLAY || bw == ScrollBarWidget.OVERLAY_ROLLOVER) {
            throw new UnsupportedOperationException();
        }

        int type = toScrollBarType(bw);
        int size = toSize(g.getSize());
        int state = toState(g.getState());
        BasicRenderer r = (data, rw, rh, w, h) -> nativePaintScrollBar(data, rw, rh, w, h,
          type, size, state, g.getThumbPosition(), g.getThumbExtent());
        return Renderer.create(r, rd);
    }

    @Override
    protected @NotNull Renderer getToolBarItemWellRenderer(@NotNull ToolBarItemWellConfiguration g)
    {
        RendererDescription rd = rendererDescriptions.getToolBarItemWellRendererDescription(g);

        int state = toState(g.getState());
        BasicRenderer r = (data, rw, rh, w, h) -> nativePaintToolBarItemWell(data, rw, rh, w, h,
          state, g.isFrameOnly());
        return Renderer.create(r, rd);
    }

    @Override
    protected @NotNull Renderer getGroupBoxRenderer(@NotNull GroupBoxConfiguration g)
    {
        RendererDescription rd = rendererDescriptions.getGroupBoxRendererDescription(g);

        int state = toState(g.getState());
        BasicRenderer r = (data, rw, rh, w, h) -> nativePaintGroupBox(data, rw, rh, w, h,
          NSNoTitle, state, g.isFrameOnly());
        return Renderer.create(r, rd);
    }

    @Override
    protected @NotNull Renderer getListBoxRenderer(@NotNull ListBoxConfiguration g)
    {
        RendererDescription rd = rendererDescriptions.getListBoxRendererDescription(g);

        int state = toState(g.getState());
        BasicRenderer r = (data, rw, rh, w, h) -> nativePaintListBox(data, rw, rh, w, h,
          state, g.isFocused(), g.isFrameOnly());
        return Renderer.create(r, rd);
    }

    @Override
    protected @NotNull Renderer getTextFieldRenderer(@NotNull TextFieldConfiguration g)
    {
        RendererDescription rd = rendererDescriptions.getTextFieldRendererDescription(g);

        // TBD: the isFrameOnly option is not working, do we need it?

        int type;

        switch (g.getWidget()) {
            case TEXT_FIELD:
                type = TextFieldNormal;
                break;
            case TEXT_FIELD_ROUND:
                type = TextFieldRound;
                break;
            case TEXT_FIELD_SEARCH:
                type = TextFieldSearch;
                break;
            case TEXT_FIELD_SEARCH_WITH_CANCEL:
                type = TextFieldSearchWithCancel;
                break;
            case TEXT_FIELD_SEARCH_WITH_MENU:
                type = TextFieldSearchWithMenu;
                break;
            case TEXT_FIELD_SEARCH_WITH_MENU_AND_CANCEL:
                type = TextFieldSearchWithMenuAndCancel;
                break;
            case TEXT_FIELD_ROUND_TOOLBAR:
                type = TextFieldRound_Toolbar;
                break;
            case TEXT_FIELD_SEARCH_TOOLBAR:
                type = TextFieldSearch_Toolbar;
                break;
            case TEXT_FIELD_SEARCH_WITH_CANCEL_TOOLBAR:
                type = TextFieldSearchWithCancel_Toolbar;
                break;
            case TEXT_FIELD_SEARCH_WITH_MENU_TOOLBAR:
                type = TextFieldSearchWithMenu_Toolbar;
                break;
            case TEXT_FIELD_SEARCH_WITH_MENU_AND_CANCEL_TOOLBAR:
                type = TextFieldSearchWithMenuAndCancel_Toolbar;
                break;
            default:
                throw new UnsupportedOperationException();
        }

        int size = toSize(g.getSize());
        int state = toState(g.getState());
        BasicRenderer r = (data, rw, rh, w, h) -> nativePaintTextField(data, rw, rh, w, h, size, state, type);
        return Renderer.create(r, rd);
    }

    @Override
    protected @NotNull Renderer getComboBoxButtonRenderer(@NotNull ComboBoxConfiguration g)
    {
        RendererDescription rd = rendererDescriptions.getComboBoxRendererDescription(g);

        int size = toSize(g.getSize());
        int state = toState(g.getState());
        int type = toComboBoxType(g.getWidget());
        int bezel = toBezelStyle(g.getWidget());
        int ld = toUILayoutDirection(g.getLayoutDirection());
        BasicRenderer r = (data, rw, rh, w, h) -> nativePaintComboBox(data, rw, rh, w, h, type, size, state, bezel, ld);
        return Renderer.create(r, rd);
    }

    @Override
    protected @NotNull Renderer getPopupButtonRenderer(@NotNull PopupButtonConfiguration g)
    {
        // On Yosemite, the square style bombs if the mini size is selected.
        // This restriction is currently handled in the configuration.

        RendererDescription rd = rendererDescriptions.getPopupButtonRendererDescription(g);

        PopupButtonWidget bw = g.getPopupButtonWidget();
        boolean isUp = g.isPopUp();
        int size = toSize(g.getSize());
        int state = toState(g.getState());
        int ld = toUILayoutDirection(g.getLayoutDirection());
        int bezelStyle = toBezelStyle(bw);

        BasicRenderer r = (data, rw, rh, w, h) -> nativePaintPopUpButton(data, rw, rh, w, h,
          isUp, size, state, bezelStyle, ld);
        return Renderer.create(r, rd);
    }

    @Override
    protected @NotNull Renderer getTitleBarRenderer(@NotNull TitleBarConfiguration g)
    {
        RendererDescription rd = rendererDescriptions.getTitleBarRendererDescription(g);

        int windowType = toWindowType(g.getWidget());
        int state = toActiveState(g.getTitleBarState());

        /*
          The native painter can force a button to paint as inactive. However, if the title bar is inactive, it cannot
          force a button to paint as active.
        */

        int closeState = toState(g.getCloseButtonState());
        int minimizeState = toState(g.getMinimizeButtonState());
        int resizeState = toState(g.getResizeButtonState());

        TitleBarConfiguration.ResizeAction resizeAction = g.getResizeAction();
        boolean isFullScreen = resizeAction == TitleBarConfiguration.ResizeAction.FULL_SCREEN_ENTER
          || resizeAction == TitleBarConfiguration.ResizeAction.FULL_SCREEN_EXIT;
        // TBD: pass encoded resize action
        BasicRenderer r = (data, rw, rh, w, h) -> nativePaintTitleBar(data, rw, rh, w, h,
          windowType, state, closeState, minimizeState, resizeState, isFullScreen, g.isDirty());
        return Renderer.create(r, rd);
    }

    @Override
    protected @NotNull Renderer getIndeterminateProgressIndicatorRenderer(@NotNull IndeterminateProgressIndicatorConfiguration g)
    {
        RendererDescription rd = rendererDescriptions.getIndeterminateProgressIndicatorRendererDescription(g);

        boolean isSpinner = g.getWidget() == ProgressWidget.INDETERMINATE_SPINNER;
        Size sz = g.getSize();

        // Small spinners have a fixed size. Large spinners are scaled to fit. Other variants do not work.
        if (isSpinner && sz != Size.SMALL) {
            sz = Size.LARGE;
        }

        int size = toSize(sz);
        int state = toState(g.getState());
        int orientation = toOrientation(g.getOrientation());
        BasicRenderer r = (data, rw, rh, w, h) -> nativePaintIndeterminateProgressIndicator(data, rw, rh, w, h,
          size, state, orientation, isSpinner, g.getAnimationFrame());
        return Renderer.create(r, rd);
    }

    @Override
    protected @NotNull Renderer getProgressIndicatorRenderer(@NotNull ProgressIndicatorConfiguration g)
    {
        RendererDescription rd = rendererDescriptions.getProgressIndicatorRendererDescription(g);

        boolean isSpinner = g.getWidget() == ProgressWidget.SPINNER;
        Size sz = g.getSize();

        // Small spinners have a fixed size. Large spinners are scaled to fit. Other variants do not work.
        if (isSpinner && sz != Size.SMALL) {
            sz = Size.LARGE;
        }

        int size = toSize(g.getSize());
        int state = toActiveState(g.getState());
        int orientation = toOrientation(g.getOrientation());
        BasicRenderer r = (data, rw, rh, w, h) -> nativePaintProgressIndicator(data, rw, rh, w, h,
          size, state, orientation, g.getValue());
        return Renderer.create(r, rd);
    }

    @Override
    protected @NotNull Renderer getSliderRenderer(@NotNull SliderConfiguration g)
    {
        RendererDescription rd = rendererDescriptions.getSliderRendererDescription(g);

        int platformVersion = JNRPlatformUtils.getPlatformVersion();

        SliderWidget sw = g.getWidget();
        Size sz = g.getSize();

        // Mini sliders were not supported in older releases (not sure when that changed)
        if (g.getSize() == Size.MINI && platformVersion < 101400) {
            sz = Size.SMALL;
        }

        // NSSlider appears to figure out the orientation from the bounds.

        if (sw == SliderWidget.SLIDER_HORIZONTAL || sw == SliderWidget.SLIDER_HORIZONTAL_RIGHT_TO_LEFT) {
            if (h >= w) {
                h = Math.max(0, w - 1);
            }
        } else if (sw == SliderWidget.SLIDER_VERTICAL || sw == SliderWidget.SLIDER_UPSIDE_DOWN) {
            if (w >= h) {
                w = Math.max(0, h - 1);
            }
            forceVertical = true;
        }

        int sliderType = toSliderType(sw);
        int size = toSize(sz);
        int state = toState(g.getState());
        int position = toTickMarkPosition(g.getTickMarkPosition());
        BasicRenderer r = (data, rw, rh, w, h) -> nativePaintSlider(data, rw, rh, w, h,
          sliderType, size, state, g.isFocused(), g.getValue(), g.getNumberOfTickMarks(), position);
        return Renderer.create(r, rd);
    }

    protected @NotNull Shape getSliderThumbOutline(@NotNull Rectangle2D bounds, @NotNull SliderThumbLayoutConfiguration g)
    {
        SliderLayoutConfiguration sg = g.getSliderLayoutConfiguration();
        double thumbPosition = g.getThumbPosition();

        double x = bounds.getX();
        double y = bounds.getY();
        double w = bounds.getWidth();
        double h = bounds.getHeight();

        SliderWidget sw = sg.getWidget();
        if (sw == SliderWidget.SLIDER_CIRCULAR) {
            // Use native code to get the information
            int sliderType = toSliderType(sw);
            int size = toSize(sg.getSize());
            int position = toTickMarkPosition(sg.getTickMarkPosition());
            float[] a = new float[4];
            int width = (int) Math.ceil(w);    // TBD: could be a problem
            int height = (int) Math.ceil(h);
            nativeGetSliderThumbBounds(a, width, height, sliderType, size, thumbPosition,
              sg.getNumberOfTickMarks(), position);

            float tx = a[0];
            float ty = a[1];
            float tw = a[2];
            float th = a[3];

            return new Rectangle2D.Double(tx, ty, tw, th);
        } else {
            // The bounds returned for a knob are not centered relative to the knob along the major axis. Those bounds
            // are good enough for hit detection, but not for focus rings. So, instead we calculate a shape.
            return super.getOutline(bounds, g);
        }
    }

    @Override
    protected @NotNull Renderer getSliderThumbRenderer(@NotNull SliderConfiguration g)
    {
        return NULL_RENDERER;
    }

    @Override
    protected @NotNull Renderer getSliderTickRenderer(@NotNull SliderTickConfiguration g)
    {
        return NULL_RENDERER;
    }

    @Override
    protected @NotNull Renderer getSpinnerArrowsRenderer(@NotNull SpinnerArrowsConfiguration g)
    {
        RendererDescription rd = rendererDescriptions.getSpinnerArrowsRendererDescription(g);

        int size = toSize(g.getSize());
        int state = toState(g.getState());
        BasicRenderer r = (data, rw, rh, w, h) -> nativePaintSpinnerArrows(data, rw, rh, w, h,
          size, state, g.isFocused(), g.isPressedTop());
        return Renderer.create(r, rd);
    }

    @Override
    protected @NotNull Renderer getSplitPaneDividerRenderer(@NotNull SplitPaneDividerConfiguration g)
    {
        RendererDescription rd = rendererDescriptions.getSplitPaneDividerRendererDescription(g);

        int dividerType = toDividerType(g.getWidget());
        int state = toState(g.getState());
        int orientation = toOrientation(g.getOrientation());
        BasicRenderer r = (data, rw, rh, w, h) -> nativePaintSplitPaneDivider(data, rw, rh, w, h,
          dividerType, state, orientation, g.getThickness());
        return Renderer.create(r, rd);
    }

    @Override
    protected @NotNull Renderer getGradientRenderer(@NotNull GradientConfiguration g)
    {
        RendererDescription rd = rendererDescriptions.getGradientRendererDescription(g);

        throw new UnsupportedOperationException();
    }

    @Override
    protected @NotNull Renderer getSegmentedButtonRenderer(@NotNull SegmentedButtonConfiguration g)
    {
        return segmentedControlPainter.createSegmentedButtonRenderer(g);
    }

    @Override
    public @NotNull Renderer getTableColumnHeaderRenderer(@NotNull TableColumnHeaderConfiguration g)
    {
        RendererDescription rd = rendererDescriptions.getTableColumnHeaderRendererDescription(g);

        // It seems that NSTableView is willing to paint a sort arrow whether or not the column is selected.
        // RTL layout direction not working

        int state = toState(g.getState());
        int direction = toDirection(g.getSortArrowDirection());
        int layoutDirection = toUILayoutDirection(g.getLayoutDirection());

        BasicRenderer r = (data, rw, rh, w, h) -> nativePaintTableColumnHeader(data, rw, rh, w, h,
          state, direction, g.isSelected(), layoutDirection);
        return Renderer.create(r, rd);
    }

    @Override
    public @Nullable RendererDebugInfo getRendererDebugInfo(
      @NotNull Configuration g, int scaleFactor, int width, int height)
    {
        if (g instanceof SegmentedButtonConfiguration) {
            SegmentedButtonConfiguration bg = (SegmentedButtonConfiguration) g;
            return segmentedControlPainter.getSegmentedButtonRendererDebugInfo(bg, scaleFactor, width, height);
        }
        return null;
    }

    protected static int toSize(@NotNull Size sz)
    {
        switch (sz)
        {
            case MINI:
                return 0;
            case SMALL:
                return 1;
            case REGULAR:
                return 2;
            case LARGE:
                return 3;
        }
        throw new UnsupportedOperationException();
    }

    /**
      Map the specified state to the integer encoding used by native code. All states are supported.
    */

    protected static int toState(@NotNull State st)
    {
        switch (st)
        {
            case ACTIVE:
                return ActiveState;
            case INACTIVE:
                return InactiveState;
            case DISABLED:
                return DisabledState;
            case DISABLED_INACTIVE:
                return DisabledInactiveState;
            case PRESSED:
                return PressedState;
            case ACTIVE_DEFAULT:
                return DefaultState;
            case ROLLOVER:
                return RolloverState;
        }
        throw new UnsupportedOperationException();
    }

    /**
      Map the specified state to the integer encoding used by native code. Only the active and inactive states are
      supported.
    */

    protected int toActiveState(@NotNull State st)
    {
        switch (st)
        {
            case ACTIVE:
                return ActiveState;
            case INACTIVE:
            case DISABLED:
            case DISABLED_INACTIVE:
                return InactiveState;
        }
        return ActiveState;
    }

    protected int toDirection(@NotNull ColumnSortArrowDirection d)
    {
        switch (d)
        {
            case NONE:
                return 0;
            case UP:
                return 1;
            case DOWN:
                return 2;
            default:
                throw new IllegalArgumentException();
        }
    }

    protected int toDirection(@NotNull Direction d)
    {
        switch (d)
        {
            case NONE:
                return 0;
            case UP:
                return 1;
            case DOWN:
                return 2;
            case LEFT:
                return 3;
            case RIGHT:
                return 4;
            default:
                throw new IllegalArgumentException();
        }
    }

    protected int toOrientation(@NotNull Orientation o)
    {
        switch (o)
        {
            case HORIZONTAL:
                return 0;
            case VERTICAL:
                return 1;
        }
        throw new UnsupportedOperationException();
    }

    public int getSideInset(@NotNull SegmentedButtonWidget bw)
    {
        int style = toSegmentedStyle(bw);
        return getSideInsetFromSegmentStyle(style);
    }

    public int getDividerWidth(@NotNull SegmentedButtonWidget bw)
    {
        int style = toSegmentedStyle(bw);
        return getDividerWidthFromSegmentStyle(style);
    }

    protected int getSideInsetFromSegmentStyle(int style)
    {
        switch (style) {
            case NSSegmentStyleTexturedRounded:
            case NSSegmentStyleTexturedSquare:
                return 1;
            case NSSegmentStyleSmallSquare:
                return 1;
        }
        return 3;
    }

    protected int getDividerWidthFromSegmentStyle(int style)
    {
        switch (style) {
            case NSSegmentStyleTexturedRounded:
                return 2;
            case NSSegmentStyleSmallSquare:
                return 1;
        }
        return 1;
    }

    protected static int toSegmentedStyle(@NotNull SegmentedButtonWidget bw)
    {
        switch (bw) {
            case BUTTON_TAB:
            case BUTTON_SEGMENTED:
                return NSSegmentStyleRounded;
            case BUTTON_SEGMENTED_INSET:
                return NSSegmentStyleRoundRect;
            case BUTTON_SEGMENTED_SCURVE:
                return NSSegmentStyleCapsule;
            case BUTTON_SEGMENTED_TEXTURED:
                return NSSegmentStyleTexturedSquare;
            case BUTTON_SEGMENTED_TEXTURED_TOOLBAR:
            case BUTTON_SEGMENTED_TEXTURED_TOOLBAR_ICONS:
                return NSSegmentStyleTexturedSquare_Toolbar;
            case BUTTON_SEGMENTED_TOOLBAR:
                return NSSegmentStyleTexturedRounded;
            case BUTTON_SEGMENTED_SMALL_SQUARE:
                return NSSegmentStyleSmallSquare;
            case BUTTON_SEGMENTED_SEPARATED:
                return NSSegmentStyleSeparated_Rounded;
            case BUTTON_SEGMENTED_TEXTURED_SEPARATED:
                return NSSegmentStyleSeparated_Textured;
            case BUTTON_SEGMENTED_TEXTURED_SEPARATED_TOOLBAR:
            case BUTTON_SEGMENTED_TEXTURED_SEPARATED_TOOLBAR_ICONS:
                return NSSegmentStyleSeparated_Toolbar;
            case BUTTON_SEGMENTED_SLIDER:
            case BUTTON_SEGMENTED_SLIDER_TOOLBAR:
            case BUTTON_SEGMENTED_SLIDER_TOOLBAR_ICONS:
                // same as rounded with the implication of select one behavior
                return NSSegmentStyleRounded;
        }
        throw new UnsupportedOperationException();
    }

    // Note: This is an internal hack; NSComboBox does not use bezel style.
    protected int toBezelStyle(@NotNull ComboBoxWidget w)
    {
        switch (w) {
            case BUTTON_COMBO_BOX:
                return NSRoundedBezelStyle;
            case BUTTON_COMBO_BOX_CELL:
                return 0;
            case BUTTON_COMBO_BOX_TEXTURED:
                return NSTexturedRoundedBezelStyle;
            case BUTTON_COMBO_BOX_TEXTURED_TOOLBAR:
                return NSTexturedRoundedBezelStyle_Toolbar;
        }
        throw new UnsupportedOperationException();
    }

    protected int toBezelStyle(@NotNull PopupButtonWidget bw)
    {
        switch (bw) {
            case BUTTON_POP_DOWN:
            case BUTTON_POP_UP:
                return NSRoundedBezelStyle;

            case BUTTON_POP_DOWN_CELL:
            case BUTTON_POP_UP_CELL:
                return 0;

            case BUTTON_POP_DOWN_BEVEL:
            case BUTTON_POP_UP_BEVEL:
                return NSRegularSquareBezelStyle;

            case BUTTON_POP_DOWN_ROUND_RECT:
            case BUTTON_POP_UP_ROUND_RECT:
                return NSRoundRectBezelStyle;  // no background

            case BUTTON_POP_DOWN_RECESSED:
            case BUTTON_POP_UP_RECESSED:
                return NSRecessedBezelStyle;

            case BUTTON_POP_DOWN_TEXTURED:
            case BUTTON_POP_UP_TEXTURED:
                return NSTexturedRoundedBezelStyle;

            case BUTTON_POP_DOWN_TEXTURED_TOOLBAR:
            case BUTTON_POP_UP_TEXTURED_TOOLBAR:
                return NSTexturedRoundedBezelStyle_Toolbar;

            case BUTTON_POP_DOWN_GRADIENT:
            case BUTTON_POP_UP_GRADIENT:
                return NSSmallSquareBezelStyle;

            case BUTTON_POP_DOWN_SQUARE:
            case BUTTON_POP_UP_SQUARE:
                return NSShadowlessSquareBezelStyle;
        }
        throw new UnsupportedOperationException();
    }

    protected int toBezelStyle(@NotNull ButtonWidget bw)
    {
        int platformVersion = JNRPlatformUtils.getPlatformVersion();

        switch (bw) {
            case BUTTON_PUSH:
                return NSRoundedBezelStyle;
            case BUTTON_BEVEL:
                return NSShadowlessSquareBezelStyle;
            case BUTTON_BEVEL_ROUND:
                return NSRegularSquareBezelStyle;  // highlight state is not as dark as CoreUI
            case BUTTON_CHECK_BOX:
                return NSSwitchButton;
            case BUTTON_RADIO:
                return NSRadioButton;
            case BUTTON_DISCLOSURE:
                return NSRoundedDisclosureBezelStyle;
            case BUTTON_DISCLOSURE_TRIANGLE:
                return NSDisclosureBezelStyle;
            case BUTTON_HELP:
                return NSHelpButtonBezelStyle;
            case BUTTON_GRADIENT:
                return NSSmallSquareBezelStyle;
            case BUTTON_RECESSED:
                return NSRecessedBezelStyle;
            case BUTTON_INLINE:
                return NSInlineBezelStyle;  // should limit height, needs gradient background
            case BUTTON_ROUNDED_RECT:
                return NSRoundRectBezelStyle;  // no background
            case BUTTON_TEXTURED:
                return NSTexturedRoundedBezelStyle;
            case BUTTON_TEXTURED_TOOLBAR:
            case BUTTON_TEXTURED_TOOLBAR_ICONS:
                return NSTexturedRoundedBezelStyle_Toolbar;
            case BUTTON_ROUND:
                return NSCircularBezelStyle;
            case BUTTON_ROUND_TEXTURED:
            case BUTTON_ROUND_TEXTURED_TOOLBAR:
                return platformVersion >= 101100 ? NSCircularBezelStyle_Toolbar : NSCircularBezelStyle;
        }
        throw new UnsupportedOperationException();
    }

    protected int toButtonType(@NotNull ButtonWidget bw)
    {
        switch (bw) {
            case BUTTON_CHECK_BOX:
                return NSSwitchButton;
            case BUTTON_RADIO:
                return NSRadioButton;
            case BUTTON_RECESSED:
            case BUTTON_INLINE:
            case BUTTON_DISCLOSURE:
            case BUTTON_DISCLOSURE_TRIANGLE:
                return NSPushOnPushOffButton;
        }

        return NSMomentaryLightButton;
    }

    protected int toButtonValue(@NotNull ButtonState bs)
    {
        switch (bs)
        {
            case ON:
                return 1;
            case MIXED:
                return 2;
            default:
                return 0;
        }
    }

    protected static int toTracking(@NotNull SwitchTracking t)
    {
        if (t == SwitchTracking.SELECT_ANY) {
            return NSSegmentSwitchTrackingSelectAny;
        }
        return NSSegmentSwitchTrackingSelectOne;
    }

    protected int toUILayoutDirection(@NotNull UILayoutDirection d)
    {
        switch (d)
        {
            case LEFT_TO_RIGHT:
                return 0;
            case RIGHT_TO_LEFT:
                return 1;
            default:
                return 0;
        }
    }

    protected int toSliderType(@NotNull SliderWidget w)
    {
        switch (w)
        {
            case SLIDER_HORIZONTAL:
                return NSLinearSlider;
            case SLIDER_HORIZONTAL_RIGHT_TO_LEFT:
                return NSLinearSliderRightToLeft;
            case SLIDER_VERTICAL:
                return NSLinearSlider;
            case SLIDER_UPSIDE_DOWN:
                return NSLinearSliderUpsideDown;
            case SLIDER_CIRCULAR:
                return NSCircularSlider;
            default:
                return NSLinearSlider;
        }
    }

    protected int toTickMarkPosition(@NotNull TickMarkPosition p)
    {
        switch (p)
        {
            case LEFT:
                return NSTickMarkLeft;
            case RIGHT:
                return NSTickMarkRight;
            case BELOW:
                return NSTickMarkBelow;
            case ABOVE:
                return NSTickMarkAbove;
            default:
                return NSTickMarkLeft;
        }
    }

    protected int toDividerType(@NotNull DividerWidget w)
    {
        switch (w)
        {
            case PANE_SPLITTER:
                return NSSplitViewDividerStylePaneSplitter;
            case THIN_DIVIDER:
                return NSSplitViewDividerStyleThin;
            case THICK_DIVIDER:
                return NSSplitViewDividerStyleThick;
            default:
                return NSSplitViewDividerStylePaneSplitter;
        }
    }

    protected int toComboBoxType(@NotNull ComboBoxWidget bw)
    {
        switch (bw)
        {
            case BUTTON_COMBO_BOX:
            default:
                return 0;
            case BUTTON_COMBO_BOX_CELL:
                return 2;
        }
    }

    protected static int toWindowType(@NotNull TitleBarWidget bw)
    {
        switch (bw)
        {
            case DOCUMENT_WINDOW:
                return DocumentWindowType;
            case UTILITY_WINDOW:
                return UtilityWindowType;
            default:
                throw new UnsupportedOperationException();
        }
    }

    protected static int toScrollBarType(@NotNull ScrollBarWidget bw)
    {
        switch (bw)
        {
            case LEGACY:
                return LegacyScrollBar;
            case OVERLAY:
                return OverlayScrollBar;
            case OVERLAY_ROLLOVER:
                return RolloverOverlayScrollBar;
            default:
                throw new UnsupportedOperationException();
        }
    }

    private static @NotNull TitleBarLayoutInfo obtainTitleBarLayoutInfo()
    {
        // Not sure how useful this is, but we can get button locations from native code.

        Rectangle[] documentButtonBounds = obtainTitleBarButtonLayoutInfo(TitleBarWidget.DOCUMENT_WINDOW);
        Rectangle[] utilityButtonBounds = obtainTitleBarButtonLayoutInfo(TitleBarWidget.UTILITY_WINDOW);
        return new TitleBarLayoutInfo(documentButtonBounds, utilityButtonBounds);
    }

    private static @NotNull Rectangle[] obtainTitleBarButtonLayoutInfo(@NotNull TitleBarWidget bw)
    {
        int windowType = toWindowType(bw);
        int[] data = nativeGetTitleBarButtonLayoutInfo(windowType);
        if (data != null) {
            Rectangle close = new Rectangle(data[0], data[1], data[2], data[3]);
            Rectangle minimize = new Rectangle(data[4], data[5], data[6], data[7]);
            Rectangle resize = new Rectangle(data[8], data[9], data[10], data[11]);
            return new Rectangle[] { close, minimize, resize };
        } else {
            int x = bw == TitleBarWidget.DOCUMENT_WINDOW ? 7 : 5;
            int sep = bw == TitleBarWidget.DOCUMENT_WINDOW ? 6 : 5;
            int w = bw == TitleBarWidget.DOCUMENT_WINDOW ? 14 : 13;
            int h = bw == TitleBarWidget.DOCUMENT_WINDOW ? 16 : 14;
            int y = 3;
            Rectangle close = new Rectangle(x, y, w, h);
            x += w + sep;
            Rectangle minimize = new Rectangle(x, y, w, h);
            x += w + sep;
            Rectangle resize = new Rectangle(x, y, w, h);
            return new Rectangle[] { close, minimize, resize };
        }
    }

    @Override
    public @NotNull String toString()
    {
        return "NSView";
    }

    private static native void nativePaintIndeterminateProgressIndicator(int[] data, int rw, int rh, float w, float h, int size, int state, int orientation, boolean isSpinner, int frame);
    private static native void nativePaintProgressIndicator(int[] data, int rw, int rh, float w, float h, int size, int state, int orientation, double value);
    private static native void nativePaintButton(int[] data, int rw, int rh, float w, float h, int buttonType, int bezelStyle, int size, int state, boolean isFocused, int value, int layoutDirection);
    private static native void nativePaintColorWell(int[] data, int rw, int rh, float w, float h, int state);
    private static native void nativePaintToolBarItemWell(int[] data, int rw, int rh, float w, float h, int state, boolean isFrameOnly);
    private static native void nativePaintGroupBox(int[] data, int rw, int rh, float w, float h, int titlePosition, int state, boolean isFrameOnly);
    private static native void nativePaintListBox(int[] data, int rw, int rh, float w, float h, int state, boolean isFocused, boolean isFrameOnly);
    private static native void nativePaintTextField(int[] data, int rw, int rh, float w, float h, int sz, int state, int type);
    private static native void nativePaintComboBox(int[] data, int rw, int rh, float w, float h, int type, int size, int state, int bezelStyle, int layoutDirection);
    private static native void nativePaintPopUpButton(int[] data, int rw, int rh, float w, float h, boolean isUp, int size, int state, int bezelStyle, int layoutDirection);
    private static native void nativePaintTableColumnHeader(int[] data, int rw, int rh, float w, float h, int state, int direction, boolean isSelected, int layoutDirection);
    private static native void nativePaintSlider(int[] data, int rw, int rh, float w, float h, int sliderType, int size, int state, boolean isFocused, double value, int numberOfTickMarks, int position);
    private static native void nativePaintSpinnerArrows(int[] data, int rw, int rh, float w, float h, int sz, int state, boolean isFocused, boolean isPressedTop);
    private static native void nativePaintSplitPaneDivider(int[] data, int rw, int rh, float w, float h, int type, int state, int orientation, int thickness);
    private static native void nativePaintTitleBar(int[] data, int rw, int rh, float w, float h, int windowType, int state, int closeState, int minimizeState, int resizeState, boolean resizeIsFullScreen, boolean isDirty);
    private static native void nativePaintScrollBar(int[] data, int rw, int rh, float w, float h, int type, int size, int state, float thumbPosition, float thumbExtent);

    private static native int[] nativeGetTitleBarButtonLayoutInfo(int windowType);
    private static native void nativeGetSliderThumbBounds(float[] a, float w, float h, int sliderType, int size, double value, int numberOfTickMarks, int position);

    public static native boolean isLayerPaintingEnabled();
    public static native void setLayerPaintingEnabled(boolean b);

    public static native int nativeDetermineSliderRenderingVersion();

    // The following methods represent a failed experiment. Although I am not sure why, asking a view for its size does
    // not always provide the information needed here.

    private static native int nativeDetermineButtonFixedHeight(int buttonType, int bezelStyle, int size);
    private static native int nativeDetermineButtonFixedWidth(int buttonType, int bezelStyle, int size);
}
