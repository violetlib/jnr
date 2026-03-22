/*
 * Copyright (c) 2025-2026 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.aqua.impl;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.violetlib.jnr.Insetter;
import org.violetlib.jnr.LayoutInfo;
import org.violetlib.jnr.aqua.*;
import org.violetlib.jnr.impl.BasicLayoutInfo;
import org.violetlib.jnr.impl.Insetters;

import java.awt.geom.Rectangle2D;

import static org.violetlib.jnr.aqua.AquaUIPainter.ButtonWidget.*;
import static org.violetlib.jnr.aqua.AquaUIPainter.ComboBoxWidget.BUTTON_COMBO_BOX_CELL;
import static org.violetlib.jnr.impl.JNRUtils.size;
import static org.violetlib.jnr.impl.JNRUtils.size2D;

/**
  Layout information for macOS 26+ widgets using the new UI.
*/

public class LayoutInfo_26
  extends LayoutInfo_15
{
    @Override
    protected @NotNull LayoutInfo getButtonLayoutInfo(@NotNull ButtonLayoutConfiguration g)
    {
        AquaUIPainter.ButtonWidget bw = g.getButtonWidget();
        AquaUIPainter.Size sz = g.getSize();

        if (bw == BUTTON_PUSH || bw == BUTTON_ROUNDED_RECT) {
            // AppKit layout heights are 36, 28, 24, 20, 16
            // Visual heights might be different
            float minWidth = size(sz, 38, 30, 26, 22, 18);
            float height = size(sz, 36, 30, 24, 20, 16);
            return BasicLayoutInfo.create(false, minWidth, true, height);
        }

        if (bw == BUTTON_BEVEL_ROUND || bw == BUTTON_GLASS) {
            // Layout parameters depend upon the button content

            float minWidth = size(sz, 38, 30, 26, 22, 18);
            float height = size(sz, 36, 30, 24, 20, 16);
            return BasicLayoutInfo.create(false, minWidth, false, height);
        }

        if (bw == BUTTON_TOOLBAR) {
            // This widget produces a glass style button, but with fixed heights.
            // Layout parameters depend upon the button content

            float minWidth = size(sz, 38, 32, 26, 22, 18);
            float height = size(sz, 38, 32, 26, 22, 18);
            return BasicLayoutInfo.create(false, minWidth, true, height);
        }

        if (bw == BUTTON_DISCLOSURE || bw == BUTTON_HELP) {
            return BasicLayoutInfo.createFixed(size(sz, 28, 24, 20, 16), size(sz, 28, 24, 20, 16));
        }

        if (bw == BUTTON_CHECK_BOX || bw == BUTTON_RADIO) {
            return BasicLayoutInfo.createFixed(size(sz, 18, 16, 14, 12), size(sz, 18, 16, 14, 12));
        }

        if (bw == BUTTON_TOOLBAR_ITEM) {
            return BasicLayoutInfo.create(false, 40, true, 56);
        }

        if (bw == BUTTON_RECESSED) {
            return BasicLayoutInfo.createFixedHeight(size(sz, 28, 24, 20, 16));
        }

        return super.getButtonLayoutInfo(g);
    }

    @Override
    public @Nullable Insetter getButtonLabelInsets(@NotNull ButtonLayoutConfiguration g)
    {
        AquaUIPainter.ButtonWidget w = g.getButtonWidget();
        AquaUIPainter.Size sz = g.getSize();

        if (w == BUTTON_PUSH || w == BUTTON_ROUNDED_RECT) {
            float side = size(sz, 8, 8, 6, 5, 4);
            float v = size2D(sz, 6, 2, 2, 1.5f, 1.5f);
            return createInsetter(g, v, v, side);
        }

        if (w == BUTTON_GLASS || w == BUTTON_BEVEL_ROUND) {
            float side = size(sz, 18, 14, 12, 10, 8);
            float v = size(sz, 11, 7, 5, 4, 2);
            return createInsetter(g, v, v, side);
        }

        if (w == BUTTON_TOOLBAR_ITEM) {
            return createInsetter(g, 0, 0, 6);
        }

        if (w == BUTTON_TOOLBAR) {
            int v = size(sz, 8, 6, 4, 3, 2);
            int s = size(sz, 8, 8, 7, 6, 6);
            return createInsetter(g, v, v, s);
        }

        if (g.getSize() == AquaUIPainter.Size.EXTRA_LARGE) {
            switch (g.getButtonWidget()) {
                case BUTTON_PUSH:
                case BUTTON_ROUNDED_RECT:
                    return createInsetter(g, 6, 6, 17);
            }
        }

        return super.getButtonLabelInsets(g);
    }

    @Override
    protected @NotNull LayoutInfo getSegmentedButtonLayoutInfo(@NotNull SegmentedButtonLayoutConfiguration g)
    {
        // Styles not supported by the NSView painter should use the layout information that corresponds to the CoreUI
        // painter.

        if (AquaNativeSegmentedControlPainter.isUnsupportedOn26(g)) {
            return super.getSegmentedButtonLayoutInfo(g);
        }
        AquaUIPainter.SegmentedButtonWidget bw = g.getWidget();
        AquaUIPainter.Size sz = g.getSize();
        float fixedHeight = size(sz, 36, 28, 24, 20, 16);
        float minimumWidth = size(sz, 50, 40, 30, 20, 20);
        if (bw == AquaUIPainter.SegmentedButtonWidget.BUTTON_SEGMENTED_SEPARATED) {
            fixedHeight = size(sz, 30, 24, 20, 16);
        }
        return BasicLayoutInfo.create(false, minimumWidth, true, fixedHeight);
    }

    @Override
    public @NotNull Insetter getSegmentedButtonLabelInsets(@NotNull SegmentedButtonLayoutConfiguration g)
    {
        AquaUIPainter.SegmentedButtonWidget bw = g.getWidget();
        if (bw.isSlider()) {
            LayoutInfo layoutInfo = getLayoutInfo(g);
            AquaUIPainter.Size sz = g.getSize();
            float top = 1;
            float bottom = 1;
            float side = size2D(sz, 10, 4, 3, 2);
            return Insetters.createFixed(top, side, bottom, side, layoutInfo);
        }

        return super.getSegmentedButtonLabelInsets(g);
    }

    @Override
    protected @NotNull LayoutInfo getPopupButtonLayoutInfo(@NotNull PopupButtonLayoutConfiguration g)
    {
        AquaUIPainter.PopupButtonWidget bw = g.getPopupButtonWidget();
        AquaUIPainter.Size sz = g.getSize();

        switch (bw) {
            case BUTTON_POP_UP:
            case BUTTON_POP_DOWN:
            case BUTTON_POP_UP_BEVEL:
            case BUTTON_POP_DOWN_BEVEL:
            case BUTTON_POP_UP_ROUND_RECT:
            case BUTTON_POP_DOWN_ROUND_RECT:
            case BUTTON_POP_UP_RECESSED:
            case BUTTON_POP_DOWN_RECESSED:
            case BUTTON_POP_UP_TEXTURED:
            case BUTTON_POP_DOWN_TEXTURED:
            case BUTTON_POP_UP_TEXTURED_TOOLBAR:
            case BUTTON_POP_DOWN_TEXTURED_TOOLBAR:
            {
                float fixedHeight = size(sz, 36, 28, 24, 20, 16);
                float minWidth = size(sz, 54, 44, 34, 26, 24);
                return BasicLayoutInfo.create(false, minWidth, true, fixedHeight);
            }
            case BUTTON_POP_UP_GRADIENT:
            case BUTTON_POP_DOWN_GRADIENT:
            case BUTTON_POP_UP_SQUARE:
            case BUTTON_POP_DOWN_SQUARE:
                // I have never seen these used. Control size has no effect in IB.
                return BasicLayoutInfo.create(false, 34, true, 23);
            case BUTTON_POP_UP_CELL:
            case BUTTON_POP_DOWN_CELL:
                return BasicLayoutInfo.create(false, 24, true, 12);
        }

        throw new UnsupportedOperationException("Unrecognized pop up button widget");
    }

    @Override
    public @NotNull Insetter getPopupButtonContentInsets(@NotNull PopupButtonLayoutConfiguration g)
    {
        AquaUIPainter.PopupButtonWidget bw = g.getPopupButtonWidget();
        AquaUIPainter.Size sz = g.getSize();

        float v;
        float far;
        float near;

        switch (bw) {
            case BUTTON_POP_UP:
            case BUTTON_POP_DOWN:
            case BUTTON_POP_UP_BEVEL:
            case BUTTON_POP_DOWN_BEVEL:
            case BUTTON_POP_UP_ROUND_RECT:
            case BUTTON_POP_DOWN_ROUND_RECT:
            case BUTTON_POP_UP_TEXTURED:
            case BUTTON_POP_DOWN_TEXTURED:
            case BUTTON_POP_UP_TEXTURED_TOOLBAR:
            case BUTTON_POP_DOWN_TEXTURED_TOOLBAR:
                v = size(sz, 2, 2, 1, 1, 1);
                far = size2D(sz, 10, 10, 5, 4, 3);
                near = bw.isPopUp() ? size2D(sz, 28, 26, 22, 18, 16) : size2D(sz, 30, 28, 24, 20, 18);
                break;

            case BUTTON_POP_UP_RECESSED:
            case BUTTON_POP_DOWN_RECESSED:
                v = size(sz, 2, 2, 1, 1, 1);
                far = size2D(sz, 14, 14, 12, 10, 8);
                near = bw.isPopUp() ? size2D(sz, 35, 35, 33, 27, 24) : size2D(sz, 33, 33, 31, 25, 22);
                break;

            case BUTTON_POP_UP_GRADIENT:
            case BUTTON_POP_DOWN_GRADIENT:
            case BUTTON_POP_UP_SQUARE:
            case BUTTON_POP_DOWN_SQUARE:
            case BUTTON_POP_UP_CELL:
            case BUTTON_POP_DOWN_CELL:
                v = 1;
                far = 3;
                near = 12;
                break;

            default:
                throw new UnsupportedOperationException("Unrecognized pop up button widget");
        }
        return createInsetter(g, v, v, far, near);
    }

    @Override
    protected @NotNull LayoutInfo getComboBoxLayoutInfo(@NotNull ComboBoxLayoutConfiguration g)
    {
        AquaUIPainter.ComboBoxWidget bw = g.getWidget();
        AquaUIPainter.Size sz = g.getSize();

        if (bw == BUTTON_COMBO_BOX_CELL) {
            return BasicLayoutInfo.createMinimumHeight(size(sz, 30, 24, 24, 20, 16));
        } else {
            float fixedHeight = size(sz, 38, 30, 26, 22, 18);
            float minWidth = size(sz, 52, 40, 40, 32, 26);
            return BasicLayoutInfo.create(false, minWidth, true, fixedHeight);
        }
    }

    @Override
    public @NotNull Insetter getComboBoxEditorInsets(@NotNull ComboBoxLayoutConfiguration g)
    {
        AquaUIPainter.ComboBoxWidget bw = g.getWidget();
        AquaUIPainter.Size sz = g.getSize();

        if (bw == BUTTON_COMBO_BOX_CELL) {
            int w = getComboBoxIndicatorWidth(g);
            return g.isLeftToRight() ? Insetters.createFixed(0, 0, 0, w) : Insetters.createFixed(0, w, 0, 0);
        }

        float near = size2D(sz, 39, 31, 31, 25, 21);
        float far = 7;
        float v = size2D(sz, 2, 2, 2, 2, 1);
        return createInsetter(g, v, v, far, near);
    }

    @Override
    public @NotNull Insetter getComboBoxIndicatorInsets(@NotNull ComboBoxLayoutConfiguration g)
    {
        int indicatorWidth = getComboBoxIndicatorWidth(g);
        return g.isLeftToRight()
          ? Insetters.createRightAligned(indicatorWidth, 0, 0, 0)
          : Insetters.createLeftAligned(indicatorWidth, 0, 0, 0);
    }

    protected int getComboBoxIndicatorWidth(@NotNull ComboBoxLayoutConfiguration g)
    {
        return size(g.getSize(), 39, 31, 31, 25, 21);
    }

    @Override
    protected @NotNull LayoutInfo getTextFieldLayoutInfo(@NotNull TextFieldLayoutConfiguration g)
    {
        AquaUIPainter.TextFieldWidget w = g.getWidget();
        AquaUIPainter.Size sz = g.getSize();

        float heights = size(sz, 36, 28, 26, 22, 18);
        float minimumWidth = size(g.getSize(), 82, 74, 68, 58, 50);

        if (w.isSearch()) {
            return BasicLayoutInfo.create(false, minimumWidth, true, heights);
        } else {
            return BasicLayoutInfo.create(false, minimumWidth, false, heights);
        }
    }

    @Override
    public @NotNull Insetter getTextFieldTextInsets(@NotNull TextFieldLayoutConfiguration g)
    {
        AquaUIPainter.TextFieldWidget w = g.getWidget();
        if (w.isSearch()) {
            return getSearchFieldTextInsets(g);
        }

        float top = 1;
        float bottom = 1;
        float side = 3.5f;
        return createInsetter(g, top, bottom, side);
    }

    protected @NotNull Insetter getSearchFieldTextInsets(@NotNull TextFieldLayoutConfiguration g)
    {
        AquaUIPainter.TextFieldWidget tw = g.getWidget();
        AquaUIPainter.Size sz = g.getSize();

        float top;
        float left;
        float right;
        float bottom;

        int d = 0;
        float gap = size(sz, 12, 10, 8, 5, 5);

        if (tw.hasMenu()) {
            d = 4;
            gap = size(sz, 12, 10, 8, 6, 4);
        }

        float near = d + size(sz, 36, 31, 28, 22, 18);
        float far = size(sz, 36, 28, 22, 19, 16);
        Insetter insets = getSearchButtonPaintingInsets(g);

        if (g.isLeftToRight()) {
            if (insets != null) {
                Rectangle2D bounds = insets.apply2D(100, 100);
                left = (float) (bounds.getX() + bounds.getWidth() + gap);
            } else {
                left = near;
            }
            right = far;
        } else {
            if (insets != null) {
                Rectangle2D bounds = insets.apply2D(100, 100);
                right = (float) (100 - bounds.getX() + gap);
            } else {
                right = near;
            }
            left = far;
        }

        if (tw.isToolbar()) {
            top = size2D(sz, 3, 1.5, 1.5);
            bottom = size2D(sz, 3, 2, 1.5);
        } else {
            top = size2D(sz, 3, 1.5, 1.5);
            bottom = size2D(sz, 3, 1.5, 1.5);
        }

        return createInsetter(g, top, bottom, left, right);
    }

    @Override
    protected @NotNull LayoutInfo getScrollBarLayoutInfo(@NotNull ScrollBarLayoutConfiguration g)
    {
        AquaUIPainter.ScrollBarWidget bw = g.getWidget();
        AquaUIPainter.Size sz = g.getSize();
        AquaUIPainter.Orientation o = g.getOrientation();

        if (bw == AquaUIPainter.ScrollBarWidget.LEGACY_SIDEBAR || bw == AquaUIPainter.ScrollBarWidget.LEGACY) {
            int d;
            if (sz == AquaUIPainter.Size.SMALL || sz == AquaUIPainter.Size.MINI) {
                d = 7;
            } else {
                d = 11;
            }
            return o == AquaUIPainter.Orientation.VERTICAL ? BasicLayoutInfo.createFixedWidth(d) : BasicLayoutInfo.createFixedHeight(d);
        }

        return super.getScrollBarLayoutInfo(g);
    }

    @Override
    protected @NotNull LayoutInfo getSpinnerArrowsLayoutInfo(@NotNull SpinnerArrowsLayoutConfiguration g)
    {
        AquaUIPainter.Size sz = g.getSize();
        int width = size(sz, 30, 23, 20, 17, 13);
        int height = size(sz, 38, 30, 26, 22, 20);
        return BasicLayoutInfo.createFixed(width, height);
    }

    @Override
    protected @NotNull LayoutInfo getSliderLayoutInfo(@NotNull SliderLayoutConfiguration g)
    {
        if (!g.isLinear()) {
            AquaUIPainter.Size sz = g.getSize();
            int d = size(sz, 36, 28, 24, 20, 16);
            if (g.hasTickMarks()) {
                d += 4;
            }
            return BasicLayoutInfo.createFixed(d, d);
        }

        return super.getSliderLayoutInfo(g);
    }

    @Override
    protected @NotNull LayoutInfo getTitleBarLayoutInfo(@NotNull TitleBarLayoutConfiguration g)
    {
        switch (g.getWidget())
        {
            case DOCUMENT_WINDOW:
                return BasicLayoutInfo.createFixedHeight(32);
            case UTILITY_WINDOW:
                return BasicLayoutInfo.createFixedHeight(20);
            default:
                throw new UnsupportedOperationException();
        }
    }
}
