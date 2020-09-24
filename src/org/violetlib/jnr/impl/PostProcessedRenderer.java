/*
 * Copyright (c) 2018-2020 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.impl;

import java.util.Arrays;

import org.jetbrains.annotations.*;

import static org.violetlib.jnr.impl.ImageUtils.*;

/**
  A basic renderer that renders into a temporary raster using a specified basic renderer, post processes the pixels,
  then copies the result to the original raster. Not thread safe.
*/

public abstract class PostProcessedRenderer
  implements BasicRenderer
{
    private final @NotNull BasicRenderer r;

    private static int[] temp;

    /**
      Create a renderer that post processes the results of a specified renderer.

      @param r The renderer to invoke.
    */

    public PostProcessedRenderer(@NotNull BasicRenderer r)
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
            process(temp, rw, rh);
            install(temp, data, rw, rh);
        }
    }

    /**
      Process the pixels int the temporary buffer.
      @param buffer The temporary buffer.
      @param rw The number of pixels in each row of the raster.
      @param rh The number of rows in the raster.
    */

    protected void process(@NotNull int[] buffer, int rw, int rh)
    {
        processPixels(buffer, rw, rh);
    }

    protected void processPixels(@NotNull int[] buffer, int rw, int rh)
    {
        for (int row = 0; row < rh; row++) {
            for (int col = 0; col < rw; col++) {
                int index = row * rw + col;
                int originalPixel = buffer[index];
                int pixel = processRawPixel(row, col, originalPixel);
                if (pixel != originalPixel) {
                    buffer[index] = pixel;
                }
            }
        }
    }

    protected int processRawPixel(int row, int col, int pixel)
    {
        int alpha = alpha(pixel);
        if (alpha == 0) {
            return pixel;
        }

        int red = red(pixel);
        int green = green(pixel);
        int blue = blue(pixel);

        // convert from premultiplied alpha
        if (alpha > 0) {
            if (red > 0) {
                red = red * 255 / alpha;
            }
            if (green > 0) {
                green = green * 255 / alpha;
            }
            if (blue > 0) {
                blue = blue * 255 / alpha;
            }
        }

        return processPixel(row, col, red, green, blue, alpha);
    }

    /**
      Process one pixel, provided as actual values (not premultiplied alpha values).
      @return the replacement pixel.
    */

    protected abstract int processPixel(int row, int col, int red, int green, int blue, int alpha);

    /**
      Create a pixel with premultipled values from actual values.
    */

    protected int createPixel(int red, int green, int blue, int alpha)
    {
        red = fix(red);
        green = fix(green);
        blue = fix(blue);
        alpha = fix(alpha);

        // convert to premultipled alpha
        if (alpha > 0) {
            if (red > 0) {
                red = red * alpha / 255;
            }
            if (green > 0) {
                green = green * alpha / 255;
            }
            if (blue > 0) {
                blue = blue * alpha / 255;
            }
        }

        int result = (alpha << 24) + (red << 16) + (green << 8) + blue;
        return result;
    }

    protected int createPixel(int color, int alpha)
    {
        return createPixel(color, color, color, alpha);
    }

    /**
      Copy the pixels from the temporary buffer to the output raster.
      @param buffer The temporary buffer.
      @param output The output raster.
      @param rw The number of pixels in each row of the raster.
      @param rh The number of rows in the raster.
    */

    protected void install(@NotNull int[] buffer, @NotNull int[] output, int rw, int rh)
    {
        int count = rw * rh;
        System.arraycopy(buffer, 0, output, 0, count);
    }

    protected int fix(int v)
    {
        if (v < 0) {
            return 0;
        }
        if (v > 255) {
            return 255;
        }
        return v;
    }
}
