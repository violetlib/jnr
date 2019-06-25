/*
 * Copyright (c) 2015 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.aqua;

import java.util.Objects;

import org.violetlib.jnr.aqua.AquaUIPainter.State;

import org.jetbrains.annotations.*;

/**
  A configuration for a list box.
*/

public class ListBoxConfiguration
  extends ListBoxLayoutConfiguration
  implements Configuration
{
    private final @NotNull State state;
    private final boolean isFocused;
    private final boolean isFrameOnly;

    public ListBoxConfiguration(@NotNull State state, boolean isFocused, boolean isFrameOnly)
    {
        this.state = state;
        this.isFocused = isFocused;
        this.isFrameOnly = isFrameOnly;
    }

    public @NotNull State getState()
    {
        return state;
    }

    public boolean isFocused()
    {
        return isFocused;
    }

    public boolean isFrameOnly()
    {
        return isFrameOnly;
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
        ListBoxConfiguration that = (ListBoxConfiguration) o;
        return state == that.state && isFocused == that.isFocused && isFrameOnly == that.isFrameOnly;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(state, isFocused, isFrameOnly);
    }

    @Override
    public @NotNull String toString()
    {
        String fs = isFocused ? " focused" : "";
        String frs = isFrameOnly ? " frame only" : "";
        return super.toString() + frs + " " + state + fs;
    }
}
