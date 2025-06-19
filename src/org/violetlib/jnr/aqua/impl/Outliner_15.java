/*
 * Copyright (c) 2025 Alan Snyder.
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
import org.violetlib.jnr.aqua.SpinnerArrowsLayoutConfiguration;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

import static org.violetlib.jnr.aqua.AquaUIPainter.ButtonWidget.*;

/**

*/

public class Outliner_15
  extends Outliner_11
{
    public Outliner_15(@NotNull LayoutInfo_15 uiLayout)
    {
        super(uiLayout);
    }

    @Override
    protected @Nullable Shape getButtonOutline(@NotNull Rectangle2D bounds, @NotNull ButtonLayoutConfiguration g)
    {
        AquaUIPainter.ButtonWidget bw = g.getButtonWidget();
        AquaUIPainter.Size sz = g.getSize();
        AquaUIPainter.ButtonState bs = getButtonState(g);

        if (bw == BUTTON_ROUND || bw == BUTTON_HELP || bw == BUTTON_ROUND_TEXTURED_TOOLBAR) {
            double x = bounds.getX();
            double y = bounds.getY();
            double width = bounds.getWidth();
            double height = width;  // layout height is larger because of shadows
            return new Ellipse2D.Double(x, y, width, height);
        }
        if (bw == BUTTON_DISCLOSURE) {
            double x = bounds.getX();
            double y = bounds.getY() + size2D(sz, 0.49f, 0, 0, 0);
            double width = bounds.getWidth();
            double height = width + size2D(sz, 0, 1, 0, 0);
            double corner = size2D(sz, 10, 10, 8, 6);
            return new RoundRectangle2D.Double(x, y, width, height, corner, corner);
        }

        return super.getButtonOutline(bounds, g);
    }

    @Override
    protected @Nullable Shape getSpinnerArrowsOutline(@NotNull Rectangle2D bounds, @NotNull SpinnerArrowsLayoutConfiguration g)
    {
        AquaUIPainter.Size sz = g.getSize();
        double x = bounds.getX() + size2D(sz, 0, 0, -0.5f);
        double y = bounds.getY() + size2D(sz, 1, 0, 0);
        double w = bounds.getWidth() + size2D(sz, 0, 0, 1);
        double h = bounds.getHeight() + size2D(sz, -2, -2, -1);
        double corner = 8;
        return new RoundRectangle2D.Double(x, y, w, h, corner, corner);
    }
}
