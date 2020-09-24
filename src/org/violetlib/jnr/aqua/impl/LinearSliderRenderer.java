/*
 * Copyright (c) 2015-2020 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.aqua.impl;

import java.awt.geom.Rectangle2D;

import org.violetlib.jnr.Insetter;
import org.violetlib.jnr.aqua.SliderConfiguration;
import org.violetlib.jnr.impl.JNRUtils;
import org.violetlib.jnr.impl.Renderer;
import org.violetlib.jnr.impl.ReusableCompositor;

import org.jetbrains.annotations.*;

import static org.violetlib.jnr.impl.ImageUtils.*;

/**
  A renderer for linear sliders using CoreUI based renderers for the track, thumb, and optionally tick marks. It
  repositions the track and thumb to match what NSSlider paints (more or less).
*/

public class LinearSliderRenderer
  extends Renderer
{
    private static final @NotNull ReusableCompositor.PixelOperator blender = new Blender();

    protected final @NotNull SliderConfiguration g;
    protected final @NotNull Renderer trackRenderer;
    protected final @NotNull Insetter trackInsets;
    protected final @Nullable Renderer tickMarkRenderer;
    protected final @Nullable Insetter tickMarkInsets;
    protected final @NotNull Renderer thumbRenderer;
    protected final @NotNull Insetter thumbInsets;
    protected final boolean isThumbTranslucent;
    protected final @Nullable ReusableCompositor.PixelOperator tickOperator;

    public LinearSliderRenderer(@NotNull SliderConfiguration g,
                                @NotNull Renderer trackRenderer,
                                @NotNull Insetter trackInsets,
                                @Nullable Renderer tickMarkRenderer,
                                @Nullable Insetter tickMarkInsets,
                                @NotNull Renderer thumbRenderer,
                                @NotNull Insetter thumbInsets,
                                boolean isThumbTranslucent,
                                @Nullable ReusableCompositor.PixelOperator tickOperator)
    {
        this.g = g;
        this.trackRenderer = trackRenderer;
        this.trackInsets = trackInsets;
        this.tickMarkRenderer = tickMarkRenderer;
        this.tickMarkInsets = tickMarkInsets;
        this.thumbRenderer = thumbRenderer;
        this.thumbInsets = thumbInsets;
        this.isThumbTranslucent = isThumbTranslucent;
        this.tickOperator = tickOperator;
    }

    @Override
    public void composeTo(@NotNull ReusableCompositor compositor)
    {
        float w = compositor.getWidth();
        float h = compositor.getHeight();
        Rectangle2D trackBounds = trackInsets.apply2D(w, h);

        {
            Renderer r = Renderer.createOffsetRenderer(trackRenderer, trackBounds);
            r.composeTo(compositor);
            if (tickMarkRenderer != null && tickMarkInsets != null) {
                Rectangle2D tickMarkBounds = tickMarkInsets.apply2D(w, h);
                Renderer tr = Renderer.createOffsetRenderer(tickMarkRenderer, tickMarkBounds);
                if (tickOperator != null) {
                    ReusableCompositor mask = compositor.createSimilar();
                    tr.composeTo(mask);
                    compositor.blendFrom(mask, tickOperator);
                } else {
                    tr.composeTo(compositor);
                }
            }
        }

        {
            Rectangle2D thumbBounds = thumbInsets.apply2D(w, h);
            Renderer r = Renderer.createOffsetRenderer(thumbRenderer, thumbBounds);

            // If a translucent thumb is directly painted, the track will show through.
            // Instead, the non-transparent thumb pixels must be copied into the raster.
            // A small adjustment is made at the boundary to avoid the appearance of a gap.

            if (isThumbTranslucent) {
                ReusableCompositor mask = compositor.createSimilar();
                r.composeTo(mask);
                compositor.blendFrom(mask, blender);
            } else {
                r.composeTo(compositor);
            }
        }
    }

    private static class Blender
      implements ReusableCompositor.PixelOperator
    {
        @Override
        public int combine(int destinationPixel, int sourcePixel)
        {
            int alpha = alpha(sourcePixel);
            if (alpha == 0) {
                return destinationPixel;
            }
            if (alpha > 20) {
                return sourcePixel;
            }
            return JNRUtils.combine(destinationPixel, sourcePixel);
        }
    }
}
