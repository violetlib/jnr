/*
 * Copyright (c) 2015-2025 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.aqua;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.violetlib.jnr.aqua.AquaUIPainter.State;

import java.util.Objects;

/**
  A configuration for a toolbar item well. For internal use. A toolbar item well background is painted only when the
  item is selected. When the item is not selected, no background is painted.
*/

public class ToolBarItemWellConfiguration
  extends ToolBarItemWellLayoutConfiguration
  implements Configuration
{
    private final @NotNull State state;
    private final boolean isFrameOnly;

    public ToolBarItemWellConfiguration(@NotNull State state, boolean isFrameOnly)
    {
        if (!AquaNativeRendering.isRaw()) {
            // In Yosemite, there are only two cases: active and inactive.
            state = state == State.ACTIVE ? State.ACTIVE : State.INACTIVE;
        }

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
        ToolBarItemWellConfiguration that = (ToolBarItemWellConfiguration) o;
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
