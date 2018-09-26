/*
 * Copyright (c) 2015-2018 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.aqua.impl;

import org.violetlib.jnr.aqua.Configuration;
import org.violetlib.jnr.impl.CachingRendererPainter;
import org.violetlib.jnr.impl.ImageCache;
import org.violetlib.jnr.impl.Renderer;
import org.violetlib.vappearances.VAppearance;

import org.jetbrains.annotations.*;

/**
	A painter that uses a renderer and caches the rendered image. The cache key is based on the renderer configuration.
*/

public class AquaRenderedPainter
	extends CachingRendererPainter
{
	private final @NotNull Configuration g;
	private final @NotNull VAppearance appearance;

	/**
		Create a widget painter based on a renderer.

		@param g The widget configuration, which may be used to cache the rendered image.
		@param r The renderer used to paint the widget.
		@param width The width of the rendering, in device independent pixels.
		@param height The height of the rendering, in device independent pixels.
	*/

	public AquaRenderedPainter(@NotNull Configuration g,
														 @NotNull VAppearance appearance,
														 @NotNull Renderer r,
														 float width,
														 float height)
	{
		super(r, width, height);

		this.g = g;
		this.appearance = appearance;
	}

	@Override
	protected @Nullable ImageCache.PixelsKey createKey(int scaleFactor, int rasterWidth, int rasterHeight)
	{
		return new AquaPixelsKey(scaleFactor, rasterWidth, rasterHeight, g, appearance);
	}
}
