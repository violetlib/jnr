/*
 * Copyright (c) 2015 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.impl;

/**
	A dynamic insetter defines a region of fixed size whose center position is computed by a subclass.
*/

public abstract class DynamicInsetter1
	extends NonInvertibleInsetterBase
	implements Insetter1
{
	private final float regionSize;
	private final float alignmentRegionSize;

	/**
		Create an insetter with a fixed region size. The region will be centered about the dynamically determined center
		position.

		@param regionSize The region size.
	*/

	protected DynamicInsetter1(float regionSize)
	{
		this.regionSize = regionSize;
		this.alignmentRegionSize = regionSize;
	}

	// TBD: Determine why this option is useful. It corresponds to left/top alignment of a visual region with the
	// alignment region. I can see it being useful in the vertical case if the visual region is larger because it
	// includes a shadow.

	protected DynamicInsetter1(float regionSize, float alignmentRegionSize)
	{
		this.regionSize = regionSize;
		this.alignmentRegionSize = alignmentRegionSize;
	}

	@Override
	public float getRegionOrigin(float componentSize)
	{
		float pos = getCenterPosition(componentSize);
		return pos - alignmentRegionSize/2;
	}

	@Override
	public float getRegionSize(float componentSize)
	{
		return regionSize;
	}

	protected abstract float getCenterPosition(float componentSize);

	@Override
	public float getFixedRegionSize()
	{
		return regionSize;
	}
}
