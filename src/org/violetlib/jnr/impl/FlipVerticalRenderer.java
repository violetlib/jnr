/*
 * Copyright (c) 2015-2018 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.impl;

import java.util.Arrays;

import org.jetbrains.annotations.*;

/**
  A renderer that vertically flips the effect of a base raster. Not thread safe.
*/

public class FlipVerticalRenderer
  implements BasicRenderer
{
    private final @NotNull BasicRenderer r;

    private static int[] temp;

    /**
      Create a renderer that will invoke the specified renderer on a temporary raster and then copy its output into the
      original raster by flipping it vertically.

      @param r The renderer to invoke.
    */

    public FlipVerticalRenderer(@NotNull BasicRenderer r)
    {
        this.r = r;
    }

    @Override
    public void render(@NotNull int[] data, int rw, int rh, float w, float h)
    {
        int requiredSize = rw * rh;
        if (requiredSize > 0) {
            if (temp == null || temp.length < requiredSize) {
                temp = new int[requiredSize];
            } else {
                Arrays.fill(temp, 0);
            }
            r.render(temp, rw, rh, w, h);
            for (int rowOffset = 0; rowOffset < rh; rowOffset++) {
                int row = rh - rowOffset - 1;
                if (row >= 0 && row < rh) {
                    for (int colOffset = 0; colOffset < rw; colOffset++) {
                        int col = colOffset;
                        if (col >= 0 && col < rw) {
                            int pixel = temp[rowOffset*rw+colOffset];
                            int alpha = (pixel >> 24) & 0xFF;
                            if (alpha != 0) {
                                data[row * rw + col] = pixel;
                            }
                        }
                    }
                }
            }
        }
    }
}
