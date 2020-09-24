/*
 * Copyright (c) 2015-2020 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.aqua.jrs;

import java.awt.geom.Rectangle2D;

import org.violetlib.jnr.Insetter;
import org.violetlib.jnr.LayoutInfo;
import org.violetlib.jnr.Painter;
import org.violetlib.jnr.aqua.*;
import org.violetlib.jnr.aqua.impl.AquaUIPainterBase;
import org.violetlib.jnr.aqua.impl.FromMaskOperator;
import org.violetlib.jnr.aqua.impl.LinearSliderRenderer;
import org.violetlib.jnr.aqua.impl.PopupRenderer;
import org.violetlib.jnr.aqua.impl.SliderTickConfiguration;
import org.violetlib.jnr.aqua.impl.TitleBarRendererBase;
import org.violetlib.jnr.impl.BasicRenderer;
import org.violetlib.jnr.impl.BasicRendererDescription;
import org.violetlib.jnr.impl.Renderer;
import org.violetlib.jnr.impl.RendererDescription;
import org.violetlib.jnr.impl.ReusableCompositor;
import org.violetlib.jnr.impl.jrs.JRSUIConstants;
import org.violetlib.jnr.impl.jrs.JRSUIControl;
import org.violetlib.jnr.impl.jrs.JRSUIState;
import org.violetlib.vappearances.VAppearance;

import org.jetbrains.annotations.*;

/**
  A painter that renders Aqua widgets using the native rendering used by the Aqua look and feel, by way of the JDK
  classes that interface to the (undocumented) Java Runtime Support framework. Caching of rendered images is supported.
*/

public class JRSPainter
  extends AquaUIPainterBase
{
    protected final JRSRendererMaker maker;

    private static final @NotNull JRSRendererDescriptions rendererDescriptions = new JRSRendererDescriptions();

    public JRSPainter()
      throws UnsupportedOperationException
    {
        super(rendererDescriptions);

        try {
            // The following will ensure that the native library support has been initialized (if possible).
            JRSUIControl.initJRSUI();
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Unable to initialize the native library: " + ex);
        }

        maker = new JRSRendererMaker();
    }

    @Override
    public @NotNull JRSPainter copy()
    {
        return new JRSPainter();
    }

    @Override
    protected @NotNull Painter getPainter(@NotNull Configuration g,
                                          @NotNull VAppearance appearance,
                                          @NotNull Renderer r,
                                          float width,
                                          float height)
    {
        BasicRenderer br = r.getBasicRenderer();
        if (br instanceof JRSRenderer) {
            JRSRenderer jr = (JRSRenderer) br;
            if (!jr.isAnimating()) {
                JRSUIState state = jr.getControlState();
                if (state != null) {
                    return new JRSRenderedPainter(state, appearance, r, width, height);
                }
            }
        }

        return super.getPainter(g, appearance, r, width, height);
    }

    protected void configureSize(@NotNull Size sz)
    {
        switch (sz) {
            case REGULAR:
                maker.set(JRSUIConstants.Size.REGULAR);
                break;
            case SMALL:
                maker.set(JRSUIConstants.Size.SMALL);
                break;
            case MINI:
                maker.set(JRSUIConstants.Size.MINI);
                break;
            case LARGE:
                maker.set(JRSUIConstants.Size.LARGE);
                break;
            default:
                throw new UnsupportedOperationException();
        }
    }

    protected void configureState(@NotNull State st)
    {
        switch (st) {
            case ACTIVE:
                maker.set(JRSUIConstants.State.ACTIVE);
                break;
            case INACTIVE:
                maker.setProperty(JRSUIConstants.State.INACTIVE);
                break;
            case DISABLED:
            case DISABLED_INACTIVE:
                maker.setProperty(JRSUIConstants.State.DISABLED);
                break;
            case PRESSED:
                maker.setProperty(JRSUIConstants.State.PRESSED);
                break;
            case ACTIVE_DEFAULT:
                maker.setProperty(JRSUIConstants.State.PULSED);
                break;
            case ROLLOVER:
                maker.setProperty(JRSUIConstants.State.ROLLOVER);
                break;
            default:
                throw new UnsupportedOperationException();
        }
    }

    protected void configureLayoutDirection(@NotNull UILayoutDirection ld)
    {
        // Not supported
    }

    protected void configureOrientation(@NotNull Orientation o)
    {
        switch (o) {
            case HORIZONTAL:
                maker.set(JRSUIConstants.Orientation.HORIZONTAL);
                break;
            case VERTICAL:
                maker.set(JRSUIConstants.Orientation.VERTICAL);
                break;
            default:
                throw new UnsupportedOperationException();
        }
    }

    protected void configureDirection(@NotNull Direction d)
    {
        switch (d) {
            case NONE:
                maker.set(JRSUIConstants.Direction.NONE);
                break;
            case UP:
                maker.set(JRSUIConstants.Direction.UP);
                break;
            case DOWN:
                maker.set(JRSUIConstants.Direction.DOWN);
                break;
            case LEFT:
                maker.set(JRSUIConstants.Direction.LEFT);
                break;
            case RIGHT:
                maker.set(JRSUIConstants.Direction.RIGHT);
                break;
            default:
                throw new UnsupportedOperationException();
        }
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

        maker.reset();

        State st = g.getState();
        ButtonState bs = g.getButtonState();

        boolean hasRolloverEffect = false;

        switch (bw) {
            case BUTTON_PUSH:
                maker.set(JRSUIConstants.Widget.BUTTON_PUSH);
                break;
            case BUTTON_BEVEL:
                maker.set(JRSUIConstants.Widget.BUTTON_BEVEL);
                break;
            case BUTTON_BEVEL_ROUND:
                maker.set(JRSUIConstants.Widget.BUTTON_BEVEL_ROUND);
                break;
            case BUTTON_CHECK_BOX:
                maker.set(JRSUIConstants.Widget.BUTTON_CHECK_BOX);
                break;
            case BUTTON_RADIO:
                maker.set(JRSUIConstants.Widget.BUTTON_RADIO);
                break;
            case BUTTON_DISCLOSURE:
                maker.set(JRSUIConstants.Widget.BUTTON_DISCLOSURE);
                break;
            case BUTTON_HELP:
                maker.set(JRSUIConstants.Widget.BUTTON_ROUND_HELP);
                break;
            case BUTTON_GRADIENT:
                maker.set(JRSUIConstants.Widget.BUTTON_BEVEL_INSET);
                break;
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

                maker.set(JRSUIConstants.Widget.BUTTON_PUSH_SCOPE);
                break;
            case BUTTON_INLINE:
                maker.set(JRSUIConstants.Widget.BUTTON_PUSH_INSET2);  // not correct, inline buttons are not supported by Core UI
                break;
            case BUTTON_ROUNDED_RECT:
                maker.set(JRSUIConstants.Widget.BUTTON_PUSH_INSET);
                break;
            case BUTTON_TEXTURED:
            case BUTTON_TEXTURED_TOOLBAR:  // not supported
                maker.set(JRSUIConstants.Widget.BUTTON_PUSH_TEXTURED);
                break;
            case BUTTON_ROUND:
                maker.set(JRSUIConstants.Widget.BUTTON_ROUND);
                break;
            case BUTTON_ROUND_INSET:
                maker.set(JRSUIConstants.Widget.BUTTON_ROUND_INSET);
                break;
            case BUTTON_ROUND_TEXTURED:
            case BUTTON_ROUND_TOOLBAR:
                throw new UnsupportedOperationException();
            case BUTTON_DISCLOSURE_TRIANGLE:
                maker.set(JRSUIConstants.Widget.DISCLOSURE_TRIANGLE);
                break;
            case BUTTON_PUSH_INSET2:
                maker.set(JRSUIConstants.Widget.BUTTON_PUSH_INSET2);
                break;
            default:
                throw new UnsupportedOperationException();
        }

        if (st == State.ROLLOVER && !hasRolloverEffect) {
            st = State.ACTIVE;
        }

        // Rounded rect and rounded bevel buttons use PRESSED instead of VALUE to indicate selection (when enabled)
        if (bw == ButtonWidget.BUTTON_ROUNDED_RECT || bw == ButtonWidget.BUTTON_BEVEL_ROUND) {
            if (bs == ButtonState.ON && (st == State.ACTIVE || st == State.INACTIVE)) {
                st = State.PRESSED;
            }
            bs = ButtonState.STATELESS;
        }

        configureSize(g.getSize());
        configureState(st);
        maker.set(JRSUIConstants.AlignmentVertical.CENTER);
        maker.set(JRSUIConstants.AlignmentHorizontal.CENTER);

        if (bw == ButtonWidget.BUTTON_DISCLOSURE_TRIANGLE) {
            switch (g.getButtonState()) {
                case ON:
                    maker.set(JRSUIConstants.Direction.DOWN);
                    break;
                case OFF:
                    maker.set(g.getLayoutDirection() == UILayoutDirection.LEFT_TO_RIGHT
                                ? JRSUIConstants.Direction.RIGHT
                                : JRSUIConstants.Direction.LEFT);
                    break;
                default:
                    throw new UnsupportedOperationException();
            }

            int animationFrame = -1;
            if (g instanceof AnimatedButtonConfiguration) {
                AnimatedButtonConfiguration ag = (AnimatedButtonConfiguration) g;
                ButtonState previousButtonState = ag.getPreviousButtonState();
                if (previousButtonState != bs) {
                    // frames are 1, 2, 3 when expanding and 3, 2, 1 when collapsing
                    animationFrame = 1 + Math.round(2 * ag.getTransition());
                    if (bs == ButtonState.OFF) {
                        animationFrame = 4 - animationFrame;
                    }
                    maker.set(g.getLayoutDirection() == UILayoutDirection.LEFT_TO_RIGHT
                                ? JRSUIConstants.Direction.RIGHT
                                : JRSUIConstants.Direction.LEFT);
                }
            }
            maker.setAnimationFrame(animationFrame);

        } else if (bw == ButtonWidget.BUTTON_DISCLOSURE) {
            maker.set(bs == ButtonState.OFF ? JRSUIConstants.Direction.DOWN : JRSUIConstants.Direction.UP);
        } else if (bw == ButtonWidget.BUTTON_CHECK_BOX || bw == ButtonWidget.BUTTON_RADIO
                     || bw == ButtonWidget.BUTTON_BEVEL || bw == ButtonWidget.BUTTON_BEVEL_ROUND
                     || bw == ButtonWidget.BUTTON_GRADIENT
                     || bw == ButtonWidget.BUTTON_TEXTURED || bw == ButtonWidget.BUTTON_TEXTURED_TOOLBAR
                     || bw == ButtonWidget.BUTTON_ROUND) {
            switch (bs) {
                case ON:
                    maker.setValue(1);
                    break;
                case OFF:
                    maker.setValue(0);
                    break;
                case MIXED:
                    maker.setValue(2);
                    break;
                case STATELESS:
                    break;
                default:
                    throw new UnsupportedOperationException();
            }
        }

        return Renderer.create(maker.getRenderer(), rd);
    }

    @Override
    protected @NotNull Renderer getTableColumnHeaderRenderer(@NotNull TableColumnHeaderConfiguration g)
    {
        RendererDescription rd = rendererDescriptions.getTableColumnHeaderRendererDescription(g);

        maker.reset();
        maker.set(JRSUIConstants.Widget.BUTTON_LIST_HEADER);
        maker.set(g.isSelected() ? JRSUIConstants.BooleanValue.YES : JRSUIConstants.BooleanValue.NO);
        maker.set(g.isFocused() ? JRSUIConstants.Focused.YES : JRSUIConstants.Focused.NO);
        configureState(g.getState());

        switch (g.getSortArrowDirection()) {
            case UP:
                maker.set(JRSUIConstants.Direction.UP);
                break;
            case DOWN:
                maker.set(JRSUIConstants.Direction.DOWN);
                break;
            case NONE:
                maker.set(JRSUIConstants.Direction.NONE);
                break;
            default:
                throw new UnsupportedOperationException();
        }

        return Renderer.create(maker.getRenderer(), rd);
    }

    @Override
    protected @NotNull Renderer getScrollColumnSizerRenderer(@NotNull ScrollColumnSizerConfiguration g)
    {
        RendererDescription rd = rendererDescriptions.getScrollColumnSizerRendererDescription(g);

        maker.reset();
        maker.set(JRSUIConstants.Widget.SCROLL_COLUMN_SIZER);
        configureState(g.getState());
        maker.set(JRSUIConstants.AlignmentVertical.CENTER);
        maker.set(JRSUIConstants.AlignmentHorizontal.CENTER);
        return Renderer.create(maker.getRenderer(), rd);
    }

    @Override
    protected @NotNull Renderer getScrollBarRenderer(@NotNull ScrollBarConfiguration g)
    {
        RendererDescription rd = rendererDescriptions.getScrollBarRendererDescription(g);
        RendererDescription trd = rendererDescriptions.getScrollBarThumbRendererDescription(g);

        State st = g.getState();

        maker.reset();

        switch (g.getWidget()) {
            case LEGACY:
                maker.set(JRSUIConstants.Widget.SCROLL_BAR);
                break;
            default:
                // The JRSUI classes do not support overlay scroll bar painting.
                throw new UnsupportedOperationException();
        }

        configureSize(g.getSize());
        configureState(st == State.ACTIVE ? State.ACTIVE : State.INACTIVE);
        configureOrientation(g.getOrientation());

        Renderer trackRenderer = null;
        Renderer thumbRenderer = null;

        // The legacy thumb renderer will paint a track, but because we need to increase the end gaps, we need to first
        // paint a track with the larger bounds.

        if (!g.isTrackSuppressed()) {
            maker.set(JRSUIConstants.NothingToScroll.YES);
            maker.set(JRSUIConstants.ScrollBarPart.NONE);
            trackRenderer = Renderer.create(maker.getRenderer(), rd);
        }

        if (st != State.DISABLED && st != State.DISABLED_INACTIVE && g.getKnobWidget() != ScrollBarKnobWidget.NONE) {
            maker.set(JRSUIConstants.NothingToScroll.NO);
            maker.setValue(g.getThumbPosition());
            maker.setThumbStart(g.getThumbPosition());
            maker.setThumbPercent(g.getThumbExtent());
            maker.set(JRSUIConstants.ShowArrows.NO);
            if (st == State.PRESSED) {
                maker.set(JRSUIConstants.ScrollBarPart.THUMB);
            } else {
                maker.set(JRSUIConstants.ScrollBarPart.NONE);
            }
            maker.set(g.isTrackSuppressed() ? JRSUIConstants.IndicatorOnly.YES : JRSUIConstants.IndicatorOnly.NO);
            thumbRenderer = Renderer.create(maker.getRenderer(), trd);
        }

        return Renderer.createCompositeRenderer(trackRenderer, thumbRenderer);
    }

    @Override
    protected @NotNull Renderer getToolBarItemWellRenderer(@NotNull ToolBarItemWellConfiguration g)
    {
        RendererDescription rd = rendererDescriptions.getToolBarItemWellRendererDescription(g);

        maker.reset();
        maker.set(JRSUIConstants.Widget.TOOLBAR_ITEM_WELL);
        configureState(g.getState());
        maker.set(g.isFrameOnly() ? JRSUIConstants.FrameOnly.YES : JRSUIConstants.FrameOnly.NO);
        return Renderer.create(maker.getRenderer(), rd);
    }

    @Override
    protected @NotNull Renderer getGroupBoxRenderer(@NotNull GroupBoxConfiguration g)
    {
        RendererDescription rd = rendererDescriptions.getGroupBoxRendererDescription(g);

        maker.reset();
        maker.set(JRSUIConstants.Widget.FRAME_GROUP_BOX);
        configureState(g.getState());
        maker.set(g.isFrameOnly() ? JRSUIConstants.FrameOnly.YES : JRSUIConstants.FrameOnly.NO);
        return Renderer.create(maker.getRenderer(), rd);
    }

    @Override
    protected @NotNull Renderer getListBoxRenderer(@NotNull ListBoxConfiguration g)
    {
        RendererDescription rd = rendererDescriptions.getListBoxRendererDescription(g);

        maker.reset();
        maker.set(JRSUIConstants.Widget.FRAME_LIST_BOX);
        configureState(g.getState());
        maker.set(g.isFrameOnly() ? JRSUIConstants.FrameOnly.YES : JRSUIConstants.FrameOnly.NO);
        return Renderer.create(maker.getRenderer(), rd);
    }

    @Override
    protected @NotNull Renderer getTextFieldRenderer(@NotNull TextFieldConfiguration g)
    {
        RendererDescription rd = rendererDescriptions.getTextFieldRendererDescription(g);

        TextFieldWidget tw = g.getWidget();
        JRSUIConstants.Widget widget = getWidget(tw);

        if (widget != null) {
            maker.reset();
            maker.set(widget);
            configureSize(g.getSize());
            configureState(g.getState());
            return Renderer.create(maker.getRenderer(), rd);
        } else if (tw.isSearch()) {
            Insetter searchButtonInsets = uiLayout.getSearchButtonPaintingInsets(g);
            Insetter cancelButtonInsets = tw.hasCancel() ? uiLayout.getCancelButtonPaintingInsets(g) : null;
            return new SearchFieldRenderer(g, searchButtonInsets, cancelButtonInsets);
        } else {
            throw new UnsupportedOperationException();
        }
    }

    private @Nullable JRSUIConstants.Widget getWidget(@NotNull TextFieldWidget tw)
    {
        switch (tw) {
            case TEXT_FIELD_ROUND:
            case TEXT_FIELD_ROUND_TOOLBAR:    // not supported
                return JRSUIConstants.Widget.FRAME_TEXT_FIELD_ROUND;
            case TEXT_FIELD:
                return JRSUIConstants.Widget.FRAME_TEXT_FIELD;
            default:
                return null;
        }
    }

    private class SearchFieldRenderer
      extends Renderer
    {
        private final @NotNull TextFieldConfiguration g;
        private final @Nullable Insetter searchButtonInsets;
        private final @Nullable Insetter cancelButtonInsets;

        public SearchFieldRenderer(@NotNull TextFieldConfiguration g,
                                   @Nullable Insetter searchButtonInsets,
                                   @Nullable Insetter cancelButtonInsets)
        {
            this.g = g;
            this.searchButtonInsets = searchButtonInsets;
            this.cancelButtonInsets = cancelButtonInsets;
        }

        @Override
        public void composeTo(@NotNull ReusableCompositor compositor)
        {
            float w = compositor.getWidth();
            float h = compositor.getHeight();

            {
                maker.reset();
                maker.set(JRSUIConstants.Widget.FRAME_TEXT_FIELD_ROUND);
                configureSize(g.getSize());
                configureState(g.getState());
                BasicRenderer br = maker.getRenderer();
                Renderer r = Renderer.create(br, new BasicRendererDescription(0, 0, 0, 0));
                compositor.compose(r);
            }

            if (searchButtonInsets != null) {
                Renderer r = getSearchFieldFindButtonRenderer(g);
                Rectangle2D bounds = searchButtonInsets.apply2D(w, h);
                compositor.compose(Renderer.createOffsetRenderer(r, bounds));
            }

            if (cancelButtonInsets != null) {
                Renderer r = getSearchFieldCancelButtonRenderer(g);
                Rectangle2D bounds = cancelButtonInsets.apply2D(w, h);
                compositor.compose(Renderer.createOffsetRenderer(r, bounds));
            }
        }
    }

    // This method is public to support evaluation
    @Override
    public @NotNull Renderer getSearchFieldFindButtonRenderer(@NotNull TextFieldConfiguration g)
    {
        TextFieldWidget widget = g.getWidget();
        boolean hasMenu = widget.hasMenu();
        maker.reset();
        maker.set(JRSUIConstants.Widget.BUTTON_SEARCH_FIELD_FIND);
        if (hasMenu) {
            maker.set(JRSUIConstants.Variant.MENU_GLYPH);
        }
        configureSize(g.getSize());
        configureState(g.getState());
        BasicRenderer r = maker.getRenderer();
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
        maker.reset();
        maker.set(JRSUIConstants.Widget.BUTTON_SEARCH_FIELD_CANCEL);
        configureSize(g.getSize());
        configureState(g.getState());
        BasicRenderer r = maker.getRenderer();
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

        maker.reset();

        switch (g.getWidget()) {
            case BUTTON_COMBO_BOX:
                maker.set(JRSUIConstants.Widget.BUTTON_COMBO_BOX);
                maker.set(JRSUIConstants.IndicatorOnly.NO);
                maker.set(JRSUIConstants.ArrowsOnly.NO);
                maker.set(JRSUIConstants.AlignmentHorizontal.RIGHT);
                maker.set(JRSUIConstants.AlignmentVertical.CENTER);
                break;
            case BUTTON_COMBO_BOX_CELL:
                // With the new JRS on El Capitan, renders as a normal combo box
//                maker.set(JRSUIConstants.Widget.BUTTON_POP_DOWN);
//                maker.set(JRSUIConstants.IndicatorOnly.NO);
//                maker.set(JRSUIConstants.ArrowsOnly.YES);
//                maker.set(JRSUIConstants.AlignmentHorizontal.RIGHT);
//                maker.set(JRSUIConstants.AlignmentVertical.CENTER);
//                break;
                throw new UnsupportedOperationException();
            default:
                throw new UnsupportedOperationException();
        }

        State st = g.getState();
        if (st == State.ROLLOVER) {
            st = State.ACTIVE;
        }

        configureSize(g.getSize());
        configureState(st);
        configureLayoutDirection(g.getLayoutDirection());

        return Renderer.create(maker.getRenderer(), rd);
    }

    @Override
    protected @NotNull Renderer getSegmentedButtonRenderer(@NotNull SegmentedButtonConfiguration g)
    {
        RendererDescription rd = rendererDescriptions.getSegmentedButtonRendererDescription(g);
        Position pos = g.getPosition();

        SegmentedButtonWidget bw = g.getWidget();
        State st = g.getState();

        maker.reset();

        switch (bw) {
            case BUTTON_TAB:
            case BUTTON_SEGMENTED_SLIDER:
                maker.set(JRSUIConstants.Widget.TAB);
                break;
            case BUTTON_SEGMENTED:
                maker.set(JRSUIConstants.Widget.BUTTON_SEGMENTED);
                break;
            case BUTTON_SEGMENTED_SEPARATED:
                // not supported
                // an attempted workaround, must coordinate with renderer description
                pos = Position.ONLY;
                maker.set(JRSUIConstants.Widget.BUTTON_SEGMENTED);
                break;
            case BUTTON_SEGMENTED_INSET:
                maker.set(JRSUIConstants.Widget.BUTTON_SEGMENTED_INSET);
                break;
            case BUTTON_SEGMENTED_SCURVE:
                maker.set(JRSUIConstants.Widget.BUTTON_SEGMENTED_SCURVE);
                break;
            case BUTTON_SEGMENTED_TEXTURED:
            case BUTTON_SEGMENTED_TEXTURED_TOOLBAR:  // not supported
                maker.set(JRSUIConstants.Widget.BUTTON_SEGMENTED_TEXTURED);
                break;
            case BUTTON_SEGMENTED_TOOLBAR:
                maker.set(JRSUIConstants.Widget.BUTTON_SEGMENTED_TOOLBAR);
                break;
            case BUTTON_SEGMENTED_SMALL_SQUARE:
                maker.set(JRSUIConstants.Widget.BUTTON_BEVEL_INSET);
                break;
            case BUTTON_SEGMENTED_TEXTURED_SEPARATED:
            case BUTTON_SEGMENTED_TEXTURED_SEPARATED_TOOLBAR:
                // not supported
                // an attempted workaround, must coordinate with renderer description
                pos = Position.ONLY;
                maker.set(JRSUIConstants.Widget.BUTTON_SEGMENTED_TEXTURED);
                break;
        }

        switch (pos) {
            case FIRST:
                maker.set(JRSUIConstants.SegmentPosition.FIRST);
                break;
            case MIDDLE:
                maker.set(JRSUIConstants.SegmentPosition.MIDDLE);
                break;
            case LAST:
                maker.set(JRSUIConstants.SegmentPosition.LAST);
                break;
            case ONLY:
                maker.set(JRSUIConstants.SegmentPosition.ONLY);
                break;
            default:
                throw new UnsupportedOperationException();
        }

        configureSize(g.getSize());
        configureState(st);
        configureDirection(g.getDirection());

        maker.set(JRSUIConstants.SegmentLeadingSeparator.NO);
        maker.set(JRSUIConstants.SegmentTrailingSeparator.YES);
        maker.set(g.isSelected() ? JRSUIConstants.BooleanValue.YES : JRSUIConstants.BooleanValue.NO);
        maker.set(g.isFocused() ? JRSUIConstants.Focused.YES : JRSUIConstants.Focused.NO);
        return Renderer.create(maker.getRenderer(), rd);
    }

    @Override
    protected @NotNull Renderer getPopupButtonRenderer(@NotNull PopupButtonConfiguration g)
    {
        Renderer basicRenderer = getBasicPopupButtonRenderer(g);
        Renderer arrowsRenderer = getPopupArrowRenderer(g);
        Insetter arrowsInsetter = arrowsRenderer != null ? uiLayout.getPopUpArrowInsets(g) : null;
        return new PopupRenderer(g, basicRenderer, arrowsRenderer, arrowsInsetter);
    }

    protected @Nullable Renderer getBasicPopupButtonRenderer(@NotNull PopupButtonConfiguration g)
    {
        if (g.isCell()) {
            return null;
        }

        State st = g.getState();

        RendererDescription rd = rendererDescriptions.getPopupButtonRendererDescription(g);

        maker.reset();

        boolean hasRolloverEffect = false;

        switch (g.getPopupButtonWidget()) {
            case BUTTON_POP_DOWN:
                maker.set(JRSUIConstants.Widget.BUTTON_POP_DOWN);
                break;

            case BUTTON_POP_UP:
                maker.set(JRSUIConstants.Widget.BUTTON_POP_UP);
                break;

            case BUTTON_POP_DOWN_BEVEL:
            case BUTTON_POP_UP_BEVEL:
                maker.set(JRSUIConstants.Widget.BUTTON_BEVEL_ROUND);
                break;

            case BUTTON_POP_DOWN_ROUND_RECT:
                maker.set(JRSUIConstants.Widget.BUTTON_POP_DOWN_INSET);
                break;

            case BUTTON_POP_UP_ROUND_RECT:
                maker.set(JRSUIConstants.Widget.BUTTON_POP_UP_INSET);
                break;

            case BUTTON_POP_DOWN_RECESSED:
            case BUTTON_POP_UP_RECESSED:
                // The button is painted only in the Rollover or Pressed states.
                if (st != State.ROLLOVER && st != State.PRESSED) {
                    return null;
                }

                hasRolloverEffect = true;
                maker.set(JRSUIConstants.Widget.BUTTON_PUSH_SCOPE);
                break;

            case BUTTON_POP_DOWN_TEXTURED:
            case BUTTON_POP_UP_TEXTURED:
            case BUTTON_POP_DOWN_TEXTURED_TOOLBAR:
            case BUTTON_POP_UP_TEXTURED_TOOLBAR:
                maker.set(JRSUIConstants.Widget.BUTTON_PUSH_TEXTURED);  // may not be exactly right
                break;

            case BUTTON_POP_DOWN_GRADIENT:
            case BUTTON_POP_UP_GRADIENT:
                maker.set(JRSUIConstants.Widget.BUTTON_BEVEL_INSET);
                break;

            case BUTTON_POP_DOWN_SQUARE:
                //maker.set(JRSUIConstants.Widget.BUTTON_POP_DOWN_SQUARE);
                maker.set(JRSUIConstants.Widget.BUTTON_BEVEL);
                break;

            case BUTTON_POP_UP_SQUARE:
                //maker.set(JRSUIConstants.Widget.BUTTON_POP_UP_SQUARE);
                maker.set(JRSUIConstants.Widget.BUTTON_BEVEL);
                break;

            default:
                throw new UnsupportedOperationException();
        }

        if (st == State.ROLLOVER && !hasRolloverEffect) {
            st = State.ACTIVE;
        }

        maker.set(JRSUIConstants.IndicatorOnly.NO);
        maker.set(JRSUIConstants.ArrowsOnly.NO);
        maker.set(JRSUIConstants.AlignmentHorizontal.CENTER);
        configureSize(g.getSize());
        configureState(st);
        configureLayoutDirection(g.getLayoutDirection());
        return Renderer.create(maker.getRenderer(), rd);
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
                return null;

            // These button widgets are unable to paint proper arrows in the rollover state on El Capitan (the color
            // is wrong), but the correct arrows are the same as the active state. It is better to use the native active
            // state arrows to avoid a flicker as the simulation is not perfect.

            case BUTTON_POP_UP_CELL:
            case BUTTON_POP_UP_GRADIENT:
            case BUTTON_POP_UP_SQUARE:
            case BUTTON_POP_UP_BEVEL:
            case BUTTON_POP_UP_TEXTURED:
            case BUTTON_POP_UP_TEXTURED_TOOLBAR:
                if (state == State.ROLLOVER) {
                    state = State.ACTIVE;
                }
                break;
        }

        // JRS cannot paint a pull down arrow
        if (!g.isPopUp()) {
            return null;
        }

        maker.reset();
        maker.set(g.isPopUp()
                    ? JRSUIConstants.Widget.BUTTON_POP_UP_SQUARE
                    : JRSUIConstants.Widget.BUTTON_POP_DOWN_SQUARE);
        maker.set(JRSUIConstants.IndicatorOnly.YES);
        maker.set(JRSUIConstants.ArrowsOnly.YES);
        maker.set(JRSUIConstants.AlignmentHorizontal.RIGHT);
        configureSize(g.getSize());
        configureState(state);
        configureLayoutDirection(g.getLayoutDirection());
        BasicRenderer r = maker.getRenderer();

        JRSRendererDescriptions rds = rendererDescriptions;
        Size arrowSize = g.getSize();  // already converted as needed to a supported size
        RendererDescription rd = rds.getPopUpArrowRendererDescription(g, arrowSize);
        return Renderer.create(r, rd);
    }

    @Override
    protected @NotNull Renderer getTitleBarRenderer(@NotNull TitleBarConfiguration g)
    {
        // Used for internal frames

        RendererDescription rd = rendererDescriptions.getTitleBarRendererDescription(g);

        Insetter closeButtonInsets = uiLayout.getTitleBarButtonInsets(g, TitleBarButtonWidget.CLOSE_BOX);
        Insetter minimizeButtonInsets = uiLayout.getTitleBarButtonInsets(g, TitleBarButtonWidget.MINIMIZE_BOX);
        Insetter resizeButtonInsets = uiLayout.getTitleBarButtonInsets(g, TitleBarButtonWidget.RESIZE_BOX);
        return new TitleBarRenderer(g, rd, closeButtonInsets, minimizeButtonInsets, resizeButtonInsets);
    }

    protected class TitleBarRenderer
      extends TitleBarRendererBase
    {
        public TitleBarRenderer(@NotNull TitleBarConfiguration g,
                                @NotNull RendererDescription rd,
                                @Nullable Insetter closeButtonInsets,
                                @Nullable Insetter minimizeButtonInsets,
                                @Nullable Insetter resizeButtonInsets)
        {
            super(g, rd, closeButtonInsets, minimizeButtonInsets, resizeButtonInsets);
        }

        protected @NotNull BasicRenderer getBasicTitleBarRenderer(float w, float h)
        {
            LayoutInfo layoutInfo = uiLayout.getLayoutInfo(g);
            float titleBarHeight = layoutInfo.getFixedVisualHeight();
            int th = (int) Math.ceil(titleBarHeight);

            maker.reset();
            maker.set(JRSUIConstants.Widget.WINDOW_FRAME);
            maker.set(JRSUIConstants.WindowClipCorners.YES);
            maker.set(toWindowType(g));
            maker.setValue(th);
            maker.set(JRSUIConstants.WindowTitleBarSeparator.YES);
            configureState(g.getTitleBarState());
            return maker.getRenderer();
        }

        protected @NotNull Renderer getButtonRenderer(@NotNull TitleBarButtonWidget bw)
        {
            return getTitleBarButtonRenderer(g, bw);
        }
    }

    // public to support evaluation
    public @NotNull Renderer getTitleBarButtonRenderer(@NotNull TitleBarConfiguration g,
                                                       @NotNull TitleBarButtonWidget bw)
    {
        JRSUIConstants.Widget widget;
        State st;

        switch (bw) {
            case CLOSE_BOX:
                widget = JRSUIConstants.Widget.TITLE_BAR_CLOSE_BOX;
                st = g.getCloseButtonState();
                break;
            case MINIMIZE_BOX:
                widget = JRSUIConstants.Widget.TITLE_BAR_COLLAPSE_BOX;
                st = g.getMinimizeButtonState();
                break;
            case RESIZE_BOX:
                widget = JRSUIConstants.Widget.TITLE_BAR_ZOOM_BOX;
                st = g.getResizeButtonState();
                break;
            default:
                throw new UnsupportedOperationException();
        }

        TitleBarWidget tw = g.getWidget();
        int offset = tw == TitleBarWidget.DOCUMENT_WINDOW ? -1 : 1;

        maker.reset();
        maker.set(widget);
        maker.set(toWindowType(g));
        maker.set(JRSUIConstants.AlignmentVertical.CENTER);
        configureState(st);
        if (bw == TitleBarButtonWidget.CLOSE_BOX) {
            maker.set(g.isDirty() ? JRSUIConstants.BooleanValue.YES : JRSUIConstants.BooleanValue.NO);
        }
        BasicRenderer r = maker.getRenderer();
        RendererDescription rd = new BasicRendererDescription(offset, 0, 0, 0);
        return Renderer.create(r, rd);
    }

    protected @NotNull JRSUIConstants.WindowType toWindowType(@NotNull TitleBarConfiguration g)
    {
        switch(g.getWidget())
        {
            case DOCUMENT_WINDOW:
                return JRSUIConstants.WindowType.DOCUMENT;
            case UTILITY_WINDOW:
                return JRSUIConstants.WindowType.UTILITY;
            default:
                throw new UnsupportedOperationException();
        }
    }

    protected @NotNull Renderer getIndeterminateProgressIndicatorRenderer(@NotNull IndeterminateProgressIndicatorConfiguration g)
    {
        RendererDescription rd = rendererDescriptions.getIndeterminateProgressIndicatorRendererDescription(g);

        Size sz = g.getSize();

        maker.reset();

        switch (g.getWidget())
        {
            case INDETERMINATE_SPINNER:
                // Small spinners have a fixed size. Large spinners are scaled to fit. Other variants do not work.
                maker.set(JRSUIConstants.Widget.PROGRESS_SPINNER);
                if (sz != Size.SMALL) {
                    sz = Size.LARGE;
                }
                break;
            case INDETERMINATE_BAR:
                JRSUIConstants.Orientation orientation =
                  g.getOrientation() == Orientation.VERTICAL
                    ? JRSUIConstants.Orientation.VERTICAL
                    : JRSUIConstants.Orientation.HORIZONTAL;
                maker.set(JRSUIConstants.Widget.PROGRESS_INDETERMINATE_BAR);
                maker.set(orientation);
                break;
            default:
                throw new UnsupportedOperationException();
        }

        configureSize(sz);
        configureState(g.getState());
        // The state for a progress bar is not an AnimationFrameState
        // maker.setAnimationFrame(frame);
        return Renderer.create(maker.getRenderer(), rd);
    }

    @Override
    protected @NotNull Renderer getProgressIndicatorRenderer(@NotNull ProgressIndicatorConfiguration g)
    {
        if (g.getWidget() == ProgressWidget.SPINNER) {
            throw new UnsupportedOperationException();
        }

        RendererDescription rd = rendererDescriptions.getProgressIndicatorRendererDescription(g);

        JRSUIConstants.Orientation orientation = g.getOrientation() == Orientation.VERTICAL
                                                   ? JRSUIConstants.Orientation.VERTICAL
                                                   : JRSUIConstants.Orientation.HORIZONTAL;

        maker.reset();
        maker.set(JRSUIConstants.Widget.PROGRESS_BAR);
        maker.set(orientation);
        configureSize(g.getSize());
        configureState(g.getState());
        maker.setValue(g.getValue());
        return Renderer.create(maker.getRenderer(), rd);
    }

    @Override
    protected @NotNull Renderer getSliderRenderer(@NotNull SliderConfiguration g)
    {
        RendererDescription rd = rendererDescriptions.getSliderRendererDescription(g);

        Size sz = g.getSize();
        SliderWidget sw = g.getWidget();
        double value = g.getValue();

        maker.reset();

        // Tinting option is not working

        State st = g.getState();
        if (st == State.ROLLOVER) {
            st = State.ACTIVE;
        }

        if (sw == SliderWidget.SLIDER_CIRCULAR) {
            if (st == State.PRESSED) {
                st = State.ACTIVE;
            }
            int degrees = (int) Math.round(value * 360);
            maker.reset();
            maker.set(JRSUIConstants.Widget.DIAL);
            maker.set(JRSUIConstants.Focused.NO);
            maker.set(JRSUIConstants.NoIndicator.YES);  // because the wrong kind of dimple would be painted
            configureSize(sz);
            configureState(st);
            maker.setValue(degrees);
            return Renderer.create(maker.getRenderer(), rd);
        }

        int style = getSliderRenderingVersion();
        Renderer trackRenderer = getSliderTrackRenderer(g);
        Renderer tickMarkRenderer = getSliderTickMarkRenderer(g);
        Renderer thumbRenderer = getSliderThumbRenderer(g);
        Insetter trackInsets = uiLayout.getSliderTrackPaintingInsets(g);
        Insetter thumbInsets = uiLayout.getSliderThumbPaintingInsets(g, g.getValue());
        Insetter tickMarkInsets = trackInsets;
        boolean isThumbTranslucent = appearance != null && appearance.isDark();
        ReusableCompositor.PixelOperator tickOperator = null;

        // The interpretation of thumb painting insets changed for the new linear slider style.
        // The use of a tick mark renderer was introduced for the new linear slider style.

        if (style == SLIDER_11_0) {
            thumbInsets = trackInsets.prepend(thumbInsets);
            tickOperator = new FromMaskOperator();
        }

        return new LinearSliderRenderer(g, trackRenderer, trackInsets, tickMarkRenderer, tickMarkInsets,
          thumbRenderer, thumbInsets, isThumbTranslucent, tickOperator);
    }

    protected @Nullable Renderer getSliderTickMarkRenderer(@NotNull SliderConfiguration g)
    {
        return null;
    }

    protected @NotNull Renderer getSliderTrackRenderer(@NotNull SliderConfiguration g)
    {
        SliderWidget sw = g.getWidget();
        if (sw == SliderWidget.SLIDER_CIRCULAR) {
            return NULL_RENDERER;
        }

        // Mini sliders are not supported (must be consistent with layout code)
        final Size sz = g.getSize() == Size.MINI ? Size.SMALL : g.getSize();

        State st = g.getState();
        if (st == State.ROLLOVER) {
            st = State.ACTIVE;
        }

        double value = g.getValue();
        if (sw == SliderWidget.SLIDER_HORIZONTAL_RIGHT_TO_LEFT) {
            sw = SliderWidget.SLIDER_HORIZONTAL;
            value = 1 - value;
        } else if (sw == SliderWidget.SLIDER_UPSIDE_DOWN) {
            sw = SliderWidget.SLIDER_VERTICAL;
            value = 1 - value;
        }

        boolean isVertical = g.isVertical();
        int tickCount = g.getNumberOfTickMarks();
        TickMarkPosition tickPosition = g.getTickMarkPosition();

        JRSUIConstants.Orientation orientation = isVertical
                                                   ? JRSUIConstants.Orientation.VERTICAL
                                                   : JRSUIConstants.Orientation.HORIZONTAL;
        JRSUIConstants.Direction direction = tickCount > 0 ? toDirection(tickPosition) : JRSUIConstants.Direction.NONE;

        maker.reset();
        maker.set(JRSUIConstants.Widget.SLIDER);
        maker.set(JRSUIConstants.NoIndicator.YES);
        configureSize(sz);
        configureState(st);
        maker.set(orientation);
        maker.set(direction);
        maker.set(g.isFocused() ? JRSUIConstants.Focused.YES : JRSUIConstants.Focused.NO);
        maker.setValue(value);
        BasicRenderer r = maker.getRenderer();
        RendererDescription rd = rendererDescriptions.getSliderTrackRendererDescription(g);
        return Renderer.create(r, rd);
    }

    @Override
    protected @NotNull Renderer getSliderThumbRenderer(@NotNull SliderConfiguration g)
    {
        SliderWidget sw = g.getWidget();
        if (sw == SliderWidget.SLIDER_CIRCULAR) {
            return NULL_RENDERER;
        }

        // Mini sliders are not supported (must be consistent with layout code)
        final Size sz = g.getSize() == Size.MINI ? Size.SMALL : g.getSize();

        State st = g.getState();
        if (st == State.ROLLOVER) {
            st = State.ACTIVE;
        }

        JRSUIConstants.Orientation orientation = g.isVertical()
                                                   ? JRSUIConstants.Orientation.VERTICAL
                                                   : JRSUIConstants.Orientation.HORIZONTAL;
        JRSUIConstants.Direction direction = g.hasTickMarks()
                                               ? toDirection(g.getTickMarkPosition())
                                               : JRSUIConstants.Direction.NONE;
        maker.reset();
        maker.set(JRSUIConstants.Widget.SLIDER_THUMB);
        configureSize(sz);
        configureState(st);
        maker.set(orientation);
        maker.set(direction);
        maker.set(g.isFocused() ? JRSUIConstants.Focused.YES : JRSUIConstants.Focused.NO);
        maker.setValue(g.getValue());
        BasicRenderer r = maker.getRenderer();
        RendererDescription rd = rendererDescriptions.getSliderThumbRendererDescription(g);
        return Renderer.create(r, rd);
    }

    @Override
    protected @NotNull Renderer getSliderTickRenderer(@NotNull SliderTickConfiguration g)
    {
        return NULL_RENDERER;
    }

    protected @NotNull JRSUIConstants.Direction toDirection(@NotNull TickMarkPosition p)
    {
        switch (p)
        {
            case ABOVE:
                return JRSUIConstants.Direction.UP;
            case BELOW:
                return JRSUIConstants.Direction.DOWN;
            case LEFT:
                return JRSUIConstants.Direction.LEFT;
            case RIGHT:
                return JRSUIConstants.Direction.RIGHT;
        }
        throw new UnsupportedOperationException();
    }

    @Override
    protected @NotNull Renderer getSpinnerArrowsRenderer(@NotNull SpinnerArrowsConfiguration g)
    {
        RendererDescription rd = rendererDescriptions.getSpinnerArrowsRendererDescription(g);

        maker.reset();
        maker.set(JRSUIConstants.Widget.BUTTON_LITTLE_ARROWS);

        configureSize(g.getSize());
        configureState(g.getState());

        if (g.getState() == State.PRESSED) {
            maker.set(g.isPressedTop() ? JRSUIConstants.BooleanValue.NO : JRSUIConstants.BooleanValue.YES);
        }

        return Renderer.create(maker.getRenderer(), rd);
    }

    @Override
    protected @NotNull Renderer getSplitPaneDividerRenderer(@NotNull SplitPaneDividerConfiguration g)
    {
        RendererDescription rd = rendererDescriptions.getSplitPaneDividerRendererDescription(g);

        maker.reset();

        // Grabber seems to be a like a dimple except linear (fixed size)

        switch (g.getWidget())
        {
            case THIN_DIVIDER:
                maker.set(JRSUIConstants.Widget.DIVIDER_SEPARATOR_BAR);
                break;
            case THICK_DIVIDER:
                throw new UnsupportedOperationException();  // not supported currently
            case PANE_SPLITTER:
                maker.set(JRSUIConstants.Widget.DIVIDER_SPLITTER);
                break;
            default:
                throw new UnsupportedOperationException();
        }

        configureState(g.getState());
        configureOrientation(g.getOrientation());
        return Renderer.create(maker.getRenderer(), rd);
    }

    @Override
    protected @NotNull Renderer getGradientRenderer(@NotNull GradientConfiguration g)
    {
        RendererDescription rd = rendererDescriptions.getGradientRendererDescription(g);

        maker.reset();
        maker.set(JRSUIConstants.Widget.GRADIENT);

        // TBD: do we need focused selection?

        switch (g.getWidget())
        {
            case GRADIENT_FIND_BAR:
            case GRADIENT_FINDER_INFO:
            case GRADIENT_FINDER_SIDE_BAR:
            case GRADIENT_FREEFORM:
                throw new UnsupportedOperationException();
            case GRADIENT_LIST_BACKGROUND_EVEN:
                maker.set(JRSUIConstants.Variant.GRADIENT_LIST_BACKGROUND_EVEN);
                break;
            case GRADIENT_LIST_BACKGROUND_ODD:
                maker.set(JRSUIConstants.Variant.GRADIENT_LIST_BACKGROUND_ODD);
                break;
            case GRADIENT_SCOPE_BACKGROUND_BAR:
            case GRADIENT_SCOPE_BACKGROUND_EVEN:
            case GRADIENT_SCOPE_BACKGROUND_ODD:
                throw new UnsupportedOperationException();
            case GRADIENT_SIDE_BAR:
                maker.set(JRSUIConstants.Variant.GRADIENT_SIDE_BAR);
                break;
            case GRADIENT_SIDE_BAR_SELECTION:
                maker.set(JRSUIConstants.Variant.GRADIENT_SIDE_BAR_SELECTION);
                break;
            case GRADIENT_SIDE_BAR_SELECTION_MULTIPLE:
                throw new UnsupportedOperationException();
        }

        maker.set(g.getState() == State.ACTIVE ? JRSUIConstants.State.ACTIVE : JRSUIConstants.State.INACTIVE);
        return Renderer.create(maker.getRenderer(), rd);
    }

    @Override
    public @NotNull String toString()
    {
        return "JRS";
    }
}
