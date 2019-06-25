/*
 * Copyright (c) 2015-2016 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.impl;

import java.awt.geom.Rectangle2D;
import java.util.Objects;

import org.jetbrains.annotations.*;

/**
  A renderer description based on fixed offsets and raster size adjustments.

  @see MultiResolutionRendererDescription
*/

public final class BasicRendererDescription
  implements RendererDescription
{
    private final float xOffset;
    private final float yOffset;
    private final float widthAdjustment;
    private final float heightAdjustment;

    /**
      Create a renderer description that does not depend upon the scale factor. All parameters are specified in device
      independent pixels.

      @param xOffset The X offset of the raster origin from the target region origin. A negative value means that the
      raster will be shifted left before being painted.
      @param yOffset The Y offset of the raster origin from the target region origin. A negative value means that the
      raster will be shifted up before being painted.
      @param widthAdjustment This adjustment will be added to the target region width to determine the width of the
      raster given to the renderer. A positive value means that the raster will be wider than the target region.
      @param heightAdjustment This adjustment will be added to the target region height to determine the height of the
      raster given to the renderer. A positive value means that the raster will be taller than the target region.
    */

    public BasicRendererDescription(float xOffset, float yOffset, float widthAdjustment, float heightAdjustment)
    {
        if (false) {
            // DEBUG: positive offsets are valid but very unusual
            if (xOffset > 0) {
                System.err.println("Suspicious X offset: " + xOffset);
            }

            if (yOffset > 0) {
                System.err.println("Suspicious Y offset: " + yOffset);
            }
        }

        this.xOffset = xOffset;
        this.yOffset = yOffset;
        this.widthAdjustment = widthAdjustment;
        this.heightAdjustment = heightAdjustment;
    }

    @Override
    public boolean isTrivial()
    {
        return xOffset == 0 && yOffset == 0 && widthAdjustment == 0 && heightAdjustment == 0;
    }

    public float getXOffset()
    {
        return xOffset;
    }

    public float getYOffset()
    {
        return yOffset;
    }

    public float getWidthAdjustment()
    {
        return widthAdjustment;
    }

    public float getHeightAdjustment()
    {
        return heightAdjustment;
    }

    @Override
    public @NotNull RasterDescription getRasterBounds(@NotNull Rectangle2D target, int scaleFactor)
    {
        float x = round(target.getX() + xOffset, scaleFactor);
        float y = round(target.getY() + yOffset, scaleFactor);
        float rasterWidth = round(target.getWidth() + widthAdjustment, scaleFactor);
        float rasterHeight = round(target.getHeight() + heightAdjustment, scaleFactor);
        return new RasterDescription(x, y, rasterWidth, rasterHeight);
    }

    private float round(double v, int scaleFactor)
    {
        double scaledValue = v * scaleFactor;
        long scaledRounded = Math.round(scaledValue);
        return ((float) scaledRounded) / scaleFactor;
    }

    public @NotNull BasicRendererDescription withAdjustments(float x, float y, float w, float h)
    {
        return new BasicRendererDescription(xOffset+x, yOffset+y, widthAdjustment+w, heightAdjustment+h);
    }

    @Override
    public boolean equals(@Nullable Object o)
    {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BasicRendererDescription that = (BasicRendererDescription) o;
        return Objects.equals(xOffset, that.xOffset) &&
                 Objects.equals(yOffset, that.yOffset) &&
                 Objects.equals(widthAdjustment, that.widthAdjustment) &&
                 Objects.equals(heightAdjustment, that.heightAdjustment);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(xOffset, yOffset, widthAdjustment, heightAdjustment);
    }
}
