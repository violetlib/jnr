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
  A configuration for a group box.
*/

public class GroupBoxConfiguration
  extends GroupBoxLayoutConfiguration
  implements Configuration
{
    private final @NotNull State state;
    private final boolean isFrameOnly;

    public GroupBoxConfiguration(@NotNull State state, boolean isFrameOnly)
    {
        this.state = state;
        this.isFrameOnly = isFrameOnly;
    }

    public @NotNull State getState()
    {
        return state;
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
        GroupBoxConfiguration that = (GroupBoxConfiguration) o;
        return state == that.state && isFrameOnly == that.isFrameOnly;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(state, isFrameOnly);
    }

    @Override
    public @NotNull String toString()
    {
        String frs = isFrameOnly ? " frame only" : "";
        return super.toString() + frs + " " + state;
    }
}
