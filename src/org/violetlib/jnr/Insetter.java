/*
 * Copyright (c) 2015 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr;

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.geom.Dimension2D;
import java.awt.geom.Rectangle2D;

import org.jetbrains.annotations.*;

/**
	An insetter defines the relationship between the bounds of a component and the bounds of a region of the component.
	For example, a combo box could have an insetter relating the combo box bounds to the bounds of the text field. It
	could have a different insetter relating the combo box bounds to the bounds of the combo box button.

	<p>
	An insetter is a generalization of AWT insets, which define a simple mapping between component (outer) bounds and
	region (inner) bounds by adding or removing fixed amounts to or from each side.
	</p>

	<p>
	Unlike AWT insets, which only support fixed inset values, an insetter can support (inner) regions whose dimensions are
	fixed along one or both axes. For example, the button portion of a combo box probably has a fixed width, regardless of
	the width of the combo box. An insetter can represent this relationship by calculating the button region from the
	combo box bounds.
	</p>

	<p>
	Some, but not all insetters are invertible, which means that given the size of the (inner) region, the insetter can
	determine the size of the (outer) component. For example, an insetter for the text field region of a combo box
	probably can compute the size of the combo box given the size of the text field. This operation could be used to
	compute the preferred size of the combo box based on the expected text. In contrast, knowing the size of the combo box
	button would not be enough to determine the size of the entire combo box.
	</p>

	<p>
	Insets with fixed values, such as AWT insets, are trivially invertible. Other invertible insetters are possible in
	theory, but unlikely in practice.
	</p>
*/

public interface Insetter
{
	/**
		Map from (outer) component bounds to (inner) region bounds by subtracting the insets from the component bounds.

		@param bounds The component bounds.

		@return the bounds that result from applying the insets to the specified bounds.
	*/

	@NotNull Rectangle2D applyToBounds2D(@NotNull Rectangle2D bounds);

	/**
		Map from (outer) component bounds to (inner) region bounds by subtracting the insets from the component bounds.

		@param bounds The component bounds.

		@return the bounds that result from applying the insets to the component bounds. The resulting region bounds will
			not exceed the nominal region bounds (using fractional metrics), unless applying the insets would result in a
			negative width or height.
	*/

	@NotNull Rectangle applyToBounds(@NotNull Rectangle bounds);

	/**
		Map from (outer) component bounds to (inner) region bounds by subtracting the insets from the component bounds.
		The origin of the component is assumed to be (0, 0).

		@param width The width of the component.
		@param height The height of the component.

		@return the bounds that result from applying the insets to the specified bounds.
	*/

	@NotNull Rectangle2D apply2D(double width, double height);

	/**
		Map from (outer) component bounds to (inner) region bounds by subtracting the insets from the component bounds.
		The origin of the component is assumed to be (0, 0).

		@param width The width of the component.
		@param height The height of the component.

		@return the bounds that result from applying the insets to the component bounds. This operation is conservative
			with respect to the insets. The insets implied by the result will always be at least as large as the nominal
			insets implied by this insetter, unless applying the insets would result in a negative width or height.
	*/

	@NotNull Rectangle apply(int width, int height);

	/**
		Indicate whether this insetter is invertible. An invertible insetter can map a region size to the component size.
	*/

	boolean isInvertible();

	/**
		Compute a component size from a region size. This operation is valid only if the insetter is invertible.

		@param regionSize The size of the region.

		@return the size of a component containing the region with the specified size. Note that the returned size may
			exceed a fixed size of the component.

		@throws InsetterNotInvertibleException if this insetter is not invertible.
	*/

	@NotNull Dimension2D expand2D(@NotNull Dimension2D regionSize)
		throws InsetterNotInvertibleException;

	/**
		Compute a component size from a region size. This operation is valid only if the insetter is invertible.

		@param regionSize The size of the region.

		@return the size of a component containing the region with the specified size. Note that the returned size may
			exceed a fixed size of the component.

		@throws InsetterNotInvertibleException if this insetter is not invertible.
	*/

	@NotNull Dimension expand(@NotNull Dimension regionSize)
		throws InsetterNotInvertibleException;

	/**
		Return the insets as fixed insets.

		@return the fixed inset values, or null if the inset values are not fixed.
	*/

	@Nullable Insets2D asInsets2D();

	/**
		Return the insets as AWT insets. Only insets with fixed values can be represented as AWT insets. If the inset values
		are not integers, the next larger integer value is returned.

		@return the insets as AWT insets, or null if the inset values are not fixed.
	*/

	@Nullable Insets asInsets();
}
