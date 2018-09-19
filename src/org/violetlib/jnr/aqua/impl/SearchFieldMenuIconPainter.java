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
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;

import org.violetlib.jnr.Insetter;
import org.violetlib.jnr.aqua.AquaUIPainter;
import org.violetlib.jnr.aqua.TextFieldConfiguration;
import org.violetlib.jnr.impl.JNRUtils;
import org.violetlib.jnr.impl.PainterExtension;
import org.violetlib.vappearances.VAppearance;

import org.jetbrains.annotations.*;

/**
	Simulates the rendering of the search field menu icon on Yosemite.
*/

public class SearchFieldMenuIconPainter
	implements PainterExtension
{
	protected final @NotNull TextFieldConfiguration tg;
	protected final @NotNull Insetter searchButtonInsets;
	protected final @NotNull VAppearance appearance;

	protected Color ICON_COLOR = new Color(89, 89, 89);
	protected Color DARK_ICON_COLOR = new Color(212, 212, 212);

	public SearchFieldMenuIconPainter(@NotNull TextFieldConfiguration g,
																		@NotNull Insetter searchButtonInsets,
																		@NotNull VAppearance appearance
																		)
	{
		this.searchButtonInsets = searchButtonInsets;
		this.tg = g;
		this.appearance = appearance;
	}

	@Override
	public void paint(@NotNull Graphics2D g, float width, float height)
	{
		Rectangle2D bounds = searchButtonInsets.applyToBounds2D(new Rectangle2D.Float(0, 0, width, height));
		g.setColor(appearance.isDark() ? DARK_ICON_COLOR : ICON_COLOR);

		AquaUIPainter.Size sz = tg.getSize();

		double w = JNRUtils.size2D(sz, 4, 4, 3);
		double h = JNRUtils.size2D(sz, 2, 2, 1.5f);
		float d = JNRUtils.size2D(sz, 0.8f, 0.8f, 0.8f);

		double x1 = bounds.getX() + JNRUtils.size2D(sz, 11, 10f, 10.5f);
		double x2 = x1 + w/2;
		double x3 = x1 + w;
		double y1 = bounds.getY() + JNRUtils.size2D(sz, 4.5f, 3.5f, 3f);
		double y2 = y1 + h;

		Path2D p = new Path2D.Double();
		p.moveTo(x1, y1);
		p.lineTo(x2, y2);
		p.lineTo(x3, y1);
		g.setStroke(new BasicStroke(d));
		g.draw(p);
	}
}
