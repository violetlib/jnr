/*
 * Copyright (c) 2018 Alan Snyder.
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
import org.violetlib.vappearances.VAppearance;

/**
	Simulates the rendering of legacy scroll bars.
*/

public class LegacyScrollBarPainterExtension
	implements PainterExtension
{
	private final @NotNull AquaUILayoutInfo uiLayout;
	private final @NotNull ScrollBarConfiguration g;
	private final boolean showThumb;
	private final boolean isLight;
	private final boolean isHighContrast;
	private final boolean isRollover;

	// the light style is used in dark mode and vice versa

	private final Color LIGHT_THUMB = new Color(255, 255, 255, 80);
	private final Color LIGHT_THUMB_BORDER = new Color(0, 0, 0, 26);
	private final Color LIGHT_TRACK = new Color(68, 68, 68, 217);
	private final Color LIGHT_TRACK_BORDER = new Color(63, 63, 63, 217);
	private final Color LIGHT_ROLLOVER_THUMB = new Color(169, 169, 169, 235);
	private final Color LIGHT_ROLLOVER_THUMB_BORDER = LIGHT_THUMB_BORDER; // new Color(59, 59, 59, 217);

	private final Color LIGHT_TRACK_BORDER_HIGH_CONTRAST = LIGHT_TRACK_BORDER;	// TBD

	private final Color DARK_THUMB = new Color(0, 0, 0, 56);
	private final Color DARK_TRACK = new Color(250, 250, 250);
	private final Color DARK_TRACK_BORDER = new Color(237, 237, 237);
	private final Color DARK_ROLLOVER_THUMB = new Color(0, 0, 0, 128);

	private final Color DARK_TRACK_BORDER_HIGH_CONTRAST = new Color(200, 200, 200);

	public LegacyScrollBarPainterExtension(@NotNull AquaUILayoutInfo uiLayout,
																				 @NotNull ScrollBarConfiguration g,
																				 @Nullable VAppearance appearance)
	{
		this.uiLayout = uiLayout;
		this.g = g;
		this.isLight = appearance != null && appearance.isDark();
		this.isHighContrast = appearance != null && appearance.isHighContrast();
		this.showThumb = g.getKnobWidget() != ScrollBarKnobWidget.NONE;
		isRollover = g.getState() == AquaUIPainter.State.ROLLOVER;
	}

	@Override
	public void paint(@NotNull Graphics2D g, float width, float height)
	{
		g = (Graphics2D) g.create();
		g.clip(new Rectangle2D.Float(0, 0, width, height));

		boolean isVertical = height > width;

		{
			// paint the track
			Rectangle2D r = new Rectangle2D.Float(0, 0, width, height);
			r.setFrameFromCenter(r.getCenterX(), r.getCenterY(), r.getMinX(), r.getMinY());

			g.setColor(getTrackBackgroundColor());
			g.fill(r);

			// The border is painted only on the long edges

			float borderThickness = getTrackBorderThickness();
			Color borderColor = getTrackBorderColor();

			if (isLight) {
				Rectangle2D inner = new Rectangle2D.Float(0, 0, width, height);
				if (isVertical) {
					inner.setFrameFromCenter(r.getCenterX(), r.getCenterY(), r.getMinX() + borderThickness, r.getMinY());
				} else {
					inner.setFrameFromCenter(r.getCenterX(), r.getCenterY(), r.getMinX(), r.getMinY() + borderThickness);
				}
				Rectangle2D border = r.createIntersection(inner);
				g.setColor(borderColor);
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
				g.setColor(borderColor);
				g.setStroke(new BasicStroke(borderThickness));
				g.draw(borderPath);
			}
		}

		if (showThumb) {
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
		double rightBottom = 3.5;

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
		return isLight
						 ? (isHighContrast ? LIGHT_TRACK_BORDER_HIGH_CONTRAST : LIGHT_TRACK_BORDER)
						 : (isHighContrast ? DARK_TRACK_BORDER_HIGH_CONTRAST : DARK_TRACK_BORDER);
	}

	protected float getTrackBorderThickness()
	{
		return isLight ? 1f : 0.8f;
	}

	protected float getThumbBorderThickness()
	{
		return 1;
	}

	protected @NotNull Color getTrackBackgroundColor()
	{
		return isLight ? LIGHT_TRACK : DARK_TRACK;
	}

	protected @NotNull Color getThumbColor()
	{
		return isLight ? (isRollover ? LIGHT_ROLLOVER_THUMB : LIGHT_THUMB) : (isRollover ? DARK_ROLLOVER_THUMB : DARK_THUMB);
	}

	protected @Nullable Color getThumbBorderColor()
	{
		return isLight ? (isRollover ? LIGHT_ROLLOVER_THUMB_BORDER : LIGHT_THUMB_BORDER) : null;
	}
}
