/*
 * Copyright (c) 2018 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.impl;

import org.jetbrains.annotations.*;

/**
  A basic renderer that inverts the output of a specified basic renderer.
*/

public class InvertRenderer
  extends PostProcessedRenderer
{
    private final float alphaMultiplier;

    public InvertRenderer(@NotNull BasicRenderer r, float alphaMultiplier)
    {
        super(r);

        this.alphaMultiplier = alphaMultiplier;
    }

    @Override
    protected int processPixel(int row, int col, int red, int green, int blue, int alpha)
    {
        red = 255 - red;
        green = 255 - green;
        blue = 255 - blue;
        alpha = (int) (alpha * alphaMultiplier);

        return createPixel(red, green, blue, alpha);
    }
}
