/*
 * Copyright (c) 2015 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr;

/**
	Layout information for a widget describes fixed and/or minimum sizes for the widget rendering. This information may be
	platform UI dependent but should not depend upon the native rendering implementation. All dimensions are specified in
	device independent pixels.
*/

public interface LayoutInfo
{
	/**
		Return the width of the visual rendering of the widget, if the width is fixed.

		@return the width, or 0 if the width is not fixed.
	*/

	float getFixedVisualWidth();

	/**
		Return the height of the visual rendering of the widget, if the height is fixed.

		@return the height, or 0 if the height is not fixed.
	*/

	float getFixedVisualHeight();

	/**
		Return the minimum width of the visual rendering of the widget. If the visual rendering has a fixed width, then
		that width is returned.

		@return the minimum visual width.
	*/

	float getMinimumVisualWidth();

	/**
		Return the minimum height of the visual rendering of the widget. If the visual rendering has a fixed height, then
		that height is returned.

		@return the minimum visual height.
	*/

	float getMinimumVisualHeight();
}
