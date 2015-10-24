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
	A render that does nothing.
*/

public class EmptyRenderer
	implements BasicRenderer
{
	@Override
	public void render(@NotNull int[] data, int rw, int rh, float w, float h)
	{
	}
}
