/*
 * Copyright (c) 2016 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.impl;

import java.awt.Image;
import java.awt.geom.Rectangle2D;

import org.jetbrains.annotations.*;

/**
	Information for debugging a segmented control renderer.
*/

public class SegmentedRendererDebugInfo
{
	protected final @Nullable Image fullImage;
	protected final @Nullable Rectangle2D controlBounds;
	protected final @Nullable Rectangle2D[] segmentBounds;

	public SegmentedRendererDebugInfo(@Nullable Image fullImage,
																		@Nullable Rectangle2D controlBounds,
																		@Nullable Rectangle2D[] segmentBounds)
	{
		this.fullImage = fullImage;
		this.controlBounds = controlBounds;
		this.segmentBounds = segmentBounds;
	}

	/**
		If the renderer subsets a larger image, return the larger image.
		@return the image, or null if none.
	*/

	public @Nullable Image getFullImage()
	{
		return fullImage;
	}

	/**
		Return the actual bounds of the segmented control.
		@return the bounds, or null if not known.
	*/

	public @Nullable Rectangle2D getControlBounds()
	{
		return controlBounds;
	}

	/**
		Return the bounds of the individual segments.
	*/

	public @Nullable Rectangle2D[] getSegmentBounds()
	{
		return segmentBounds;
	}
}
