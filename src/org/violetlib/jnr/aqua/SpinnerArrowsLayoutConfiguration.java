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
import org.violetlib.jnr.aqua.AquaUIPainter.Size;

import java.util.Objects;

import static org.violetlib.jnr.aqua.AquaUIPainter.macOS26;

/**
  A layout configuration for spinner arrows.
*/

public class SpinnerArrowsLayoutConfiguration
  extends LayoutConfiguration
{
    private final @NotNull Size size;

    public SpinnerArrowsLayoutConfiguration(@NotNull Size size)
    {
        if (!AquaNativeRendering.isRaw()) {
            int version = AquaNativeRendering.getSystemRenderingVersion();
            if ((size == Size.EXTRA_LARGE || size == Size.LARGE) && version < macOS26) {
                size = Size.REGULAR;
            }
        }

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
