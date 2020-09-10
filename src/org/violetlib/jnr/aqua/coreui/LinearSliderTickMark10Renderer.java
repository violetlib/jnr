/*
 * Copyright (c) 2020 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.aqua.coreui;

import java.awt.geom.Rectangle2D;

import org.violetlib.jnr.aqua.AquaUIPainter;
import org.violetlib.jnr.aqua.SliderConfiguration;
import org.violetlib.jnr.impl.JNRUtils;
import org.violetlib.jnr.impl.Renderer;
import org.violetlib.jnr.impl.ReusableCompositor;

import org.jetbrains.annotations.*;

/**
  A renderer that draws tick marks for a linear slider.
*/

public class LinearSliderTickMark10Renderer  // for possible future use, not correct now
  extends Renderer
{
    protected final @NotNull SliderConfiguration g;
    protected final @NotNull Renderer tickRenderer;

    /**
      Create a renderer that draws tick marks for a linear slider.
      @param g The slider configuration.
      @param tickRenderer The renderer used to draw an individual tick mark.
    */
    public LinearSliderTickMark10Renderer(@NotNull SliderConfiguration g, @NotNull Renderer tickRenderer)
    {
        this.g = g;
        this.tickRenderer = tickRenderer;
    }

    @Override
    public void composeTo(@NotNull ReusableCompositor compositor)
    {
        int tickCount = g.getNumberOfTickMarks();
        if (tickCount < 2) {
            return;
        }

        float width = compositor.getWidth();
        float height = compositor.getHeight();
        AquaUIPainter.SliderWidget sw = g.getWidget();
        AquaUIPainter.Size sz = g.getSize();
        boolean isHorizontal = g.isHorizontal();

        double w = JNRUtils.size(sz, 2, 2, 1);
        double h = JNRUtils.size(sz, 4, 3, 3);

        if (isHorizontal) {
            width -= w;
            double y = (height - h) / 2;
            double x = 0;
            double spacing = width / (tickCount - 1);
            for (int i = 0; i < tickCount; i++) {
                drawTick(compositor, x, y, w, h);
                x += spacing;
            }
        } else {
            double temp = h;
            h = w;
            w = temp;
            height -= h;
            double x = (width - 2) / 2;
            double y = 0;
            double spacing = height / (tickCount - 1);
            for (int i = 0; i < tickCount; i++) {
                drawTick(compositor, x, y, w, h);
                y += spacing;
            }
        }
    }

    private void drawTick(@NotNull ReusableCompositor compositor, double x, double y, double w, double h)
    {
        Rectangle2D tickBounds = new Rectangle2D.Double(x, y, w, h);
        Renderer r = Renderer.createOffsetRenderer(tickRenderer, tickBounds);
        r.composeTo(compositor);
    }
}
