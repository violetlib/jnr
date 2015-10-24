/*
 * Copyright (c) 2015 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.impl;

import org.jetbrains.annotations.*;

import org.violetlib.jnr.Insetter;
import org.violetlib.jnr.LayoutInfo;

/**
	Convenience methods for creating insetters.
*/

public class Insetters
{
	/**
		Create insets with fixed values.

		@param top The top inset.
		@param left The left inset.
		@param bottom The bottom inset.
		@param right The right inset.
		@return the specified insets.
		@throws IllegalArgumentException if any parameter is negative.
	*/

	public static @NotNull Insetter createFixed(float top, float left, float bottom, float right)
	{
		return new CombinedInsetter(new FixedInsetter1(left, right), new FixedInsetter1(top, bottom));
	}

	/**
		Create insets with fixed values and optionally a fixed component width and/or height.

		@param top The top inset.
		@param left The left inset.
		@param bottom The bottom inset.
		@param right The right inset.
		@return the specified insets.
		@throws IllegalArgumentException if any parameter is negative.
	*/

	public static @NotNull Insetter createFixed(float top, float left, float bottom, float right, @Nullable LayoutInfo layoutInfo)
	{
		Insetter1 horizontal = null;
		Insetter1 vertical = null;

		if (layoutInfo != null) {
			float fixedComponentWidth = layoutInfo.getFixedVisualWidth();
			if (fixedComponentWidth > 0) {
				horizontal = new RigidInsetter1(fixedComponentWidth - (left + right), fixedComponentWidth, left);
			}
			float fixedComponentHeight = layoutInfo.getFixedVisualHeight();
			if (fixedComponentHeight > 0) {
				vertical = new RigidInsetter1(fixedComponentHeight - (top + bottom), fixedComponentHeight, top);
			}
		}

		if (horizontal == null) {
			horizontal = new FixedInsetter1(left, right);
		}

		if (vertical == null) {
			vertical = new FixedInsetter1(top, bottom);
		}

		return new CombinedInsetter(horizontal, vertical);
	}





	/**
		Create insets for a region with a fixed width positioned at a fixed offset from the left edge.

		@param regionWidth The fixed region width.
		@param left The left inset.
		@param top The top inset.
		@param bottom The bottom inset.
		@return the specified insets.
		@throws IllegalArgumentException if any parameter is negative.
	*/

	public static @NotNull Insetter createLeftAligned(float regionWidth, float left, float top, float bottom)
	{
		return new CombinedInsetter(FloatingInsetter1.createLeftTopAligned(regionWidth, left), new FixedInsetter1(top, bottom));
	}

	/**
		Create insets for a region with a fixed width positioned at a fixed offset from the right edge.

		@param regionWidth The fixed region width.
		@param right The right inset.
		@param top The top inset.
		@param bottom The bottom inset.
		@return the specified insets.
		@throws IllegalArgumentException if any parameter is negative.
	*/

	public static @NotNull Insetter createRightAligned(float regionWidth, float right, float top, float bottom)
	{
		return new CombinedInsetter(FloatingInsetter1.createRightBottomAligned(regionWidth, right), new FixedInsetter1(top, bottom));
	}

	/**
		Create insets for a region with a fixed height positioned at a fixed offset from the top edge.

		@param regionHeight The fixed region height.
		@param top The top inset.
		@param left The left inset.
		@param right The right inset.
		@return the specified insets.
		@throws IllegalArgumentException if any parameter is negative.
	*/

	public static @NotNull Insetter createTopAligned(float regionHeight, float top, float left, float right)
	{
		return new CombinedInsetter(new FixedInsetter1(left, right), FloatingInsetter1.createLeftTopAligned(regionHeight, top));
	}

	/**
		Create insets for a region with a fixed height positioned at a fixed offset from the bottom edge.

		@param regionHeight The fixed region height.
		@param bottom The bottom inset.
		@param left The left inset.
		@param right The right inset.
		@return the specified insets.
		@throws IllegalArgumentException if any parameter is negative.
	*/

	public static @NotNull Insetter createBottomAligned(float regionHeight, float bottom, float left, float right)
	{
		return new CombinedInsetter(new FixedInsetter1(left, right), FloatingInsetter1.createRightBottomAligned(regionHeight, bottom));
	}
}
