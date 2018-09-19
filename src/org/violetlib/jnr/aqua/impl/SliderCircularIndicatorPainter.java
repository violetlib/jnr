/*
 * Copyright (c) 2015-2018 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.aqua.impl;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;

import org.violetlib.vappearances.VAppearance;

import org.jetbrains.annotations.*;

/**
	Paint tick marks for a horizontal slider.
*/

public class SliderCircularIndicatorPainter
{
	protected final @NotNull Color DIMPLE_COLOR = new Color(160, 160, 160);
	protected final @NotNull Color DIMPLE_TOP = new Color(105, 105, 105);
	protected final @NotNull Color DARK_DIMPLE_COLOR = new Color(231, 231, 231);

	protected final double x;
	protected final double y;
	protected final double radius;
	protected final double zeroAngle;
	protected final double p;

	protected final boolean isDark;

	public SliderCircularIndicatorPainter(double x,
																				double y,
																				double radius,
																				double zeroAngle,
																				double p,
																				@Nullable VAppearance appearance)
	{
		this.x = x;
		this.y = y;
		this.radius = radius;
		this.zeroAngle = zeroAngle;
		this.p = p;
		this.isDark = appearance != null && appearance.isDark();
	}

	public void paint(@NotNull Graphics2D g)
	{
		double d = 5;
		double angle = zeroAngle - p * 2 * Math.PI;
		double x0 = x + Math.cos(angle) * radius;
		double y0 = y - Math.sin(angle) * radius;
		Shape s = new Ellipse2D.Double(x0 - d / 2, y0 - d / 2, d, d);
		g.setColor(isDark ? DARK_DIMPLE_COLOR : DIMPLE_COLOR);
		g.fill(s);

		if (!isDark) {
			// TBD: looks more like a gradient
			s = new Ellipse2D.Double(x0 - d / 4, y0 - d / 2, d / 2, d / 2);
			g.setColor(DIMPLE_TOP);
			g.fill(s);
		}
	}
}
