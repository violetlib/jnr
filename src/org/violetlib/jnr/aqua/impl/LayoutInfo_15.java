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
import static org.violetlib.jnr.aqua.AquaUIPainter.PopupButtonWidget.*;
import static org.violetlib.jnr.aqua.AquaUIPainter.ScrollBarWidget.LEGACY_SIDEBAR;
import static org.violetlib.jnr.aqua.AquaUIPainter.SegmentedButtonWidget.BUTTON_SEGMENTED_TOOLBAR;
import static org.violetlib.jnr.impl.JNRUtils.size;
import static org.violetlib.jnr.impl.JNRUtils.size2D;

/**
  Layout info for macOS 15 widgets.
*/

public class LayoutInfo_15
  extends LayoutInfo_11
{
    @Override
    protected @NotNull LayoutInfo getButtonLayoutInfo(@NotNull ButtonLayoutConfiguration g)
    {
        AquaUIPainter.ButtonWidget bw = g.getButtonWidget();
        if (bw == BUTTON_RECESSED) {
            AquaUIPainter.Size sz = g.getSize();
            return BasicLayoutInfo.createFixedHeight(size(sz, 28, 18, 16, 14));
        }
        return super.getButtonLayoutInfo(g);
    }

    @Override
    public @Nullable Insetter getButtonLabelInsets(@NotNull ButtonLayoutConfiguration g)
    {
        AquaUIPainter.ButtonWidget bw = g.getButtonWidget();
        if (bw == BUTTON_TOOLBAR_ITEM) {
            return createInsetter(g, 4, 1, 4, 4);
        }
        return super.getButtonLabelInsets(g);
    }

    @Override
    protected @NotNull LayoutInfo getSegmentedButtonLayoutInfo(@NotNull SegmentedButtonLayoutConfiguration g)
    {
        AquaUIPainter.SegmentedButtonWidget bw = g.getWidget();
        AquaUIPainter.Size sz = g.getSize();

        switch (bw) {
            case BUTTON_TAB:
            case BUTTON_SEGMENTED:
            case BUTTON_SEGMENTED_SLIDER:
            case BUTTON_SEGMENTED_SEPARATED:
            case BUTTON_SEGMENTED_SLIDER_TOOLBAR:
                return BasicLayoutInfo.createFixedHeight(size(sz, 30, 22, 18, 15));

            case BUTTON_SEGMENTED_INSET:
                return BasicLayoutInfo.createFixedHeight(size(sz, 18, 16, 14));

            case BUTTON_SEGMENTED_SMALL_SQUARE:
                return BasicLayoutInfo.createFixedHeight(size(sz, 21, 21, 19, 17));

            case BUTTON_SEGMENTED_TEXTURED_TOOLBAR:
            case BUTTON_SEGMENTED_TEXTURED_SEPARATED_TOOLBAR:
            case BUTTON_SEGMENTED_SCURVE:
                return BasicLayoutInfo.createFixedHeight(size(sz, 28, 22, 18, 15));

            case BUTTON_SEGMENTED_TEXTURED:
                return BasicLayoutInfo.createFixedHeight(size(sz, 28, 20, 16, 13));

            case BUTTON_SEGMENTED_TEXTURED_SEPARATED:
                return BasicLayoutInfo.createFixedHeight(size(sz, 20, 20, 16, 13));

            case BUTTON_SEGMENTED_TOOLBAR:
                if (g.getPosition() == AquaUIPainter.Position.ONLY) {
                    // This is probably a bug in AppKit
                    return BasicLayoutInfo.createFixedHeight(size(sz, 20, 20, 16, 13));
                }
                return BasicLayoutInfo.createFixedHeight(size(sz, 30, 22, 18, 15));

            default:
                throw new UnsupportedOperationException();
        }
    }

    public @NotNull Insetter getSegmentedButtonLabelInsets(@NotNull SegmentedButtonLayoutConfiguration g)
    {
        AquaUIPainter.SegmentedButtonWidget bw = g.getWidget();
        if (bw == BUTTON_SEGMENTED_TOOLBAR) {
            AquaUIPainter.Position pos = g.getPosition();
            LayoutInfo layoutInfo = getLayoutInfo(g);
            AquaUIPainter.Size sz = g.getSize();
            float v = size2D(sz, 2.5, 2, 1.5, 1);
            // need an inset on each side if the segment is selected (and that is not known here)
            float left = pos == AquaUIPainter.Position.FIRST || pos == AquaUIPainter.Position.ONLY ? 3 : 2;
            float right = 3;
            return Insetters.createFixed(v, left, v, right, layoutInfo);
        } else if (bw.isTextured() && bw.isToolbar()) {
            LayoutInfo layoutInfo = getLayoutInfo(g);
            AquaUIPainter.Position pos = g.getPosition();
            boolean isLeftEnd = pos == AquaUIPainter.Position.FIRST || pos == AquaUIPainter.Position.ONLY;
            boolean isRightEnd = pos == AquaUIPainter.Position.LAST || pos == AquaUIPainter.Position.ONLY;
            float top = 1;
            float bottom = 1;
            float left = bw.isSeparated() ? 3 : 1;
            float right = left;
            float endAdjust = bw.isSeparated() ? 0 : 2;
            if (isLeftEnd) {
                left += endAdjust;
            }
            if (isRightEnd) {
                right += endAdjust;
            }
            return Insetters.createFixed(top, left, bottom, right, layoutInfo);
        }

        return super.getSegmentedButtonLabelInsets(g);
    }

    @Override
    protected @NotNull LayoutInfo getPopupButtonLayoutInfo(@NotNull PopupButtonLayoutConfiguration g)
    {
        AquaUIPainter.PopupButtonWidget bw = g.getPopupButtonWidget();
        AquaUIPainter.Size sz = g.getSize();

        if (bw.isTextured()) {
            float fixedHeight = size(sz, 28, 22, 18, 15);
            float minWidth = size(sz, 39, 32, 25);
            return BasicLayoutInfo.create(false, minWidth, true, fixedHeight);
        }

        return super.getPopupButtonLayoutInfo(g);
    }

    @Override
    public @NotNull Insetter getPopupButtonContentInsets(@NotNull PopupButtonLayoutConfiguration g)
    {
        AquaUIPainter.PopupButtonWidget bw = g.getPopupButtonWidget();
        if (bw.isTextured()) {
            AquaUIPainter.Size sz = g.getSize();
            float v = size(sz, 2, 3, 2, 2);
            float far = size2D(sz, 6, 5, 4, 3);
            float near = size2D(sz, 25, 18, 16, 14);
            return createInsetter(g, v, v, far, near);
        } else if (bw == BUTTON_POP_UP_CELL) {
            AquaUIPainter.Size sz = g.getSize();
            float top = size2D(sz, 3, 3, 3.5);
            float bottom = size2D(sz, 2.5, 2.5, 2);
            float far = 3;
            float near = size2D(sz, 17.5, 15, 17);
            return createInsetter(g, top, bottom, far, near);
        } else if (bw == BUTTON_POP_DOWN_CELL) {
            AquaUIPainter.Size sz = g.getSize();
            float top = 3;
            float bottom = 3;
            float far = 3;
            float near = size2D(sz, 18.5, 15.5, 18.5);
            return createInsetter(g, top, bottom, far, near);
        } else if (bw == BUTTON_POP_UP_BEVEL || bw == BUTTON_POP_DOWN_BEVEL) {
            float top = 1;
            float bottom = 2;
            float far = 3;
            float near = 18;
            return createInsetter(g, top, bottom, far, near);
        }

        return super.getPopupButtonContentInsets(g);
    }

    @Override
    protected @NotNull LayoutInfo getComboBoxLayoutInfo(@NotNull ComboBoxLayoutConfiguration g)
    {
        AquaUIPainter.ComboBoxWidget bw = g.getWidget();
        AquaUIPainter.Size sz = g.getSize();

        if (bw == BUTTON_COMBO_BOX_CELL) {
            return BasicLayoutInfo.createMinimumHeight(size(sz, 14, 11, 11));

        } else if (bw.isTextured()) {
            float fixedHeight = size(sz, 28, 22, 18, 15);
            float minWidth = size(sz, 38, 31, 23, 20);
            return BasicLayoutInfo.create(false, minWidth, true, fixedHeight);
        } else {
            float fixedHeight = size(sz, 22, 19, 15);
            float minWidth = size(sz, 34, 27, 24);
            return BasicLayoutInfo.create(false, minWidth, true, fixedHeight);
        }
    }

    @Override
    public @NotNull Insetter getComboBoxEditorInsets(@NotNull ComboBoxLayoutConfiguration g)
    {
        AquaUIPainter.ComboBoxWidget bw = g.getWidget();
        AquaUIPainter.Size sz = g.getSize();

        if (bw == BUTTON_COMBO_BOX_CELL) {
            float inset = size(sz, 18, 18, 13);
            return g.isLeftToRight() ? Insetters.createFixed(0, 0, 0, inset) : Insetters.createFixed(0, inset, 0, 0);
        }

        float near;
        float far;
        float top;
        float bottom;

        if (bw.isTextured()) {
            near = size(sz, 27, 21, 17, 14);
            far = size(sz, 5, 4, 3, 2);
            top = 1;
            bottom = 1;
        } else {
            near = size(sz, 21, 21, 17, 14);
            far = size(sz, 5, 5, 4, 3);
            top = 1;
            bottom = 1;
        }

        return createInsetter(g, top, bottom, far, near);
    }

//    @Override
//    protected @NotNull LayoutInfo getTextFieldLayoutInfo(@NotNull TextFieldLayoutConfiguration g)
//    {
//        AquaUIPainter.TextFieldWidget w = g.getWidget();
//        if (w.isSearch()) {
//            AquaUIPainter.Size sz = g.getSize();
//            int minimumWidth = w.hasMenu() ? size(sz, 28, 26, 26) : size(sz, 24, 22, 20);
//            int fixedHeight = size(sz, 28, 21, 17, 12);
//            return BasicLayoutInfo.create(false, minimumWidth, true, fixedHeight);
//        }
//
//        return super.getTextFieldLayoutInfo(g);
//    }

    @Override
    public @NotNull Insetter getTextFieldTextInsets(@NotNull TextFieldLayoutConfiguration g)
    {
        AquaUIPainter.TextFieldWidget tw = g.getWidget();
        AquaUIPainter.Size sz = g.getSize();
        float left = 2;
        float right = 2;
        float top;
        float bottom;

        if (tw == AquaUIPainter.TextFieldWidget.TEXT_FIELD) {
            return createInsetter(g, 1, 1, 1, 1);
        }

        if (tw.isSearch()) {
            int d = 0;
            float gap = 6;
            if (tw.hasMenu()) {
                d = 4;
                gap = 3.5f;
            }
            if (g.isLeftToRight()) {
                Insetter insets = getSearchButtonPaintingInsets(g);
                if (insets != null) {
                    Rectangle2D bounds = insets.apply2D(100, 100);
                    left = (float) (bounds.getX() + bounds.getWidth() + gap);
                } else {
                    left = size(sz, 27+d, 26+d, 22+d);
                }
                right = size(sz, 24, 21, 18);
            } else {
                Insetter insets = getSearchButtonPaintingInsets(g);
                if (insets != null) {
                    Rectangle2D bounds = insets.apply2D(100, 100);
                    right = (float) (100 - bounds.getX() + gap);
                } else {
                    right = size(sz, 27+d, 26+d, 22+d);
                }
                left = size(sz, 24, 21, 18);
            }
        }

        if (tw == AquaUIPainter.TextFieldWidget.TEXT_FIELD_ROUND) {
            top = size2D(sz, 2, 2, 3, 4);
            bottom = size2D(sz, 2, 2, 1.5, 2);
        } else if (tw == AquaUIPainter.TextFieldWidget.TEXT_FIELD_ROUND_TOOLBAR) {
            top = size2D(sz, 2, 2, 2, 2);
            bottom = size2D(sz, 2, 2, 1.5, 2);
        } else {
            top = size2D(sz, 2, 2, 1.5, 1);
            bottom = size2D(sz, 2, 2, 1.5, 1.5);
        }

        return createInsetter(g, top, bottom, left, right);
    }

    protected double getScrollTrackEndInset(@NotNull ScrollBarLayoutConfiguration g)
    {
        AquaUIPainter.ScrollBarWidget bw = g.getWidget();

        if (bw == LEGACY_SIDEBAR) {
            return 0;
        }

        return super.getScrollTrackEndInset(g);
    }

    @Override
    protected @NotNull LayoutInfo getTitleBarLayoutInfo(@NotNull TitleBarLayoutConfiguration g)
    {
        // Not sure when these values changed
        switch (g.getWidget())
        {
            case DOCUMENT_WINDOW:
                return BasicLayoutInfo.createFixedHeight(28);
            case UTILITY_WINDOW:
                return BasicLayoutInfo.createFixedHeight(19);
            default:
                throw new UnsupportedOperationException();
        }
    }
}
