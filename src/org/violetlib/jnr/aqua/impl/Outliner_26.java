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
import org.violetlib.jnr.aqua.AquaUIPainter;
import org.violetlib.jnr.aqua.ButtonLayoutConfiguration;
import org.violetlib.jnr.aqua.PopupButtonLayoutConfiguration;
import org.violetlib.jnr.aqua.TextFieldLayoutConfiguration;
import org.violetlib.jnr.impl.JNRUtils;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

import static org.violetlib.jnr.aqua.AquaUIPainter.ButtonWidget.*;

/**

*/

public class Outliner_26
  extends Outliner_15
{
    public Outliner_26(@NotNull LayoutInfo_26 uiLayout)
    {
        super(uiLayout);
    }

    private static final float GLASS_DELTA = 0.75f;

    @Override
    protected @Nullable Shape getButtonOutline(@NotNull Rectangle2D bounds, @NotNull ButtonLayoutConfiguration g)
    {
        if (g.getButtonWidget() == BUTTON_TOOLBAR) {
            return getToolbarButtonOutline(bounds, g.getSize(), GLASS_DELTA);
        }

        if (isLikeGlass(g)) {
            return getGlassButtonOutline(bounds, g.getSize(), GLASS_DELTA);
        }
        if (g.getButtonWidget() == BUTTON_COLOR_WELL) {
            return getColorWellOutline(bounds, g.getSize());
        }
        return super.getButtonOutline(bounds, g);
    }

    protected @Nullable Shape getGlassButtonOutline(@NotNull Rectangle2D bounds,
                                                    @NotNull AquaUIPainter.Size size,
                                                    float d)
    {
        float arc = getGlassCornerRadius(size);
        return getRoundedRectangle(bounds, arc, d);
    }

    protected float getGlassCornerRadius(AquaUIPainter.Size sz)
    {
        return JNRUtils.size2D(sz, 36, 30, 24, 20, 16);
    }

    protected @Nullable Shape getToolbarButtonOutline(@NotNull Rectangle2D bounds,
                                                      @NotNull AquaUIPainter.Size size,
                                                      float d)
    {
        float arc = getToolbarCornerRadius(size);
        return getRoundedRectangle(bounds, arc, d);
    }

    protected float getToolbarCornerRadius(AquaUIPainter.Size sz)
    {
        return JNRUtils.size2D(sz, 38, 32, 26, 22, 18);
    }

    protected @Nullable Shape getColorWellOutline(@NotNull Rectangle2D bounds, @NotNull AquaUIPainter.Size size)
    {
        float arc = getColorWellCornerRadius(size);
        return getRoundedRectangle(bounds, arc, 0);
    }

    protected float getColorWellCornerRadius(AquaUIPainter.Size sz)
    {
        return 32;
    }

    protected boolean isLikeGlass(@NotNull ButtonLayoutConfiguration g)
    {
        AquaUIPainter.ButtonWidget bw = g.getButtonWidget();
        if (bw == BUTTON_GLASS || bw == BUTTON_TOOLBAR) {
            return true;
        }
        if (bw == BUTTON_PUSH || bw == BUTTON_ROUNDED_RECT || bw == BUTTON_BEVEL_ROUND) {
            AquaUIPainter.Size sz = g.getSize();
            return sz == AquaUIPainter.Size.LARGE || sz == AquaUIPainter.Size.EXTRA_LARGE;
        }
        return false;
    }

    @Override
    protected @Nullable Shape getPopUpButtonOutline(@NotNull Rectangle2D bounds,
                                                    @NotNull PopupButtonLayoutConfiguration g)
    {
        AquaUIPainter.PopupButtonWidget w = g.getPopupButtonWidget();
        AquaUIPainter.ButtonWidget bw = w.getEquivalentButtonWidget();
        if (bw == BUTTON_PUSH) {
            return getGlassButtonOutline(bounds, g.getSize(), GLASS_DELTA);
        }
        return null;
    }

    @Override
    protected @Nullable Shape getTextFieldOutline(@NotNull Rectangle2D bounds, @NotNull TextFieldLayoutConfiguration g)
    {
        double x = bounds.getX();
        double y = bounds.getY();
        double width = bounds.getWidth();
        double height = bounds.getHeight();

        AquaUIPainter.TextFieldWidget w = g.getWidget();
        AquaUIPainter.Size sz = g.getSize();
        double corner;
        if (w.isSearch()) {
            corner = JNRUtils.size2D(sz, 32, 24, 20, 16, 12);
        } else {
            corner = JNRUtils.size2D(sz, 16, 11, 10, 10, 10);
        }
        return new RoundRectangle2D.Double(x, y, width, height, corner, corner);
    }
}
