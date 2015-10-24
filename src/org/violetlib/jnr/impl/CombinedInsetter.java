/*
 * Copyright (c) 2015 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.impl;

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.geom.Dimension2D;
import java.awt.geom.Rectangle2D;

import org.jetbrains.annotations.*;

import org.violetlib.jnr.Insets2D;
import org.violetlib.jnr.Insetter;
import org.violetlib.jnr.InsetterNotInvertibleException;

/**
	Create an insetter from two single axis insetters.
*/

public class CombinedInsetter
	implements Insetter
{
	private final @NotNull Insetter1 horizontal;
	private final @NotNull Insetter1 vertical;

	public CombinedInsetter(@NotNull Insetter1 horizontal, @NotNull Insetter1 vertical)
	{
		this.horizontal = horizontal;
		this.vertical = vertical;
	}

	@Override
	public @NotNull Rectangle2D applyToBounds2D(@NotNull Rectangle2D bounds)
	{
		float x = (float) bounds.getX();
		float y = (float) bounds.getY();
		float w = (float) bounds.getWidth();
		float h = (float) bounds.getHeight();
		return apply(x, y, w, h);
	}

	@Override
	public @NotNull Rectangle2D apply2D(double width, double height)
	{
		return apply(0, 0, (float) width, (float) height);
	}

	protected @NotNull Rectangle2D apply(float cx, float cy, float cwidth, float cheight)
	{
		float x = cx + horizontal.getRegionOrigin(cwidth);
		float y = cy + vertical.getRegionOrigin(cheight);
		float w = horizontal.getRegionSize(cwidth);
		float h = vertical.getRegionSize(cheight);
		return new Rectangle2D.Float(x, y, w, h);
	}

	@Override
	public @NotNull Rectangle applyToBounds(@NotNull Rectangle bounds)
	{
		return apply(bounds.x, bounds.y, bounds.width, bounds.height);
	}

	@Override
	public @NotNull Rectangle apply(int width, int height)
	{
		return apply(0, 0, width, height);
	}

	protected @NotNull Rectangle apply(int cx, int cy, int cwidth, int cheight)
	{
		double xx = cx + horizontal.getRegionOrigin(cwidth);
		double ww = horizontal.getRegionSize(cwidth);
		int x = (int) Math.ceil(xx);
		ww -= (x - xx);
		int w = (int) Math.floor(ww);

		double yy = cy + vertical.getRegionOrigin(cheight);
		double hh = vertical.getRegionSize(cheight);
		int y = (int) Math.ceil(yy);
		hh -= (y - yy);
		int h = (int) Math.floor(hh);

		return new Rectangle(x, y, w, h);
	}

	@Override
	public boolean isInvertible()
	{
		return horizontal.isInvertible() && vertical.isInvertible();
	}

	@Override
	public @NotNull Dimension2D expand2D(@NotNull Dimension2D regionSize)
	throws InsetterNotInvertibleException
	{
		// I guess no one wants floating point dimensions, if they are used mainly for layout.
		int width = (int) Math.ceil(horizontal.getComponentSize((float) regionSize.getWidth()));
		int height = (int) Math.ceil(vertical.getComponentSize((float) regionSize.getHeight()));
		return new Dimension(width, height);
	}

	@Override
	public @NotNull Dimension expand(@NotNull Dimension regionSize)
	throws InsetterNotInvertibleException
	{
		int width = (int) Math.ceil(horizontal.getComponentSize(regionSize.width));
		int height = (int) Math.ceil(vertical.getComponentSize(regionSize.height));
		return new Dimension(width, height);
	}

	@Override
	public @Nullable Insets2D asInsets2D()
	{
		float left = horizontal.getFixedInset1();
		if (left < 0) {
			return null;
		}
		float right = horizontal.getFixedInset2();
		if (right < 0) {
			return null;
		}
		float top = vertical.getFixedInset1();
		if (top < 0) {
			return null;
		}
		float bottom = vertical.getFixedInset2();
		if (bottom < 0) {
			return null;
		}
		return new Insets2D(top, left, bottom, right);
	}

	@Override
	public @Nullable Insets asInsets()
	{
		float left = horizontal.getFixedInset1();
		if (left < 0) {
			return null;
		}
		float right = horizontal.getFixedInset2();
		if (right < 0) {
			return null;
		}
		float top = vertical.getFixedInset1();
		if (top < 0) {
			return null;
		}
		float bottom = vertical.getFixedInset2();
		if (bottom < 0) {
			return null;
		}
		int tn = (int) Math.ceil(top);
		int ln = (int) Math.ceil(left);
		int bn = (int) Math.ceil(bottom);
		int rn = (int) Math.ceil(right);
		return new Insets(tn, ln, bn, rn);
	}

	public float getFixedRegionWidth()
	{
		return horizontal.getFixedRegionSize();
	}

	public float getFixedRegionHeight()
	{
		return vertical.getFixedRegionSize();
	}
}
