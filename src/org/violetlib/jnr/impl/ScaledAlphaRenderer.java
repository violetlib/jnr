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
  A basic renderer that alters the output of a basic renderer by scaling the alpha channel.
*/

public class ScaledAlphaRenderer
  extends PostProcessedRenderer
{
    private final float multiplier;

    public ScaledAlphaRenderer(@NotNull BasicRenderer r, float multiplier)
    {
        super(r);

        this.multiplier = multiplier;
    }

    @Override
    protected int processPixel(int row, int col, int red, int green, int blue, int alpha)
    {
        if (alpha > 0) {
            alpha = Math.round(alpha * multiplier);
            return createPixel(red, green, blue, alpha);
        } else {
            return 0;
        }
    }
}
