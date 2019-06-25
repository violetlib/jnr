/*
 * Copyright (c) 2016 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.impl;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.util.WeakHashMap;

import org.jetbrains.annotations.*;

/**
  Java platform specific support for Java 8 and later.
*/
public class Java8Support
  implements JavaSupport.JavaSupportImpl
{
    @Override
    public int getScaleFactor(@NotNull Graphics g)
    {
        // Is it fair to assume that a graphics context always is associated with the same device,
        // in other words, they are not reused in some sneaky way?
        Integer n = scaleMap.get(g);
        if (n != null) {
            return n;
        }

        int scaleFactor;
        if (g instanceof Graphics2D) {
            Graphics2D gg = (Graphics2D) g;
            GraphicsConfiguration gc = gg.getDeviceConfiguration();
            scaleFactor = getScaleFactor(gc);
        } else {
            scaleFactor = 1;
        }

        scaleMap.put(g, scaleFactor);

        return scaleFactor;
    }

    private static final @NotNull WeakHashMap<Graphics,Integer> scaleMap = new WeakHashMap<>();

    private static int getScaleFactor(@NotNull GraphicsConfiguration gc)
    {
        GraphicsDevice device = gc.getDevice();
        Object scale = null;

        try {
            Field field = device.getClass().getDeclaredField("scale");
            if (field != null) {
                field.setAccessible(true);
                scale = field.get(device);
            }
        } catch (Exception ignore) {}

        if (scale instanceof Integer) {
            return (Integer) scale;
        }

        return 1;
    }

    @Override
    public Image createMultiResolutionImage(int baseImageWidth, int baseImageHeight, @NotNull BufferedImage im)
    {
        return new JNR8MultiResolutionImage(baseImageWidth, baseImageHeight, im);
    }
}
