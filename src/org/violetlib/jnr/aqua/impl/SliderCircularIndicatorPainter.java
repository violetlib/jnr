/*
 * Copyright (c) 2015 Alan Snyder.
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

import org.jetbrains.annotations.*;

/**
	Paint tick marks for a horizontal slider.
*/

public class SliderCircularIndicatorPainter
{
	protected Color DIMPLE_COLOR = new Color(160, 160, 160);
	protected Color DIMPLE_TOP = new Color(105, 105, 105);

	protected final double x;
	protected final double y;
	protected final double radius;
	protected final double zeroAngle;
	protected final double p;

	public SliderCircularIndicatorPainter(double x,
																				double y,
																				double radius,
																				double zeroAngle,
																				double p)
	{
		this.x = x;
		this.y = y;
		this.radius = radius;
		this.zeroAngle = zeroAngle;
		this.p = p;
	}

	public void paint(@NotNull Graphics2D g)
	{
		// TBD: looks more like a gradient
		double d = 5;
		double angle = zeroAngle - p * 2 * Math.PI;
		double x0 = x + Math.cos(angle) * radius;
		double y0 = y - Math.sin(angle) * radius;
		Shape s = new Ellipse2D.Double(x0 - d/2, y0 - d/2, d, d);
		g.setColor(DIMPLE_COLOR);
		g.fill(s);
		s = new Ellipse2D.Double(x0 - d/4, y0 - d/2, d/2, d/2);
		g.setColor(DIMPLE_TOP);
		g.fill(s);
	}
}
