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
import org.violetlib.jnr.impl.ImageCache;

/**
	The key used by the AquaNativePainter to cache images.
*/

public class AquaPixelsKey
	implements ImageCache.PixelsKey
{
	private final int pixelCount;
	private final int hash;

	private final int scaleFactor;
	private final int w;
	private final int h;
	private final @NotNull Configuration g;

	public AquaPixelsKey(int scaleFactor, int w, int h, @NotNull Configuration g)
	{
		this.pixelCount = w * h;
		this.scaleFactor = scaleFactor;
		this.w = w;
		this.h = h;
		this.g = g;
		this.hash = hash();
	}

	@Override
	public int getPixelCount()
	{
		return pixelCount;
	}

	private int hash()
	{
		int hash = scaleFactor;
		hash = 31 * hash + w;
		hash = 31 * hash + h;
		hash = 31 * hash + g.hashCode();
		return hash;
	}

	@Override
	public int hashCode()
	{
		return hash;
	}

	@Override
	public boolean equals(@Nullable Object obj)
	{
		if (obj != null && obj.getClass() == AquaPixelsKey.class) {
			AquaPixelsKey that = (AquaPixelsKey) obj;
			return scaleFactor == that.scaleFactor && w == that.w && h == that.h && g.equals(that.g);
		}
		return false;
	}
}
