/*
 * Copyright (c) 2015 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr;

import java.awt.Graphics;

import org.jetbrains.annotations.*;

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
}
