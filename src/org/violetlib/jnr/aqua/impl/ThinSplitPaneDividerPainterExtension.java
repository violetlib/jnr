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
import java.awt.geom.Rectangle2D;

import org.violetlib.jnr.aqua.AquaUIPainter;
import org.violetlib.jnr.aqua.SplitPaneDividerConfiguration;
import org.violetlib.jnr.impl.PainterExtension;
import org.violetlib.vappearances.VAppearance;

import org.jetbrains.annotations.*;

/**
	Simulates the rendering of a thin style split pane divider.
*/

public class ThinSplitPaneDividerPainterExtension
	implements PainterExtension
{
	protected final @NotNull SplitPaneDividerConfiguration dg;
	protected final @NotNull Color dividerColor;

	protected static final Color DEFAULT_COLOR = new Color(128, 128, 128, 80);

	public ThinSplitPaneDividerPainterExtension(@NotNull SplitPaneDividerConfiguration g,
																							@Nullable VAppearance appearance)
	{
		this.dg = g;
		this.dividerColor = determineDividerColor(g, appearance);
	}

	private @NotNull Color determineDividerColor(@NotNull SplitPaneDividerConfiguration g,
																							 @Nullable VAppearance appearance)
	{
		return appearance != null ? appearance.getColors().get("separator") : DEFAULT_COLOR;
	}

	@Override
	public void paint(@NotNull Graphics2D g, float width, float height)
	{
		float d = 1;
		Shape s;
		if (dg.getOrientation() == AquaUIPainter.Orientation.VERTICAL) {
			float x = (width - d) / 2;
			s = new Rectangle2D.Double(x, 0, d, height);
		} else {
			float y = (height - d) / 2;
			s = new Rectangle2D.Double(0, y, width, d);
		}
		g.setColor(dividerColor);
		g.fill(s);
	}
}
