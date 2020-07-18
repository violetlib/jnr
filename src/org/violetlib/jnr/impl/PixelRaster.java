/*
 * Copyright (c) 2020 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.impl;

import org.jetbrains.annotations.*;

/**
  An object that provides pixels in the form of an array of pixels in INT_ARGB_PRE format.
*/

public interface PixelRaster
{
    @FunctionalInterface
    interface Accessor
    {
        /**
          This method provides temporary access to the pixel data.
          @param data The pixels, arranged in row major order. This array must not be modified or retained. This array
          contains at least as many elements as implied by the width and height parameters.
          @param width The raster width, in pixels.
          @param height The raster height, in pixels.
        */
        void access(@NotNull int[] data, int width, int height);
    }

    /**
       Provide temporary, read-only access to the pixel data.
    */

    void provide(@NotNull Accessor a);
}
