/*
 * Copyright (c) 2020 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.aqua.impl;

import org.violetlib.jnr.Insetter;
import org.violetlib.jnr.LayoutInfo;
import org.violetlib.jnr.aqua.AquaUIPainter;
import org.violetlib.jnr.aqua.AquaUIPainter.PopupButtonWidget;
import org.violetlib.jnr.aqua.ButtonLayoutConfiguration;
import org.violetlib.jnr.aqua.PopupButtonLayoutConfiguration;
import org.violetlib.jnr.aqua.TextFieldLayoutConfiguration;
import org.violetlib.jnr.aqua.ToolBarItemWellLayoutConfiguration;
import org.violetlib.jnr.impl.BasicLayoutInfo;
import org.violetlib.jnr.impl.Insetters;

import org.jetbrains.annotations.*;

import static org.violetlib.jnr.aqua.AquaUIPainter.PopupButtonWidget.*;
import static org.violetlib.jnr.impl.JNRUtils.*;

/**
  Layout information for macOS 11+ widgets.
*/

public class BigSurLayoutInfo
  extends ElCapitanLayoutInfo
{
    @Override
    protected @NotNull LayoutInfo getButtonLayoutInfo(@NotNull ButtonLayoutConfiguration g)
    {
        AquaUIPainter.ButtonWidget bw = g.getButtonWidget();

        if (bw == AquaUIPainter.ButtonWidget.BUTTON_TOOLBAR_ITEM) {
            ToolBarItemWellLayoutConfiguration tg = new ToolBarItemWellLayoutConfiguration();
            return getToolBarItemWellLayoutInfo(tg);
        }

        AquaUIPainter.Size sz = g.getSize();

        if (bw == AquaUIPainter.ButtonWidget.BUTTON_PUSH) {
            return BasicLayoutInfo.createFixedHeight(size(sz, 40, 32, 27, 16));

        } else if (bw == AquaUIPainter.ButtonWidget.BUTTON_BEVEL) {
            return BasicLayoutInfo.getInstance();

        } else if (bw == AquaUIPainter.ButtonWidget.BUTTON_BEVEL_ROUND) {
            return BasicLayoutInfo.getInstance();

        } else if (bw == AquaUIPainter.ButtonWidget.BUTTON_CHECK_BOX) {
            return BasicLayoutInfo.createFixed(size(sz, 16, 14, 12, 10), size(sz, 16, 14, 12, 10));

        } else if (bw == AquaUIPainter.ButtonWidget.BUTTON_RADIO) {
            return BasicLayoutInfo.createFixed(size(sz, 18, 16, 14, 10), size(sz, 18, 16, 14, 10));

        } else if (bw == AquaUIPainter.ButtonWidget.BUTTON_DISCLOSURE) {
            return BasicLayoutInfo.createFixed(size(sz, 30, 28, 28, 19), size(sz, 30, 26, 26, 16));

        } else if (bw == AquaUIPainter.ButtonWidget.BUTTON_HELP) {
            return BasicLayoutInfo.createFixed(size(sz, 35, 25, 18, 16), size(sz, 35, 25, 19, 17));

        } else if (bw == AquaUIPainter.ButtonWidget.BUTTON_GRADIENT) {
            return BasicLayoutInfo.getInstance();

        } else if (bw == AquaUIPainter.ButtonWidget.BUTTON_RECESSED) {
            return BasicLayoutInfo.createFixedHeight(size(sz, 19, 19, 17, 15));

        } else if (bw == AquaUIPainter.ButtonWidget.BUTTON_INLINE) {
            // Note that the NSView does not limit the size, but there seems to be an intended fixed size.
            return BasicLayoutInfo.createFixedHeight(16);

        } else if (bw == AquaUIPainter.ButtonWidget.BUTTON_ROUNDED_RECT) {
            return BasicLayoutInfo.createFixedHeight(size(sz, 19, 19, 17, 15));

        } else if (bw == AquaUIPainter.ButtonWidget.BUTTON_TEXTURED) {
            return BasicLayoutInfo.createFixedHeight(size(sz, 25, 23, 19, 16));

        } else if (bw == AquaUIPainter.ButtonWidget.BUTTON_TEXTURED_TOOLBAR) {
            return BasicLayoutInfo.createFixedHeight(size(sz, 25, 24, 19, 16));

        } else if (bw == AquaUIPainter.ButtonWidget.BUTTON_ROUND) {
            return BasicLayoutInfo.createFixed(size(sz, 34, 26, 22, 19), size(sz, 34, 26, 22, 19));

        } else if (bw == AquaUIPainter.ButtonWidget.BUTTON_ROUND_INSET) {
            return BasicLayoutInfo.createFixed(18, 18);

        } else if (bw == AquaUIPainter.ButtonWidget.BUTTON_ROUND_TEXTURED) {
            return BasicLayoutInfo.createFixed(size(sz, 21, 18, 15), size(sz, 22, 19, 16));

        } else if (bw == AquaUIPainter.ButtonWidget.BUTTON_ROUND_TOOLBAR) {
            return BasicLayoutInfo.createFixed(size(sz, 26, 23, 20, 17), size(sz, 26, 23, 20, 17));

        } else if (bw == AquaUIPainter.ButtonWidget.BUTTON_DISCLOSURE_TRIANGLE) {
            return BasicLayoutInfo.createFixed(13, 13);

        } else if (bw == AquaUIPainter.ButtonWidget.BUTTON_PUSH_INSET2) {
            return BasicLayoutInfo.createFixedHeight(size(sz, 19, 17, 15));

        } else if (bw == AquaUIPainter.ButtonWidget.BUTTON_COLOR_WELL) {
            return BasicLayoutInfo.createMinimum(44, 23);

        } else {
            return BasicLayoutInfo.getInstance();
        }
    }

    @Override
    public @NotNull
    Insetter getPopupButtonContentInsets(@NotNull PopupButtonLayoutConfiguration g)
    {
        PopupButtonWidget bw = g.getPopupButtonWidget();
        if (bw == BUTTON_POP_UP || bw == BUTTON_POP_DOWN) {
            AquaUIPainter.Size sz = g.getSize();
            float top = 1;
            float bottom = 1;
            float far = 3;
            float near = size2D(sz, 21, 17, 14);

            switch (bw) {
                case BUTTON_POP_UP:
                default:
                    bottom = size2D(sz, 2.5f, 2.5f, 2);
                    top = size2D(sz, 0.5f, 0.5f, 1);
                    break;
                case BUTTON_POP_DOWN:
                    bottom = 2;
                    break;
            }
            LayoutInfo layoutInfo = getLayoutInfo(g);
            return g.isLeftToRight()
                     ? Insetters.createFixed(top, far, bottom, near, layoutInfo)
                     : Insetters.createFixed(top, near, bottom, far, layoutInfo);
        }
        return super.getPopupButtonContentInsets(g);
    }

    @Override
    protected @NotNull LayoutInfo getTextFieldLayoutInfo(@NotNull TextFieldLayoutConfiguration g)
    {
        AquaUIPainter.TextFieldWidget w = g.getWidget();
        if (w.isRound() || w.isSearch()) {
            if (w.isToolbar()) {
                // The actual sizes for small and mini are bogus. We do not simulate this bug.
                return BasicLayoutInfo.createFixedHeight(size(g.getSize(), 30, 25, 20, 17));
            } else {
                return BasicLayoutInfo.createFixedHeight(size(g.getSize(), 30, 22, 19, 17));
            }
        }

        return BasicLayoutInfo.getInstance();
    }
}
