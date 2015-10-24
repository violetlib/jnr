/*
 * Copyright (c) 2015 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.geom;

/**
 A bounds along one axis.
*/

public class LinearBounds
{
	private final double origin;
	private final double length;

	public LinearBounds(double origin, double length)
	{
		this.origin = origin;
		this.length = length;
	}

	public double getOrigin()
	{
		return origin;
	}

	public double getLength()
	{
		return length;
	}
}
