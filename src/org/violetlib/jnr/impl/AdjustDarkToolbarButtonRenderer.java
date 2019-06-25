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
  A basic renderer that corrects the output of a specified basic renderer for toolbar controls in dark mode.
*/

public class AdjustDarkToolbarButtonRenderer
  extends PostProcessedRenderer
{
    public AdjustDarkToolbarButtonRenderer(@NotNull BasicRenderer r)
    {
        super(r);
    }

    @Override
    protected int processPixel(int row, int col, int red, int green, int blue, int alpha)
    {
        // The native renderer creates an image with an opaque background and a translucent border.
        // We want to make the background brighter.

        if (alpha > 250 && red < 60) {
            int newAlpha = alpha * 220 / 255;
            if (red > 30) {
                // active state
                return createPixel(red + 67, newAlpha);
            } else {
                // inactive state
                return createPixel(red + 49, newAlpha);
            }
        }

        return createPixel(red, green, blue, alpha);
    }
}
