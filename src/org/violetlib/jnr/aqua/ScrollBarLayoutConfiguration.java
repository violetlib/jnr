/*
 * Copyright (c) 2015 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.aqua;

import java.util.Objects;

import org.violetlib.jnr.aqua.AquaUIPainter.Orientation;
import org.violetlib.jnr.aqua.AquaUIPainter.ScrollBarWidget;
import org.violetlib.jnr.aqua.AquaUIPainter.Size;

import org.jetbrains.annotations.*;

/**
  A layout configuration for a scroll bar.
*/

public class ScrollBarLayoutConfiguration
  extends LayoutConfiguration
{
    private final @NotNull ScrollBarWidget bw;
    private final @NotNull Size size;
    private final @NotNull Orientation o;

    public ScrollBarLayoutConfiguration(@NotNull ScrollBarWidget bw, @NotNull Size size, @NotNull Orientation o)
    {
        this.bw = bw;
        this.size = size;
        this.o = o;
    }

    protected ScrollBarLayoutConfiguration(@NotNull ScrollBarLayoutConfiguration g)
    {
        this.bw = g.getWidget();
        this.size = g.getSize();
        this.o = g.getOrientation();
    }

    @Override
    public @NotNull ScrollBarWidget getWidget()
    {
        return bw;
    }

    public @NotNull Size getSize()
    {
        return size;
    }

    public @NotNull Orientation getOrientation()
    {
        return o;
    }

    @Override
    public boolean equals(@Nullable Object o1)
    {
        if (this == o1) {
            return true;
        }
        if (o1 == null || getClass() != o1.getClass()) {
            return false;
        }
        ScrollBarLayoutConfiguration that = (ScrollBarLayoutConfiguration) o1;
        return bw == that.bw && size == that.size && o == that.o;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(bw, size, o);
    }

    @Override
    public @NotNull String toString()
    {
        return bw + " " + size + " " + o;
    }
}
