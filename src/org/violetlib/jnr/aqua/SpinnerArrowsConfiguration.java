/*
 * Copyright (c) 2015 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.aqua;

import java.util.Objects;

import org.violetlib.jnr.aqua.AquaUIPainter.Size;
import org.violetlib.jnr.aqua.AquaUIPainter.State;

import org.jetbrains.annotations.*;

/**
  A configuration for spinner arrows.
*/

public class SpinnerArrowsConfiguration
  extends SpinnerArrowsLayoutConfiguration
  implements Configuration
{
    private final @NotNull State state;
    private final boolean isFocused;
    private final boolean isPressedTop;

    public SpinnerArrowsConfiguration(@NotNull Size size,
                                      @NotNull State state,
                                      boolean isFocused,
                                      boolean isPressedTop)
    {
        super(size);
        this.state = state;
        this.isFocused = isFocused;
        this.isPressedTop = isPressedTop;
    }

    public @NotNull State getState()
    {
        return state;
    }

    public boolean isFocused()
    {
        return isFocused;
    }

    public boolean isPressedTop()
    {
        return isPressedTop;
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
        if (!super.equals(o)) {
            return false;
        }
        SpinnerArrowsConfiguration that = (SpinnerArrowsConfiguration) o;
        return state == that.state && isFocused == that.isFocused && isPressedTop == that.isPressedTop;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(super.hashCode(), state, isFocused, isPressedTop);
    }

    @Override
    public @NotNull String toString()
    {
        String fs = isFocused ? " focused" : "";
        String ts = state == State.PRESSED ? (isPressedTop ? "-Top" : "-Bottom") : "";
        return super.toString() + " " + state + ts + fs;
    }
}
