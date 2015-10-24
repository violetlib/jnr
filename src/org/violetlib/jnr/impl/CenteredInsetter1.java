/*
 * Copyright (c) 2015 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.impl;

import org.jetbrains.annotations.*;

/**
	A floating insetter defines a region of fixed size that is centered in the component, with an optional offset.
*/

public class CenteredInsetter1
	extends NonInvertibleInsetterBase
	implements Insetter1
{
	private final float regionSize;
	private final float alignmentRegionSize;
	private final float offset;

	/**
		Create a single dimension insets for a fixed size region that is centered in the component.

		@param regionSize The fixed region size.
		@throws IllegalArgumentException if {@code regionSize} is negative.
	*/

	public static @NotNull CenteredInsetter1 createCentered(float regionSize)
	{
		if (regionSize < 0) {
			throw new IllegalArgumentException("Invalid negative region size");
		}

		return new CenteredInsetter1(regionSize, regionSize, 0);
	}

	/**
		Create a single dimension insets for a fixed size region that is centered in the component.

		@param regionSize The fixed region size.
		@param offset An optional offset that is applied after centering.
		@throws IllegalArgumentException if {@code regionSize} is negative.
	*/

	public static @NotNull CenteredInsetter1 createCentered(float regionSize, float offset)
	{
		if (regionSize < 0) {
			throw new IllegalArgumentException("Invalid negative region size");
		}

		return new CenteredInsetter1(regionSize, regionSize, offset);
	}

	/**
		Create a single dimension insets for a fixed size region that is centered in the component.

		@param regionSize The fixed region size.
		@param alignmentRegionSize A virtual region size that is used to calculate the centered position.
		@param offset An optional offset that is applied after centering.
		@throws IllegalArgumentException if {@code regionSize} is negative.
	*/

	public static @NotNull CenteredInsetter1 createCentered(float regionSize, float alignmentRegionSize, float offset)
	{
		if (regionSize < 0) {
			throw new IllegalArgumentException("Invalid negative region size");
		}

		return new CenteredInsetter1(regionSize, alignmentRegionSize, offset);
	}

	private CenteredInsetter1(float regionSize, float alignmentRegionSize, float offset)
	{
		this.regionSize = regionSize;
		this.alignmentRegionSize = alignmentRegionSize;
		this.offset = offset;
	}

	@Override
	public float getRegionOrigin(float componentSize)
	{
		float inset = (componentSize - alignmentRegionSize) / 2;
		return inset + offset;
	}

	@Override
	public float getRegionSize(float componentSize)
	{
		return regionSize;
	}

	@Override
	public float getFixedRegionSize()
	{
		return regionSize;
	}
}
