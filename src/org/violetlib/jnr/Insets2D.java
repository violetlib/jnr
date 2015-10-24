/*
 * Copyright (c) 2015 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr;

/**
	A generalization of insets using floating point attributes.
*/

public class Insets2D
{
	private final float top;
	private final float left;
	private final float bottom;
	private final float right;

	public Insets2D(float top, float left, float bottom, float right)
	{
		this.top = top;
		this.left = left;
		this.bottom = bottom;
		this.right = right;
	}

	public float getTop()
	{
		return top;
	}

	public float getLeft()
	{
		return left;
	}

	public float getBottom()
	{
		return bottom;
	}

	public float getRight()
	{
		return right;
	}
}
