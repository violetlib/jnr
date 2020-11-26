/*
 * Copyright (c) 2015 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.aqua;

import java.util.Objects;

import org.violetlib.jnr.aqua.AquaUIPainter.DividerWidget;
import org.violetlib.jnr.aqua.AquaUIPainter.Orientation;

import org.jetbrains.annotations.*;

/**
  A layout configuration for a split pane divider.
*/

public class SplitPaneDividerLayoutConfiguration
  extends LayoutConfiguration
{
    private final @NotNull DividerWidget dw;
    private final @NotNull Orientation o;
    private final int thickness;

    public SplitPaneDividerLayoutConfiguration(@NotNull DividerWidget dw, @NotNull Orientation o, int thickness)
    {
        this.dw = dw;
        this.o = o;
        this.thickness = thickness;
    }

    @Override
    public @NotNull DividerWidget getWidget()
    {
        return dw;
    }

    public @NotNull Orientation getOrientation()
    {
        return o;
    }

    public int getThickness()
    {
        return thickness;
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
        SplitPaneDividerLayoutConfiguration that = (SplitPaneDividerLayoutConfiguration) o1;
        return dw == that.dw && o == that.o && thickness == that.thickness;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(dw, o, thickness);
    }

    @Override
    public @NotNull String toString()
    {
        String ts = thickness > 0 ? " " + thickness : "";
        return dw + " " + o + ts;
    }
}
