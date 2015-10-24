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
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

import org.jetbrains.annotations.*;

import org.violetlib.jnr.aqua.AquaUILayoutInfo;
import org.violetlib.jnr.aqua.AquaUIPainter;
import org.violetlib.jnr.aqua.AquaUIPainter.ScrollBarKnobWidget;
import org.violetlib.jnr.aqua.ScrollBarConfiguration;
import org.violetlib.jnr.impl.PainterExtension;

/**
	Simulates the rendering of Yosemite overlay scroll bars.
*/

public class OverlayScrollBarPainterExtension
	implements PainterExtension
{
	private final @NotNull AquaUILayoutInfo uiLayout;
	private final @NotNull ScrollBarConfiguration g;
	private final @NotNull ScrollBarKnobWidget kw;
	private final boolean isRollover;

	private final Color LIGHT_TRACK = new Color(68, 68, 68, 217);
	private final Color LIGHT_TRACK_BORDER = new Color(63, 63, 63, 217);
	private final Color LIGHT_THUMB = new Color(255, 255, 255, 128);
	private final Color LIGHT_THUMB_BORDER = new Color(0, 0, 0, 26);
	private final Color LIGHT_ROLLOVER_THUMB = new Color(169, 169, 169, 235);
	private final Color LIGHT_ROLLOVER_THUMB_BORDER = new Color(59, 59, 59, 217);

	private final Color DARK_THUMB = new Color(0, 0, 0, 128);
	private final Color DARK_TRACK = new Color(250, 250, 250, 191);
	private final Color DARK_TRACK_BORDER = new Color(220, 220, 220, 200);

	public OverlayScrollBarPainterExtension(@NotNull AquaUILayoutInfo uiLayout, @NotNull ScrollBarConfiguration g)
	{
		this.uiLayout = uiLayout;
		this.g = g;
		this.kw = g.getKnobWidget();
		this.isRollover = g.getWidget() == AquaUIPainter.ScrollBarWidget.OVERLAY_ROLLOVER;
	}

	@Override
	public void paint(@NotNull Graphics2D g, float width, float height)
	{
		g = (Graphics2D) g.create();
		g.clip(new Rectangle2D.Float(0, 0, width, height));

		boolean isVertical = height > width;

		if (isRollover) {
			// In the rollover state, we paint a track
			Rectangle2D r = new Rectangle2D.Float(0, 0, width, height);
			r.setFrameFromCenter(r.getCenterX(), r.getCenterY(), r.getMinX(), r.getMinY());

			g.setColor(getTrackBackgroundColor());
			g.fill(r);

			float borderThickness = getTrackBorderThickness();

			if (kw == ScrollBarKnobWidget.LIGHT) {
				Rectangle2D inner = new Rectangle2D.Float(0, 0, width, height);
				inner.setFrameFromCenter(r.getCenterX(), r.getCenterY(), r.getMinX() + borderThickness, r.getMinY() + borderThickness);
				Rectangle2D border = r.createIntersection(inner);
				g.setColor(getTrackBorderColor());
				g.fill(border);

			} else {
				double o = borderThickness / 2;
				Path2D borderPath = new Path2D.Float();
				if (isVertical) {
					borderPath.moveTo(r.getX() + o, r.getY() + o);
					borderPath.lineTo(r.getX() + o, r.getY() + r.getHeight() - o);
					borderPath.moveTo(r.getX() + r.getWidth() - o, r.getY() + o);
					borderPath.lineTo(r.getX() + r.getWidth() - o, r.getY() + r.getHeight() - o);
				} else {
					borderPath.moveTo(r.getX() + o, r.getY() + o);
					borderPath.lineTo(r.getX() + r.getWidth() - o, r.getY() + o);
					borderPath.moveTo(r.getX() + o, r.getY() + r.getHeight() - o);
					borderPath.lineTo(r.getX() + r.getWidth() - o, r.getY() + r.getHeight() - o);
				}
				g.setColor(getTrackBorderColor());
				g.setStroke(new BasicStroke(borderThickness));
				g.draw(borderPath);
			}
		}

		if (kw != ScrollBarKnobWidget.NONE) {
			Shape thumbShape = createThumbShape(width, height);
			g.setColor(getThumbColor());
			g.fill(thumbShape);

			Color thumbBorderColor = getThumbBorderColor();
			if (thumbBorderColor != null) {
				float thickness = getThumbBorderThickness();
				g.setColor(thumbBorderColor);
				g.setStroke(new BasicStroke(thickness * 1.5f));
				Shape thumbBorderShape = createThumbShape(width, height);
				g.clip(thumbShape);
				if (false) {
					g.clip(new Rectangle2D.Float(0, 0, width / 2, height / 2)); // for testing
				}
				g.draw(thumbBorderShape);
			}
		}

		g.dispose();
	}

	protected @NotNull Shape createThumbShape(float width, float height)
	{
		boolean isVertical = height > width;

		double leftTop = 3.5;
		double rightBottom = 1.5;

		Rectangle2D bounds = new Rectangle2D.Float(0, 0, width, height);
		Rectangle2D thumbBounds = uiLayout.getScrollBarThumbBounds(bounds, g);

		if (isVertical) {
			double w = width - leftTop - rightBottom;
			return new RoundRectangle2D.Double(leftTop, thumbBounds.getY(), w, thumbBounds.getHeight(), w, w);

		} else {
			double h = height - leftTop - rightBottom;
			return new RoundRectangle2D.Double(thumbBounds.getX(), leftTop, thumbBounds.getWidth(), h, h, h);
		}
	}

	protected @NotNull Color getTrackBorderColor()
	{
		switch (kw) {
			case LIGHT:
				return LIGHT_TRACK_BORDER;
			default:
				return DARK_TRACK_BORDER;
		}
	}

	protected float getTrackBorderThickness()
	{
		switch (kw) {
			case LIGHT:
				return 1f;
			default:
				return 0.8f;
		}
	}

	protected float getThumbBorderThickness()
	{
		switch (kw) {
			case LIGHT:
				return 1;
			default:
				return 1;
		}
	}

	protected @NotNull Color getTrackBackgroundColor()
	{
		switch (kw) {
			case LIGHT:
				return LIGHT_TRACK;
			default:
				return DARK_TRACK;
		}
	}

	protected @NotNull Color getThumbColor()
	{
		switch (kw) {
			case LIGHT:
				return isRollover ? LIGHT_ROLLOVER_THUMB : LIGHT_THUMB;
			default:
				return DARK_THUMB;
		}
	}

	protected @Nullable Color getThumbBorderColor()
	{
		switch (kw) {
			case LIGHT:
				return isRollover ? LIGHT_ROLLOVER_THUMB_BORDER : LIGHT_THUMB_BORDER;
			default:
				return null;
		}
	}
}
