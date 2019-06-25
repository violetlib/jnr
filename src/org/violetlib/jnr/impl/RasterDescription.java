/*
 * Copyright (c) 2015 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.impl;

/**
  A raster description defines a raster used for native rendering. The width and height attributes define the size of
  the raster. The X and Y attributes define the intended origin of the raster in the graphics coordinate space. All
  values are specified in device independent pixels (points).

  @see RendererDescription
*/

public final class RasterDescription
{
    private final float x;
    private final float y;
    private final float width;
    private final float height;

    public RasterDescription(float x, float y, float width, float height)
    {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public float getX()
    {
        return x;
    }

    public float getY()
    {
        return y;
    }

    public float getWidth()
    {
        return width;
    }

    public float getHeight()
    {
        return height;
    }
}
