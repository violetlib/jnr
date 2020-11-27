/*
 * Copyright (c) 2015-2020 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.aqua.coreui;

import java.awt.geom.Rectangle2D;
import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.List;

import org.violetlib.jnr.Insetter;
import org.violetlib.jnr.aqua.*;
import org.violetlib.jnr.aqua.impl.AquaUIPainterBase;
import org.violetlib.jnr.aqua.impl.FromMaskOperator;
import org.violetlib.jnr.aqua.impl.LinearSliderRenderer;
import org.violetlib.jnr.aqua.impl.NativeSupport;
import org.violetlib.jnr.aqua.impl.PopupRenderer;
import org.violetlib.jnr.aqua.impl.SliderTickConfiguration;
import org.violetlib.jnr.aqua.impl.TitleBarRendererBase;
import org.violetlib.jnr.impl.BasicRenderer;
import org.violetlib.jnr.impl.BasicRendererDescription;
import org.violetlib.jnr.impl.FlipVerticalRenderer;
import org.violetlib.jnr.impl.JNRPlatformUtils;
import org.violetlib.jnr.impl.Renderer;
import org.violetlib.jnr.impl.RendererDescription;
import org.violetlib.jnr.impl.ReusableCompositor;
import org.violetlib.jnr.impl.SliderTickMarkRendererFactory;

import org.jetbrains.annotations.*;

import static org.violetlib.jnr.aqua.AquaUIPainter.SegmentedButtonWidget.*;
import static org.violetlib.jnr.aqua.coreui.CoreUIKeys.*;
import static org.violetlib.jnr.aqua.coreui.CoreUISegmentSeparatorTypes.*;

/**
  A painter that renders Aqua widgets using the native rendering used by the Aqua look and feel, implemented by the
  private Core UI framework in OS X. There are two options for the API used to access the native rendering. One option
  uses the Java Runtime Support framework. This option does not work properly unless the revised framework (circa 2015)
  is used. The second option uses a private method on NSAppearance. This option does not use the Java Runtime Support
  framework.

  The implementation is not optimized. There is no fancy protocol for transferring data to native code. A CFDictionary
  is created and released for each rendering operation.
*/

public class CoreUIPainter
  extends AquaUIPainterBase
  implements AquaUIPainter
{
    /*
      Overlay scroll bars cannot be supported using this native painter. The problem is that overlay scroll bars are
      painted by native code that handles mouse events.

      The isFocused option should not be combined with the inactive or disabled states. The combination of isFocused and
      pressed, pulsed, or rollover seems odd, but perhaps it occurs as an accessibility option?
    */

    protected static boolean debugFlag = false;

    static {
        java.security.AccessController.doPrivileged(new PrivilegedAction<Void>() {
            public Void run() {
                NativeSupport.load();
                return null;
            }
        });
    }

    protected static final @NotNull CoreUIRendererDescriptions rendererDescriptions = new CoreUIRendererDescriptions();

    protected boolean useJRS;  // if true, use the Java Runtime Support framework to access Core UI rendering

    /**
      If true, use layers except where known problems must be avoided.
      If false, do not use layers.
      If null, use the default behavior specified on a case-by-case basis.
    */

    protected @Nullable Boolean forceLayers;

    /**
      Create a painter that uses Core UI rendering by way of the Java Runtime Support framework.
    */

    public CoreUIPainter()
    {
        this(true);
    }

    /**
      Create a painter that uses Core UI rendering.

      @param useJRS If true, the Java Runtime Support framework is used to access Core UI rendering. If false, a private
      method is used to access Core UI rendering.
    */

    public CoreUIPainter(boolean useJRS)
    {
        this(useJRS, null);
    }

    public CoreUIPainter(boolean useJRS, @Nullable Boolean forceLayers)
    {
        super(rendererDescriptions);

        this.useJRS = useJRS;
        this.forceLayers = forceLayers;
    }

    @Override
    public @NotNull CoreUIPainter copy()
    {
        return new CoreUIPainter();
    }

    @Override
    protected @NotNull Renderer getButtonRenderer(@NotNull ButtonConfiguration g)
    {
        ButtonWidget bw = toCanonicalButtonStyle(g.getButtonWidget());

        if (bw == ButtonWidget.BUTTON_TOOLBAR_ITEM) {
            // A tool bar item button only paints a background when ON
            if (g.getButtonState() != ButtonState.ON) {
                return NULL_RENDERER;
            }
            ToolBarItemWellConfiguration tg = new ToolBarItemWellConfiguration(g.getState(), true);
            return getToolBarItemWellRenderer(tg);
        }

        RendererDescription rd = rendererDescriptions.getButtonRendererDescription(g);
        State st = g.getState();
        ButtonState bs = g.getButtonState();
        Size sz = g.getSize();
        int platformVersion = JNRPlatformUtils.getPlatformVersion();

        boolean hasRolloverEffect = false;

        String widget;

        switch (bw) {
            case BUTTON_PUSH:
                widget = CoreUIWidgets.BUTTON_PUSH; break;
            case BUTTON_BEVEL:
                widget = CoreUIWidgets.BUTTON_BEVEL; break;
            case BUTTON_BEVEL_ROUND:
                widget = CoreUIWidgets.BUTTON_BEVEL_ROUND; break;
            case BUTTON_CHECK_BOX:
                widget = CoreUIWidgets.BUTTON_CHECK_BOX;
                break;
            case BUTTON_RADIO:
                widget = CoreUIWidgets.BUTTON_RADIO;
                break;
            case BUTTON_DISCLOSURE:
                widget = CoreUIWidgets.BUTTON_DISCLOSURE;
                break;

            // The following button types are not used in the Aqua look and feel. Some are used in Quaqua.

            case BUTTON_DISCLOSURE_TRIANGLE:
                widget = CoreUIWidgets.BUTTON_DISCLOSURE_TRIANGLE; break;
            case BUTTON_GRADIENT:
                widget = CoreUIWidgets.BUTTON_BEVEL_INSET; break;
            case BUTTON_HELP:
                widget = CoreUIWidgets.BUTTON_HELP; break;
            case BUTTON_RECESSED:

                // CoreUI may paint a background when there should be no background.
                // On 10.10, it incorrectly paints a background when the button is disabled.
                // On 10.10 and 10.13, it incorrectly paints a background when the button state is OFF.

                if (!shouldPaintRecessedBackground(st, bs)) {
                    return NULL_RENDERER;
                }

                hasRolloverEffect = true;

                // CoreUI incorrectly paints a different background when the button is inactive.

                st = adjustRecessedState(st);

                widget = CoreUIWidgets.BUTTON_PUSH_SCOPE; break;
            case BUTTON_ROUNDED_RECT:
                widget = CoreUIWidgets.BUTTON_PUSH_INSET; break;
            case BUTTON_ROUND:
                widget = CoreUIWidgets.BUTTON_ROUND; break;
            case BUTTON_ROUND_INSET:
                widget = CoreUIWidgets.BUTTON_ROUND_INSET; break;
            case BUTTON_ROUND_TEXTURED:
                widget = CoreUIWidgets.BUTTON_ROUND_TEXTURED; break;
            case BUTTON_ROUND_TEXTURED_TOOLBAR:
                widget = CoreUIWidgets.BUTTON_ROUND_TOOLBAR; break;
            case BUTTON_INLINE:
                widget = CoreUIWidgets.BUTTON_PUSH_SLIDESHOW; break;  // not correct, inline buttons are not supported by Core UI
            case BUTTON_TEXTURED:
                widget = CoreUIWidgets.BUTTON_SEGMENTED_TEXTURED; break;
            case BUTTON_TEXTURED_TOOLBAR:
                widget = platformVersion >= 101100 ? CoreUIWidgets.BUTTON_SEGMENTED_TOOLBAR : CoreUIWidgets.BUTTON_SEGMENTED_TEXTURED; break;
            case BUTTON_PUSH_INSET2:
                widget = CoreUIWidgets.BUTTON_PUSH_INSET2; break;
            case BUTTON_COLOR_WELL:
                widget = CoreUIWidgets.COLOR_WELL; break;

            default:
                throw new UnsupportedOperationException();
        }

        if (st == State.ROLLOVER && !hasRolloverEffect) {
            st = State.ACTIVE;
        }

        Object direction = null;
        Object background = null;
        Integer animationFrame = null;

        if (bw == ButtonWidget.BUTTON_DISCLOSURE_TRIANGLE) {
            background = CoreUIBackgroundTypes.BACKGROUND_LIGHT;
            if (bs == ButtonState.OFF) {
                direction = g.getLayoutDirection() == UILayoutDirection.LEFT_TO_RIGHT ? CoreUIDirections.RIGHT : CoreUIDirections.LEFT;
            } else {
                direction = CoreUIDirections.DOWN;
            }
            animationFrame = -1;
            if (g instanceof AnimatedButtonConfiguration) {
                AnimatedButtonConfiguration ag = (AnimatedButtonConfiguration) g;
                ButtonState previousButtonState = ag.getPreviousButtonState();
                if (previousButtonState != bs) {
                    // frames are 1, 2, 3 when expanding and 3, 2, 1 when collapsing
                    animationFrame = 1 + Math.round(2 * ag.getTransition());
                    if (bs == ButtonState.OFF) {
                        animationFrame = 4 - animationFrame;
                    }
                    direction = g.getLayoutDirection() == UILayoutDirection.LEFT_TO_RIGHT ? CoreUIDirections.RIGHT : CoreUIDirections.LEFT;
                }
            }
        } else if (bw == ButtonWidget.BUTTON_DISCLOSURE) {
            direction = bs == ButtonState.OFF ? CoreUIDirections.DOWN : CoreUIDirections.UP;
        }

        Object buttonState = toButtonState(bs);

        // Rounded rect and rounded bevel buttons (previously) use PRESSED instead of VALUE to indicate selection (when enabled)
        if (bw == ButtonWidget.BUTTON_ROUNDED_RECT || (platformVersion < 101400 && bw == ButtonWidget.BUTTON_BEVEL_ROUND)) {
            if (bs == ButtonState.ON && (st == State.ACTIVE || st == State.INACTIVE)) {
                st = State.PRESSED;
            }
            buttonState = null;
        }

        Object size = toSize(sz);

        if (bw == ButtonWidget.BUTTON_COLOR_WELL) {
            size = null;
        }

        BasicRenderer r = getRenderer(
          WIDGET_KEY, widget,
          BACKGROUND_TYPE_KEY, background,
          SIZE_KEY, size,
          STATE_KEY, toState(st),
          PRESENTATION_STATE_KEY, toPresentationState(st),
          IS_FOCUSED_KEY, getFocused(g, g.isFocused()),
          VALUE_KEY, buttonState,
          DIRECTION_KEY, direction,
          ANIMATION_FRAME_KEY, animationFrame
        );
        return Renderer.create(r, rd);
    }

    @Override
    protected @NotNull Renderer getScrollColumnSizerRenderer(@NotNull ScrollColumnSizerConfiguration g)
    {
        RendererDescription rd = rendererDescriptions.getScrollColumnSizerRendererDescription(g);
        String widget = CoreUIWidgets.SCROLL_COLUMN_SIZER;

        BasicRenderer r =  getRenderer(
          WIDGET_KEY, widget,
          STATE_KEY, toState(g.getState()),
          IS_FOCUSED_KEY, getFocused(g, g.isFocused())
        );
        return Renderer.create(r, rd);
    }

    @Override
    protected @NotNull Renderer getScrollBarRenderer(@NotNull ScrollBarConfiguration g)
    {
        RendererDescription rd = rendererDescriptions.getScrollBarRendererDescription(g);
        RendererDescription trd = rendererDescriptions.getScrollBarThumbRendererDescription(g);
        ScrollBarWidget bw = g.getWidget();
        Size sz = g.getSize();
        State st = g.getState();
        Orientation o = g.getOrientation();
        float thumbPosition = g.getThumbPosition();
        float thumbExtent = g.getThumbExtent();

        String widget;
        boolean nothingToScroll = st == State.DISABLED || st == State.DISABLED_INACTIVE;
        boolean indicatorOnly = false;

        switch (bw)
        {
            case LEGACY:
                widget = CoreUIWidgets.SCROLL_BAR;
                // use pressed display for rollover
                if (st == State.ROLLOVER) {
                    st = State.PRESSED;
                }
                if (g.isTrackSuppressed()) {
                    indicatorOnly = true;
                }
                break;
            case OVERLAY:
                if (nothingToScroll) {
                    return NULL_RENDERER;
                }
                widget = CoreUIWidgets.OVERLAY_SCROLL_BAR;
                indicatorOnly = true;
                break;
            case OVERLAY_ROLLOVER:
                if (nothingToScroll) {
                    return NULL_RENDERER;
                }
                widget = CoreUIWidgets.OVERLAY_SCROLL_BAR;
                indicatorOnly = true;
                break;
            default:
                throw new UnsupportedOperationException();
        }

        if (nothingToScroll) {
            BasicRenderer r =  getRenderer(
              WIDGET_KEY, widget,
              SIZE_KEY, toSize(sz),
              ORIENTATION_KEY, toOrientation(o),
              NO_INDICATOR_KEY, true
            );
            return Renderer.create(r, rd);
        }

        ScrollBarKnobWidget kw = g.getKnobWidget();
        String variant = null;
        switch (kw)
        {
            case LIGHT:
                variant = CoreUIVariants.VARIANT_LIGHT;
                break;
            case DARK:
                variant = CoreUIVariants.VARIANT_DARK;
                break;
        }

        Renderer trackRenderer = null;
        Renderer thumbRenderer = null;

        // The legacy thumb renderer will paint a track, but because we need to increase the end gaps, we need to first
        // paint a track with the larger bounds.

        if (bw == ScrollBarWidget.OVERLAY_ROLLOVER || bw == ScrollBarWidget.LEGACY && !g.isTrackSuppressed()) {
            // This scroll bar has a visible track
            BasicRenderer r = getRenderer(
              WIDGET_KEY, widget,
              VARIANT_KEY, variant,
              SIZE_KEY, toSize(sz),
              ORIENTATION_KEY, toOrientation(o),
              NO_INDICATOR_KEY, true);
            trackRenderer = Renderer.create(r, rd);
        }

        if (kw != ScrollBarKnobWidget.NONE) {
            if (st == State.PRESSED || bw == ScrollBarWidget.OVERLAY_ROLLOVER) {
                BasicRenderer r = getRenderer(
                  WIDGET_KEY, widget,
                  VARIANT_KEY, variant,
                  SIZE_KEY, toSize(sz),
                  ORIENTATION_KEY, toOrientation(o),
                  NO_ARROWS_KEY, true,
                  THUMB_PROPORTION_KEY, (double) thumbExtent,
                  INDICATOR_ONLY_KEY, indicatorOnly,
                  STATE_KEY, "rollover",
                  //PRESSED_PART_KEY, "thumb",
                  VALUE_KEY, (double) thumbPosition);
                thumbRenderer = Renderer.create(r, trd);
            } else {
                BasicRenderer r = getRenderer(
                  WIDGET_KEY, widget,
                  VARIANT_KEY, variant,
                  SIZE_KEY, toSize(sz),
                  ORIENTATION_KEY, toOrientation(o),
                  NO_ARROWS_KEY, true,
                  THUMB_PROPORTION_KEY, (double) thumbExtent,
                  INDICATOR_ONLY_KEY, indicatorOnly,
                  VALUE_KEY, (double) thumbPosition);
                thumbRenderer = Renderer.create(r, trd);
            }
        }

        return Renderer.createCompositeRenderer(trackRenderer, thumbRenderer);
    }

    @Override
    protected @NotNull Renderer getToolBarItemWellRenderer(@NotNull ToolBarItemWellConfiguration g)
    {
        RendererDescription rd = rendererDescriptions.getToolBarItemWellRendererDescription(g);
        String widget = CoreUIWidgets.TOOL_BAR_ITEM_WELL;
        State st = g.getState();
        int platformVersion = JNRPlatformUtils.getPlatformVersion();
        boolean useLayer = platformVersion >= 101600;  // workaround?

        BasicRenderer r =  getRendererOptionallyLayered(
          useLayer,
          WIDGET_KEY, widget,
          STATE_KEY, toState(st),
          PRESENTATION_STATE_KEY, toPresentationState(st),
          FRAME_ONLY_KEY, g.isFrameOnly()
        );
        return Renderer.create(r, rd);
    }

    @Override
    protected @NotNull Renderer getGroupBoxRenderer(@NotNull GroupBoxConfiguration g)
    {
        RendererDescription rd = rendererDescriptions.getGroupBoxRendererDescription(g);
        String widget = CoreUIWidgets.FRAME_GROUP_BOX;

        BasicRenderer r = getRenderer(
          WIDGET_KEY, widget,
          STATE_KEY, toState(g.getState()),
          FRAME_ONLY_KEY, g.isFrameOnly()
        );
        return Renderer.create(r, rd);
    }

    protected @NotNull Renderer getGroupBoxMaskRenderer(@NotNull GroupBoxConfiguration g)
    {
        RendererDescription rd = rendererDescriptions.getGroupBoxRendererDescription(g);
        String widget = CoreUIWidgets.FRAME_GROUP_BOX;

        BasicRenderer r = getRenderer(
          WIDGET_KEY, widget,
          STATE_KEY, toState(g.getState()),
          FRAME_ONLY_KEY, g.isFrameOnly(),
          MASK_ONLY_KEY, true
        );
        return Renderer.create(r, rd);
    }

    @Override
    protected @NotNull Renderer getListBoxRenderer(@NotNull ListBoxConfiguration g)
    {
        RendererDescription rd = rendererDescriptions.getListBoxRendererDescription(g);
        String widget = CoreUIWidgets.FRAME_LIST_BOX;

        BasicRenderer r =  getRenderer(
          WIDGET_KEY, widget,
          STATE_KEY, toState(g.getState()),
          IS_FOCUSED_KEY, getFocused(g, g.isFocused()),
          FRAME_ONLY_KEY, g.isFrameOnly()
        );
        return Renderer.create(r, rd);
    }

    @Override
    protected @NotNull Renderer getTextFieldRenderer(@NotNull TextFieldConfiguration g)
    {
        RendererDescription rd = rendererDescriptions.getTextFieldRendererDescription(g);
        TextFieldWidget tw = g.getWidget();

        String widget = getWidget(tw);

        if (widget != null) {
            String variant = getVariant(tw);
            BasicRenderer r =  getRenderer(
              WIDGET_KEY, widget,
              SIZE_KEY, tw == TextFieldWidget.TEXT_FIELD ? toSize(Size.LARGE) : toSize(g.getSize()),
              STATE_KEY, toState(g.getState()),
              VARIANT_KEY, variant,
              IS_FOCUSED_KEY, getFocused(g, g.isFocused())
            );
            return Renderer.create(r, rd);
        } else if (tw.isSearch()) {
            Insetter searchButtonInsets = uiLayout.getSearchButtonPaintingInsets(g);
            Insetter cancelButtonInsets = tw.hasCancel() ? uiLayout.getCancelButtonPaintingInsets(g) : null;
            return new SearchFieldRenderer(g, rd, searchButtonInsets, cancelButtonInsets);
        } else {
            throw new UnsupportedOperationException();
        }
    }

    private @Nullable String getWidget(@NotNull TextFieldWidget tw)
    {
        switch (tw) {
            case TEXT_FIELD_ROUND:
            case TEXT_FIELD_ROUND_TOOLBAR:
                return CoreUIWidgets.FRAME_TEXT_FIELD_ROUND;
            case TEXT_FIELD:
                return CoreUIWidgets.FRAME_TEXT_FIELD;
            default:
                return null;
        }
    }

    private @Nullable String getVariant(@NotNull TextFieldWidget tw)
    {
        return tw.isToolbar() ? CoreUIWidgets.VARIANT_TEXT_FIELD_ROUND_TOOLBAR : null;
    }

    private class SearchFieldRenderer
      extends Renderer
    {
        private final @NotNull TextFieldConfiguration g;
        private final @NotNull RendererDescription rd;
        private final @Nullable Insetter searchButtonInsets;
        private final @Nullable Insetter cancelButtonInsets;

        public SearchFieldRenderer(@NotNull TextFieldConfiguration g,
                                   @NotNull RendererDescription rd,
                                   @Nullable Insetter searchButtonInsets,
                                   @Nullable Insetter cancelButtonInsets)
        {
            this.g = g;
            this.rd = rd;
            this.searchButtonInsets = searchButtonInsets;
            this.cancelButtonInsets = cancelButtonInsets;
        }

        @Override
        public void composeTo(@NotNull ReusableCompositor compositor)
        {
            float w = compositor.getWidth();
            float h = compositor.getHeight();

            {
                String variant = getVariant(g.getWidget());
                BasicRenderer br = getRenderer(
                  WIDGET_KEY, CoreUIWidgets.FRAME_TEXT_FIELD_ROUND,
                  SIZE_KEY, toSize(g.getSize()),
                  STATE_KEY, toState(g.getState()),
                  VARIANT_KEY, variant,
                  IS_FOCUSED_KEY, getFocused(g, g.isFocused())
                );
                Renderer r = Renderer.create(br, rd);
                r.composeTo(compositor);
            }

            if (searchButtonInsets != null) {
                Renderer br = getSearchFieldFindButtonRenderer(g);
                Rectangle2D bounds = searchButtonInsets.apply2D(w, h);
                Renderer r = Renderer.createOffsetRenderer(br, bounds);
                r.composeTo(compositor);
            }

            if (cancelButtonInsets != null) {
                Renderer br = getSearchFieldCancelButtonRenderer(g);
                Rectangle2D bounds = cancelButtonInsets.apply2D(w, h);
                Renderer r = Renderer.createOffsetRenderer(br, bounds);
                r.composeTo(compositor);
            }
        }
    }

    // This method is public to support evaluation
    @Override
    public @NotNull Renderer getSearchFieldFindButtonRenderer(@NotNull TextFieldConfiguration g)
    {
        TextFieldWidget widget = g.getWidget();
        boolean hasMenu = widget.hasMenu();
        BasicRenderer r = getRenderer(
          WIDGET_KEY, CoreUIWidgets.BUTTON_SEARCH_FIELD_FIND,
          USER_INTERFACE_LAYOUT_DIRECTION_KEY, toLayoutDirection(g.getLayoutDirection()),
          SIZE_KEY, toSize(g.getSize()),
          STATE_KEY, toState(g.getState()),
          VARIANT_KEY, hasMenu ? CoreUIVariants.VARIANT_WITH_MENU_GLYPH : null
        );
        RendererDescription rd = getSearchFieldFindButtonRendererDescription(g);
        return Renderer.create(r, rd);
    }

    protected @NotNull RendererDescription getSearchFieldFindButtonRendererDescription(@NotNull TextFieldConfiguration g)
    {
        return getSearchButtonRendererDescription(g);
    }

    // This method is public to support evaluation
    @Override
    public @NotNull Renderer getSearchFieldCancelButtonRenderer(@NotNull TextFieldConfiguration g)
    {
        BasicRenderer r = getRenderer(
          WIDGET_KEY, CoreUIWidgets.BUTTON_SEARCH_FIELD_CANCEL,
          USER_INTERFACE_LAYOUT_DIRECTION_KEY, toLayoutDirection(g.getLayoutDirection()),
          SIZE_KEY, toSize(g.getSize()),
          STATE_KEY, toState(g.getState())
          // value: 1 ?
        );
        RendererDescription rd = getSearchFieldCancelButtonRendererDescription(g);
        return Renderer.create(r, rd);
    }

    protected @NotNull RendererDescription getSearchFieldCancelButtonRendererDescription(@NotNull TextFieldConfiguration g)
    {
        return new BasicRendererDescription(0, 0, 0, 0);
    }

    @Override
    protected @NotNull Renderer getComboBoxButtonRenderer(@NotNull ComboBoxConfiguration g)
    {
        RendererDescription rd = rendererDescriptions.getComboBoxRendererDescription(g);
        ComboBoxWidget bw = g.getWidget();
        Size sz = g.getSize();
        State st = g.getState();
        UILayoutDirection ld = g.getLayoutDirection();

        if (st == State.ROLLOVER) {
            st = State.ACTIVE;
        }

        if (bw == ComboBoxWidget.BUTTON_COMBO_BOX_CELL) {
            BasicRenderer r =  getRenderer(
              WIDGET_KEY, CoreUIWidgets.BUTTON_COMBO_BOX,
              SIZE_KEY, toSize(sz),
              STATE_KEY, toState(st),
              PRESENTATION_STATE_KEY, toPresentationState(st),
              USER_INTERFACE_LAYOUT_DIRECTION_KEY, toLayoutDirection(ld),
              NO_FRAME_KEY, true);
            return Renderer.create(r, rd);
        }

        String widget = getWidget(bw);

        BasicRenderer r =  getRenderer(
          WIDGET_KEY, widget,
          SIZE_KEY, toSize(sz),
          STATE_KEY, toState(st),
          PRESENTATION_STATE_KEY, toPresentationState(st),
          USER_INTERFACE_LAYOUT_DIRECTION_KEY, toLayoutDirection(ld),
          IS_FOCUSED_KEY, getFocused(g, g.isFocused())
        );
        return Renderer.create(r, rd);
    }

    private @NotNull String getWidget(@NotNull ComboBoxWidget w)
    {
        int platformVersion = JNRPlatformUtils.getPlatformVersion();
        switch (w) {
            case BUTTON_COMBO_BOX_TEXTURED:
                return CoreUIWidgets.BUTTON_COMBO_BOX_TEXTURED;
            case BUTTON_COMBO_BOX_TEXTURED_TOOLBAR:
                return platformVersion >= 101100
                         ? CoreUIWidgets.BUTTON_COMBO_BOX_TOOLBAR
                         : CoreUIWidgets.BUTTON_COMBO_BOX_TEXTURED;
            default:
                return CoreUIWidgets.BUTTON_COMBO_BOX;
        }
    }

    @Override
    protected @NotNull Renderer getPopupButtonRenderer(@NotNull PopupButtonConfiguration g)
    {
        Renderer basicRenderer = getBasicPopupButtonRenderer(g);
        Renderer arrowsRenderer = getPopupArrowRenderer(g);
        Insetter arrowsInsetter = arrowsRenderer != null ? uiLayout.getPopUpArrowInsets(g) : null;
        return new PopupRenderer(g, basicRenderer, arrowsRenderer, arrowsInsetter);
    }

    /**
      Return the renderer used to draw the button part of a pop up button.
    */

    public @Nullable Renderer getBasicPopupButtonRenderer(@NotNull PopupButtonConfiguration g)
    {
        if (g.isCell()) {
            return null;
        }

        State st = g.getState();
        Size sz = g.getSize();
        UILayoutDirection ld = g.getLayoutDirection();
        String widget;
        List<String> extraParameters = null;
        RendererDescription rd = rendererDescriptions.getBasicPopupButtonRendererDescription(g);
        int platformVersion = JNRPlatformUtils.getPlatformVersion();

        boolean hasRolloverEffect = false;

        switch (g.getPopupButtonWidget()) {

            case BUTTON_POP_DOWN:
                widget = CoreUIWidgets.BUTTON_POP_DOWN;
                break;

            case BUTTON_POP_UP:
                widget = CoreUIWidgets.BUTTON_POP_UP;
                break;

            case BUTTON_POP_DOWN_BEVEL:
            case BUTTON_POP_UP_BEVEL:
                widget = CoreUIWidgets.BUTTON_BEVEL_ROUND;
                break;

            case BUTTON_POP_DOWN_ROUND_RECT:
                widget = CoreUIWidgets.BUTTON_POP_DOWN_INSET;
                break;

            case BUTTON_POP_UP_ROUND_RECT:
                widget = CoreUIWidgets.BUTTON_POP_UP_INSET;
                break;

            case BUTTON_POP_DOWN_RECESSED:
            case BUTTON_POP_UP_RECESSED:
                // The button is painted only in the Rollover or Pressed states.
                if (st != State.ROLLOVER && st != State.PRESSED) {
                    return null;
                }
                hasRolloverEffect = true;
                widget = CoreUIWidgets.BUTTON_PUSH_SCOPE;
                break;

            case BUTTON_POP_DOWN_TEXTURED:
                widget = CoreUIWidgets.BUTTON_POP_DOWN_TEXTURED;
                break;

            case BUTTON_POP_DOWN_TEXTURED_TOOLBAR:
                widget = platformVersion >= 101100
                           ? CoreUIWidgets.BUTTON_POP_DOWN_TOOLBAR
                           : CoreUIWidgets.BUTTON_POP_DOWN_TEXTURED;
                break;

            case BUTTON_POP_UP_TEXTURED:
                widget = CoreUIWidgets.BUTTON_POP_UP_TEXTURED;
                break;

            case BUTTON_POP_UP_TEXTURED_TOOLBAR:
                widget = platformVersion >= 101100
                           ? CoreUIWidgets.BUTTON_POP_UP_TOOLBAR
                           : CoreUIWidgets.BUTTON_POP_UP_TEXTURED;
                break;

            case BUTTON_POP_DOWN_GRADIENT:
            case BUTTON_POP_UP_GRADIENT:
                widget = CoreUIWidgets.BUTTON_BEVEL_INSET;
                break;

            case BUTTON_POP_DOWN_SQUARE:
            case BUTTON_POP_UP_SQUARE:
                widget = CoreUIWidgets.BUTTON_BEVEL;
                break;

            default:
                throw new UnsupportedOperationException();
        }

        if (st == State.ROLLOVER && !hasRolloverEffect) {
            st = State.ACTIVE;
        }

        BasicRenderer r;

        if (extraParameters == null) {
            r = getRenderer(
              WIDGET_KEY, widget,
              SIZE_KEY, toSize(sz),
              STATE_KEY, toState(st),
              PRESENTATION_STATE_KEY, toPresentationState(st),
              USER_INTERFACE_LAYOUT_DIRECTION_KEY, toLayoutDirection(ld));
        } else {
            List<Object> parameters = Arrays.asList(WIDGET_KEY, widget,
              SIZE_KEY, toSize(sz),
              STATE_KEY, toState(st),
              PRESENTATION_STATE_KEY, toPresentationState(st),
              USER_INTERFACE_LAYOUT_DIRECTION_KEY, toLayoutDirection(ld));
            parameters.addAll(extraParameters);
            r = getRenderer(parameters.toArray());
        }

        return Renderer.create(r, rd);
    }

    /**
      Return the renderer used to draw the arrows of pop up button.
    */

    public @Nullable Renderer getPopupArrowRenderer(@NotNull PopupButtonConfiguration g)
    {
        PopupButtonWidget w = g.getPopupButtonWidget();
        State state = g.getState();

        switch (w) {
            // These button widgets paint their own arrows
            case BUTTON_POP_DOWN:
            case BUTTON_POP_UP:
            case BUTTON_POP_DOWN_ROUND_RECT:
            case BUTTON_POP_UP_ROUND_RECT:
            case BUTTON_POP_DOWN_TEXTURED:
            case BUTTON_POP_UP_TEXTURED:
            case BUTTON_POP_DOWN_TEXTURED_TOOLBAR:
            case BUTTON_POP_UP_TEXTURED_TOOLBAR:
                return null;

            // These button widgets are unable to paint proper arrows in the rollover state on El Capitan (the color
            // is wrong), but the correct arrows are the same as the active state. It is better to use the native active
            // state arrows to avoid a flicker as the simulation is not perfect.

            case BUTTON_POP_UP_CELL:
            case BUTTON_POP_UP_GRADIENT:
            case BUTTON_POP_UP_SQUARE:
            case BUTTON_POP_UP_BEVEL:
                if (state == State.ROLLOVER) {
                    state = State.ACTIVE;
                }
                break;
        }

        Object ld = toLayoutDirection(g.getLayoutDirection());
        Object bt = CoreUIBackgroundTypes.BACKGROUND_LIGHT;
        Object st = toState(state);

        // TBD: background type should be lowered for a recessed button in rollover state

        if (g.isPopUp()) {
            Size arrowSize = g.getSize();  // already converted as needed to a supported size
            BasicRenderer r = getRenderer(
              WIDGET_KEY, CoreUIWidgets.BUTTON_POP_UP,
              ARROWS_ONLY_KEY, true,
              BACKGROUND_TYPE_KEY, bt,
              USER_INTERFACE_LAYOUT_DIRECTION_KEY, ld,
              SIZE_KEY, toSize(arrowSize),
              STATE_KEY, st);
            CoreUIRendererDescriptions rds = rendererDescriptions;
            RendererDescription rd = rds.getPopUpArrowRendererDescription(g, arrowSize);
            return Renderer.create(r, rd);
        } else {
            int platformVersion = JNRPlatformUtils.getPlatformVersion();
            String imageName = platformVersion >= 101100 ? "DropDownIndicator" : "image.DropDownIndicator";
            BasicRenderer r = getRenderer(
              WIDGET_KEY, CoreUIWidgets.IMAGE,
              IMAGE_IS_GRAYSCALE_KEY, true,
              IMAGE_NAME_KEY, imageName,
              BACKGROUND_TYPE_KEY, bt,
              USER_INTERFACE_LAYOUT_DIRECTION_KEY, ld,
              STATE_KEY, st);
            CoreUIRendererDescriptions rds = rendererDescriptions;
            RendererDescription rd = rds.getPullDownArrowRendererDescription(g);
            return Renderer.create(r, rd);
        }
    }

    @Override
    protected @NotNull Renderer getTitleBarRenderer(@NotNull TitleBarConfiguration g)
    {
        RendererDescription rd = rendererDescriptions.getTitleBarRendererDescription(g);
        Insetter closeButtonInsets = uiLayout.getTitleBarButtonInsets(g, TitleBarButtonWidget.CLOSE_BOX);
        Insetter minimizeButtonInsets = uiLayout.getTitleBarButtonInsets(g, TitleBarButtonWidget.MINIMIZE_BOX);
        Insetter resizeButtonInsets = uiLayout.getTitleBarButtonInsets(g, TitleBarButtonWidget.RESIZE_BOX);
        return new TitleBarRenderer(g, rd, closeButtonInsets, minimizeButtonInsets, resizeButtonInsets);
    }

    protected class TitleBarRenderer
      extends TitleBarRendererBase
    {
        private final @NotNull Object windowType;

        public TitleBarRenderer(@NotNull TitleBarConfiguration g,
                                @NotNull RendererDescription rd,
                                @Nullable Insetter closeButtonInsets,
                                @Nullable Insetter minimizeButtonInsets,
                                @Nullable Insetter resizeButtonInsets)
        {
            super(g, rd, closeButtonInsets, minimizeButtonInsets, resizeButtonInsets);

            switch (g.getWidget()) {
                case DOCUMENT_WINDOW:
                    windowType = CoreUIWindowTypes.DOCUMENT; break;
                case UTILITY_WINDOW:
                    windowType = CoreUIWindowTypes.UTILITY; break;
                default:
                    throw new UnsupportedOperationException();
            }
        }

        protected @NotNull BasicRenderer getBasicTitleBarRenderer(float w, float h)
        {
            String widget = CoreUIWidgets.WINDOW_FRAME;
            String state = toActiveState(g.getTitleBarState());

            return getRenderer(
              WIDGET_KEY, widget,
              WINDOW_TYPE_KEY, windowType,
              "kCUIWindowFrameDrawClippedKey", 1,
              "kCUIWindowFrameDrawTitleSeparatorKey", 1,
              "kCUIWindowFrameUnifiedTitleBarHeightKey", (double) h,
              STATE_KEY, state,
              VALUE_KEY, (double) h
            );
        }

        protected @NotNull Renderer getButtonRenderer(@NotNull TitleBarButtonWidget bw)
        {
            String widget;
            State st;

            switch (bw) {
                case CLOSE_BOX:
                    widget = CoreUIWidgets.TITLE_BAR_CLOSE_BOX;
                    st = g.getCloseButtonState();
                    break;
                case MINIMIZE_BOX:
                    widget = CoreUIWidgets.TITLE_BAR_COLLAPSE_BOX;
                    st = g.getMinimizeButtonState();
                    break;
                case RESIZE_BOX:
                    switch (g.getResizeAction()) {
                        case ZOOM_ENTER:
                        case ZOOM_EXIT:
                            widget = CoreUIWidgets.TITLE_BAR_ZOOM_BOX;
                            break;
                        case FULL_SCREEN_ENTER:
                            widget = CoreUIWidgets.TITLE_BAR_FULL_SCREEN_ENTER;
                            break;
                        case FULL_SCREEN_EXIT:
                            widget = CoreUIWidgets.TITLE_BAR_FULL_SCREEN_EXIT;
                            break;
                        default:
                            throw new UnsupportedOperationException();
                    }
                    st = g.getResizeButtonState();
                    break;
                default:
                    throw new UnsupportedOperationException();
            }

            String ps = g.getTitleBarState() == State.ACTIVE
                          ? CoreUIPresentationStates.ACTIVE
                          : CoreUIPresentationStates.INACTIVE;

            if (g.isDirty() && bw == TitleBarButtonWidget.CLOSE_BOX) {
                BasicRenderer r =  getRenderer(
                  WIDGET_KEY, widget,
                  WINDOW_TYPE_KEY, windowType,
                  PRESENTATION_STATE_KEY, ps,
                  STATE_KEY, toState(st),
                  VALUE_KEY, 1
                );
                return Renderer.create(r, null);
            } else {
                BasicRenderer r =  getRenderer(
                  WIDGET_KEY, widget,
                  WINDOW_TYPE_KEY, windowType,
                  PRESENTATION_STATE_KEY, ps,
                  STATE_KEY, toState(st)
                );
                return Renderer.create(r, null);
            }
        }
    }

    @Override
    protected @NotNull Renderer getIndeterminateProgressIndicatorRenderer(@NotNull IndeterminateProgressIndicatorConfiguration g)
    {
        RendererDescription rd = rendererDescriptions.getIndeterminateProgressIndicatorRendererDescription(g);
        Size sz = g.getSize();
        ProgressWidget w = g.getWidget();
        String widget;

        switch (w) {
            case INDETERMINATE_SPINNER:
                // Small spinners have a fixed size. Large spinners are scaled to fit. Other variants do not work.
                widget = CoreUIWidgets.PROGRESS_SPINNER_INDETERMINATE;
                if (sz != Size.SMALL) {
                    sz = Size.LARGE;
                }
                break;

            case INDETERMINATE_BAR:

                if (sz == Size.MINI) {
                    throw new UnsupportedOperationException();  // Mini size renders Mavericks style on Yosemite
                }

                widget = CoreUIWidgets.PROGRESS_BAR_INDETERMINATE; break;
            default:
                throw new UnsupportedOperationException();
        }

        Object orientation = w == ProgressWidget.INDETERMINATE_BAR ? toOrientation(g.getOrientation()) : null;

        BasicRenderer r =  getRenderer(
          WIDGET_KEY, widget,
          SIZE_KEY, toSize(sz),
          STATE_KEY, toState(g.getState()),
          ORIENTATION_KEY, orientation,
          ANIMATION_FRAME_KEY, g.getAnimationFrame()
        );
        return Renderer.create(r, rd);
    }

    @Override
    protected @NotNull Renderer getProgressIndicatorRenderer(@NotNull ProgressIndicatorConfiguration g)
    {
        RendererDescription rd = rendererDescriptions.getProgressIndicatorRendererDescription(g);

        Size sz = g.getSize();
        ProgressWidget w = g.getWidget();
        String widget = CoreUIWidgets.PROGRESS_BAR;

        if (w == ProgressWidget.SPINNER) {
            // Small spinners have a fixed size. Large spinners are scaled to fit. Other variants do not work.
            widget = CoreUIWidgets.PROGRESS_SPINNER;
            if (sz != Size.SMALL) {
                sz = Size.LARGE;
            }
        }

        if (g.getSize() == Size.MINI) {
            throw new UnsupportedOperationException();  // Mini size renders Mavericks style on Yosemite
        }

        Object orientation = w == ProgressWidget.BAR ? toOrientation(g.getOrientation()) : null;
        Object layoutDirection = w == ProgressWidget.BAR ? toLayoutDirection(g.getLayoutDirection()) : null;

        BasicRenderer r =  getRenderer(
          WIDGET_KEY, widget,
          SIZE_KEY, toSize(sz),
          STATE_KEY, toActiveStateCode(g.getState()),
          PRESENTATION_STATE_KEY, toPresentationState(g.getState()),
          ORIENTATION_KEY, orientation,
          USER_INTERFACE_LAYOUT_DIRECTION_KEY, layoutDirection,
          VALUE_KEY, g.getValue()
        );
        return Renderer.create(r, rd);
    }

    @Override
    protected @NotNull Renderer getSliderRenderer(@NotNull SliderConfiguration g)
    {
        SliderWidget sw = g.getWidget();
        if (sw == SliderWidget.SLIDER_CIRCULAR) {
            return getCircularSliderRenderer(g);
        } else {
            return getLinearSliderRenderer(g);
        }
    }

    protected @NotNull Renderer getCircularSliderRenderer(@NotNull SliderConfiguration g)
    {
        RendererDescription rd = rendererDescriptions.getSliderRendererDescription(g);

        Size sz = g.getSize();

        // Tinting option is not working

        int degrees = (int) Math.round(g.getValue() * 360);

        State st = g.getState();
        if (st == State.PRESSED || st == State.ROLLOVER) {
            st = State.ACTIVE;
        }

        BasicRenderer r =  getRenderer(
          WIDGET_KEY, CoreUIWidgets.DIAL,
          SIZE_KEY, toSize(sz),
          STATE_KEY, toState(st),
          IS_FOCUSED_KEY, false,
          NO_INDICATOR_KEY, true,  // because the wrong kind of dimple would be painted
          VALUE_KEY, degrees
        );
        return Renderer.create(r, rd);
    }

    protected @NotNull Renderer getLinearSliderRenderer(@NotNull SliderConfiguration g)
    {
        int style = getSliderRenderingVersion();

        // CoreUI is unable to render an upside-down slider, so we render it as a normal vertical slider, then flip
        // the rendering.

        SliderConfiguration flippedConfiguration = getFlippedConfiguration(g);
        if (flippedConfiguration != null) {
            g = flippedConfiguration;
        }

        Renderer trackRenderer = getSliderTrackRenderer(g);
        Renderer tickMarkRenderer = getSliderTickMarkRenderer(g);
        Renderer thumbRenderer = getSliderThumbRenderer(g);
        Insetter trackInsets = uiLayout.getSliderTrackPaintingInsets(g);
        Insetter thumbInsets = uiLayout.getSliderThumbPaintingInsets(g, g.getValue());
        Insetter tickMarkInsets = uiLayout.getSliderTickMarkPaintingInsets(g);

        // Translucent is a misnomer. Actually it is picking up color from the background, but the painting is opaque.

        boolean isThumbTranslucent = appearance != null && appearance.isDark();
        ReusableCompositor.PixelOperator tickOperator = null;

        // The interpretation of thumb painting insets changed for the new linear slider style.
        // The use of a tick mark renderer was introduced for the new linear slider style.

        if (style == SLIDER_11_0) {
            thumbInsets = trackInsets.prepend(thumbInsets);
            tickOperator = new FromMaskOperator();
        }

        Renderer r = new LinearSliderRenderer(g, trackRenderer, trackInsets, tickMarkRenderer, tickMarkInsets,
          thumbRenderer, thumbInsets, isThumbTranslucent, tickOperator);
        if (flippedConfiguration != null) {
            r = new FlipVerticalRenderer(r);
        }
        return r;
    }

    private @NotNull Insetter getThumbInsets(@NotNull SliderConfiguration g)
    {

        if (g.getWidget() == SliderWidget.SLIDER_UPSIDE_DOWN) {
            g = new SliderConfiguration(SliderWidget.SLIDER_VERTICAL, g.getSize(), g.getState(), g.isFocused(),
              g.getValue(), g.getNumberOfTickMarks(), g.getTickMarkPosition());
        }
        return uiLayout.getSliderThumbPaintingInsets(g, g.getValue());
    }

    private @Nullable SliderConfiguration getFlippedConfiguration(@NotNull SliderConfiguration g)
    {
        if (g.getWidget() == SliderWidget.SLIDER_UPSIDE_DOWN) {
            return new SliderConfiguration(SliderWidget.SLIDER_VERTICAL, g.getSize(), g.getState(), g.isFocused(),
              g.getValue(), g.getNumberOfTickMarks(), g.getTickMarkPosition());
        }
        return null;
    }

    protected @Nullable Renderer getSliderTickMarkRenderer(@NotNull SliderConfiguration g)
    {
        if (g.isLinear() && g.hasTickMarks()) {
            int style = getSliderRenderingVersion();
            if (style == SLIDER_11_0) {
                return getLinearSlider11TickMarkRenderer(g);
            }
        }
        return null;
    }

    protected @NotNull Renderer getLinearSlider11TickMarkRenderer(@NotNull SliderConfiguration g)
    {
        SliderTickMarkRendererFactory f = getLinearSlider11IndividualTickMarkRendererFactory();
        return new LinearSliderTickMarkRenderer(g, f);
    }

    @Override
    protected @NotNull Renderer getSliderTickRenderer(@NotNull SliderTickConfiguration g)
    {
        SliderConfiguration sg = g.getSliderConfiguration();
        if (sg.isLinear() && sg.hasTickMarks()) {
            int style = getSliderRenderingVersion();
            if (style == SLIDER_11_0) {
                SliderTickMarkRendererFactory f = getLinearSlider11IndividualTickMarkRendererFactory();
                return f.getSliderTickMarkRenderer(sg, g.isTinted());
            }
        }
        return NULL_RENDERER;
    }

    protected @NotNull SliderTickMarkRendererFactory getLinearSlider11IndividualTickMarkRendererFactory()
    {
        return (g, isTinted) -> {

            // isTinted was used to distinguish ticks above vs. below the thumb.
            // That distinction was made initially, but abandoned.
            isTinted = false;

            Size sz = g.getSize();
            State st = g.getState();
            // The inactive state is used to paint gray tick marks.
            // Accent colors were used initially, but abandoned.
            if (st != State.DISABLED && st != State.DISABLED_INACTIVE) {
                st = State.INACTIVE;
            }

            String orientation = g.isVertical() ? CoreUIOrientations.VERTICAL : CoreUIOrientations.HORIZONTAL;
            Object uiDirection = CoreUIUserInterfaceDirections.LEFT_TO_RIGHT;

            // Mask Only is used by AppKit to get an opaque tick mark, but then it is painted red.
            // The solution here is to generate a translucent tick mark and fix it in the LinearSlidererRenderer.

            BasicRenderer r = getRenderer(
              WIDGET_KEY, CoreUIWidgets.SLIDER_TICK_MARK_11,
              MASK_ONLY_KEY, false,
              SIZE_KEY, toSize(sz),
              STATE_KEY, toState(st),
              PRESENTATION_STATE_KEY, toPresentationState(st),
              ORIENTATION_KEY, orientation,
              USER_INTERFACE_LAYOUT_DIRECTION_KEY, uiDirection,
              VALUE_KEY, isTinted ? 1 : 0
            );

            RendererDescription rd = rendererDescriptions.getSliderTickMarkRendererDescription(g);
            return Renderer.create(r, rd);
        };
    }

    protected @NotNull Renderer getSliderTrackRenderer(@NotNull SliderConfiguration g)
    {
        SliderWidget sw = g.getWidget();
        if (sw == SliderWidget.SLIDER_CIRCULAR) {
            return NULL_RENDERER;
        }

        int style = getSliderRenderingVersion();
        Size sz = g.getSize();
        Object uiDirection = CoreUIUserInterfaceDirections.LEFT_TO_RIGHT;
        double value = g.getValue();

        if (sw == SliderWidget.SLIDER_HORIZONTAL_RIGHT_TO_LEFT) {
            sw = SliderWidget.SLIDER_HORIZONTAL;
            uiDirection = CoreUIUserInterfaceDirections.RIGHT_TO_LEFT;
        } else if (sw == SliderWidget.SLIDER_UPSIDE_DOWN) {
            // Slider will be rendered as a normal vertical slider, then the rendering will be flipped.
            sw = SliderWidget.SLIDER_VERTICAL;
        }

        State st = g.getState();
        if (st == State.ROLLOVER) {
            st = State.ACTIVE;
        }
        boolean isTinted = st != State.DISABLED && st != State.DISABLED_INACTIVE && !g.hasTickMarks();
        String orientation = sw == SliderWidget.SLIDER_VERTICAL ? CoreUIOrientations.VERTICAL : CoreUIOrientations.HORIZONTAL;
        Object direction = g.hasTickMarks() && style == SLIDER_10_10 ? toDirection(g.getTickMarkPosition()) : CoreUIDirections.NONE;

        BasicRenderer r = getRenderer(
          WIDGET_KEY, style == SLIDER_11_0 ? CoreUIWidgets.SLIDER_11 : CoreUIWidgets.SLIDER,
          NO_INDICATOR_KEY, true,
          SIZE_KEY, toSize(sz),
          STATE_KEY, toState(st),
          ORIENTATION_KEY, orientation,
          DIRECTION_KEY, direction,
          USER_INTERFACE_LAYOUT_DIRECTION_KEY, uiDirection,
          SLIDER_DRAW_TRACK_TINTED_KEY, isTinted,
          VALUE_KEY, value
        );

        RendererDescription rd = rendererDescriptions.getSliderTrackRendererDescription(g);
        return Renderer.create(r, rd);
    }

    @Override
    protected @NotNull Renderer getSliderThumbRenderer(@NotNull SliderConfiguration g)
    {
        return getSliderThumbRenderer(g, false);
    }

    protected @NotNull Renderer getSliderThumbMaskRenderer(@NotNull SliderConfiguration g)
    {
        return getSliderThumbRenderer(g, true);
    }

    protected @NotNull Renderer getSliderThumbRenderer(@NotNull SliderConfiguration g, boolean isMask)
    {
        SliderWidget sw = g.getWidget();
        if (sw == SliderWidget.SLIDER_CIRCULAR) {
            return NULL_RENDERER;
        }

        int style = getSliderRenderingVersion();
        Size sz = g.getSize();
        State st = g.getState();
        if (st == State.ROLLOVER) {
            st = State.ACTIVE;
        }
        String orientation = sw == SliderWidget.SLIDER_VERTICAL ? CoreUIOrientations.VERTICAL : CoreUIOrientations.HORIZONTAL;
        Object direction = g.hasTickMarks() ? toDirection(g.getTickMarkPosition()) : CoreUIDirections.NONE;
        BasicRenderer r = getRenderer(
          WIDGET_KEY, style == SLIDER_11_0 ? CoreUIWidgets.SLIDER_THUMB_11 : CoreUIWidgets.SLIDER_THUMB,
          SIZE_KEY, toSize(sz),
          STATE_KEY, toState(st),
          ORIENTATION_KEY, orientation,
          DIRECTION_KEY, direction,
          IS_FOCUSED_KEY, getFocused(g, g.isFocused()),
          VALUE_KEY, g.getValue(),
          MASK_ONLY_KEY, isMask,
          INDICATOR_ONLY_KEY, true
        );
        RendererDescription rd = rendererDescriptions.getSliderThumbRendererDescription(g);
        return Renderer.create(r, rd);
    }

    @Override
    protected @NotNull Renderer getSpinnerArrowsRenderer(@NotNull SpinnerArrowsConfiguration g)
    {
        RendererDescription rd = rendererDescriptions.getSpinnerArrowsRendererDescription(g);
        State st = g.getState();
        if (st == State.PRESSED && !g.isPressedTop()) {
            BasicRenderer r =  getRenderer(
              WIDGET_KEY, CoreUIWidgets.BUTTON_LITTLE_ARROWS,
              SIZE_KEY, toSize(g.getSize()),
              STATE_KEY, toState(st),
              VALUE_KEY, 1
            );
            return Renderer.create(r, rd);
        } else {
            BasicRenderer r =  getRenderer(
              WIDGET_KEY, CoreUIWidgets.BUTTON_LITTLE_ARROWS,
              SIZE_KEY, toSize(g.getSize()),
              STATE_KEY, toState(st)
            );
            return Renderer.create(r, rd);
        }
    }

    @Override
    protected @NotNull Renderer getSplitPaneDividerRenderer(@NotNull SplitPaneDividerConfiguration g)
    {
        RendererDescription rd = rendererDescriptions.getSplitPaneDividerRendererDescription(g);
        String widget = CoreUIWidgets.SPLITTER;
        String variant;
        String orientation = toOrientation(g.getOrientation());

        switch (g.getWidget()) {
            case THIN_DIVIDER:
                // Thin dividers are not drawn using CoreUI
                return NULL_RENDERER;
            case THICK_DIVIDER:
                variant = CoreUIVariants.DIVIDER_SPLITTER_DIMPLE;
                break;
            case PANE_SPLITTER:
            default:
                variant = CoreUIVariants.DIVIDER_SPLITTER_ROD_MAIL;
                break;
        }

        BasicRenderer r =  getRenderer(
          WIDGET_KEY, widget,
          STATE_KEY, toState(g.getState()),
          VARIANT_KEY, variant,
          ORIENTATION_KEY, orientation
        );
        return Renderer.create(r, rd);
    }

    @Override
    protected @NotNull Renderer getSegmentedButtonRenderer(@NotNull SegmentedButtonConfiguration g)
    {
        RendererDescription rd = rendererDescriptions.getSegmentedButtonRendererDescription(g);
        BasicRenderer r = getSegmentedButtonBasicRenderer(g, false);
        return Renderer.create(r, rd);
    }

    protected @NotNull Renderer getSegmentedButtonMaskRenderer(@NotNull SegmentedButtonConfiguration g)
    {
        RendererDescription rd = rendererDescriptions.getSegmentedButtonRendererDescription(g);
        BasicRenderer r = getSegmentedButtonBasicRenderer(g, true);
        return Renderer.create(r, rd);
    }

    protected @NotNull BasicRenderer getSegmentedButtonBasicRenderer(@NotNull SegmentedButtonConfiguration g, boolean isMask)
    {
        SegmentedButtonWidget bw = g.getWidget();
        State st = g.getState();
        int platformVersion = JNRPlatformUtils.getPlatformVersion();
        boolean isSelected = g.isSelected();
        boolean isLeftNeighborSelected = g.getLeftDividerState() == SegmentedButtonConfiguration.DividerState.SELECTED;
        boolean isRightNeighborSelected = g.getRightDividerState() == SegmentedButtonConfiguration.DividerState.SELECTED;
        boolean wantLeadingSeparator = g.getLeftDividerState() != SegmentedButtonConfiguration.DividerState.NONE;
        boolean wantTrailingSeparator =  g.getRightDividerState() != SegmentedButtonConfiguration.DividerState.NONE;

        Object leftType;
        Object rightType;

        if (bw.isSlider()) {
            leftType = isSelected ? BOTH_SELECTED : NONE_SELECTED;
            rightType = isSelected || isRightNeighborSelected ? BOTH_SELECTED : NONE_SELECTED;
            wantLeadingSeparator = false;
        } else {
            leftType = CoreUISegmentSeparatorTypes.NONE_SELECTED;
            if (isSelected) {
                leftType = isLeftNeighborSelected
                             ? BOTH_SELECTED
                             : CoreUISegmentSeparatorTypes.RIGHT_SELECTED;
            } else if (isLeftNeighborSelected) {
                leftType = CoreUISegmentSeparatorTypes.LEFT_SELECTED;
            }

            rightType = CoreUISegmentSeparatorTypes.NONE_SELECTED;
            if (isSelected) {
                rightType = isRightNeighborSelected
                              ? BOTH_SELECTED
                              : CoreUISegmentSeparatorTypes.LEFT_SELECTED;
            } else if (isRightNeighborSelected) {
                rightType = CoreUISegmentSeparatorTypes.RIGHT_SELECTED;
            }
        }

        boolean useLayer = false;

        // In 11.0, segment button backgrounds do not change when disabled.
        if (platformVersion >= 101600) {
            useLayer = true;
            if (st == State.DISABLED) {
                st = State.ACTIVE;
            } else if (st == State.DISABLED_INACTIVE) {
                st = State.INACTIVE;
            }
        }

        // On 10.14, textured segmented button backgrounds do not change when inactive, but CoreUI will paint them
        // differently. The configurations cannot be canonicalized because the text colors differ.

        if (bw.isTextured() && !bw.isToolbar() && st.isInactive() && (platformVersion >= 101400 && platformVersion < 101500)) {
            st = st.toActive();
        }

        // Selected buttons in a textured select-any control use the background of the corresponding unselected button.
        if (g.isTextured() && isSelected && g.getTracking() == SwitchTracking.SELECT_ANY) {
            isSelected = false;
        }

        // On 10.14 dark mode, the background colors for an inactive unselected textured separated button on the toolbar
        // are incorrect when painted using CoreUI draw, but are correct using CoreUI layers.

        if (appearance != null && appearance.isDark() && st.isInactive() && !isSelected
              && bw == BUTTON_SEGMENTED_TEXTURED_SEPARATED_TOOLBAR) {
            useLayer = true;
        }

        Object state = toState(st);

        String widget = CoreUIWidgets.BUTTON_SEGMENTED;
        switch (bw) {
            case BUTTON_TAB:
                widget = CoreUIWidgets.BUTTON_TAB; break;
            case BUTTON_SEGMENTED:
                widget = CoreUIWidgets.BUTTON_SEGMENTED; break;
            case BUTTON_SEGMENTED_SLIDER:
                widget = CoreUIWidgets.BUTTON_SEGMENTED_SLIDER; break;
            case BUTTON_SEGMENTED_SLIDER_TOOLBAR:
            case BUTTON_SEGMENTED_SLIDER_TOOLBAR_ICONS:
                widget = CoreUIWidgets.BUTTON_SEGMENTED_SLIDER_TOOLBAR; break;
            case BUTTON_SEGMENTED_INSET:
                widget = CoreUIWidgets.BUTTON_SEGMENTED_INSET; break;
            case BUTTON_SEGMENTED_SCURVE:
                widget = CoreUIWidgets.BUTTON_SEGMENTED_SCURVE; break;
            case BUTTON_SEGMENTED_TEXTURED:
                widget = CoreUIWidgets.BUTTON_SEGMENTED_TEXTURED; break;
            case BUTTON_SEGMENTED_TEXTURED_TOOLBAR:
            case BUTTON_SEGMENTED_TEXTURED_TOOLBAR_ICONS:
                if (platformVersion >= 101600) {
                    // selection state is indicated by the text/icon color
                    isSelected = false;
                }
                widget = platformVersion >= 101100
                           ? CoreUIWidgets.BUTTON_SEGMENTED_TOOLBAR
                           : CoreUIWidgets.BUTTON_SEGMENTED_TEXTURED;
                break;
            case BUTTON_SEGMENTED_TOOLBAR:
                widget = CoreUIWidgets.BUTTON_SEGMENTED_TOOLBAR; break;
            case BUTTON_SEGMENTED_SMALL_SQUARE:
                widget = CoreUIWidgets.BUTTON_BEVEL_INSET; break;
            case BUTTON_SEGMENTED_SEPARATED:
                widget = CoreUIWidgets.BUTTON_SEGMENTED_SEPARATED; break;
            case BUTTON_SEGMENTED_TEXTURED_SEPARATED:
                widget = platformVersion >= 101100
                           ? CoreUIWidgets.BUTTON_SEGMENTED_SEPARATED_TEXTURED
                           : CoreUIWidgets.BUTTON_SEGMENTED_SEPARATED_TOOLBAR; break;
            case BUTTON_SEGMENTED_TEXTURED_SEPARATED_TOOLBAR:
            case BUTTON_SEGMENTED_TEXTURED_SEPARATED_TOOLBAR_ICONS:
                if (platformVersion >= 101600 && g.getTracking() == SwitchTracking.SELECT_ANY) {
                    // selection state is indicated by the text/icon color
                    isSelected = false;
                }
                widget = platformVersion >= 101100
                           ? CoreUIWidgets.BUTTON_SEGMENTED_SEPARATED_TOOLBAR
                           : CoreUIWidgets.BUTTON_SEGMENTED_SEPARATED_TOOLBAR; break;
        }

        String variant = null;
        if (platformVersion >= 101600 && bw.isToolbar()) {
            if (bw.isIconsOnly()) {
                variant = "";
            } else {
                variant = CoreUIVariants.VARIANT_TEXT_CONTENT;
            }
        }

        return getRendererOptionallyLayered(
          useLayer,
          WIDGET_KEY, widget,
          SIZE_KEY, toSize(g.getSize()),
          STATE_KEY, state,
          PRESENTATION_STATE_KEY, toPresentationState(st),
          IS_FOCUSED_KEY, getFocused(g, g.isFocused()),
          USER_INTERFACE_LAYOUT_DIRECTION_KEY, CoreUIUserInterfaceDirections.LEFT_TO_RIGHT,
          DIRECTION_KEY, toDirection(g.getDirection()),
          POSITION_KEY, toSegmentPosition(g.getPosition()),
          SEGMENT_LEADING_SEPARATOR_KEY, wantLeadingSeparator,
          SEGMENT_TRAILING_SEPARATOR_KEY, wantTrailingSeparator,
          SEGMENT_LEADING_SEPARATOR_TYPE_KEY, leftType,
          SEGMENT_TRAILING_SEPARATOR_TYPE_KEY, rightType,
          VALUE_KEY, isSelected ? 1 : 0,
          MASK_ONLY_KEY, isMask,
          VARIANT_KEY, variant
        );
    }

    @Override
    protected @NotNull Renderer getTableColumnHeaderRenderer(@NotNull TableColumnHeaderConfiguration g)
    {
        RendererDescription rd = rendererDescriptions.getTableColumnHeaderRendererDescription(g);
        // Aqua look and feel:
        // horizontal alignment is LEFT
        // vertical alignment is TOP
        // direction is for the (optional) sort arrow, no arrow if direction is NONE
        // The height of a list header button as drawn by CoreUI is fixed.
        // That is not so good if the application has specified a larger height.
        // The Aqua look and feel uses a BUTTON_BEVEL when the header is tall.
        // Unfortunately, this prevents the sort arrow from being painted.

        // CoreUI in Yosemite uses the value parameter to indicate that a sort arrow should be drawn.
        // There is no other concept of display as selected.
        // The arrow cannot be turned off using direction NONE.

        // CoreUI always paints a 1 px divider on each side.
        // The NO_FRAME parameter has no effect.

        Object value = g.getSortArrowDirection() != ColumnSortArrowDirection.NONE;

        BasicRenderer r = getRenderer(
          WIDGET_KEY, CoreUIWidgets.BUTTON_LIST_HEADER,
          STATE_KEY, toState(g.getState()),
          IS_FOCUSED_KEY, getFocused(g, g.isFocused()),
          DIRECTION_KEY, toDirection(g.getSortArrowDirection()),
          //USER_INTERFACE_LAYOUT_DIRECTION_KEY, toLayoutDirection(g.getLayoutDirection()),
          //NO_FRAME_KEY, CoreUIDirections.NONE,
          //BACKGROUND_TYPE_KEY, CoreUIBackgroundTypes.BACKGROUND_BORDERLESS,
          //VERTICAL_ALIGNMENT_KEY, toVerticalAlignment(VerticalAlignment.TOP),
          //HORIZONTAL_ALIGNMENT_KEY, toHorizontalAlignment(HorizontalAlignment.LEFT),
          VALUE_KEY, value
        );
        return Renderer.create(r, rd);
    }

    @Override
    protected @NotNull Renderer getGradientRenderer(@NotNull GradientConfiguration g)
    {
        RendererDescription rd = rendererDescriptions.getGradientRendererDescription(g);
        // In Yosemite, FREE_FORM does nothing, and the state has no effect.

        String variant = toVariant(g.getWidget());
        Object state = toActiveStateCode(g.getState());

        BasicRenderer r =  getRenderer(
          WIDGET_KEY, CoreUIWidgets.GRADIENT,
          VARIANT_KEY, variant,
          STATE_KEY, state);
        return Renderer.create(r, rd);
    }

    protected boolean getFocused(@NotNull Configuration g, boolean b)
    {
        // This may be a temporary workaround. Since the introduction of focus rings, CoreUI has not drawn focused
        // widgets differently. However, in the 10.14 beta, it draws focus borders, which are unwanted.

        return false;
    }

    protected @NotNull Object toSize(@NotNull Size sz)
    {
        switch (sz)
        {
            case MINI:
                return CoreUISizeVariants.MINI;
            case SMALL:
                return CoreUISizeVariants.SMALL;
            case REGULAR:
                return CoreUISizeVariants.REGULAR;
            case LARGE:
                return CoreUISizeVariants.LARGE;
        }
        throw new UnsupportedOperationException();
    }

    /**
      Map the specified state to the encoding used by native code. All states are supported.
    */

    protected @NotNull String toState(@NotNull State st)
    {
        switch (st)
        {
            case ACTIVE:
                return "normal";
            case INACTIVE:
                return "inactive";
            case DISABLED:
                return "disabled";
            case DISABLED_INACTIVE:
                return "disabled";
            case PRESSED:
                return "pressed";
            case ACTIVE_DEFAULT:
                return "pulsed";
            case ROLLOVER:
                return "rollover";
        }
        throw new UnsupportedOperationException();
    }

    /**
      Map the specified state to the encoding used by native code. Only the active and inactive states are
      supported.
    */

    protected @NotNull String toActiveState(@NotNull State st)
    {
        switch (st)
        {
            case INACTIVE:
            case DISABLED:
            case DISABLED_INACTIVE:
                return "inactive";
        }
        return "normal";
    }

    /**
      Map the specified state to the encoding used by native code. All non-active states are mapped to disabled.
    */

    protected @NotNull String toActiveDisabledState(@NotNull State st)
    {
        switch (st)
        {
            case INACTIVE:
            case DISABLED:
            case DISABLED_INACTIVE:
                return "disabled";
        }
        return "normal";
    }

    /**
      Map the specified state to the integer encoding used by native code. Only the active and inactive states are
      supported.
    */

    protected int toActiveStateCode(@NotNull State st)
    {
        switch (st)
        {
            case INACTIVE:
            case DISABLED:
            case DISABLED_INACTIVE:
                return 1;
        }
        return 0;
    }

    protected @NotNull Object toPresentationState(@NotNull State st)
    {
        switch (st)
        {
            case ACTIVE:
                return CoreUIPresentationStates.ACTIVE;
            case INACTIVE:
                return CoreUIPresentationStates.INACTIVE;
            case DISABLED:
                return CoreUIPresentationStates.ACTIVE;
            case DISABLED_INACTIVE:
                return CoreUIPresentationStates.INACTIVE;
            case PRESSED:
                return CoreUIPresentationStates.ACTIVE;
            case ACTIVE_DEFAULT:
                return CoreUIPresentationStates.ACTIVE;
            case ROLLOVER:
                return CoreUIPresentationStates.ACTIVE;
        }
        throw new UnsupportedOperationException();
    }

    protected int toButtonState(@NotNull ButtonState s)
    {
        switch (s) {
            case OFF:
            case STATELESS:
                return 0;
            case ON:
                return 1;
            case MIXED:
                return 2;
        }
        throw new UnsupportedOperationException();
    }

    protected @NotNull Object toHorizontalAlignment(@NotNull HorizontalAlignment ha)
    {
        switch (ha)
        {
            case LEFT:
                return "left";
            case CENTER:
                return "center";
            case RIGHT:
                return "right";
        }
        throw new UnsupportedOperationException();
    }

    protected @NotNull Object toVerticalAlignment(@NotNull VerticalAlignment va)
    {
        switch (va)
        {
            case TOP:
                return "top";
            case CENTER:
                return "center";
            case BOTTOM:
                return "bottom";
        }
        throw new UnsupportedOperationException();
    }

    protected @NotNull String toOrientation(@NotNull Orientation o)
    {
        switch (o)
        {
            case HORIZONTAL:
                return CoreUIOrientations.HORIZONTAL;
            case VERTICAL:
                return CoreUIOrientations.VERTICAL;
        }
        throw new UnsupportedOperationException();
    }

    protected @NotNull Object toDirection(@NotNull ColumnSortArrowDirection d)
    {
        switch (d)
        {
            case NONE:
                return CoreUIDirections.NONE;
            case UP:
                return CoreUIDirections.UP;
            case DOWN:
                return CoreUIDirections.DOWN;
        }
        throw new UnsupportedOperationException();
    }

    protected @NotNull Object toDirection(@NotNull Direction d)
    {
        switch (d)
        {
            case NONE:
                return CoreUIDirections.NONE;
            case UP:
                return CoreUIDirections.UP;
            case DOWN:
                return CoreUIDirections.DOWN;
            case LEFT:
                return CoreUIDirections.LEFT;
            case RIGHT:
                return CoreUIDirections.RIGHT;
        }
        throw new UnsupportedOperationException();
    }

    protected @NotNull Object toDirection(@NotNull TickMarkPosition p)
    {
        switch (p)
        {
            case ABOVE:
                return CoreUIDirections.UP;
            case BELOW:
                return CoreUIDirections.DOWN;
            case LEFT:
                return CoreUIDirections.LEFT;
            case RIGHT:
                return CoreUIDirections.RIGHT;
        }
        throw new UnsupportedOperationException();
    }

    protected @NotNull Object toLayoutDirection(@NotNull UILayoutDirection ld)
    {
        switch (ld)
        {
            case LEFT_TO_RIGHT:
                return CoreUIUserInterfaceDirections.LEFT_TO_RIGHT;
            case RIGHT_TO_LEFT:
                return CoreUIUserInterfaceDirections.RIGHT_TO_LEFT;
            default:
                throw new UnsupportedOperationException();
        }
    }

    protected @NotNull Object toSegmentPosition(@NotNull Position sp)
    {
        switch (sp)
        {
            case FIRST:
                return CoreUISegmentPositions.FIRST;
            case MIDDLE:
                return CoreUISegmentPositions.MIDDLE;
            case LAST:
                return CoreUISegmentPositions.LAST;
            case ONLY:
                return CoreUISegmentPositions.ONLY;
        }
        throw new UnsupportedOperationException();
    }

    protected @NotNull String toVariant(@NotNull GradientWidget gw)
    {
        switch (gw) {
            case GRADIENT_FIND_BAR:
                return CoreUIVariants.GRADIENT_FIND_BAR;
            case GRADIENT_FINDER_INFO:
                return CoreUIVariants.GRADIENT_FINDER_INFO;
            case GRADIENT_FINDER_SIDE_BAR:
                return CoreUIVariants.GRADIENT_FINDER_SIDE_BAR;
            case GRADIENT_FREEFORM:
                return CoreUIVariants.GRADIENT_FREEFORM;
            case GRADIENT_LIST_BACKGROUND_EVEN:
                return CoreUIVariants.GRADIENT_LIST_BACKGROUND_EVEN;
            case GRADIENT_LIST_BACKGROUND_ODD:
                return CoreUIVariants.GRADIENT_LIST_BACKGROUND_ODD;
            case GRADIENT_SCOPE_BACKGROUND_BAR:
                return CoreUIVariants.GRADIENT_SCOPE_BACKGROUND_BAR;
            case GRADIENT_SCOPE_BACKGROUND_EVEN:
                return CoreUIVariants.GRADIENT_SCOPE_BACKGROUND_EVEN;
            case GRADIENT_SCOPE_BACKGROUND_ODD:
                return CoreUIVariants.GRADIENT_SCOPE_BACKGROUND_ODD;
            case GRADIENT_SIDE_BAR:
                return CoreUIVariants.GRADIENT_SIDE_BAR;
            case GRADIENT_SIDE_BAR_SELECTION:
                return CoreUIVariants.GRADIENT_SIDE_BAR_SELECTION;
            case GRADIENT_SIDE_BAR_SELECTION_MULTIPLE:
                return CoreUIVariants.GRADIENT_SIDE_BAR_SELECTION_MULTIPLE;
            default:
                throw new UnsupportedOperationException();
        }
    }

    protected @NotNull BasicRenderer getRenderer(Object... args)
    {
        return getRendererOptionallyLayered(false, args);
    }

    protected @NotNull BasicRenderer getRendererOptionallyLayered(boolean useLayer, Object... args)
    {
        if ((args.length % 2) != 0) {
            throw new IllegalArgumentException("getRenderer requires an even number of parameters");
        }

        return (data, rw, rh, w, h) -> {
            float xscale = ((float) rw) / w;
            float yscale = ((float) rh) / h;

            if (debugFlag) {
                showRenderingArguments(args);
            }

            if (appearance != null) {
                configureNativeAppearance(appearance);
            }

            if (useJRS) {
                nativeJRSPaint(data, rw, rh, xscale, yscale, args);
            } else {
                // Layer painting is experimental and in many cases does not work.

                boolean shouldUseLayer = useLayer;

                if (Boolean.TRUE.equals(forceLayers)) {
                    shouldUseLayer = true;
                } else if (Boolean.FALSE.equals(forceLayers)) {
                    shouldUseLayer = false;
                }

                if (shouldUseLayer && args.length >= 2) {
                    if (args[1].equals("kCUIWidgetWindowFrame")) {
                        // this widget provokes an exception on 10.14 at least
                        shouldUseLayer = false;
                    }
                }

                try {
                    nativePaint(data, rw, rh, xscale, yscale, args, shouldUseLayer);
                } catch (RuntimeException ex) {
                    System.err.println("Exception during native painting");
                    if (!debugFlag) {
                        showRenderingArguments(args);
                    }
                    ex.printStackTrace();
                }
            }
        };
    }

    private static void showRenderingArguments(@NotNull Object[] args)
    {
        String msg = "Rendering";
        for (Object o : args) {
            msg += " " + o;
        }
        System.err.println(msg);
        System.err.flush();
    }

    @Override
    public @NotNull String toString()
    {
        return useJRS ? "Core UI via JRS" : "Core UI";
    }

    private static native void nativePaint(int[] data, int w, int h, float xscale, float yscale, Object[] args, boolean useLayer);
    private static native void nativeJRSPaint(int[] data, int w, int h, float xscale, float yscale, Object[] args);
}
