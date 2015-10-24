/*
 * Copyright (c) 2015 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.impl;

import org.violetlib.jnr.InsetterNotInvertibleException;

/**
	A pair of fixed insets.
*/

public class FixedInsetter1
	implements Insetter1
{
	private final float d1;
	private final float d2;

	/**
		Create an insets for one axis with fixed values. The values must not be negative.
		@param d1 The left or top inset.
		@param d2 The right or bottom inset.
	*/

	public FixedInsetter1(float d1, float d2)
	{
		if (d1 < 0 || d2 < 0) {
			throw new IllegalArgumentException("Invalid negative insets");
		}

		this.d1 = d1;
		this.d2 = d2;
	}

	@Override
	public float getRegionOrigin(float componentSize)
	{
		return d1;
	}

	@Override
	public float getRegionSize(float componentSize)
	{
		return componentSize - (d1 + d2);
	}

	@Override
	public boolean isInvertible()
	{
		return true;
	}

	@Override
	public float getComponentSize(float regionSize)
	throws InsetterNotInvertibleException
	{
		return regionSize + d1 + d2;
	}

	@Override
	public float getFixedInset1()
	{
		return d1;
	}

	@Override
	public float getFixedInset2()
	{
		return d2;
	}

	@Override
	public float getFixedRegionSize()
	{
		return 0;
	}
}
