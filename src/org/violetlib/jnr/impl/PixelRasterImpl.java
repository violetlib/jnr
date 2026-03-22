/*
 * Copyright (c) 2020-2025 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.impl;

import org.jetbrains.annotations.NotNull;

/**

*/

public class PixelRasterImpl
  implements PixelRaster
{
    private final int @NotNull [] data;
    private final int width;
    private final int height;

    public PixelRasterImpl(int @NotNull [] data, int width, int height)
    {
        this.data = data;
        this.width = width;
        this.height = height;
    }

    @Override
    public void provide(@NotNull Accessor a)
    {
        a.access(data, width, height);
    }
}
