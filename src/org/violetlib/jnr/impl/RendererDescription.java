/*
 * Copyright (c) 2015 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.impl;

import java.awt.geom.Rectangle2D;

import org.jetbrains.annotations.*;

/**
	Instances of this class provide information about individual native renderers.

	All dimensions are specified in device independent pixels, sometimes called points. Dimensions may be fractional. As
	current displays support scale factors of 1 or 2, the dimensions are probably in half pixel units.

	When a widget is rendered, it is rendered into a region defined by the desired layout. This region is defined by the
	UI, so it should be the same for every implementation of the renderer. Native renderers render into a raster buffer.
	The relationship between the raster buffer and the target region may be different for different renderers. This
	class captures the data that expresses that relationship.

	A native renderer may require a raster buffer to have a specific size or a size that is larger than the target region.
	There are several possible reasons for these requirements. Some native renderers use only a portion of the provided
	raster buffer. If the raster buffer does not include this extra space, the rendering may be clipped. Some native
	renderers use the size of the raster buffer to determine the rendering. For example, a renderer that scales an image
	to the specified raster buffer size will produce a distorted rendering if given a raster buffer of the wrong size.
	This class specifies the required raster buffer size and its relation to the target region by a mapping from target
	region bounds to raster bounds. The raster bounds capture the required raster buffer size and any translation needed
	to align the rendering with the target location.

	Native renderers produce different renderings based on the display scale factor. Sometimes these differences are
	pronounced, not simply a matter of a single image that is scaled appropriately. For this reason, this class supports
	different mappings for different scale factors.

	@see RasterDescription
*/

public interface RendererDescription
{
	/**
		Determine the required raster size and origin to properly render into the specified region of the graphics
		coordinate system.

		@param target The region of the graphics coordinate system that is the target of the rendering.
		@param scaleFactor The display scale factor, which determines the actual size of the raster buffer that will be
			given to the native renderer.
		@return a raster description that specifies the required size of the raster buffer (in device independent pixels)
			and the translation needed to align the rendering properly with the specified target region.
	*/

	@NotNull RasterDescription getRasterBounds(@NotNull Rectangle2D target, int scaleFactor);

	/**
		Indicate whether the renderer has no offset or raster size adjustment.
		@return true if and only if the renderer as no offset or raster size adjustment.
	*/

	boolean isTrivial();
}
