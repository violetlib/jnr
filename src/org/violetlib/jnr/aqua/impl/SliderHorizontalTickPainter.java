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

public class SliderHorizontalTickPainter
{
    protected final @NotNull Color color;
    protected final double w;
    protected final double h;
    protected final double x0;
    protected final double x1;
    protected final double y;
    protected final int tickCount;

    public SliderHorizontalTickPainter(@NotNull Color color, double w, double h, double x0, double x1, double y, int tickCount)
    {
        this.color = color;
        this.w = w;
        this.h = h;
        this.x0 = x0;
        this.x1 = x1;
        this.y = y;
        this.tickCount = tickCount;
    }

    public void paint(@NotNull Graphics2D g)
    {
        g.setColor(color);

        if (tickCount == 1) {
            drawTick(g, x0);
        } else {
            double spacing = (x1 - x0) / (tickCount - 1);
            double x = x0;
            for (int i = 0; i < tickCount; i++) {
                drawTick(g, x);
                x += spacing;
            }
        }
    }

    protected void drawTick(@NotNull Graphics2D g, double x)
    {
        // TBD: the tick marks should be tapered
        Shape s = new Rectangle2D.Double(x - w/2, y, w, h);
        g.fill(s);
    }
}
