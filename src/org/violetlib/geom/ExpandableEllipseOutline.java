/*
 * Copyright (c) 2015 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.geom;

import java.awt.*;
import java.awt.geom.Ellipse2D;

import org.jetbrains.annotations.*;

/**
	An expandable outline in the form of an ellipse.
*/

public final class ExpandableEllipseOutline
	extends ExpandableOutline
{
	private final double x;
	private final double y;
	private final double width;
	private final double height;

	public ExpandableEllipseOutline(double x, double y, double width, double height)
	{
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}

	public ExpandableEllipseOutline(Ellipse2D source)
	{
		this.x = source.getX();
		this.y = source.getY();
		this.width = source.getWidth();
		this.height = source.getHeight();
	}

	@Override
	public @NotNull Shape getShape(float offset)
	{
		double nx = x - offset;
		double ny = y - offset;
		double nwidth = width + 2 * offset;
		double nheight = height + 2 * offset;
		return new Ellipse2D.Double(nx, ny, nwidth, nheight);
	}
}
