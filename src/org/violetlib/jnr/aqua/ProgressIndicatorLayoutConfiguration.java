/*
 * Copyright (c) 2015-2026 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.aqua;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.violetlib.jnr.aqua.AquaUIPainter.Orientation;
import org.violetlib.jnr.aqua.AquaUIPainter.ProgressWidget;
import org.violetlib.jnr.aqua.AquaUIPainter.Size;

import java.util.Objects;

import static org.violetlib.jnr.aqua.AquaUIPainter.macOS11;

/**
  A layout configuration for a progress indicator.
*/

public class ProgressIndicatorLayoutConfiguration
  extends LayoutConfiguration
{
    private final @NotNull ProgressWidget pw;
    private final @NotNull Size size;
    private final @NotNull Orientation o;

    public ProgressIndicatorLayoutConfiguration(@NotNull ProgressWidget pw,
                                                @NotNull Size size,
                                                @NotNull Orientation o)
    {
        if (!AquaNativeRendering.isRaw()) {
            // progress bars can be regular or small on macOS 11, previously have only one size
            // spinners can be regular or small
            int version = AquaNativeRendering.getSystemRenderingVersion();
            size = pw == ProgressWidget.SPINNER || pw == ProgressWidget.INDETERMINATE_SPINNER || version >= macOS11
              ? size == Size.MINI || size == Size.SMALL ? Size.SMALL : Size.REGULAR
              : Size.REGULAR;
        }

        this.size = size;
        this.o = o;
        this.pw = pw;
    }

    protected ProgressIndicatorLayoutConfiguration(@NotNull ProgressIndicatorLayoutConfiguration g)
    {
        this.size = g.getSize();
        this.o = g.getOrientation();
        this.pw = g.getWidget();
    }

    @Override
    public @NotNull ProgressWidget getWidget()
    {
        return pw;
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
        ProgressIndicatorLayoutConfiguration that = (ProgressIndicatorLayoutConfiguration) o1;
        return pw == that.pw && size == that.size && o == that.o;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(pw, size, o);
    }

    @Override
    public @NotNull String toString()
    {
        return pw + " " + size + " " + o;
    }
}
