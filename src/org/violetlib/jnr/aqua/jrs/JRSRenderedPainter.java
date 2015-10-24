/*
 * Copyright (c) 2015 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.aqua.jrs;

import org.jetbrains.annotations.*;

import apple.laf.JRSUIState;
import org.violetlib.jnr.impl.CachingRendererPainter;
import org.violetlib.jnr.impl.ImageCache;
import org.violetlib.jnr.impl.Renderer;

/**
	A painter that uses a native painter from the Aqua look and feel.
*/

public class JRSRenderedPainter
	extends CachingRendererPainter
{
	private final @NotNull JRSUIState state;

	public JRSRenderedPainter(@NotNull JRSUIState state,
														@NotNull Renderer r,
														float width,
														float height)
	{
		super(r, width, height);

		this.state = state;
	}

	@Override
	protected @Nullable ImageCache.PixelsKey createKey(int scaleFactor, int rasterWidth, int rasterHeight)
	{
		return new JRSPixelsKey(scaleFactor, rasterWidth, rasterHeight, state);
	}
}
