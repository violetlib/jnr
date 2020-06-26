/*
 * Copyright (c) 2015-2016 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.impl;

import java.awt.Image;

import org.jetbrains.annotations.*;

/**
  A painter that uses a renderer and caches the rendered image. The cache key is created by a subclass method.
*/

public abstract class CachingRendererPainter
  extends RendererPainter
{
    private static boolean isCachingEnabled = getDefaultCachingEnabled();

    private static boolean getDefaultCachingEnabled()
    {
        String s = System.getProperty("VAqua.enableCaching");
        if ("false".equals(s)) {
            System.err.println("VAqua caching disabled");
            return false;
        }
        return true;
    }

    public static void setCachingEnabled(boolean b)
    {
        if (b != isCachingEnabled) {
            isCachingEnabled = b;
            System.err.println("VAqua caching " + (isCachingEnabled ? "enabled" : "disabled"));
        }
    }

    /**
      Initialize this base class.

      @param r The renderer.
      @param width The width of the rendering, in device independent pixels.
      @param height The height of the rendering, in device independent pixels.
    */

    protected CachingRendererPainter(@NotNull Renderer r, float width, float height)
    {
        super(r, width, height);
    }

    @Override
    protected @Nullable Image getImage(int scaleFactor, int width, int height)
    {
        ImageCache.PixelsKey key = createKey(scaleFactor, width, height);

        if (key != null && isCachingEnabled) {
            ImageCache cache = ImageCache.getInstance();
            Image im = cache.getImage(key);
            if (im != null) {
                return im;
            }
            im = createImage(scaleFactor, width, height);
            if (im != null) {
                cache.setImage(key, im);
            }
            return im;
        } else {
            return createImage(scaleFactor, width, height);
        }
    }

    protected abstract @Nullable ImageCache.PixelsKey createKey(int scaleFactor, int rasterWidth, int rasterHeight);
}
