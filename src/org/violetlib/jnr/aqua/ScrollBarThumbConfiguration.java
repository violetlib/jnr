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

/**
	A configuration for a scroll bar thumb.
*/

public class ScrollBarThumbConfiguration
	extends ScrollBarConfiguration
{
	private final int value;

	public ScrollBarThumbConfiguration(@NotNull ScrollBarConfiguration g, int value)
	{
		super(g);

		this.value = value;
	}

	public int getValue()
	{
		return value;
	}

	@Override
	public boolean equals(@Nullable Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;
		ScrollBarThumbConfiguration that = (ScrollBarThumbConfiguration) o;
		return value == that.value;
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(super.hashCode(), value);
	}

	@Override
	public @NotNull String toString()
	{
		return super.toString() + " " + value;
	}
}
