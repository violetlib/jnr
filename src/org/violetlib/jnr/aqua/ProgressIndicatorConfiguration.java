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

import org.violetlib.jnr.aqua.AquaUIPainter.Orientation;
import org.violetlib.jnr.aqua.AquaUIPainter.ProgressWidget;
import org.violetlib.jnr.aqua.AquaUIPainter.Size;
import org.violetlib.jnr.aqua.AquaUIPainter.State;
import org.violetlib.jnr.aqua.AquaUIPainter.UILayoutDirection;
import org.violetlib.jnr.impl.JNRUtils;

/**
	A configuration for a determinate progress indicator.
*/

public class ProgressIndicatorConfiguration
	extends ProgressIndicatorLayoutConfiguration
	implements Configuration
{
	private final @NotNull State state;
	private final double value;
	private final @NotNull UILayoutDirection ld;

	public ProgressIndicatorConfiguration(@NotNull ProgressWidget pw,
																				@NotNull Size sz,
																				@NotNull State state,
																				@NotNull Orientation o,
																				double value,
																				@NotNull UILayoutDirection ld)
	{
		super(pw, sz, o);

		this.state = state;
		this.value = value;
		this.ld = ld;
	}

	public ProgressIndicatorConfiguration(@NotNull ProgressIndicatorLayoutConfiguration g,
																				@NotNull State state,
																				double value,
																				@NotNull UILayoutDirection ld)
	{
		super(g);

		this.state = state;
		this.value = value;
		this.ld = ld;
	}

	public @NotNull State getState()
	{
		return state;
	}

	public double getValue()
	{
		return value;
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
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;
		ProgressIndicatorConfiguration that = (ProgressIndicatorConfiguration) o;
		return state == that.state && value == that.value && this.ld == that.ld;
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(super.hashCode(), state, value, ld);
	}

	@Override
	public @NotNull String toString()
	{
		String lds = ld == UILayoutDirection.RIGHT_TO_LEFT ? " RTL" : "";
		return super.toString() + " " + state + " " + JNRUtils.format2(value) + lds;
	}
}
