/*
 * Copyright (c) 2018-2020 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.impl;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;
import java.awt.image.RGBImageFilter;

import org.jetbrains.annotations.*;

/**

*/

public class ImageUtils
{
    public static @NotNull Image invertForDarkMode(@NotNull Image source)
    {
        return createFilteredImage(source, new InvertImageForDarkModeFilter());
    }

    private static Image createFilteredImage(Image image, ImageFilter filter)
    {
        ImageProducer prod = new FilteredImageSource(image.getSource(), filter);
        return waitForImage(Toolkit.getDefaultToolkit().createImage(prod));
    }

    private static class InvertImageForDarkModeFilter extends RGBImageFilter
    {
        public InvertImageForDarkModeFilter()
        {
            canFilterIndexColorModel = true;
        }

        public int filterRGB(int x, int y, int rgb)
        {
            return toGray(rgb);
        }
    }

    public static int toGray(int pixel)
    {
        // Use NTSC conversion formula.
        int gray = (int)((0.30 * red(pixel) + 0.59 * green(pixel) + 0.11 * blue(pixel)) / 3);
        gray = (int) ((255 - gray) * 0.7);
        if (gray < 0) gray = 0;
        if (gray > 255) gray = 255;
        return createPixel(alpha(pixel), gray, gray, gray);
    }

    public static int createPixel(int alpha, int red, int green, int blue)
    {
        return ((alpha & 0xff) << 24) + ((red & 0xff) << 16) + ((green & 0xff) << 8) + (blue & 0xff);
    }

    public static int alpha(int pixel)
    {
        return (pixel >> 24) & 0xff;
    }

    public static int red(int pixel)
    {
        return (pixel >> 16) & 0xff;
    }

    public static int green(int pixel)
    {
        return (pixel >> 8) & 0xff;
    }

    public static int blue(int pixel)
    {
        return (pixel >> 0) & 0xff;
    }

    private static @NotNull Image waitForImage(@NotNull Image image)
    {
        boolean[] mutex = new boolean[] { false };
        ImageObserver observer = (Image img, int infoflags, int x, int y, int width, int height) -> {
            if ((width != -1 && height != -1 && (infoflags & ImageObserver.ALLBITS) != 0) || (infoflags & ImageObserver.ABORT) != 0) {
                synchronized (mutex) {
                    mutex[0] = true;
                    mutex.notify();
                }
                return false;
            } else {
                return true;
            }
        };
        synchronized (mutex) {
            while (!mutex[0] && image.getWidth(observer) == -1) {
                try {
                    mutex.wait();
                } catch (InterruptedException e) {
                    break;
                }
            }
        }
        return image;
    }
}
