/*
 * Copyright (c) 2015 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.eval;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import org.jetbrains.annotations.*;

/**
	Analyze an image that may be scaled for HiDPI. Return attributes measured in layout points.

	@see ImageAnalyzer
*/

public class ScaledImageAnalyzer
{
	private final float scale;
	private final @NotNull ImageAnalyzer analyzer;
	private final @NotNull Rectangle effectiveBounds;

	public ScaledImageAnalyzer(int scale, @NotNull BufferedImage b)
	{
		this.scale = scale;
		this.analyzer = new ImageAnalyzer(b);
		Rectangle r = analyzer.getEffectiveBounds();

		this.effectiveBounds = r != null ? r : new Rectangle(0, 0, 0, 0);
	}

	public float getX()
	{
		return effectiveBounds.x / scale;
	}

	public float getY()
	{
		return effectiveBounds.y / scale;
	}

	public float getWidth()
	{
		return effectiveBounds.width / scale;
	}

	public float getHeight()
	{
		return effectiveBounds.height / scale;
	}
}
