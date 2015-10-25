/*
 * Copyright (c) 2015 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.aqua.coreui;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.color.ColorSpace;
import java.awt.image.ColorModel;

import org.jetbrains.annotations.*;

import org.violetlib.jnr.aqua.ButtonConfiguration;
import org.violetlib.jnr.impl.BasicRenderer;
import org.violetlib.jnr.impl.PainterExtension;
import org.violetlib.jnr.impl.Renderer;
import org.violetlib.jnr.impl.ReusableCompositor;

/**
 	This renderer erases the central area where the background should show through.
*/

public class ColorWellRenderer
	extends Renderer
{
	private final @NotNull ButtonConfiguration bg;
	private final @NotNull Renderer basic;

	public ColorWellRenderer(@NotNull ButtonConfiguration bg, @NotNull Renderer basic)
	{
		this.bg = bg;
		this.basic = basic;
	}

	@Override
	public void composeTo(@NotNull ReusableCompositor compositor)
	{
		int rw = compositor.getRasterWidth();
		int rh = compositor.getRasterHeight();
		int scaleFactor = compositor.getScaleFactor();

		int lt = scaleFactor;			// line thickness
		int gt = 4 * scaleFactor; // gap thickness
		int bt = lt * 2 + gt;			// border thickness (two lines plus a gap)

		basic.composeTo(compositor);

		BorderPainter bp = new BorderPainter(scaleFactor);

		// Painting should work, but it does not paint pixels as expected
		//compositor.composePainter(bp, 0, 0);

		compositor.renderFrom(bp);

		{
			// erase the central portion so that the background shows through
			int dx = bt;
			int dy = bt;
			int dw = rw - 2 * bt;
			int dh = rh - 2 * bt;
			compositor.erase(dx, dy, dw, dh);
		}
	}

	protected class BorderPainter
		implements PainterExtension, BasicRenderer
	{
		private final Color outer;
		private final Color inner;
		private final int scaleFactor;

		public BorderPainter(int scaleFactor)
		{
			this.scaleFactor = scaleFactor;

			switch (bg.getState()) {
				case DISABLED:
				case DISABLED_INACTIVE:
					outer = new Color(170, 170, 170, 127);
					inner = new Color(168, 168, 168);
					break;
				case PRESSED:
					outer = new Color(175, 175, 175);
					inner = new Color(86, 86, 86);
					break;
				default:
					outer = new Color(175, 175, 175);
					inner = new Color(138, 138, 138);
			}
		}

		@Override
		public void paint(@NotNull Graphics2D g, float width, float height)
		{
			int w = (int) width;
			int h = (int) height;

			g.setColor(outer);
			g.drawRect(0, 0, w, h);
			g.setColor(inner);
			g.drawRect(5, 5, w-10, h-10);
		}

		@Override
		public void render(@NotNull int[] data, int rw, int rh, float w, float h)
		{
			drawRect(data, rw, rh, outer, 0, 0, rw, rh);
			drawRect(data, rw, rh, inner, 5 * scaleFactor, 5 * scaleFactor, rw - 10 * scaleFactor, rh - 10 * scaleFactor);
		}

		protected void drawRect(int[] data, int rw, int rh, Color c, int dx, int dy, int dw, int dh)
		{
			if (dw > 0 && dh > 0) {

				ColorModel cm = ReusableCompositor.getColorModel();
				ColorSpace cs = cm.getColorSpace();
				float[] components = c.getComponents(cs, null);
				float alpha = components[3];
				for (int i = 0; i < 3; i++) {
					components[i] *= alpha;
				}
				int pixel = cm.getDataElement(components, 0);

				for (int i = 0; i < scaleFactor; i++) {
					drawRect1(data, rw, rh, pixel, dx+i, dy+i, dw-2*i, dh-2*i);
				}
			}
		}

		protected void drawRect1(int[] data, int rw, int rh, int pixel, int dx, int dy, int dw, int dh)
		{
			if (dw > 0 && dh > 0) {
				for (int x = dx; x < dx+dw; x++) {
					data[dy * rw + x] = pixel;
					data[(dy+dh-1) * rw + x] = pixel;
				}
				for (int y = dy+1; y < dy+dh-1; y++) {
					data[y * rw + dx] = pixel;
					data[y * rw + dx+dw-1] = pixel;
				}
			}
		}
	}
}
