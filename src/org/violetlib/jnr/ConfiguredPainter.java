/*
 * Copyright (c) 2015 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr;

/**
	This interface is supported by painters that provide information about layout sizes.
*/

public interface ConfiguredPainter
	extends Painter
{
	float getFixedWidth();

	float getFixedHeight();
}
