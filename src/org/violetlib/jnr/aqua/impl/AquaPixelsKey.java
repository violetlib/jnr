/*
 * Copyright (c) 2015-2025 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.aqua.impl;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.violetlib.jnr.aqua.Configuration;
import org.violetlib.jnr.impl.ImageCache;
import org.violetlib.vappearances.VAppearance;

/**
  The key used by the AquaNativePainter to cache images.
*/

public class AquaPixelsKey
  implements ImageCache.PixelsKey
{
    private final int pixelCount;
    private final int hash;

    private final int scaleFactor;
    private final int w;
    private final int h;
    private final @NotNull Configuration g;
    private final @NotNull VAppearance appearance;

    public AquaPixelsKey(int scaleFactor, int w, int h, @NotNull Configuration g, @NotNull VAppearance appearance)
    {
        this.pixelCount = w * h;
        this.scaleFactor = scaleFactor;
        this.w = w;
        this.h = h;
        this.g = g;
        this.appearance = appearance;
        this.hash = hash();
    }

    @Override
    public int getPixelCount()
    {
        return pixelCount;
    }

    private int hash()
    {
        int hash = scaleFactor;
        hash = 31 * hash + w;
        hash = 31 * hash + h;
        hash = 31 * hash + g.hashCode();
        hash = 31 * hash + appearance.hashCode();
        return hash;
    }

    @Override
    public int hashCode()
    {
        return hash;
    }

    @Override
    public boolean equals(@Nullable Object obj)
    {
        if (obj != null && obj.getClass() == AquaPixelsKey.class) {
            AquaPixelsKey that = (AquaPixelsKey) obj;
            return scaleFactor == that.scaleFactor && w == that.w && h == that.h && g.equals(that.g)
                     && appearance.equals(that.appearance);
        }
        return false;
    }

    @Override
    public @NotNull String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(g);
        if (appearance.isDark()) {
            sb.append(" DARK");
        }
        if (appearance.isHighContrast()) {
            sb.append(" HC");
        }
        return sb.toString();
    }
}
