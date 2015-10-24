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

import org.violetlib.jnr.aqua.AquaUIPainter.GradientWidget;
import org.violetlib.jnr.aqua.AquaUIPainter.State;

/**
	A configuration for painting a gradient.
*/

public class GradientConfiguration
	extends GradientLayoutConfiguration
	implements Configuration
{
	private final @NotNull GradientWidget gw;
	private final @NotNull State state;

	public GradientConfiguration(@NotNull GradientWidget gw, @NotNull State state)
	{
		this.gw = gw;
		this.state = state;
	}

	public @NotNull GradientWidget getWidget()
	{
		return gw;
	}

	public @NotNull State getState()
	{
		return state;
	}

	@Override
	public boolean equals(@Nullable Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		GradientConfiguration that = (GradientConfiguration) o;
		return gw == that.gw && state == that.state;
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(gw, state);
	}

	@Override
	public @NotNull String toString()
	{
		return gw + " " + state;
	}
}
