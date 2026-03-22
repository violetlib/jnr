/*
 * Copyright (c) 2015-2025 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.awt.image.BufferedImage;

import static java.awt.image.BufferedImage.TYPE_INT_ARGB_PRE;

/**
  A painter that does nothing.
*/

public class NullPainter
  implements ConfiguredPainter
{
    private final @Nullable LayoutInfo layoutInfo;

    public NullPainter(@Nullable LayoutInfo layoutInfo)
    {
        this.layoutInfo = layoutInfo;
    }

    @Override
    public float getFixedWidth()
    {
        return layoutInfo != null ? layoutInfo.getFixedVisualWidth() : 0;
    }

    @Override
    public float getFixedHeight()
    {
        return layoutInfo != null ? layoutInfo.getFixedVisualHeight() : 0;
    }

    @Override
    public void paint(@NotNull Graphics g, float x, float y)
    {
    }

    @Override
    public @Nullable Image getImage(int scaleFactor, int width, int height)
    {
        return new BufferedImage(0, 0, TYPE_INT_ARGB_PRE);
    }
}
