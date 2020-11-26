/*
 * Copyright (c) 2015-2020 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.aqua;

import java.util.Objects;

import org.violetlib.jnr.aqua.AquaUIPainter.ButtonWidget;
import org.violetlib.jnr.aqua.AquaUIPainter.Size;
import org.violetlib.jnr.aqua.AquaUIPainter.UILayoutDirection;

import org.jetbrains.annotations.*;

/**
  A layout configuration for a button.
*/

public class ButtonLayoutConfiguration
  extends LayoutConfiguration
{
    private final @NotNull ButtonWidget bw;
    private final @NotNull Size size;
    private final @NotNull UILayoutDirection ld;

    public ButtonLayoutConfiguration(@NotNull ButtonWidget bw, @NotNull Size size, @NotNull UILayoutDirection ld)
    {
        this.bw = bw;
        this.size = size;
        this.ld = ld;
    }

    @Override
    public @NotNull Object getWidget()
    {
        return bw;
    }

    public @NotNull ButtonWidget getButtonWidget()
    {
        return bw;
    }

    public @NotNull Size getSize()
    {
        return size;
    }

    public @NotNull UILayoutDirection getLayoutDirection()
    {
        return ld;
    }

    public boolean isLeftToRight()
    {
        return ld == UILayoutDirection.LEFT_TO_RIGHT;
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
        ButtonLayoutConfiguration that = (ButtonLayoutConfiguration) o;
        return bw == that.bw && size == that.size && ld == that.ld;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(bw, size, ld);
    }

    @Override
    public @NotNull String toString()
    {
        String lds = ld == UILayoutDirection.RIGHT_TO_LEFT ? " RTL" : "";
        return bw + " " + size + lds;
    }
}
