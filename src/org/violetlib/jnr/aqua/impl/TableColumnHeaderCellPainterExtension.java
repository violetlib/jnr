/*
 * Copyright (c) 2015-2018 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.aqua.impl;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

import org.violetlib.jnr.aqua.AquaUIPainter;
import org.violetlib.jnr.aqua.TableColumnHeaderConfiguration;
import org.violetlib.jnr.impl.Colors;
import org.violetlib.jnr.impl.ImageUtils;
import org.violetlib.jnr.impl.PainterExtension;
import org.violetlib.vappearances.VAppearance;

import org.jetbrains.annotations.*;

/**
	Simulates the rendering of a table header cell.
*/

public class TableColumnHeaderCellPainterExtension
	implements PainterExtension
{
	// Java needs to support table headers of arbitrary height. Even to replicate the Finder, a nonstandard height is
	// necessary. Most native renderers support only a fixed height. Furthermore, to replicate the Finder, a nonstandard
	// background color must be supported. The solution is to avoid the native renderers and to paint the dividers and
	// sort arrows here, without painting a background.

	// Java expects a divider to be painted along the right edge.

	protected final @NotNull TableColumnHeaderConfiguration tg;
	protected final @NotNull Colors colors;
	protected final boolean isDark;

	public TableColumnHeaderCellPainterExtension(@NotNull TableColumnHeaderConfiguration g,
																							 @Nullable VAppearance appearance)
	{
		this.tg = g;
		this.colors = Colors.getColors(appearance);
		this.isDark = appearance != null && appearance.isDark();
	}

	@Override
	public void paint(@NotNull Graphics2D g, float width, float height)
	{
		if (width == 0 || height == 0) {
			return;
		}

		// This code attempts to work in a reasonable way for standard tables and Finder tables

		// The arrow gap is the width of the gap between the divider and the arrow.
		// The arrow gap is 4 for a standard table and 8 for Finder
		float extraHeight = height - 16;
		float extraGap = Math.min(4, extraHeight / 2);
		float arrowGap = 4 + extraGap;

		// The divider gap is the top and bottom inset around the divider.
		// The divider gap is 2.5 for a standard table and 2 for Finder. 2.5 is good enough.
		float dividerGap = 2.5f;

		float dividerHeight = Math.max(0, height - 2 * dividerGap);

		if (dividerHeight > 0) {
			float x0 = width - 1;
			float y0 = dividerGap;
			Rectangle2D r = new Rectangle2D.Float(x0, y0, 1, dividerHeight);
			g.setColor(colors.get("tableHeaderDivider"));
			g.fill(r);
		}

		AquaUIPainter.ColumnSortArrowDirection direction = tg.getSortArrowDirection();
		if (direction != AquaUIPainter.ColumnSortArrowDirection.NONE && width >= 12) {
			Image im = null;
			switch (direction)
			{
				case UP:
					im = Toolkit.getDefaultToolkit().getImage("NSImage://NSAscendingSortIndicator");
					break;
				case DOWN:
					im = Toolkit.getDefaultToolkit().getImage("NSImage://NSDescendingSortIndicator");
					break;
			}
			if (im != null) {
				if (isDark) {
					im = ImageUtils.invertForDarkMode(im);
				}
				int imageWidth = im.getWidth(null);
				int imageHeight = im.getHeight(null);
				float x = tg.isLeftToRight() ? width - 1 - arrowGap - imageWidth + 1	// +1 because image has insets
					: arrowGap + 1;
				float y = (height - imageHeight) / 2;
				AffineTransform tr = AffineTransform.getTranslateInstance(x, y);
				g.drawImage(im, tr, null);
			}
		}
	}
}
