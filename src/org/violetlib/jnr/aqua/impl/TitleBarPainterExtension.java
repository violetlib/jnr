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
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;

import org.jetbrains.annotations.*;

import org.violetlib.jnr.aqua.AquaUIPainter;
import org.violetlib.jnr.aqua.AquaUIPainter.State;
import org.violetlib.jnr.aqua.AquaUIPainter.TitleBarButtonWidget;
import org.violetlib.jnr.aqua.AquaUIPainter.TitleBarWidget;
import org.violetlib.jnr.aqua.TitleBarConfiguration;
import org.violetlib.jnr.aqua.TitleBarConfiguration.ResizeAction;
import org.violetlib.jnr.impl.PainterExtension;

import static org.violetlib.jnr.aqua.TitleBarConfiguration.ResizeAction.*;

/**
	Simulates some features of the Yosemite title bar not supported by the native renderer.
*/

public class TitleBarPainterExtension
	implements PainterExtension
{
	/*
		There are a few things the native code does not do that we correct for here.

		It does not paint the rollover icon, except in the pressed state for buttons other than the resize button.

		It does not paint the pressed icon for the resize button.

		If the title bar is inactive, it paints the buttons as inactive except in the pressed state.
	*/

	protected final @NotNull TitleBarConfiguration tg;
	protected final @NotNull TitleBarLayoutInfo layoutInfo;

	protected final @NotNull Color RED_COLOR = new Color(255, 96, 88);
	protected final @NotNull Color YELLOW_COLOR = new Color(255, 189, 46);
	protected final @NotNull Color GREEN_COLOR = new Color(40, 201, 64);

	public TitleBarPainterExtension(@NotNull TitleBarLayoutInfo layoutInfo, @NotNull TitleBarConfiguration tg)
	{
		this.tg = tg;
		this.layoutInfo = layoutInfo;
	}

	@Override
	public void paint(@NotNull Graphics2D g, float width, float height)
	{
		Rectangle2D bounds = new Rectangle2D.Float(0, 0, width, height);
		paintIfNeeded(bounds, g, tg.getCloseButtonState(), AquaUIPainter.TitleBarButtonWidget.CLOSE_BOX);
		paintIfNeeded(bounds, g, tg.getMinimizeButtonState(), AquaUIPainter.TitleBarButtonWidget.MINIMIZE_BOX);
		paintIfNeeded(bounds, g, tg.getResizeButtonState(), AquaUIPainter.TitleBarButtonWidget.RESIZE_BOX);
	}

	protected void paintIfNeeded(@NotNull Rectangle2D bounds,
															 @NotNull Graphics2D g,
															 @NotNull State state,
															 @NotNull TitleBarButtonWidget bw)
	{
		// Background painting is needed if the title bar is inactive and the button state is not inactive or pressed. Icon
		// painting is needed if the button state is rollover, or the button state is pressed and the button is the resize
		// button, or we painted the background and the dirty indicator is needed.

		State titleBarState = tg.getTitleBarState();
		boolean isDirty = tg.isDirty() && bw == TitleBarButtonWidget.CLOSE_BOX;

		boolean paintBackground = titleBarState == State.INACTIVE && state != State.INACTIVE && state != State.PRESSED;
		boolean paintIcon = state == State.ROLLOVER
			|| state == State.PRESSED && bw == TitleBarButtonWidget.RESIZE_BOX
			|| paintBackground && isDirty;

		if (!paintBackground && !paintIcon) {
			return;
		}

		Shape s = layoutInfo.getButtonShape(bounds, tg, bw);

		// If the title bar is inactive, then we must simulate the painting of the button in the active state.

		if (paintBackground) {
			switch (bw)
			{
				case CLOSE_BOX:
					g.setPaint(RED_COLOR);
					break;
				case MINIMIZE_BOX:
					g.setPaint(YELLOW_COLOR);
					break;
				case RESIZE_BOX:
					g.setPaint(GREEN_COLOR);
			}
			g.fill(s);

			RectangularShape rs = (RectangularShape) s;
			RectangularShape outline = (RectangularShape) rs.clone();
			outline.setFrameFromCenter(rs.getCenterX(), rs.getCenterY(), rs.getMinX() + 0.2, rs.getMinY() + 0.2);
			g.setPaint(new Color(0, 0, 0, 30));
			g.setStroke(new BasicStroke(0.8f));
			g.draw(outline);
		}

		// If the state is rollover we need to draw the appropriate icon.
		// If the state is pressed and the button is the resize icon, we need to draw the appropriate icon.
		// The document edited icon takes precedence over the rollover icon.
		// If the title bar state is inactive we may need to draw the document edited icon.

		if (paintIcon) {
			if (paintBackground && isDirty) {
				double dd = tg.getWidget() == TitleBarWidget.DOCUMENT_WINDOW ? 4 : 3;
				double dr = dd / 2.0;
				RectangularShape rs = (RectangularShape) s;
				Shape dot = new Ellipse2D.Double(rs.getCenterX() - dr, rs.getCenterY() - dr, dd, dd);
				g.setPaint(Color.BLACK);
				g.fill(dot);

			} else {
				Rectangle2D buttonBounds = s.getBounds2D();
				g.setPaint(new Color(100, 100, 100));
				g.setStroke(new BasicStroke(0.8f));

				if (bw == TitleBarButtonWidget.MINIMIZE_BOX) {
					double rx = buttonBounds.getX() + 2;
					double ry = buttonBounds.getCenterY() - 0.5;
					double rwidth = buttonBounds.getWidth() - 4;
					double rheight = 1;
					Shape icon = new Rectangle2D.Double(rx, ry, rwidth, rheight);
					g.fill(icon);
				}

				if (bw == TitleBarButtonWidget.CLOSE_BOX) {
					g.setStroke(new BasicStroke(2f));
					double a = 3.5;
					double x1 = buttonBounds.getX() + a;
					double y1 = buttonBounds.getY() + a;
					double x2 = buttonBounds.getX() + buttonBounds.getWidth() - a;
					double y2 = buttonBounds.getY() + buttonBounds.getHeight() - a;
					Shape icon = new Line2D.Double(x1, y1, x2, y2);
					g.draw(icon);
					icon = new Line2D.Double(x1, y2, x2, y1);
					g.draw(icon);
				}

				if (bw == TitleBarButtonWidget.RESIZE_BOX) {
					ResizeAction resizeAction = tg.getResizeAction();
					boolean isFullScreen = resizeAction == FULL_SCREEN_ENTER || resizeAction == FULL_SCREEN_EXIT;

					if (isFullScreen) {
						g.setPaint(new Color(100, 100, 100));

						if (resizeAction == FULL_SCREEN_ENTER) {
							// The icon is a pair of arrows on the diagonal, effectively two triangles
							double a = 3;
							double left = buttonBounds.getX() + a;
							double top = buttonBounds.getY() + a;
							double right = buttonBounds.getX() + buttonBounds.getWidth() - a;
							double bottom = buttonBounds.getY() + buttonBounds.getHeight() - a;

							double o = 0.8;
							Path2D.Double t = new Path2D.Double();
							t.moveTo(left, top+o);
							t.lineTo(right-o, bottom);
							t.lineTo(left, bottom);
							t.lineTo(left, top+o);
							g.fill(t);

							t = new Path2D.Double();
							t.moveTo(right, bottom-o);
							t.lineTo(left+o, top);
							t.lineTo(right, top);
							t.lineTo(right, bottom-o);
							g.fill(t);
						} else {
							// The icon is a pair of arrows on the diagonal, effectively two triangles
							double a = 1.5;
							double left = buttonBounds.getX() + a;
							double top = buttonBounds.getY() + a;
							double right = buttonBounds.getX() + buttonBounds.getWidth() - a;
							double bottom = buttonBounds.getY() + buttonBounds.getHeight() - a;

							double xcenter = (left + right) / 2;
							double ycenter = (top + bottom) / 2;
							double o = 0.2;
							Path2D.Double t = new Path2D.Double();
							t.moveTo(left, ycenter+o);
							t.lineTo(xcenter-o, bottom);
							t.lineTo(xcenter-o, ycenter+o);
							t.lineTo(left, ycenter+o);
							g.fill(t);

							t = new Path2D.Double();
							t.moveTo(right, ycenter-o);
							t.lineTo(xcenter+o, top);
							t.lineTo(xcenter+o, ycenter-o);
							t.lineTo(right, ycenter-o);
							g.fill(t);
						}
					} else {
						{
							double rx = buttonBounds.getX() + 2;
							double ry = buttonBounds.getCenterY() - 0.5;
							double rwidth = buttonBounds.getWidth() - 4;
							double rheight = 1;
							Shape icon = new Rectangle2D.Double(rx, ry, rwidth, rheight);
							g.fill(icon);
						}

						{
							double rx = buttonBounds.getCenterX() - 0.5;
							double ry = buttonBounds.getY() + 2;
							double rwidth = 1;
							double rheight = buttonBounds.getHeight() - 4;
							Shape icon = new Rectangle2D.Double(rx, ry, rwidth, rheight);
							g.setPaint(new Color(100, 100, 100));
							g.fill(icon);
						}
					}
				}
			}
		}
	}
}
