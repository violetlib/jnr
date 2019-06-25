/*
 * Copyright (c) 2015 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.aqua.impl;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;

import org.jetbrains.annotations.*;

/**
  Paint tick marks for a horizontal slider.
*/

public class SliderVerticalTickPainter
{
    protected final @NotNull Color color;
    protected final double w;
    protected final double h;
    protected final double x;
    protected final double y0;
    protected final double y1;
    protected final int tickCount;

    public SliderVerticalTickPainter(@NotNull Color color, double w, double h, double x,
                                     double y0, double y1, int tickCount)
    {
        this.color = color;
        this.w = w;
        this.h = h;
        this.x = x;
        this.y0 = y0;
        this.y1 = y1;
        this.tickCount = tickCount;
    }

    public void paint(@NotNull Graphics2D g)
    {
        g.setColor(color);

        if (tickCount == 1) {
            drawTick(g, y0);
        } else {
            double spacing = (y1 - y0) / (tickCount - 1);
            double y = y0;
            for (int i = 0; i < tickCount; i++) {
                drawTick(g, y);
                y += spacing;
            }
        }
    }

    protected void drawTick(@NotNull Graphics2D g, double y)
    {
        // TBD: the tick marks should be tapered
        Shape s = new Rectangle2D.Double(x, y - w/2, h, w);
        g.fill(s);
    }
}
