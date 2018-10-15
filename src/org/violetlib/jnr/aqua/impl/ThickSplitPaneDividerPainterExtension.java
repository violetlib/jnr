/*
 * Copyright (c) 2015-2018 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.aqua.impl;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;

import org.violetlib.jnr.aqua.SplitPaneDividerConfiguration;
import org.violetlib.jnr.impl.Colors;
import org.violetlib.jnr.impl.PainterExtension;
import org.violetlib.vappearances.VAppearance;

import org.jetbrains.annotations.*;

/**
	Simulates the rendering of a thick style split pane divider.
*/

public class ThickSplitPaneDividerPainterExtension
	implements PainterExtension
{
	protected final @NotNull SplitPaneDividerConfiguration g;
	protected final @NotNull Colors colors;

	public ThickSplitPaneDividerPainterExtension(@NotNull SplitPaneDividerConfiguration g,
																							 @Nullable VAppearance appearance)
	{
		this.g = g;
		this.colors = Colors.getColors(appearance);
	}

	@Override
	public void paint(@NotNull Graphics2D g, float width, float height)
	{
		// TBD: shadow

		Color dimpleColor = colors.get("thickDividerDimple");
		Color dimpleBorderColor = colors.getOptional("thickDividerDimpleBorder");

		float d = 6;
		float x = (width - d) / 2;
		float y = (height - d) / 2;
		Shape s = new Ellipse2D.Double(x, y, d, d);
		g.setColor(dimpleColor);
		g.fill(s);
		if (dimpleBorderColor != null) {
			g.setColor(dimpleBorderColor);
			g.setStroke(new BasicStroke(1));
			s = new Ellipse2D.Double(x+0.5, y+0.5, d-1, d-1);
			g.draw(s);
		}
	}
}
