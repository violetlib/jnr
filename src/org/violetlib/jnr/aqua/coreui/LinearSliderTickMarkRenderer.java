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
import org.violetlib.jnr.impl.SliderTickMarkRendererFactory;

import org.jetbrains.annotations.*;

/**
  A renderer that draws tick marks for a linear slider.
*/

public class LinearSliderTickMarkRenderer
  extends Renderer
{
    protected final @NotNull SliderConfiguration g;
    protected final @NotNull Renderer tintedRenderer;
    protected final @NotNull Renderer untintedRenderer;

    /**
      Create a renderer that draws tick marks for a linear slider.
      @param g The slider configuration.
      @param tickRendererFactory A factory that creates renderers used to draw an individual tick mark.
    */
    public LinearSliderTickMarkRenderer(@NotNull SliderConfiguration g,
                                        @NotNull SliderTickMarkRendererFactory tickRendererFactory)
    {
        this.g = g;
        this.tintedRenderer = tickRendererFactory.getSliderTickMarkRenderer(g, true);
        this.untintedRenderer = tickRendererFactory.getSliderTickMarkRenderer(g, false);
    }

    @Override
    public void composeTo(@NotNull ReusableCompositor compositor)
    {
        int tickCount = g.getNumberOfTickMarks();
        if (tickCount < 2) {
            return;
        }

        // The bounds of the compositor should define the minimum region in which ticks should be drawn.
        //
        // For a horizontal slider, the region height should match the tick mark height and the region width should
        // match the track width.
        //
        // For a vertical slider, the region width should match the tick mark width and the region height should
        // match the track height.

        float width = compositor.getWidth();
        float height = compositor.getHeight();
        AquaUIPainter.Size sz = g.getSize();
        boolean isHorizontal = g.isHorizontal();
        boolean isRTL = g.getWidget() == AquaUIPainter.SliderWidget.SLIDER_HORIZONTAL_RIGHT_TO_LEFT;
        double value = g.getValue();

        double thickness = JNRUtils.size(sz, 2, 2, 1);
        double length = JNRUtils.size(sz, 8, 8, 7);

        if (isHorizontal) {
            width -= thickness;
            double x = 0;
            double spacing = width / (tickCount - 1);
            for (int i = 0; i < tickCount; i++) {
                double v = isRTL ? 1 - (x / width) : x / width;
                boolean isTinted = v <= value;
                drawTick(compositor, isTinted, x, 0, thickness, length);
                x += spacing;
            }
        } else {
            height -= thickness;
            double y = 0;
            double spacing = height / (tickCount - 1);
            for (int i = 0; i < tickCount; i++) {
                double v = 1 - (y / height);
                boolean isTinted = v <= value;
                drawTick(compositor, isTinted, 0, y, length, thickness);
                y += spacing;
            }
        }
    }

    private void drawTick(@NotNull ReusableCompositor compositor, boolean isTinted,
                          double x, double y, double w, double h)
    {
        Renderer tr = isTinted ? tintedRenderer : untintedRenderer;
        Rectangle2D tickBounds = new Rectangle2D.Double(x, y, w, h);
        Renderer r = Renderer.createOffsetRenderer(tr, tickBounds);
        r.composeTo(compositor);
    }
}
