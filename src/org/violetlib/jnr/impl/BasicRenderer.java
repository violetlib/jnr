/*
 * Copyright (c) 2015 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.impl;

import org.jetbrains.annotations.*;

/**
  An interface for a native renderer that renders directly into a raster.
*/

public interface BasicRenderer
{
    /**
      Perform a rendering by writing pixels into a raster.

      <p>
      This library expects the pixels to be represented as INT_ARGB_PRE. The dimensions of the raster may differ from
      the dimensions of the image when rendering to a HiDPI display or when rendering to a scaled graphics context.

      @param data A raster viewed as a two dimensional array of pixels in row major order whose dimension are given by
      {@code rw} and {@code rh}.
      @param rw The number of pixels in each row of the raster.
      @param rh The number of rows in the raster.
      @param w The width of the rendered image in device independent pixels (sometimes called points).
      @param h The height of the rendered image in device independent pixels (sometimes called points).
    */

    void render(@NotNull int[] data, int rw, int rh, float w, float h);
}
