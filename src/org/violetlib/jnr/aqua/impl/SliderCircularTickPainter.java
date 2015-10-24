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
import java.awt.geom.Rectangle2D;

import org.jetbrains.annotations.*;

/**
	Paint tick marks for a horizontal slider.
*/

public class SliderCircularTickPainter
{
	protected final @NotNull Color color;
	protected final double w;
	protected final double h;
	protected final double x;
	protected final double y;
	protected final double radius;
	protected final double zeroAngle;
	protected final int tickCount;
	protected final double p0;
	protected final double p1;

	public SliderCircularTickPainter(@NotNull Color color,
																	 double w, double h,
																	 double x, double y,
																	 double radius,
																	 double zeroAngle,
																	 int tickCount,
																	 double p0, double p1)
	{
		this.color = color;
		this.w = w;
		this.h = h;
		this.x = x;
		this.y = y;
		this.radius = radius;
		this.zeroAngle = zeroAngle;
		this.tickCount = tickCount;
		this.p0 = p0;
		this.p1 = p1;
	}

	public void paint(@NotNull Graphics2D g)
	{
		g.setColor(color);

		if (tickCount == 1) {
			drawTick(g, p0);
		} else {
			double spacing = (p1 - p0) / tickCount;
			double p = p0;
			for (int i = 0; i < tickCount; i++) {
				drawTick(g, p);
				p += spacing;
			}
		}
	}

	protected void drawTick(@NotNull Graphics2D g, double thumbPosition)
	{
		double angle = zeroAngle - thumbPosition * 2 * Math.PI;
		double x0 = x + Math.cos(angle) * radius - w/2;
		double y0 = y - Math.sin(angle) * radius - h/2;
		g.setColor(color);
		Shape s = new Rectangle2D.Double(x0, y0, w, h);
		g.fill(s);
	}
}
