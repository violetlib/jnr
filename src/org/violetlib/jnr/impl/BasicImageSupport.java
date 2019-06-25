/*
 * Copyright (c) 2015-2016 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.impl;

import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;
import java.awt.image.DirectColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

import org.jetbrains.annotations.*;

/**
  Basic support for creating images from INT_ARGB_PRE raster data.
*/

public class BasicImageSupport
{
    private static final @NotNull ColorModel colorModel = createColorModel();

    public static @NotNull ColorModel getColorModel()
    {
        return colorModel;
    }

    public static @NotNull BufferedImage createImage(@NotNull int[] buffer, int w, int h)
    {
        return createImage(buffer, w, h, w);
    }

    public static @NotNull BufferedImage createImage(@NotNull int[] buffer, int w, int h, int scan)
    {
        return createBufferedImage(colorModel, buffer, w, h, scan);
    }

    /**
      Create a color model for INT_ARGB_PRE.
    */

    private static @NotNull ColorModel createColorModel()
    {
        return new DirectColorModel(
          ColorSpace.getInstance(ColorSpace.CS_sRGB),
          32, 0x00ff0000, 0x0000ff00, 0x000000ff, 0xff000000, true, DataBuffer.TYPE_INT
        );
    }

    private static @NotNull BufferedImage createBufferedImage(@NotNull ColorModel cm, @NotNull int[] buffer, int w, int h, int scan)
    {
        DataBuffer db = new DataBufferInt(buffer, buffer.length);
        int[] bandMasks = new int[4];
        bandMasks[0] = 0x00ff0000;
        bandMasks[1] = 0x0000ff00;
        bandMasks[2] = 0x000000ff;
        bandMasks[3] = 0xff000000;
        WritableRaster r = Raster.createPackedRaster(db, w, h, scan, bandMasks, null);
        return new BufferedImage(cm, r, true, null);
    }
}
