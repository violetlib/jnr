/*
 * Copyright (c) 2015 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.impl;

import java.awt.geom.Rectangle2D;

import org.jetbrains.annotations.*;

/**

*/

public final class TrivialRendererDescription
  implements RendererDescription
{
    private static final @NotNull TrivialRendererDescription INSTANCE = new TrivialRendererDescription();

    public static @NotNull TrivialRendererDescription getInstance()
    {
        return INSTANCE;
    }

    private TrivialRendererDescription()
    {
    }

    @Override
    public @NotNull RasterDescription getRasterBounds(@NotNull Rectangle2D target, int scaleFactor)
    {
        float x = (float) target.getX();
        float y = (float) target.getY();
        float rasterWidth = (float) target.getWidth();
        float rasterHeight = (float) target.getHeight();
        return new RasterDescription(x, y, rasterWidth, rasterHeight);
    }

    @Override
    public boolean isTrivial()
    {
        return true;
    }
}
