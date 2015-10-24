/*
 * Copyright (c) 2015 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.impl;

import java.util.Arrays;

import org.jetbrains.annotations.*;

/**
	A renderer that vertically flips the effect of a base raster. Not thread safe.
*/

public class FlipVerticalRenderer
	implements BasicRenderer
{
	private final @NotNull BasicRenderer r;

	private static int[] temp;

	/**
		Create a renderer that will invoke the specified renderer on a temporary raster and then copy its output into the
		original raster by flipping it vertically.

		@param r The renderer to invoke.
	*/

	public FlipVerticalRenderer(@NotNull BasicRenderer r)
	{
		this.r = r;
	}

	@Override
	public void render(@NotNull int[] data, int rw, int rh, float w, float h)
	{
		int requiredSize = rw * rh;
		if (requiredSize > 0) {
			if (temp == null || temp.length < requiredSize) {
				temp = new int[requiredSize];
			} else {
				Arrays.fill(temp, 0);
			}
			r.render(temp, rw, rh, w, h);
			for (int rowOffset = 0; rowOffset < rh; rowOffset++) {
				int row = rh - rowOffset - 1;
				if (row >= 0 && row < rh) {
					for (int colOffset = 0; colOffset < rw; colOffset++) {
						int col = colOffset;
						if (col >= 0 && col < rw) {
							int pixel = temp[rowOffset*rw+colOffset];
							int alpha = (pixel >> 24) & 0xFF;
							if (alpha != 0) {
								if (alpha != 0xFF) {
									pixel = combine(data[row * rw + col], pixel);
								}
								data[row * rw + col] = pixel;
							}
						}
					}
				}
			}
		}
	}

	private static int combine(int oldPixel, int newPixel)
	{
		int oldAlpha = (oldPixel >> 24) & 0xFF;
		int oldRed = (oldPixel >> 16) & 0xFF;
		int oldGreen = (oldPixel >> 8) & 0xFF;
		int oldBlue = (oldPixel >> 0) & 0xFF;
		int newAlpha = (newPixel >> 24) & 0xFF;
		int newRed = (newPixel >> 16) & 0xFF;
		int newGreen = (newPixel >> 8) & 0xFF;
		int newBlue = (newPixel >> 0) & 0xFF;
		int f = 255 - newAlpha;
		int red = (newRed + ((oldRed * f) >> 8)) & 0xFF;
		int green = (newGreen + ((oldGreen * f) >> 8)) & 0xFF;
		int blue = (newBlue + ((oldBlue * f) >> 8)) & 0xFF;
		int alpha =  ((255 * newAlpha + oldAlpha * f) / 255) & 0xFF;
		int result = (alpha << 24) + (red << 16) + (green << 8) + blue;
		return result;
	}
}
