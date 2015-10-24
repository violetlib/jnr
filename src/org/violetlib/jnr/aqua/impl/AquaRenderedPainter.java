/*
 * Copyright (c) 2015 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.aqua.impl;

import org.jetbrains.annotations.*;

import org.violetlib.jnr.aqua.Configuration;
import org.violetlib.jnr.impl.CachingRendererPainter;
import org.violetlib.jnr.impl.ImageCache;
import org.violetlib.jnr.impl.Renderer;

/**
	A painter that uses a renderer and caches the rendered image. The cache key is based on the renderer configuration.
*/

public class AquaRenderedPainter
	extends CachingRendererPainter
{
	private final @NotNull Configuration g;

	/**
		Create a widget painter based on a renderer.

		@param g The widget configuration, which may be used to cache the rendered image.
		@param r The renderer used to paint the widget.
		@param width The width of the rendering, in device independent pixels.
		@param height The height of the rendering, in device independent pixels.
	*/

	public AquaRenderedPainter(@NotNull Configuration g,
														 @NotNull Renderer r,
														 float width,
														 float height)
	{
		super(r, width, height);

		this.g = g;
	}

	@Override
	protected @Nullable ImageCache.PixelsKey createKey(int scaleFactor, int rasterWidth, int rasterHeight)
	{
		return new AquaPixelsKey(scaleFactor, rasterWidth, rasterHeight, g);
	}
}
