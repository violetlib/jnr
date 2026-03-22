/*
 * Copyright (c) 2015-2025 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.impl;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.violetlib.jnr.Painter;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

/**
  A painter that uses a renderer.
*/

public abstract class RendererPainter
  implements Painter
{
    protected final @NotNull Renderer r;
    protected final float width;
    protected final float height;

    /**
      Create a painter that uses a native renderer.

      @param r The renderer.
      @param width The width of the rendering, in device independent pixels.
      @param height The height of the rendering, in device independent pixels.
    */

    public RendererPainter(@NotNull Renderer r, float width, float height)
    {
        this.r = r;
        this.width = width;
        this.height = height;
    }

    @Override
    public void paint(@NotNull Graphics gg, float x, float y)
    {
        if (width > 0 && height > 0) {
            int scaleFactor = JavaSupport.getScaleFactor(gg);
            int w = (int) Math.ceil(width);
            int h = (int) Math.ceil(height);
            Image im = getImage(scaleFactor, w, h);
            if (im != null) {
                Graphics2D g2 = JNRPlatformUtils.toGraphics2D(gg);
                if (g2 != null) {
                    Graphics2D g = (Graphics2D) g2.create();
                    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    AffineTransform tr = AffineTransform.getTranslateInstance(x, y);
                    g.drawImage(im, tr, null);
                    g.dispose();
                }
            }
        }
    }

    @Override
    public @Nullable Image getImage(int scaleFactor, int width, int height)
    {
        return createImage(scaleFactor, width, height);
    }

    protected @Nullable Image createImage(int scaleFactor, int width, int height)
    {
        // Because we know the scale factor, we can create the image now rather than waiting for the multiresolution
        // image to ask for it.

        ReusableCompositor rc = new ReusableCompositor();
        int rasterWidth = width * scaleFactor;
        int rasterHeight = height * scaleFactor;
        rc.reset(rasterWidth, rasterHeight, scaleFactor);
        r.composeTo(rc);
        BufferedImage theImage = rc.getImage();
        return theImage != null ? JavaSupport.createMultiResolutionImage(width, height, theImage) : null;
    }
}
