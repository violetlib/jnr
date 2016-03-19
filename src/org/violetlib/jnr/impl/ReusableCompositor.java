/*
 * Copyright (c) 2015-2016 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.impl;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.util.Arrays;

import org.jetbrains.annotations.*;

/**
	A compositor that renders into an INT_ARGB_PRE raster from various sources. Multiple renderings can be composed into
	the same raster. The raster can be drawn to a graphics context or returned as an image.

	<p>
	The compositor has an integer scale factor, used to support high resolution displays. The scale factor is available to
	the sources so that they can produce a rendering at the appropriate scale; it is not used for drawing the raster or
	returning the raster as an image.
	</p>

	<p>
	The compositor can be reused without reallocating the raster. Reusing a compositor is permitted except when the raster
	image is in use. The raster size is configured when the compositor is first used or reused.
	</p>

	<p>
	This class is not thread safe.
	</p>
*/

public class ReusableCompositor
{
	// TBD: would it be faster to turn everything into an Image and use graphics operations?

	private @Nullable int[] data;	// the actual raster buffer, reallocated as needed to contain at least the required number of pixels.
		// May be null if the raster has zero size.

	private @Nullable BufferedImage b;	// an image using the raster buffer, created on demand and released when the raster buffer is replaced.
		// May be null if the raster has zero size.

	private boolean isConfigured;	// true if the raster dimensions have been changed but the raster has not been updated
	private boolean isEmpty;			// true if the raster is known to have no content (allows compose to be faster)

	private int rasterWidth;			// the current raster width (may be inconsistent with the raster buffer until it has been configured)
	private int rasterHeight;			// the current raster height (may be inconsistent with the raster buffer until it has been configured)
	private int scaleFactor;			// the scale factor

	/**
		This interface permits additional image sources to be supported. They must be implemented using already supported
		sources.
	*/

	public interface PixelSource
	{
		/**
			Render pixels into the specified compositor.

			@param compositor The compositor.
		*/

		void composeTo(@NotNull ReusableCompositor compositor);
	}

	/**
		Create a reusable compositor. The raster width and height are zero.
	*/

	public ReusableCompositor()
	{
	}

	/**
		Return the INT_ARGB_PRE color model.
	*/

	public static @NotNull ColorModel getColorModel()
	{
		return BasicImageSupport.getColorModel();
	}

	/**
		Return the width of the raster, in pixels.
	*/

	public int getRasterWidth()
	{
		return rasterWidth;
	}

	/**
		Return the height of the raster, in pixels.
	*/

	public int getRasterHeight()
	{
		return rasterHeight;
	}

	/**
		Return the scale factor, which relates raster pixels to device independent pixels.
	*/

	public int getScaleFactor()
	{
		return scaleFactor;
	}

	/**
		Return the width of the raster, in device independent pixels (according to the scale factor).
	*/

	public float getWidth()
	{
		return ((float) rasterWidth) / scaleFactor;
	}

	/**
		Return the height of the raster, in device independent pixels (according to the scale factor).
	*/

	public float getHeight()
	{
		return ((float) rasterHeight) / scaleFactor;
	}

	/**
		Resize and clear the raster in preparation for rendering. The actual clearing or reallocation of the raster may be
		deferred until the raster is needed.

		@param rasterWidth The new raster width.
		@param rasterHeight The new raster height.
		@param scaleFactor The scale factor that relates raster pixels to device independent pixels.
	*/

	public void reset(int rasterWidth, int rasterHeight, int scaleFactor)
	{
		if (rasterWidth < 0 || rasterHeight < 0) {
			throw new IllegalArgumentException("Invalid negative raster width and/or height");
		}

		if (scaleFactor < 1 || scaleFactor > 8) {
			throw new IllegalArgumentException("Invalid or unsupported scale factor");
		}

		this.rasterWidth = rasterWidth;
		this.rasterHeight = rasterHeight;
		this.scaleFactor = scaleFactor;
		isEmpty = true;
	}

	/**
		Ensure that the raster buffer has been configured to the proper size and cleared if needed. This method supports
		lazy configuration of the raster buffer.
	*/

	protected void ensureConfigured()
	{
		if (!isConfigured) {
			isConfigured = true;
			int requiredSize = rasterWidth * rasterHeight;
			if (requiredSize > 0) {
				if (data == null || data.length < requiredSize) {
					data = new int[requiredSize];
					b = null;
				} else {
					Arrays.fill(data, 0);
				}
			}
		}
	}

	/**
		Render into the raster after resizing it and clearing it. This is a convenience method.

		@param r The renderer.
		@param rasterWidth The new raster width.
		@param rasterHeight The new raster height.
		@param scaleFactor The scale factor that relates raster pixels to device independent pixels.
	*/

	public void render(@NotNull BasicRenderer r, int rasterWidth, int rasterHeight, int scaleFactor)
	{
		reset(rasterWidth, rasterHeight, scaleFactor);
		ensureConfigured();
		if (data != null) {
			float rw = ((float) rasterWidth) / scaleFactor;
			float rh = ((float) rasterHeight) / scaleFactor;
			r.render(data, rasterWidth, rasterHeight, rw, rh);
			isEmpty = false;
		}
	}

	/**
		Render into the raster, composing with existing contents.

		@param o The source of the pixels to compose with the existing contents. This object may be any of the standard
			sources ({@link BasicRenderer}, {@link PainterExtension}, or another {@link ReusableCompositor}), or an object
			that supports the {@link PixelSource} interface.
	*/

	public void compose(@NotNull Object o)
	{
		if (o instanceof BasicRenderer) {
			BasicRenderer br = (BasicRenderer) o;
			composeRenderer(br);
		} else if (o instanceof PainterExtension) {
			PainterExtension px = (PainterExtension) o;
			composePainter(px, 0, 0);
		} else if (o instanceof ReusableCompositor) {
			ReusableCompositor rc = (ReusableCompositor) o;
			composeFrom(rc, 0, 0, rasterWidth, rasterHeight);
		} else if (o instanceof PixelSource) {
			PixelSource sr = (PixelSource) o;
			sr.composeTo(this);
		} else {
			throw new UnsupportedOperationException("Unsupported pixel source");
		}
	}

	/**
		Render into the raster, composing with existing contents.

		@param r The renderer that provides the pixels.
	*/

	public void composeRenderer(@NotNull BasicRenderer r)
	{
		ensureConfigured();
		if (data != null) {
			if (isEmpty) {
				float rw = ((float) rasterWidth) / scaleFactor;
				float rh = ((float) rasterHeight) / scaleFactor;
				r.render(data, rasterWidth, rasterHeight, rw, rh);
				isEmpty = false;
			} else {
				composeRenderer(r, 0, 0, rasterWidth, rasterHeight);
			}
		}
	}

	/**
		Render into a region of the raster, composing with existing contents.

		@param r The renderer.
		@param dx The X origin of the raster region.
		@param dy The Y origin of the raster region.
		@param dw The width of the raster region.
		@param dh The height of the raster region.
	*/

	public void composeRenderer(@NotNull BasicRenderer r, int dx, int dy, int dw, int dh)
	{
		if (dw > 0 && dh > 0) {
			ReusableCompositor temp = new ReusableCompositor();
			temp.render(r, dw, dh, scaleFactor);
			composeFrom(temp, dx, dy, dw, dh);
		}
	}

	/**
		Render a painter extension into a region of the raster, composing with existing contents.

		@param px The painter.
		@param dx The X origin of the raster region.
		@param dy The Y origin of the raster region.
		@param dw The width of the raster region.
		@param dh The height of the raster region.
	*/

	public void composePainter(@NotNull PainterExtension px, int dx, int dy, int dw, int dh)
	{
		if (dw > 0 && dh > 0) {
			ReusableCompositor temp = new ReusableCompositor();
			temp.reset(dw, dh, scaleFactor);
			temp.composePainter(px, 0, 0);
			composeFrom(temp, dx, dy, dw, dh);
		}
	}

	/**
		Render from a compositor into a region of the raster, composing with existing contents.

		@param source The compositor that is the source of the pixels.
		@param dx The X origin of the raster region.
		@param dy The Y origin of the raster region.
		@param dw The width of the raster region.
		@param dh The height of the raster region.
	*/

	public void composeFrom(@NotNull ReusableCompositor source, int dx, int dy, int dw, int dh)
	{
		ensureConfigured();

		if (data != null) {
			int[] sourceData = source.data;
			if (sourceData != null) {
				isEmpty = false;
				int sourceSpan = source.getRasterWidth();
				for (int rowOffset = 0; rowOffset < dh; rowOffset++) {
					int row = dy + rowOffset;
					if (row >= 0 && row < rasterHeight) {
						for (int colOffset = 0; colOffset < dw; colOffset++) {
							int col = dx + colOffset;
							if (col >= 0 && col < rasterWidth) {
								int pixel = sourceData[rowOffset * sourceSpan + colOffset];
								int alpha = (pixel >> 24) & 0xFF;
								if (alpha != 0) {
									if (alpha != 0xFF) {
										pixel = combine(data[row * rasterWidth + col], pixel);
									}
									data[row * rasterWidth + col] = pixel;
								}
							}
						}
					}
				}
			}
		}
	}

	/**
		Render from a designated region of a compositor into a designated region of the raster, composing with existing
		contents.

		@param source The compositor that is the source of the pixels.
		@param sx The X origin of the source region.
		@param sy The Y origin of the source region.
		@param dx The X origin of the raster region.
		@param dy The Y origin of the raster region.
		@param dw The width of the region.
		@param dh The height of the region.
	*/

	public void composeFrom(@NotNull ReusableCompositor source, int sx, int sy, int dx, int dy, int dw, int dh)
	{
		ensureConfigured();

		if (data != null) {
			int[] sourceData = source.data;
			if (sourceData != null) {
				isEmpty = false;
				int sourceWidth = source.getRasterWidth();
				int sourceHeight = source.getRasterHeight();
				for (int rowOffset = 0; rowOffset < dh; rowOffset++) {
					int sourceRow = sy + rowOffset;
					int row = dy + rowOffset;
					if (row >= 0 && row < rasterHeight && sourceRow >= 0 && sourceRow < sourceHeight) {
						for (int colOffset = 0; colOffset < dw; colOffset++) {
							int sourceColumn = sx + colOffset;
							int col = dx + colOffset;
							if (col >= 0 && col < rasterWidth && sourceColumn >= 0 && sourceColumn < sourceWidth) {
								int pixel = sourceData[sourceRow * sourceWidth + sourceColumn];
								int alpha = (pixel >> 24) & 0xFF;
								if (alpha != 0) {
									if (alpha != 0xFF) {
										pixel = combine(data[row * rasterWidth + col], pixel);
									}
									data[row * rasterWidth + col] = pixel;
								}
							}
						}
					}
				}
			}
		}
	}

	/**
		Render a painter extension into the raster, composing with existing contents.
	*/

	public void composePainter(@NotNull PainterExtension px, float x, float y)
	{
		BufferedImage im = getImage();	// this method configures the raster buffer and the buffered image

		if (im != null) {
			isEmpty = false;
			Graphics2D g = im.createGraphics();
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.scale(scaleFactor, scaleFactor);
			g.translate(x, y);
			float rw = ((float) rasterWidth) / scaleFactor;
			float rh = ((float) rasterHeight) / scaleFactor;
			px.paint(g, rw, rh);
		}
	}

	/**
		A very special case. Allows direct manipulation of the pixels, not just composing.
	*/

	public void renderFrom(@NotNull BasicRenderer r)
	{
		ensureConfigured();

		if (data != null) {
			isEmpty = false;
			float w = ((float) rasterWidth) / scaleFactor;
			float h = ((float) rasterHeight) / scaleFactor;
			r.render(data, rasterWidth, rasterHeight, w, h);
		}
	}

	/**
		Erase pixels in the existing contents.
	*/

	public void erase(int dx, int dy, int dw, int dh)
	{
		if (dw > 0 && dh > 0) {
			ensureConfigured();

			if (data != null) {
				for (int rowOffset = 0; rowOffset < dh; rowOffset++) {
					int row = dy + rowOffset;
					if (row >= 0 && row < rasterHeight) {
						for (int colOffset = 0; colOffset < dw; colOffset++) {
							int col = dx + colOffset;
							if (col >= 0 && col < rasterWidth) {
								data[row * rasterWidth + col] = 0;
							}
						}
					}
				}
			}
		}
	}

	/**
		Return the raster as an image. The image shares the raster buffer with this compositor. The compositor should not
		be reused until the image is no longer in use.

		@return the image, or null if the raster has zero size.
	*/

	public @Nullable BufferedImage getImage()
	{
		ensureConfigured();

		if (b == null && data != null) {
			b = BasicImageSupport.createImage(data, rasterWidth, rasterHeight);
		}

		return b;
	}

	/**
		Draw the raster to the specified graphics context.
		@param g The graphics context.
	*/

	public void paint(@NotNull Graphics2D g)
	{
		BufferedImage im = getImage();

		if (im != null) {
			g.drawImage(im, null, null);
		}
	}

	public static void compose(int[] data, int rw, int rh, int x, int y, int pixel)
	{
		int alpha = (pixel >> 24) & 0xFF;
		if (alpha != 0 && x >= 0 && x < rw && y >= 0 && y < rh) {
			int index = y * rw + x;
			int oldPixel = data[index];
			int newPixel = alpha != 0xFF ? combine(oldPixel, pixel) : pixel;
			data[index] = newPixel;
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
