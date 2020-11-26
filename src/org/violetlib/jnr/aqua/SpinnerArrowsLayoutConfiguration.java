/*
 * Copyright (c) 2015-2020 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.aqua;

import java.util.Objects;

import org.violetlib.jnr.aqua.AquaUIPainter.Size;

import org.jetbrains.annotations.*;

/**
  A layout configuration for spinner arrows.
*/

public class SpinnerArrowsLayoutConfiguration
  extends LayoutConfiguration
{
    private final @NotNull Size size;

    public SpinnerArrowsLayoutConfiguration(@NotNull Size size)
    {
        this.size = size;
    }

    @Override
    public @NotNull Object getWidget()
    {
        return this;
    }

    public @NotNull Size getSize()
    {
        return size;
    }

    @Override
    public boolean equals(@Nullable Object o)
    {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SpinnerArrowsLayoutConfiguration that = (SpinnerArrowsLayoutConfiguration) o;
        return size == that.size;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(size);
    }

    @Override
    public @NotNull String toString()
    {
        return "Spinner Arrows " + size;
    }
}
