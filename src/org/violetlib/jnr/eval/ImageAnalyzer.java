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

import org.jetbrains.annotations.*;

import static org.violetlib.jnr.impl.ImageUtils.*;

/**
  A tool that can be useful for determining the layout properties of native rendering. Note that although automatic
  analysis often works, there are cases where it does not. For this reason, the layout parameters used by this library
  are not automatically generated.
*/

public class ImageAnalyzer
{
    private final @NotNull BufferedImage r;
    private final int width;
    private final int height;
    private final int minX;
    private final int minY;
    private final int maxX;
    private final int maxY;

    /**
      Analyze an image to determine where it contains non-transparent pixels.
      @param r The image.
    */

    public ImageAnalyzer(@NotNull BufferedImage r)
    {
        this.r = r;

        width = r.getWidth();
        height = r.getHeight();

        minX = r.getMinX();
        minY = r.getMinY();
        maxX = minX + width - 1;
        maxY = minY + height - 1;
    }

    /**
      Determine the minimum bounds containing all non-transparent pixels.

      @return the bounds, or null if there are no non-transparent pixels.
    */

    public @Nullable Rectangle getEffectiveBounds()
    {
        if (width == 0 || height == 0) {
            return null;
        }
        int p1 = r.getRGB(minX, minY);
        int p2 = r.getRGB(maxX, maxY);
        if (!isTransparent(p1) && !isTransparent(p2)) {
            return new Rectangle(minX, minY, width, height);
        }

        int effectiveMinY = Integer.MAX_VALUE;

        for (int y = minY; y <= maxY; y++) {
            if (!isRowTransparent(y)) {
                effectiveMinY = y;
                break;
            }
        }

        if (effectiveMinY == Integer.MAX_VALUE) {
            return null;
        }

        int effectiveMaxY = effectiveMinY;
        for (int y = maxY; y > effectiveMinY; y--) {
            if (!isRowTransparent(y)) {
                effectiveMaxY = y;
                break;
            }
        }

        int effectiveMinX = maxX;  // we know the raster is not fully transparent
        for (int x = minX; x < maxX; x++) {
            if (!isColumnTransparent(x, effectiveMinY, effectiveMaxY)) {
                effectiveMinX = x;
                break;
            }
        }

        int effectiveMaxX = effectiveMinX;
        for (int x = maxX; x > effectiveMinX; x--) {
            if (!isColumnTransparent(x, effectiveMinY, effectiveMaxY)) {
                effectiveMaxX = x;
                break;
            }
        }

        int effectiveWidth = effectiveMaxX - effectiveMinX + 1;
        int effectiveHeight = effectiveMaxY - effectiveMinY + 1;
        return new Rectangle(effectiveMinX, effectiveMinY, effectiveWidth, effectiveHeight);
    }

    private boolean isRowTransparent(int y)
    {
        for (int x = minX; x <= maxX; x++) {
            if (!isTransparent(r.getRGB(x, y))) {
                return false;
            }
        }
        return true;
    }

    private boolean isColumnTransparent(int x, int ymin, int ymax)
    {
        for (int y = ymin; y <= ymax; y++) {
            if (!isTransparent(r.getRGB(x, y))) {
                return false;
            }
        }
        return true;
    }

    private static boolean isTransparent(int pixel)
    {
        return alpha(pixel) == 0;
    }
}
