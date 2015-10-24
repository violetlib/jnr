/*
 * Copyright (c) 2015 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.impl;

import java.awt.geom.Rectangle2D;
import java.util.Objects;

import org.jetbrains.annotations.*;

/**

*/

public final class MultiResolutionRendererDescription
	implements RendererDescription
{
	private final @NotNull RendererDescription rd1;
	private final @NotNull RendererDescription rd2;

	public MultiResolutionRendererDescription(@NotNull RendererDescription rd1, @NotNull RendererDescription rd2)
	{
		this.rd1 = rd1;
		this.rd2 = rd2;
	}

	@Override
	public boolean isTrivial()
	{
		return rd1.isTrivial() && rd2.isTrivial();
	}

	public @NotNull RendererDescription getDescription1()
	{
		return rd1;
	}

	public @NotNull RendererDescription getDescription2()
	{
		return rd2;
	}

	@Override
	public @NotNull RasterDescription getRasterBounds(@NotNull Rectangle2D target, int scaleFactor)
	{
		return scaleFactor == 1 ? rd1.getRasterBounds(target, scaleFactor) : rd2.getRasterBounds(target, scaleFactor);
	}

	@Override
	public boolean equals(@Nullable Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		MultiResolutionRendererDescription that = (MultiResolutionRendererDescription) o;
		return Objects.equals(rd1, that.rd1) && Objects.equals(rd2, that.rd2);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(rd1, rd2);
	}
}
