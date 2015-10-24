/*
 * Copyright (c) 2015 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr;

import javax.swing.plaf.UIResource;

/**
	A generalization of insets using floating point attributes. This class is used for defaults installed by a look and
	feel.
*/

public class Insets2DUIResource extends Insets2D implements UIResource
{
	public Insets2DUIResource(float top, float left, float bottom, float right)
	{
		super(top, left, bottom, right);
	}

	public Insets2DUIResource(Insets2D n)
	{
		super(n.getTop(), n.getLeft(), n.getBottom(), n.getRight());
	}
}
