/*
 * Copyright (c) 2015 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.aqua;

import java.util.Objects;

import org.jetbrains.annotations.*;

import org.violetlib.jnr.aqua.AquaUIPainter.State;

/**
	A configuration for a tool bar item well. For internal use. A tool bar item well background is painted only when the
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
		// In Yosemite, there are only two cases: active and inactive.

		this.state = state == State.ACTIVE ? State.ACTIVE : State.INACTIVE;
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
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
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
