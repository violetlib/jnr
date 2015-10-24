/*
 * Copyright (c) 2015 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.geom;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

import org.jetbrains.annotations.*;

/**
	An outline shape that can be expanded to create the shape or boundary of a focus ring. A goal of the expansion is that
	the expanded version of an outline should have a uniform space between it and the original outline, simulating the
	effect of drawing a stroke. There are specific kinds of expandable outlines that support expansion with this property.
	Other shapes can be used to create outlines that expand in a generic way that may or may not meet the objective.
*/

public abstract class ExpandableOutline
{
	public interface ExpandableShape
	{
		@NotNull Shape createExpandedShape(float offset);
		@NotNull Shape createTranslatedShape(double x, double y);
	}

	/**
		Return the outline as a shape, optionally expanded.

		@param offset The expansion offset. If zero, the original outline is returned. If positive, a larger outline is
		returned. If negative, a smaller outline is returned.
		@return the outline, altered as specified.
	*/

	public abstract @NotNull Shape getShape(float offset);

	/**
		Convert a shape to the equivalent expandable outline.

		@param s The shape.
		@return an expandable outline equivalent to {@code s}.
	*/

	public static @NotNull ExpandableOutline fromShape(@NotNull Shape s)
	{
		if (s instanceof RoundRectangle2D) {
			return new ExpandableRoundRectOutline((RoundRectangle2D) s);
		} else if (s instanceof Rectangle2D) {
			return new ExpandableRectangleOutline((Rectangle2D) s);
		} else if (s instanceof Ellipse2D) {
			return new ExpandableEllipseOutline((Ellipse2D) s);
		} else if (s instanceof ExpandableShape) {
			ExpandableShape es = (ExpandableShape) s;
			return new ExpandableShapeOutline(es);
		} else {
			return new GenericOutline(s);
		}
	}

	/**
		Create a translated version of a shape. Unlike AffineTransform.createTransformedShape(), this method preserves the
		native expansion capabilities of an expandable outline.

		@param s The shape.
		@return the translated version of {@code s}.
	*/

	public static @NotNull Shape createTranslatedShape(@NotNull Shape s, double x, double y)
	{
		if (s instanceof RoundRectangle2D) {
			RoundRectangle2D rr = (RoundRectangle2D) s;
			return new RoundRectangle2D.Double(rr.getX() + x, rr.getY() + y, rr.getWidth(), rr.getHeight(), rr.getArcWidth(), rr.getArcHeight());
		} else if (s instanceof Rectangle2D) {
			Rectangle2D rr = (Rectangle2D) s;
			return new Rectangle2D.Double(rr.getX() + x, rr.getY() + y, rr.getWidth(), rr.getHeight());
		} else if (s instanceof Ellipse2D) {
			Ellipse2D rr = (Ellipse2D) s;
			return new Ellipse2D.Double(rr.getX() + x, rr.getY() + y, rr.getWidth(), rr.getHeight());
		} else if (s instanceof ExpandableShape) {
			ExpandableShape es = (ExpandableShape) s;
			return es.createTranslatedShape(x, y);
		} else {
			AffineTransform tr = AffineTransform.getTranslateInstance(x, y);
			return tr.createTransformedShape(s);
		}
	}

	private static class ExpandableShapeOutline
		extends ExpandableOutline
	{
		private final @NotNull ExpandableShape s;

		public ExpandableShapeOutline(@NotNull ExpandableShape s)
		{
			this.s = s;
		}

		@Override
		public @NotNull Shape getShape(float offset)
		{
			return s.createExpandedShape(offset);
		}
	}

	private static class GenericOutline
		extends ExpandableOutline
	{
		private final @NotNull Shape s;

		public GenericOutline(@NotNull Shape s)
		{
			this.s = s;
		}

		@Override
		public @NotNull Shape getShape(float offset)
		{
			Rectangle2D bounds = s.getBounds2D();
			double cx = bounds.getCenterX();
			double cy = bounds.getCenterY();
			AffineTransform tr = AffineTransform.getTranslateInstance(cx, cy);
			double xfactor = (bounds.getWidth() + 2 * offset) / bounds.getWidth();
			double yfactor = (bounds.getHeight() + 2 * offset) / bounds.getHeight();
			tr.scale(xfactor, yfactor);
			tr.translate(-cx, -cy);
			return tr.createTransformedShape(s);
		}
	}
}
