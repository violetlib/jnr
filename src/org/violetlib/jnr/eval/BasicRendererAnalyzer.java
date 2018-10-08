/*
 * Copyright (c) 2015 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.eval;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import org.violetlib.jnr.aqua.impl.NativeSupport;
import org.violetlib.jnr.impl.BasicRenderer;
import org.violetlib.jnr.impl.Renderer;
import org.violetlib.jnr.impl.ReusableCompositor;

import org.jetbrains.annotations.*;

/**
  Examine the output of a renderer to determine its characteristics.
*/

public class BasicRendererAnalyzer
{
    public static final int DEFAULT_WIDTH = 200;
    public static final int DEFAULT_HEIGHT = 100;

    protected final @NotNull Renderer r;
    protected final int scaleFactor;
    protected final int testWidth;
    protected final int testHeight;

    protected final @Nullable RendererAnalysisResults results;

    public BasicRendererAnalyzer(@NotNull Renderer r, int scaleFactor, boolean forceVertical)
    {
        this(r, scaleFactor, forceVertical ? DEFAULT_HEIGHT : DEFAULT_WIDTH, forceVertical ? DEFAULT_WIDTH : DEFAULT_HEIGHT);
    }

    public BasicRendererAnalyzer(@NotNull Renderer r, int scaleFactor, int testWidth, int testHeight)
    {
        if (scaleFactor <= 0 | scaleFactor > 16) {
            throw new IllegalArgumentException("Invalid or unsupported scale factor");
        }

        if (testWidth < 8 | testHeight < 8) {
            throw new IllegalArgumentException("Invalid or unsupported width or height");
        }

        this.r = r;
        this.scaleFactor = scaleFactor;
        this.testWidth = testWidth;
        this.testHeight = testHeight;

        results = analyze();
    }

    public @Nullable RendererAnalysisResults getResults()
    {
        return results;
    }

    protected @Nullable RendererAnalysisResults analyze()
    {
        int w = scaleFactor * testWidth;
        int h = scaleFactor * testHeight;

        ImageAnalyzer a = analyzeRendering(r, w, h);
        Rectangle bounds = a.getEffectiveBounds();

        if (bounds == null) {
            return null;
        }

        int fx = bounds.x;
        int fy = bounds.y;
        int fw = bounds.width;
        int fh = bounds.height;

        assert fw > 0;
        assert fh > 0;

        int topInset = fy;
        int bottomInset = h - fy - fh;
        int leftInset = fx;
        int rightInset = w - fx - fw;

        // If the top or bottom insets are large, the height is fixed
        int fixedHeight = 0;
        if (topInset > 30 || bottomInset > 30) {
            fixedHeight = h - topInset - bottomInset;
        }
        // If the left or right insets are large, the width is fixed
        int fixedWidth = 0;
        if (leftInset > 30 || rightInset > 30) {
            fixedWidth = w - leftInset - rightInset;
        }

        // If the height is not fixed, then we know the actual top and bottom insets.
        // Otherwise, we need to experiment to figure out the minimum required raster height, from which we can
        // determine the corresponding top and bottom insets. We determine the minimum required raster height by
        // increasing the raster height until we find one that yields the same effective height, which implies that all
        // the desired pixels are present.

        if (fixedHeight > 0) {
            int ch;
            int limit = fh + 20 * scaleFactor;
            for (ch = fixedHeight; ch <= limit; ch++) {
                ImageAnalyzer ca = analyzeRendering(r, w, ch);
                Rectangle b = ca.getEffectiveBounds();
                if (b != null) {
                    double actualHeight = b.getHeight();
                    if (isEqualInteger(actualHeight, fh)) {
                        topInset = b.y;
                        bottomInset = ch - b.y - b.height;
                        break;
                    } else if (actualHeight > fh) {
                        NativeSupport.log("Unexpectedly got larger height than the supposed fixed height: " + actualHeight + " " + fh);
                        break;
                    }
                }
            }
        }

        // Same for width.

        if (fixedWidth > 0) {
            int cw;
            int limit = fw + 20 * scaleFactor;
            for (cw = fixedWidth; cw <= limit; cw++) {
                ImageAnalyzer ca = analyzeRendering(r, cw, h);
                Rectangle b = ca.getEffectiveBounds();
                if (b != null) {
                    double actualWidth = b.getWidth();
                    if (isEqualInteger(actualWidth, fw)) {
                        leftInset = b.x;
                        rightInset = cw - b.x - b.width;
                        break;
                    } else if (actualWidth > fw) {
                        NativeSupport.log("Unexpectedly got larger width than the supposed fixed width: " + actualWidth + " " + fw);
                        break;
                    }
                }
            }
        }

        float width = ((float) fixedWidth) / scaleFactor;
        float height = ((float) fixedHeight) / scaleFactor;
        float xOrigin = -leftInset / scaleFactor;
        float yOrigin = -topInset / scaleFactor;
        float widthAdjust = (leftInset + rightInset) / scaleFactor;
        float heightAdjust = (topInset + bottomInset) / scaleFactor;
        return new RendererAnalysisResults(width, height, xOrigin, yOrigin, widthAdjust, heightAdjust);
    }

    protected boolean isEqualInteger(double d, int n)
    {
        double delta = Math.abs(d - n);
        return delta < 0.00001;
    }

    /**
      Analyze the rendering for a given raster size.
      @param r The renderer.
      @param w The width of the raster in pixels.
      @param h The height of the raster in pixels.
      @return the analysis.
    */

    protected @NotNull ImageAnalyzer analyzeRendering(@NotNull Renderer r, int w, int h)
    {
        ReusableCompositor rc = new ReusableCompositor();
        rc.reset(w, h, scaleFactor);

        // It is the basic renderer we are trying to analyze
        BasicRenderer br = r.getBasicRenderer();
        if (br != null) {
            rc.composeRenderer(br);
        } else {
            r.composeTo(rc);
        }

        BufferedImage b = rc.getImage();
        if (b == null) {
            b = new BufferedImage(0, 0, BufferedImage.TYPE_INT_ARGB_PRE);
        }
        return new ImageAnalyzer(b);
    }
}
