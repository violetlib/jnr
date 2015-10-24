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

import org.jetbrains.annotations.*;

import org.violetlib.jnr.aqua.AquaUIPainter;
import org.violetlib.jnr.aqua.SliderConfiguration;
import org.violetlib.jnr.impl.JNRUtils;
import org.violetlib.jnr.impl.PainterExtension;

/**
	Simulates the rendering of circular sliders (dials).
*/

public class CircularSliderPainterExtension
	implements PainterExtension
{
	protected final @NotNull SliderConfiguration sg;

	protected Color TICK_COLOR = new Color(10, 10, 10, 110);

	public CircularSliderPainterExtension(@NotNull SliderConfiguration g)
	{
		this.sg = g;
	}

	@Override
	public void paint(@NotNull Graphics2D g, float width, float height)
	{
		AquaUIPainter.SliderWidget sw = sg.getWidget();

		if (sw == AquaUIPainter.SliderWidget.SLIDER_CIRCULAR) {
			if (sg.hasTickMarks()) {
				paintCircularTickMarks(g, width, height);
			}
			paintCircularIndicator(g, width, height);
		}
	}

	protected void paintCircularTickMarks(@NotNull Graphics2D g, float width, float height)
	{
		double x = width / 2.0;
		double y = height / 2.0;
		int tickCount = sg.getNumberOfTickMarks();
		double p0 = tickCount > 1 ? 0 : 0.5;
		double p1 = tickCount > 1 ? 1 : 0.5;
		double zeroAngle = Math.PI/2;
		float radius = JNRUtils.size2D(sg.getSize(), 15.5f, 10.5f, 10.5f);
		SliderCircularTickPainter p = new SliderCircularTickPainter(
			TICK_COLOR, 1, 1, x, y, radius, zeroAngle, tickCount, p0, p1);
		p.paint(g);
	}

	protected void paintCircularIndicator(@NotNull Graphics2D g, float width, float height)
	{
		double x = width / 2.0;
		double y = height / 2.0;
		double zeroAngle = Math.PI/2;
		double thumbPosition = sg.getValue();
		float radius = JNRUtils.size2D(sg.getSize(), 7.5f, 3.5f, 3.5f);
		SliderCircularIndicatorPainter p = new SliderCircularIndicatorPainter(x, y, radius, zeroAngle, thumbPosition);
		p.paint(g);
	}
}
