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
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

import org.violetlib.jnr.aqua.AquaUILayoutInfo;
import org.violetlib.jnr.aqua.AquaUIPainter;
import org.violetlib.jnr.aqua.AquaUIPainter.ScrollBarKnobWidget;
import org.violetlib.jnr.aqua.ScrollBarConfiguration;
import org.violetlib.jnr.impl.Colors;
import org.violetlib.jnr.impl.PainterExtension;
import org.violetlib.vappearances.VAppearance;

import org.jetbrains.annotations.*;

import static org.violetlib.jnr.aqua.AquaUIPainter.ScrollBarKnobWidget.*;

/**
	Simulates the rendering of overlay scroll bars.
*/

public class OverlayScrollBarPainterExtension
	implements PainterExtension
{
	private final @NotNull AquaUILayoutInfo uiLayout;
	private final @NotNull ScrollBarConfiguration g;
	private final @NotNull ScrollBarKnobWidget kw;
	private final boolean isRollover;
	private final @NotNull Colors colors;

	public OverlayScrollBarPainterExtension(@NotNull AquaUILayoutInfo uiLayout,
																					@NotNull ScrollBarConfiguration g,
																					@Nullable VAppearance appearance)
	{
		this.uiLayout = uiLayout;
		this.g = g;
		this.kw = g.getKnobWidget();
		this.isRollover = g.getWidget() == AquaUIPainter.ScrollBarWidget.OVERLAY_ROLLOVER;

		// Overlay scroll bars support a light and dark style. The light style is the default for dark mode.
		// The dark style is default for light mode. Both styles are altered when the high contrast option
		// is enabled.

		boolean isDark = getCorrespondingAppearanceIsDark(appearance, kw);
		boolean isHighContrast = appearance != null && appearance.isHighContrast();
		this.colors = Colors.getColors(isDark, isHighContrast);
	}

	private boolean getCorrespondingAppearanceIsDark(@Nullable VAppearance appearance, @NotNull ScrollBarKnobWidget w)
	{
		if (w == DEFAULT) {
			// Use the default style for the appearance.
			return appearance != null && appearance.isDark();
		} else if (w == LIGHT) {
			// The light style is defined by the dark appearance
			return true;
		} else {
			// The dark style is defined by the light appearance.
			return false;
		}
	}

	@Override
	public void paint(@NotNull Graphics2D g, float width, float height)
	{
		g = (Graphics2D) g.create();
		g.clip(new Rectangle2D.Float(0, 0, width, height));

		boolean isVertical = height > width;

		if (isRollover) {
			// In the rollover state, we paint a track
			Rectangle2D bounds = new Rectangle2D.Float(0, 0, width, height);
			g.setColor(getTrackBackgroundColor());
			g.fill(bounds);

			// The border is painted only on the long edges
			Color innerBorderColor = getInnerBorderColor();
			Color outerBorderColor = getOuterBorderColor();
			if (innerBorderColor != null || outerBorderColor != null) {
				int w = (int) width;
				int h = (int) height;
				if (isVertical) {
					if (innerBorderColor != null) {
						g.setColor(innerBorderColor);
						g.fillRect(0, 0, 1, h);
					}
					if (outerBorderColor != null) {
						g.setColor(outerBorderColor);
						g.fillRect(w-1, 0, 1, h);
					}
				} else {
					if (innerBorderColor != null) {
						g.setColor(innerBorderColor);
						g.fillRect(0, 0, w, 1);
					}
					if (outerBorderColor != null) {
						g.setColor(outerBorderColor);
						g.fillRect(0, h-1, w, 1);
					}
				}
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

	protected @Nullable Color getInnerBorderColor()
	{
		return colors.get("overlayScrollTrackBorder");
	}

	protected @Nullable Color getOuterBorderColor()
	{
		return colors.get("overlayScrollTrackBorder");
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
		return colors.get("overlayScrollTrack");
	}

	protected @NotNull Color getThumbColor()
	{
		if (isRollover) {
			Color c = colors.getOptional("overlayThumb_rollover");
			if (c != null) {
				return c;
			}
		}

		return colors.get("overlayThumb");
	}

	protected @Nullable Color getThumbBorderColor()
	{
		if (isRollover) {
			Color c = colors.getOptional("overlayThumbBorder_rollover");
			if (c != null) {
				return c;
			}
		}

		return colors.getOptional("overlayThumbBorder");
	}
}
