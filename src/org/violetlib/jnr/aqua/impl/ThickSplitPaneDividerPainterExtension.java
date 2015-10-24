/*
 * Copyright (c) 2015 Alan Snyder.
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

import org.jetbrains.annotations.*;

import org.violetlib.jnr.aqua.SplitPaneDividerConfiguration;
import org.violetlib.jnr.impl.PainterExtension;

/**
	Simulates the rendering of a Yosemite thick style split pane divider.
*/

public class ThickSplitPaneDividerPainterExtension
	implements PainterExtension
{
	protected final @NotNull SplitPaneDividerConfiguration g;

	protected Color DIMPLE_COLOR = new Color(30, 30, 30, 40);
	protected Color DIMPLE_BORDER = new Color(0, 0, 0, 40);

	public ThickSplitPaneDividerPainterExtension(@NotNull SplitPaneDividerConfiguration g)
	{
		this.g = g;
	}

	@Override
	public void paint(@NotNull Graphics2D g, float width, float height)
	{
		// TBD: shadow

		float d = 6;
		float x = (width - d) / 2;
		float y = (height - d) / 2;
		Shape s = new Ellipse2D.Double(x, y, d, d);
		g.setColor(DIMPLE_COLOR);
		g.fill(s);
		g.setColor(DIMPLE_BORDER);
		g.setStroke(new BasicStroke(1));
		s = new Ellipse2D.Double(x+0.5, y+0.5, d-1, d-1);
		g.draw(s);
	}
}
