/*
 * Copyright (c) 2015 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr;

import java.awt.Dimension;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;

import org.jetbrains.annotations.*;

/**
	This interface defines additional methods that may be supported by a painter for sliders.
*/

public interface SliderPainter
	extends ConfiguredPainter
{
	/**
		Return the bounds of the thumb area based on the configured parameters.
	*/

	@NotNull Rectangle2D getThumbBounds();

	/**
		Return the outline of the thumb based on the configured parameters.
	*/

	@NotNull Shape getThumbOutline();

	/**
	  Map a mouse coordinate to a slider thumb position. This method relies on the previously configured widget size.

	  @param x The x coordinate relative to the configured bounds.
	  @param y The y coordinate relative to the configured bounds.

	  @return the thumb position as a fraction of the slider range, if in the range 0 to 1 (inclusive), or a value less
				than 0 if the coordinate is outside the slider range in the area corresponding to low values, or a value greater
				than 1 if the coordinate is outside the slider range in the area corresponding to high values.
	*/

	double getThumbPosition(int x, int y);

	/**
		Return the recommended bounds for a label corresponding to a given thumb position, based on the configured
		parameters.

		@param value The thumb position represented by the label, expressed as a fraction of the slider range, in the range
			0 to 1 (inclusive).
		@param size The intended size of the label.
		@return the recommended bounds for the label.
	*/

	@NotNull Rectangle2D getLabelBounds(double value, @NotNull Dimension size);
}
