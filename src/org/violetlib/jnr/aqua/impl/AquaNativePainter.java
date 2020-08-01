/*
 * Copyright (c) 2015-2020 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.aqua.impl;

import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.lang.annotation.Native;
import java.security.PrivilegedAction;

import org.violetlib.jnr.aqua.*;
import org.violetlib.jnr.impl.*;
import org.violetlib.vappearances.VAppearance;

import org.jetbrains.annotations.*;

import static org.violetlib.jnr.aqua.SegmentedButtonConfiguration.*;
import static org.violetlib.jnr.aqua.impl.SegmentedControl4LayoutInfo.*;
import static org.violetlib.jnr.aqua.impl.SegmentedControl4LayoutInfo.DividerPosition.*;

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

    public AquaNativePainter()
    {
        super(rendererDescriptions, createLayout(true));
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

    // array indexes for debugging output

    public static final int DEBUG_SEGMENT_WIDTH = 0;
    public static final int DEBUG_SEGMENT_HEIGHT = 1;
    public static final int DEBUG_SEGMENT_X_OFFSET = 2;
    public static final int DEBUG_SEGMENT_Y_OFFSET = 3;
    public static final int DEBUG_SEGMENT_DIVIDER_WIDTH = 4;
    public static final int DEBUG_SEGMENT_OUTER_LEFT_INSET = 5;
    public static final int DEBUG_SEGMENT_LEFT_INSET = 6;
    public static final int DEBUG_SEGMENT_RIGHT_INSET = 7;

    protected @NotNull Renderer getOldSegmentedButtonRenderer(@NotNull SegmentedButtonConfiguration g)
    {
        RendererDescription rd = rendererDescriptions.getSegmentedButtonRendererDescription(g);
        int size = toSize(g.getSize());
        int state = toState(g.getState());
        int segmentStyle = toSegmentedStyle(g.getWidget());
        int segmentPosition = toSegmentPosition(g.getPosition());
        int flags = toSegmentFlags(g);

        BasicRenderer r = (data, rw, rh, w, h) -> nativePaintSegmentedButton(data, rw, rh, w, h, segmentStyle,
          segmentPosition, size, state, g.isFocused(), flags, null, null);

        return Renderer.create(r, rd);
    }

    @Override
    protected @NotNull Renderer getSegmentedButtonRenderer(@NotNull SegmentedButtonConfiguration g)
    {
        return new SegmentedButtonRenderer(g);
    }

    private class SegmentedButtonRenderer
      extends Renderer
    {
        private final @NotNull SegmentedButtonConfiguration g;

        public SegmentedButtonRenderer(@NotNull SegmentedButtonConfiguration g)
        {
            this.g = g;
        }

        @Override
        public void composeTo(@NotNull ReusableCompositor compositor)
        {
            SegmentedControlDescriptions scds = new SegmentedControlDescriptions();
            int scale = compositor.getScaleFactor();
            int buttonWidth = Math.round(compositor.getWidth());
            int buttonHeight = Math.round(compositor.getHeight());
            RenderInsets s = scds.getInsets(g, scale);
            int rasterWidth;
            int rasterHeight;
            int[] raster;
            int x;
            int y;
            int w;
            int h;
            if (g.getPosition() == Position.ONLY) {
                SegmentedControl1LayoutInfo layout = scds.getSegment1LayoutInfo(g, scale);
                SegmentButtonRenderingConfiguration1 bc
                  = getRenderConfiguration(g.isSelected(), s, layout, scale, buttonWidth, buttonHeight);
                SegmentedControlConfiguration1 cc = createControlConfiguration(g, bc);
                rasterWidth = Math.round(scale * bc.rasterWidth);
                rasterHeight = Math.round(scale * bc.rasterHeight);
                raster = new int[rasterWidth * rasterHeight];
                paintSegmentedControl1(raster, rasterWidth, rasterHeight, scale, cc, false);
                x = Math.round(scale * s.left);
                y = Math.round(scale * s.top);
                w = Math.round(scale * buttonWidth);
                h = Math.round(scale * buttonHeight);
            } else {
                SegmentedControl4LayoutInfo layout = scds.getSegment4LayoutInfo(g, scale);
                SegmentButtonRenderingConfiguration4 bc
                  = getRenderConfiguration(g, s, layout, scale, buttonWidth, buttonHeight);
                SegmentedControlConfiguration4 cc = createControlConfiguration(g, bc);
                rasterWidth = Math.round(scale * bc.rasterWidth);
                rasterHeight = Math.round(scale * bc.rasterHeight);
                raster = new int[rasterWidth * rasterHeight];
                paintSegmentedControl4(raster, rasterWidth, rasterHeight, scale, cc, false);
                Rectangle2D bounds = bc.bounds;
                x = Math.round(scale * (float) bounds.getX());
                y = Math.round(scale * (float) bounds.getY());
                w = Math.round(scale * (float) bounds.getWidth());
                h = Math.round(scale * (float) bounds.getHeight());
            }
            PixelRaster px = new PixelRasterImpl(raster, rasterWidth, rasterHeight);
            compositor.composeFrom(px, x, y, 0, 0, w, h);
        }
    }

    /**
      Paint a segmented control with a single segment.
      @return the bounds of the control in the raster.
    */

    private @NotNull Rectangle paintSegmentedButton1(@NotNull int[] data, int rw, int rh, float w, float h,
                                                     @NotNull SegmentedButtonConfiguration g)
    {
        int scale = Math.round(rw / w);
        SegmentedControlDescriptions scds = new SegmentedControlDescriptions();
        RenderInsets insets = scds.getInsets(g, scale);
        SegmentedControl1LayoutInfo layout = scds.getSegment1LayoutInfo(g, scale);
        int buttonWidth = Math.round(w - insets.widthAdjust);
        int buttonHeight = Math.round(h - insets.heightAdjust);
        boolean isSelected = g.isSelected();

        SegmentButtonRenderingConfiguration1 bc
          = getRenderConfiguration(isSelected, insets, layout, scale, buttonWidth, buttonHeight);
        SegmentedControlConfiguration1 cc = createControlConfiguration(g, bc);
        paintSegmentedControl1(data, rw, rh, scale, cc, false);
        int left = Math.round(insets.left * scale);
        int top = Math.round(insets.top * scale);
        int width = Math.round(buttonWidth * scale);
        int height = Math.round(buttonHeight * scale);
        return new Rectangle(left, top, width, height);
    }

    /**
      Paint a segmented control with a four segments.
      @return the bounds of the button in the raster.
    */

    private @NotNull Rectangle paintSegmentedButton4(@NotNull int[] data, int rw, int rh, float w, float h,
                                                     @NotNull SegmentedButtonConfiguration g)
    {
        int scale = Math.round(rw / w);
        SegmentedControlDescriptions scds = new SegmentedControlDescriptions();
        RenderInsets insets = scds.getInsets(g, scale);
        SegmentedControl4LayoutInfo layout = scds.getSegment4LayoutInfo(g, scale);
        int buttonWidth = Math.round(w - insets.widthAdjust);
        int buttonHeight = Math.round(h - insets.heightAdjust);

        SegmentButtonRenderingConfiguration4 bc
          = getRenderConfiguration(g, insets, layout, scale, buttonWidth, buttonHeight);
        SegmentedControlConfiguration4 cc = createControlConfiguration(g, bc);
        paintSegmentedControl4(data, rw, rh, scale, cc, false);
        Rectangle2D bounds = bc.bounds;
        int left = Math.round((float) bounds.getX() * scale);
        int top = Math.round((float) bounds.getY() * scale);
        int width = Math.round((float) bounds.getWidth() * scale);
        int height = Math.round((float) bounds.getHeight() * scale);
        return new Rectangle(left, top, width, height);
    }

    public static final int TEST_SEGMENTED_ONE_SEGMENT_MASK = (1 << 10);
    public static final int TEST_SEGMENTED_FOUR_SEGMENT_MASK = (1 << 11);

    /**
      Render a segmented control for the purpose of debugging its layout.
      @param g This configuration designates the widget and the size.

      @param option This parameter is a bit mask that determines the number of segments in the control and which
      segments, if any, are displayed as selected. If the bit identified by @{code TEST_SEGMENTED_ONE_SEGMENT_MASK} is
      true, one segment is displayed. If the bit identified by @{code TEST_SEGMENTED_ONE_SEGMENT_MASK} is true, one
      segment is displayed. Exactly one of these two bits must be set. The segment at index {@code i} is displayed as
      selected if the bit identified by the mask {@code 1<<i} is true. Older versions of this library interpret this
      parameter differently. If the parameter is -1, a single segment is displayed as not selected. If the parameter is
      -2, four segments are dispalyed as unselected. If this parameter has a value between 0 and 3, four segments are
      displayed and the indicated segment is displayed as selected.

      @param scaleFactor The scale factor for the display.
      @param width The raster width in points.
      @param height The raster height in points.
      @param controlWidth The width of the segmented control in points.
      @param controlHeight The height of the segmented control in points.
      @return the rendered control display and associated information.
    */

    public @Nullable AnnotatedSegmentedControlImage
    getSegmentedRendererDebugInfo(@NotNull SegmentedButtonConfiguration g,
                                  int option,
                                  int scaleFactor,
                                  int width, int height,
                                  float controlWidth, float controlHeight, float segmentWidth,
                                  boolean isSelectAny)
    {
        int size = toSize(g.getSize());
        int segmentStyle = toSegmentedStyle(g.getWidget());

        int rw = (int) Math.ceil(scaleFactor * width);
        int rh = (int) Math.ceil(scaleFactor * height);
        int[] data = new int[rw * rh];

        boolean isOneSegmentRequested = (option & TEST_SEGMENTED_ONE_SEGMENT_MASK) != 0;
        boolean isFourSegmentsRequested = (option & TEST_SEGMENTED_FOUR_SEGMENT_MASK) != 0;

        boolean isOneSegment = isOneSegmentRequested && !isFourSegmentsRequested || option == -1;

        float[] debugOutput = new float[20];
        nativeTestSegmentedButton(data, rw, rh, width, height, segmentStyle, option, size, controlWidth, controlHeight,
          segmentWidth, isSelectAny, debugOutput);

        Rectangle2D controlBounds = null;
        Rectangle2D[] segmentBounds = null;
        if (!isZero(debugOutput, 0, 4)) {
            controlBounds = extractBounds(debugOutput, 0);
        }
        int start = 4;
        if (!isZero(debugOutput, start, 16)) {
            int count = isOneSegment ? 1 : 4;
            segmentBounds = new Rectangle2D[count];
            int offset = start;
            for (int i = 0; i < count; i++) {
                segmentBounds[i] = extractBounds(debugOutput, offset);
                offset += 4;
            }
        }
        BufferedImage im = BasicImageSupport.createImage(data, rw, rh);
        return new AnnotatedSegmentedControlImage(im, segmentBounds);
    }

    private static boolean isZero(@NotNull float[] fs, int start, int count)
    {
        for (int i = 0; i < count; i++) {
            if (fs[start + i] != 0) {
                return false;
            }
        }
        return true;
    }

    private static @NotNull Rectangle2D extractBounds(@NotNull float[] fs, int start)
    {
        return new Rectangle2D.Float(fs[start], fs[start+1], fs[start+2], fs[start+3]);
    }

    @Override
    protected @Nullable RendererDebugInfo getSegmentedButtonRendererDebugInfo(@NotNull SegmentedButtonConfiguration g,
                                                                              int scaleFactor, int width, int height)
    {
        int size = toSize(g.getSize());
        int state = toState(g.getState());
        int segmentStyle = toSegmentedStyle(g.getWidget());
        int segmentPosition = toSegmentPosition(g.getPosition());
        int flags = toSegmentFlags(g);

        float[] debugOutput = new float[8];
        int[] debugData = new int[40000];

        Rectangle2D bounds = new Rectangle2D.Float(0, 0, width, height);
        RendererDescription rd = rendererDescriptions.getSegmentedButtonRendererDescription(g);
        RasterDescription sd = rd.getRasterBounds(bounds, scaleFactor);
        int rw = (int) Math.ceil(scaleFactor * sd.getWidth());
        int rh = (int) Math.ceil(scaleFactor * sd.getHeight());
        float w = sd.getWidth();
        float h = sd.getHeight();
        int[] data = new int[rw * rh];
        nativePaintSegmentedButton(data, rw, rh, w, h, segmentStyle, segmentPosition, size,
          state, g.isFocused(), flags, debugOutput, debugData);

        int imageWidth = (int) debugOutput[DEBUG_SEGMENT_WIDTH];
        int imageHeight = (int) debugOutput[DEBUG_SEGMENT_HEIGHT];
        float xOffset = debugOutput[DEBUG_SEGMENT_X_OFFSET];
        float yOffset = debugOutput[DEBUG_SEGMENT_Y_OFFSET];
        float dividerWidth = debugOutput[DEBUG_SEGMENT_DIVIDER_WIDTH];
        float outerLeftInset = debugOutput[DEBUG_SEGMENT_OUTER_LEFT_INSET];
        float leftInset = debugOutput[DEBUG_SEGMENT_LEFT_INSET];
        float rightInset = debugOutput[DEBUG_SEGMENT_RIGHT_INSET];

        String info = "Outer left: " + outerLeftInset
                        + "; left: " + leftInset
                        + "; right: " + rightInset
                        + "; divider: " + dividerWidth;

        Image im = BasicImageSupport.createImage(debugData, imageWidth, imageHeight);
        Rectangle2D frame = new Rectangle2D.Float(xOffset, yOffset, w, h);
        return new RendererDebugInfo(im, frame, info);
    }

    public @Nullable float[] getSegmentedButtonLayoutParameters(@NotNull SegmentedButtonLayoutConfiguration g)
    {
        int size = toSize(g.getSize());
        int segmentStyle = toSegmentedStyle(g.getWidget());
        float[] debugOutput = new float[8];
        int result = nativeDetermineSegmentedButtonLayoutParameters(segmentStyle, size, debugOutput);
        return result == 0 ? debugOutput : null;
    }

    public static final int SEGMENT_POSITION_FIRST = 0;
    public static final int SEGMENT_POSITION_MIDDLE = 1;
    public static final int SEGMENT_POSITION_LAST = 2;
    public static final int SEGMENT_POSITION_ONLY = 3;

    private int toSegmentPosition(@NotNull Position segmentPosition)
    {
        switch (segmentPosition) {
            case FIRST:
                return SEGMENT_POSITION_FIRST;
            case MIDDLE:
                return SEGMENT_POSITION_MIDDLE;
            case LAST:
                return SEGMENT_POSITION_LAST;
            case ONLY:
                return SEGMENT_POSITION_ONLY;
        }
        throw new UnsupportedOperationException();
    }

    public static final int SEGMENT_FLAG_IS_SELECTED = 1;
    public static final int SEGMENT_FLAG_IS_LEFT_NEIGHBOR_SELECTED = 2;
    public static final int SEGMENT_FLAG_IS_RIGHT_NEIGHBOR_SELECTED = 4;
    public static final int SEGMENT_FLAG_DRAW_LEADING_SEPARATOR = 8;
    public static final int SEGMENT_FLAG_DRAW_TRAILING_SEPARATOR = 16;

    private int toSegmentFlags(@NotNull SegmentedButtonConfiguration g)
    {
        int flags = 0;

        if (g.isSelected()) {
            flags |= SEGMENT_FLAG_IS_SELECTED;
        }
        if (g.getLeftDividerState() == SegmentedButtonConfiguration.DividerState.SELECTED) {
            flags |= SEGMENT_FLAG_IS_LEFT_NEIGHBOR_SELECTED;
        }
        if (g.getRightDividerState() == SegmentedButtonConfiguration.DividerState.SELECTED) {
            flags |= SEGMENT_FLAG_IS_RIGHT_NEIGHBOR_SELECTED;
        }
        if (g.getLeftDividerState() != SegmentedButtonConfiguration.DividerState.NONE) {
            flags |= SEGMENT_FLAG_DRAW_LEADING_SEPARATOR;
        }
        if (g.getRightDividerState() != SegmentedButtonConfiguration.DividerState.NONE) {
            flags |= SEGMENT_FLAG_DRAW_TRAILING_SEPARATOR;
        }
        return flags;
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
                return NSSegmentStyleSeparated_Toolbar;
            case BUTTON_SEGMENTED_SLIDER:
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
                return NSTexturedRoundedBezelStyle_Toolbar;
            case BUTTON_ROUND:
                return NSCircularBezelStyle;
            case BUTTON_ROUND_TOOLBAR:
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

    @Native private static final int SELECT_SEGMENT_1 = 1 << 3;
    @Native private static final int SELECT_SEGMENT_2 = 1 << 2;
    @Native private static final int SELECT_SEGMENT_3 = 1 << 1;
    @Native private static final int SELECT_SEGMENT_4 = 1 << 0;

    @Native private static final int CONTEXT_WINDOW = 1;
    @Native private static final int CONTEXT_TOOLBAR = 2;

    public @NotNull SegmentedControl4LayoutInfo
    getSegment4LayoutInfo(@NotNull SegmentedButtonConfiguration g, int scale)
    {
        SegmentedControlDescriptions ds = new SegmentedControlDescriptions();
        return ds.getSegment4LayoutInfo(g, scale);
    }

    /**
      Compute a configuration used to render a segmented button.
      This method is public for debugging use.

      @param g The button configuration. The currently required information is: whether the button style is a separated
      style; whether the button is in the selected state; the position of the button in the control;
      @param s The render insets.
      @param layout The segmented control layout information.
      @param scale The display scale.
      @param segmentWidth The desired segment width (in points).
      @param controlHeight The desired control height (in points).
      @return a rendering configuration.
    */

    public @NotNull SegmentButtonRenderingConfiguration4
    getRenderConfiguration(@NotNull SegmentedButtonConfiguration g,
                           @NotNull RenderInsets s,
                           @NotNull SegmentedControl4LayoutInfo layout,
                           int scale, int segmentWidth, int controlHeight)
    {
        boolean isSeparated = g.getWidget().isSeparated();
        boolean isSelected = g.isSelected();
        Position pos = g.getPosition();
        DividerState leftDivider = g.getLeftDividerState();
        DividerState rightDivider = g.getRightDividerState();
        return getRenderConfiguration(isSeparated, isSelected, pos, leftDivider, rightDivider,
          s, layout, scale, segmentWidth, controlHeight);
    }

    /**
      Compute a configuration used to render a segmented button.
      This method is public for debugging use.

      @param isSelected True if and only if the button state is selected (on).
      @param s The render insets.
      @param layout The segmented control layout information.
      @param scale The display scale.
      @param buttonWidth The desired button width (in points).
      @param buttonHeight The desired button height (in points).
      @return a rendering configuration.
    */

    public @NotNull SegmentButtonRenderingConfiguration1 getRenderConfiguration(
      boolean isSelected,
      @NotNull RenderInsets s,
      @NotNull SegmentedControl1LayoutInfo layout,
      int scale,
      int buttonWidth,
      int buttonHeight)
    {
        float segmentWidth = buttonWidth - layout.widthAdjustment;
        int rasterWidth = (int) Math.ceil(buttonWidth + s.widthAdjust);
        int rasterHeight = (int) Math.ceil(buttonHeight + s.heightAdjust);
        return new SegmentButtonRenderingConfiguration1(scale, isSelected, segmentWidth, rasterWidth, rasterHeight);
    }

    /**
      Compute a configuration used to render a segmented button.
      This method is public for debugging use.

      @param isSeparated True if and only if the button style is a separated style.
      @param isSelected True if and only if the button state is selected (on).
      @param pos The position of the button in the control. Must not be ONLY.
      @param leftDivider The requested state of the divider on the left side of the segment.
      @param rightDivider The requested state of the divider on the right side of the segment.
      @param s The render insets.
      @param layout The segmented control layout information.
      @param scale The display scale.
      @param segmentWidth The desired segment width (in points).
      @param controlHeight The desired control height (in points).
      @return a rendering configuration.
    */

    public @NotNull SegmentButtonRenderingConfiguration4 getRenderConfiguration(
      boolean isSeparated,
      boolean isSelected,
      @NotNull Position pos,
      @NotNull DividerState leftDivider,
      @NotNull DividerState rightDivider,
      @NotNull RenderInsets s,
      @NotNull SegmentedControl4LayoutInfo layout,
      int scale,
      int segmentWidth,
      int controlHeight)
    {
        DividerPosition dividerPosition = layout.dividerPosition;
        float dividerVisualWidth = layout.dividerVisualWidth;
        float firstExtra = layout.firstSegmentWidthAdjustment;
        float middleExtra = layout.middleSegmentWidthAdjustment;
        float lastExtra = layout.lastSegmentWidthAdjustment;
        float otherSegmentNominalWidth = 20;
        float renderedSegmentNominalWidth;
        int segmentIndex;
        int selectedSegmentIndex = -1;
        float xOffset = 0;
        float widthAdjustment = 0;

        if (pos == Position.FIRST) {
            segmentIndex = 0;
            renderedSegmentNominalWidth = segmentWidth - firstExtra;
        } else if (pos == Position.MIDDLE) {
            segmentIndex = 1;
            renderedSegmentNominalWidth = segmentWidth - middleExtra;
        } else if (pos == Position.LAST) {
            segmentIndex = 3;
            renderedSegmentNominalWidth = segmentWidth - lastExtra;
        } else {
            throw new AssertionError("Unexpected segment position");
        }

        if (isSelected) {
            selectedSegmentIndex = segmentIndex;
        } else if (leftDivider == SegmentedButtonConfiguration.DividerState.SELECTED) {
            if (segmentIndex > 0) {
                selectedSegmentIndex = segmentIndex - 1;
            }
        } else if (rightDivider == SegmentedButtonConfiguration.DividerState.SELECTED) {
            if (segmentIndex == 1) {
                segmentIndex = 2;
                selectedSegmentIndex = 3;
            } else if (segmentIndex == 0) {
                selectedSegmentIndex = 1;
            }
        }

        if (segmentIndex > 0) {
            // the width of the first segment is otherSegmentWidth + firstExtra
            xOffset = segmentIndex * (otherSegmentNominalWidth + middleExtra) + (firstExtra - middleExtra);
            widthAdjustment += firstExtra;
        }

        if (segmentIndex < 3) {
            // the width of the last segment is otherSegmentWidth + lastExtra
            widthAdjustment += lastExtra;
        }

        if (segmentIndex == 0 || segmentIndex == 3) {
            widthAdjustment += 2 * middleExtra;
        } else {
            widthAdjustment += middleExtra;
        }

        // The next step is to adjust the segment width and extracted region location to either hide or reveal dividers,
        // as needed to satisfy the requested set of dividers. This adjustment is inhibited in the case of a
        // separated style in 2x when the "divider" is centered. What this basically means
        // is that we always want to show the entire segment, as the half divider on either side is the border or
        // space that we want to be visible.

        boolean isSeparatedCentered2X = isSeparated && scale != 1 && dividerPosition == CENTER;
        if (!isSeparatedCentered2X && dividerVisualWidth > 0) {
            if (segmentIndex > 0) {
                // adjust so that the left divider is not visible by default
                if (dividerPosition != LEFT) {
                    //NSLog(@"Shifting and widening to hide left divider");
                    xOffset += dividerVisualWidth;
                    renderedSegmentNominalWidth += dividerVisualWidth;
                    widthAdjustment += dividerVisualWidth;
                }
            }

            if (segmentIndex < 3) {
                // adjust so that the right divider is not visible by default
                if (dividerPosition != RIGHT) {
                    //NSLog(@"Widening to hide right divider");
                    renderedSegmentNominalWidth += dividerVisualWidth;
                    widthAdjustment += dividerVisualWidth;
                }
            }

            boolean drawLeadingDivider = segmentIndex > 0 && leftDivider != DividerState.NONE;
            boolean drawTrailingDivider = segmentIndex < 3 && rightDivider != DividerState.NONE;
            if (drawLeadingDivider) {
                xOffset -= dividerVisualWidth;
                int adjustment = (int) Math.ceil(dividerVisualWidth);
                if (dividerPosition == CENTER) {
                    xOffset -= dividerVisualWidth;
                    adjustment += dividerVisualWidth;
                }
                renderedSegmentNominalWidth -= adjustment;
                widthAdjustment -= adjustment;
                System.err.println("Shifting to reveal left divider");
            }

            if (drawTrailingDivider) {
                renderedSegmentNominalWidth -= dividerVisualWidth;
                widthAdjustment -= dividerVisualWidth;
                if (dividerPosition == CENTER) {
                    renderedSegmentNominalWidth -= dividerVisualWidth;
                    widthAdjustment -= dividerVisualWidth;
                }
                System.err.println("Shrinking to reveal right divider");
            }
        }

        float controlWidth = 3 * otherSegmentNominalWidth + segmentWidth + widthAdjustment;
        int rasterWidth = (int) Math.ceil(controlWidth + s.widthAdjust);
        int rasterHeight = (int) Math.ceil(controlHeight + s.heightAdjust);

        // TBD: the only novelty in the segment bounds is the xOffset.
        // The rest is based on provided inputs and the raster insets.

        Rectangle2D segmentBounds = createPixelBounds(scale, s.left + xOffset, s.top, segmentWidth, controlHeight);

        return new SegmentButtonRenderingConfiguration4(scale, segmentIndex + 1, selectedSegmentIndex + 1,
          renderedSegmentNominalWidth, otherSegmentNominalWidth, rasterWidth, rasterHeight, segmentBounds);
    }

    private static @NotNull Rectangle2D createPixelBounds(int scale, double x, double y, double w, double h)
    {
        double x2 = round(x + w, scale);
        double y2 = round(y + h, scale);
        x = round(x, scale);
        y = round(y, scale);
        w = x2 - x;
        h = y2 - y;
        return new Rectangle2D.Double(x, y, w, h);
    }

    private static double round(double d, int scale)
    {
        if (scale == 1) {
            return Math.round(d);
        }
        return Math.round(d * scale) / (double) scale;
    }

    public @NotNull SegmentedControlConfiguration1
    createControlConfiguration(@NotNull SegmentedButtonConfiguration g, @Nullable SegmentButtonRenderingConfiguration1 b)
    {
        SegmentedButtonWidget widget = g.getWidget();
        boolean isToolbar = widget.isToolbar();
        AquaUIPainter.Size sz = g.getSize();
        AquaUIPainter.State st = g.getState();
        AquaUIPainter.SwitchTracking tracking = g.getTracking();
        SegmentedButtonWidget basicWidget = widget.toBasicWidget();
        float w = b != null ? b.segmentWidth : 20;
        boolean isSelected = b != null && b.isSelected;
        return new SegmentedControlConfiguration1(basicWidget, isToolbar, sz, st, w, isSelected);
    }

    public @NotNull SegmentedControlConfiguration4
    createControlConfiguration(@NotNull SegmentedButtonConfiguration g, @Nullable SegmentButtonRenderingConfiguration4 b)
    {
        SegmentedButtonWidget widget = g.getWidget();
        boolean isToolbar = widget.isToolbar();
        AquaUIPainter.Size sz = g.getSize();
        AquaUIPainter.State st = g.getState();
        AquaUIPainter.SwitchTracking tracking = g.getTracking();
        SegmentedButtonWidget basicWidget = widget.toBasicWidget();

        float sw1 = b != null ? b.otherSegmentWidth : 20;
        float sw2 = b != null ? b.otherSegmentWidth : 20;
        float sw3 = b != null ? b.otherSegmentWidth : 20;
        float sw4 = b != null ? b.otherSegmentWidth : 20;
        boolean sel1 = false;
        boolean sel2 = false;
        boolean sel3 = false;
        boolean sel4 = false;

        if (b != null) {
            int i = b.designatedSegment;
            if (i == 1) {
                sw1 = b.designatedSegmentWidth;
            } else if (i == 2) {
                sw2 = b.designatedSegmentWidth;
            } else if (i == 3) {
                sw3 = b.designatedSegmentWidth;
            } else if (i == 4) {
                sw4 = b.designatedSegmentWidth;
            }
            int leftSelectedIndex = g.getLeftDividerState() == SegmentedButtonConfiguration.DividerState.SELECTED
                                      && b.selectedSegment > 0 ? b.selectedSegment - 1 : 0;
            int rightSelectedIndex = g.getRightDividerState() == SegmentedButtonConfiguration.DividerState.SELECTED
                                       && b.selectedSegment > 0 ? b.selectedSegment + 1 : 0;

            i = b.selectedSegment;
            if (i == 1 || leftSelectedIndex == 1) {
                sel1 = true;
            }
            if (i == 2 || leftSelectedIndex == 2 || rightSelectedIndex == 2) {
                sel2 = true;
            }
            if (i == 3 || leftSelectedIndex == 3 || rightSelectedIndex == 3) {
                sel3 = true;
            }
            if (i == 4 || rightSelectedIndex == 4) {
                sel4 = true;
            }
        }

        return new SegmentedControlConfiguration4(basicWidget, isToolbar, sz, st, tracking, sw1, sw2, sw3, sw4, sel1, sel2, sel3, sel4);
    }

    /**
      A configuration defining the parameters that must be specified to render a segmented button by rendering a control
      with one segment.
    */

    public static class SegmentButtonRenderingConfiguration1
    {
        public final float scale; // the display scale factor
        public final boolean isSelected;
        public final float segmentWidth; // nominal width
        public final int rasterWidth; // the required width of the raster
        public final int rasterHeight; // the required height of the raster

        public SegmentButtonRenderingConfiguration1(float scale,
                                                    boolean isSelected,
                                                    float segmentWidth,
                                                    int rasterWidth,
                                                    int rasterHeight)
        {
            this.scale = scale;
            this.isSelected = isSelected;
            this.segmentWidth = segmentWidth;
            this.rasterWidth = rasterWidth;
            this.rasterHeight = rasterHeight;
        }
    }

    /**
      A configuration defining the parameters that must be specified to render a segmented button by rendering a control
      with four segments.
    */

    public static class SegmentButtonRenderingConfiguration4
    {
        public final float scale; // the display scale factor
        public final int designatedSegment; // 1 to 4
        public final int selectedSegment; // 0 or 1 to 4
        public final float designatedSegmentWidth; // nominal width
        public final float otherSegmentWidth; // the nominal width of other segments
        public final int rasterWidth; // the required width of the raster
        public final int rasterHeight; // the required height of the raster
        public final Rectangle2D bounds; // the bounds of the button rendering relative to the raster

        public SegmentButtonRenderingConfiguration4(float scale,
                                                    int designatedSegment,
                                                    int selectedSegment,
                                                    float designatedSegmentWidth,
                                                    float otherSegmentWidth,
                                                    int rasterWidth,
                                                    int rasterHeight,
                                                    Rectangle2D bounds)
        {
            this.scale = scale;
            this.designatedSegment = designatedSegment;
            this.selectedSegment = selectedSegment;
            this.designatedSegmentWidth = designatedSegmentWidth;
            this.otherSegmentWidth = otherSegmentWidth;
            this.rasterWidth = rasterWidth;
            this.rasterHeight = rasterHeight;
            this.bounds = bounds;
        }
    }

    /**
      A configuration defining the parameters that must be specified to render a segmented control with one segment.
    */

    public static class SegmentedControlConfiguration1
    {
        public final float w;
        public final @NotNull SegmentedButtonWidget widget;
        public final boolean isSelected;
        public final boolean isToolbar;
        public final @NotNull Size size;
        public final @NotNull State state;

        /**
          Create a configuration for a single segment control.
          @param widget A widget defining the control style. Toolbar styles should not be used.
          @param isToolbar True if and only if the control should be rendered as it would in a toolbar.
          @param sz The size variant.
          @param st The control state.
          @param w The nominal width (in points) of the segment.
          @param isSelected True if the segment should be selected.
        */

        public SegmentedControlConfiguration1(@NotNull SegmentedButtonWidget widget,
                                              boolean isToolbar,
                                              @NotNull Size sz,
                                              @NotNull State st,
                                              float w,
                                              boolean isSelected
        )
          throws IllegalArgumentException
        {
            validateSegmentWidth(w);
            if (widget.isToolbar()) {
                throw new IllegalArgumentException("Toolbar widget not supported");
            }

            this.w = w;
            this.widget = widget;
            this.isSelected = isSelected;
            this.isToolbar = isToolbar;
            this.size = sz;
            this.state = st;
        }
    }

    /**
      A configuration defining the parameters that must be specified to render a segmented control with four segments.
    */

    public static class SegmentedControlConfiguration4
    {
        public final float w1;
        public final float w2;
        public final float w3;
        public final float w4;
        public final @NotNull SegmentedButtonWidget widget;
        public final @NotNull SwitchTracking tracking;
        public final boolean s1;
        public final boolean s2;
        public final boolean s3;
        public final boolean s4;
        public final boolean isToolbar;
        public final @NotNull Size size;
        public final @NotNull State state;

        /**
          Create a configuration for a 4 segment control.
          @param widget A widget defining the control style. Toolbar styles should not be used.
          @param isToolbar True if and only if the control should be rendered as it would in a toolbar.
          @param sz The size variant.
          @param st The control state.
          @param tr The tracking mode (select one or select any).
          @param w1 The nominal width (in points) of the 1st (leftmost) segment.
          @param w2 The nominal width (in points) of the 2nd segment.
          @param w3 The nominal width (in points) of the 3rd segment.
          @param w4 The nominal width (in points) of the 4th (rightmost) segment.
          @param s1 True if the 1st segment should be selected.
          @param s2 True if the 2nd segment should be selected.
          @param s3 True if the 3rd segment should be selected.
          @param s4 True if the 4th segment should be selected.
        */

        public SegmentedControlConfiguration4(@NotNull SegmentedButtonWidget widget,
                                              boolean isToolbar,
                                              @NotNull Size sz,
                                              @NotNull State st,
                                              @NotNull SwitchTracking tr,
                                              float w1, float w2, float w3, float w4,
                                              boolean s1, boolean s2, boolean s3, boolean s4
        )
          throws IllegalArgumentException
        {
            validateSegmentWidth(w1);
            validateSegmentWidth(w2);
            validateSegmentWidth(w3);
            validateSegmentWidth(w4);
            if (widget.isToolbar()) {
                throw new IllegalArgumentException("Toolbar widget not supported");
            }

            this.w1 = w1;
            this.w2 = w2;
            this.w3 = w3;
            this.w4 = w4;
            this.widget = widget;
            this.tracking = tr;
            this.s1 = s1;
            this.s2 = s2;
            this.s3 = s3;
            this.s4 = s4;
            this.isToolbar = isToolbar;
            this.size = sz;
            this.state = st;
        }
    }

    /**
      Paint a segmented control with one segment into a raster buffer. Experimental.
      The goal is to do as much calculation in Java as possible.

      @param data The raster buffer where 32-bit RGBA pixels will be written.
      @param rw The width of the raster buffer, in pixels.
      @param rh The height of the raster buffer, in pixels.
      @param scale The scale factor, typically 1 or 2.
      @param g The control configuration.
      @param requestDebugOutput True to request debug information.
      @return the requested debug information, or null if not requested.
    */

    public @Nullable AnnotatedSegmentedControlImage paintSegmentedControl1(
      @NotNull int[] data,
      int rw,
      int rh,
      float scale,
      SegmentedControlConfiguration1 g,
      boolean requestDebugOutput
    )
    {
        if (rw < 1 || rh < 1 || data.length < rw * rh) {
            throw new IllegalArgumentException("Invalid raster size or length");
        }
        if (scale < 0.01 || scale > 100) {
            throw new IllegalArgumentException("Invalid or unsupported scale factor");
        }

        int size = toSize(g.size);
        int state = toState(g.state);
        int style = toSegmentedStyle(g.widget);
        boolean isSelected = g.isSelected;
        int context = g.isToolbar ? CONTEXT_TOOLBAR : CONTEXT_WINDOW;

        float[] debugData = requestDebugOutput ? new float[4] : null;

        nativePaintSegmentedControl1(data, rw, rh, scale, g.w, style, isSelected, context, size, state, debugData);

        if (debugData != null) {
            int count = debugData.length / 4;
            Rectangle2D[] segmentBounds = new Rectangle2D[count];
            int offset = 0;
            for (int i = 0; i < count; i++) {
                segmentBounds[i] = extractBounds(debugData, offset);
                offset += 4;
            }
            BufferedImage im = BasicImageSupport.createImage(data, rw, rh);
            return new AnnotatedSegmentedControlImage(im, segmentBounds);
        } else {
            return null;
        }
    }

    /**
      Paint a segmented control with 4 segments into a raster buffer. Experimental.
      The goal is to do as much calculation in Java as possible.

      @param data The raster buffer where 32-bit RGBA pixels will be written.
      @param rw The width of the raster buffer, in pixels.
      @param rh The height of the raster buffer, in pixels.
      @param scale The scale factor, typically 1 or 2.
      @param g The control configuration.
      @param requestDebugOutput True to request debug information.
      @return the requested debug information, or null if not requested.
    */

    public @Nullable AnnotatedSegmentedControlImage paintSegmentedControl4(
      @NotNull int[] data,
      int rw,
      int rh,
      float scale,
      SegmentedControlConfiguration4 g,
      boolean requestDebugOutput
    )
    {
        if (rw < 1 || rh < 1 || data.length < rw * rh) {
            throw new IllegalArgumentException("Invalid raster size or length");
        }
        if (scale < 0.01 || scale > 100) {
            throw new IllegalArgumentException("Invalid or unsupported scale factor");
        }

        int size = toSize(g.size);
        int state = toState(g.state);
        int style = toSegmentedStyle(g.widget);
        int tracking = toTracking(g.tracking);
        int selectionFlags = toSelectionFlags(g.s1, g.s2, g.s3, g.s4);
        int context = g.isToolbar ? CONTEXT_TOOLBAR : CONTEXT_WINDOW;

        float[] debugData = requestDebugOutput ? new float[16] : null;

        nativePaintSegmentedControl4(data, rw, rh, scale, g.w1, g.w2, g.w3, g.w4, style, tracking,
          selectionFlags, context, size, state, debugData);

        if (debugData != null) {
            int count = debugData.length / 4;
            Rectangle2D[] segmentBounds = new Rectangle2D[count];
            int offset = 0;
            for (int i = 0; i < count; i++) {
                segmentBounds[i] = extractBounds(debugData, offset);
                offset += 4;
            }
            BufferedImage im = BasicImageSupport.createImage(data, rw, rh);
            return new AnnotatedSegmentedControlImage(im, segmentBounds);
        } else {
            return null;
        }
    }

    private static void validateSegmentWidth(float w)
    {
        if (w < 1 || w > 10000) {
            throw new IllegalArgumentException("Invalid or unsupported segment width");
        }
    }

    private static int toSelectionFlags(boolean s1, boolean s2, boolean s3, boolean s4)
    {
        return (s1 ? SELECT_SEGMENT_1 : 0)
                 | (s2 ? SELECT_SEGMENT_2 : 0)
                 | (s3 ? SELECT_SEGMENT_3 : 0)
                 | (s4 ? SELECT_SEGMENT_4 : 0);
    }

    private static native void nativePaintIndeterminateProgressIndicator(int[] data, int rw, int rh, float w, float h, int size, int state, int orientation, boolean isSpinner, int frame);
    private static native void nativePaintProgressIndicator(int[] data, int rw, int rh, float w, float h, int size, int state, int orientation, double value);
    private static native void nativePaintButton(int[] data, int rw, int rh, float w, float h, int buttonType, int bezelStyle, int size, int state, boolean isFocused, int value, int layoutDirection);
    private static native void nativePaintColorWell(int[] data, int rw, int rh, float w, float h, int state);
    private static native void nativePaintToolBarItemWell(int[] data, int rw, int rh, float w, float h, int state, boolean isFrameOnly);
    private static native void nativePaintGroupBox(int[] data, int rw, int rh, float w, float h, int titlePosition, int state, boolean isFrameOnly);
    private static native void nativePaintListBox(int[] data, int rw, int rh, float w, float h, int state, boolean isFocused, boolean isFrameOnly);
    private static native void nativePaintTextField(int[] data, int rw, int rh, float w, float h, int sz, int state, int type);
    private static native void nativePaintSegmentedButton(int[] data, int rw, int rh, float w, float h, int segmentType, int segmentPosition, int size, int state, boolean isFocused, int flags, float[] debugOutput, int[] debugData);
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

    private static native void nativeTestSegmentedButton(int[] data, int rw, int rh, float w, float h,
                                                         int segmentType, int option, int size,
                                                         float cw, float ch, float segmentWidth,
                                                         boolean isSelectAny,
                                                         float[] debugOutput);

    private static native int nativeDetermineSegmentedButtonLayoutParameters(int segmentStyle, int size, float[] data);
    public static native int nativeDetermineSegmentedButtonRenderingVersion();
    public static native int nativeDetermineSliderRenderingVersion();

    // The following is experimental. The goal is to do as much calculation in Java as possible.

    /**
      Paint a segmented control with 4 segments into a raster buffer.

      @param data The raster buffer where 32-bit RGBA pixels will be written.
      @param rw The width of the raster buffer, in pixels.
      @param rh The height of the raster buffer, in pixels.
      @param scale The scale factor, typically 1 or 2.
      @param w1 the nominal width (in points) of the 1st segment
      @param w2 the nominal width (in points) of the 2nd segment
      @param w3 the nominal width (in points) of the 3rd segment
      @param w4 the nominal width (in points) of the 4th segment
      @param style the control style
      @param tracking the tracking mode (select one or select any)
      @param selections a bitmask indicating which segments are to be selected (lower 4 bits as 1,2,3,4)
      @param context the context of the control (toolbar = 1, normal = 0)
      @param sz the size variant
      @param st the control state
      @return 0 if successful, an error code otherwise.
    */

    private static native int nativePaintSegmentedControl4(
      int[] data,
      int rw,
      int rh,
      float scale,
      float w1,
      float w2,
      float w3,
      float w4,
      int style,
      int tracking,
      int selections,
      int context,
      int sz,
      int st,
      @Nullable float[] debugOutput
    );

    /**
      Paint a segmented control with one segment into a raster buffer.

      @param data The raster buffer where 32-bit RGBA pixels will be written.
      @param rw The width of the raster buffer, in pixels.
      @param rh The height of the raster buffer, in pixels.
      @param scale The scale factor, typically 1 or 2.
      @param w the nominal width (in points) of the segment
      @param style the control style
      @param isSelected true if the segment should be selected
      @param context the context of the control (toolbar = 1, normal = 0)
      @param sz the size variant
      @param st the control state
      @return 0 if successful, an error code otherwise.
    */

    private static native int nativePaintSegmentedControl1(
      int[] data,
      int rw,
      int rh,
      float scale,
      float w,
      int style,
      boolean isSelected,
      int context,
      int sz,
      int st,
      @Nullable float[] debugOutput
    );

    // The following methods represent a failed experiment. Although I am not sure why, asking a view for its size does
    // not always provide the information needed here.

    private static native int nativeDetermineButtonFixedHeight(int buttonType, int bezelStyle, int size);
    private static native int nativeDetermineButtonFixedWidth(int buttonType, int bezelStyle, int size);
    private static native int nativeDetermineSegmentedButtonFixedHeight(int segmentStyle, int size);
}
