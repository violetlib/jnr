/*
 * Copyright (c) 2015 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.impl;

import java.awt.Graphics2D;

import org.jetbrains.annotations.*;

/**
	A painter extension is a way of extending the behavior of a native painter using AWT graphics.
*/

public interface PainterExtension
{
	/**
		Draw as needed into the graphics context.

		@param g The graphics context.
		@param width The width of the region to draw into, in graphics coordinates (device independent).
		@param height The height of the region to draw into, in graphics coordinates (device independent).
	*/

	void paint(@NotNull Graphics2D g, float width, float height);
}
