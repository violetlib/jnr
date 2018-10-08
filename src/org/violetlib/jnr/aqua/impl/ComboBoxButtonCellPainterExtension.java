/*
 * Copyright (c) 2015-2018 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.aqua.impl;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;

import org.violetlib.jnr.aqua.AquaUIPainter;
import org.violetlib.jnr.aqua.ComboBoxConfiguration;
import org.violetlib.jnr.impl.Colors;
import org.violetlib.jnr.impl.PainterExtension;
import org.violetlib.vappearances.VAppearance;

import org.jetbrains.annotations.*;

/**
	Simulates the rendering of a combo box button cell (a combo box in a table cell).
*/

public class ComboBoxButtonCellPainterExtension
	implements PainterExtension
{
	protected final @NotNull ComboBoxConfiguration gg;
	protected final @NotNull Colors colors;

	public ComboBoxButtonCellPainterExtension(@NotNull ComboBoxConfiguration g, @Nullable VAppearance appearance)
	{
		this.gg = g;
		this.colors = Colors.getColors(appearance);
	}

	@Override
	public void paint(@NotNull Graphics2D g, float width, float height)
	{
		AquaUIPainter.Size sz = gg.getSize();
		double h = sz == AquaUIPainter.Size.REGULAR ? 4 : 4;
		double w = sz == AquaUIPainter.Size.REGULAR ? 6 : 5;
		double right = 7;
		double sep = sz == AquaUIPainter.Size.REGULAR ? 4 : 3;

		double hh = 2 * h + sep;

		double x1 = width - w - right;
		double y1 = (height - hh) / 2;
		double x2 = x1 + w/2;
		double y2 = y1 + h;
		double x3 = x1 + w;
		double y4 = y2 + h + sep;
		double y5 = y4 - h;

		Path2D p1 = new Path2D.Double();
		p1.moveTo(x1, y2);
		p1.lineTo(x2, y1);
		p1.lineTo(x3, y2);
		Path2D p2 = new Path2D.Double();
		p2.moveTo(x1, y5);
		p2.lineTo(x2, y4);
		p2.lineTo(x3, y5);
		g.setColor(colors.get("comboBoxArrow"));
		g.setStroke(new BasicStroke(1.5f));
		g.fill(p1);
		g.fill(p2);
	}
}
