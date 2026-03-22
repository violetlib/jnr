/*
 * Copyright (c) 2015-2025 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.impl;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.violetlib.jnr.Painter;

import java.awt.*;

/**

*/

public class OffsetPainter
  implements Painter
{
    private final @NotNull Painter p;
    private final float xOffset;
    private final float yOffset;

    public OffsetPainter(@NotNull Painter p, float xOffset, float yOffset)
    {
        this.p = p;
        this.xOffset = xOffset;
        this.yOffset = yOffset;
    }

    @Override
    public void paint(@NotNull Graphics g, float x, float y)
    {
        p.paint(g, x + xOffset, y + yOffset);
    }

    @Override
    public @Nullable Image getImage(int scaleFactor, int width, int height)
    {
        // Offset not supported
        return p.getImage(scaleFactor, width, height);
    }
}
