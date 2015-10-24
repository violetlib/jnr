/*
 * Copyright (c) 2015 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.impl;

import java.text.DecimalFormat;

import org.jetbrains.annotations.*;

import org.violetlib.jnr.aqua.AquaUIPainter;

/**
	Utilities
*/

public class JNRUtils
{
	static final @NotNull DecimalFormat df2 = new DecimalFormat("0.00");

	static {
		df2.setDecimalSeparatorAlwaysShown(true);
	}

	public static int size(@NotNull AquaUIPainter.Size sz, int regular, int small, int mini)
	{
		switch (sz) {
			case SMALL:
				return small;
			case MINI:
				return mini;
			default:
				return regular;
		}
	}

	public static float size2D(@NotNull AquaUIPainter.Size sz, float regular, float small, float mini)
	{
		switch (sz) {
			case SMALL:
				return small;
			case MINI:
				return mini;
			default:
				return regular;
		}
	}

	public static @NotNull String format2(double v)
	{
		return df2.format(v);
	}

	public static @Nullable BasicRendererDescription toBasicRendererDescription(@NotNull RendererDescription rd)
	{
		if (rd instanceof BasicRendererDescription) {
			return (BasicRendererDescription) rd;
		}
		return null;
	}

	/**
		Return the basic renderer description corresponding to a renderer description assuming a specific scale factor.
		@param rd The source renderer description.
		@param scaleFactor The scale factor.
		@return the basic renderer description corresponding to {@code rd}, or null if not determinable.
	*/

	public static @Nullable BasicRendererDescription toBasicRendererDescription(@NotNull RendererDescription rd, int scaleFactor)
	{
		if (rd instanceof BasicRendererDescription) {
			return (BasicRendererDescription) rd;
		} else if (rd instanceof MultiResolutionRendererDescription) {
			MultiResolutionRendererDescription mrd = (MultiResolutionRendererDescription) rd;
			RendererDescription ard = scaleFactor == 1 ? mrd.getDescription1() : mrd.getDescription2();
			if (ard instanceof BasicRendererDescription) {
				return (BasicRendererDescription) ard;
			}
		}
		return null;
	}

	public static @NotNull RendererDescription adjustRendererDescription(@NotNull RendererDescription rd,
																																			 float deltaX, float deltaY, float deltaWA, float deltaHA)
		throws UnsupportedOperationException
	{
		if (deltaX == 0 && deltaY == 0 && deltaWA == 0 && deltaHA == 0) {
			return rd;
		}

		BasicRendererDescription brd = toBasicRendererDescription(rd);
		if (brd != null) {
			return brd.withAdjustments(deltaX, deltaY, deltaWA, deltaHA);
		}

		if (rd instanceof MultiResolutionRendererDescription) {
			MultiResolutionRendererDescription mrd = (MultiResolutionRendererDescription) rd;
			BasicRendererDescription rd1 = toBasicRendererDescription(mrd.getDescription1());
			BasicRendererDescription rd2 = toBasicRendererDescription(mrd.getDescription2());
			if (rd1 != null && rd2 != null) {
				rd1 = rd1.withAdjustments(deltaX, deltaY, deltaWA, deltaHA);
				rd2 = rd2.withAdjustments(deltaX, deltaY, deltaWA, deltaHA);
				return new MultiResolutionRendererDescription(rd1, rd2);
			}
		}

		throw new UnsupportedOperationException("Renderer description cannot be adjusted");
	}

	public static final float NO_CHANGE = -123456;

	public static @NotNull RendererDescription changeRendererDescription(@NotNull RendererDescription rd,
																																			 float x, float y, float wa, float ha)
		throws UnsupportedOperationException
	{
		if (x == NO_CHANGE && y == NO_CHANGE && wa == NO_CHANGE && ha == NO_CHANGE) {
			return rd;
		}

		BasicRendererDescription brd = toBasicRendererDescription(rd);
		if (brd != null) {
			return change(brd, x, y, wa, ha);
		}

		if (rd instanceof MultiResolutionRendererDescription) {
			MultiResolutionRendererDescription mrd = (MultiResolutionRendererDescription) rd;
			BasicRendererDescription rd1 = toBasicRendererDescription(mrd.getDescription1());
			BasicRendererDescription rd2 = toBasicRendererDescription(mrd.getDescription2());
			if (rd1 != null && rd2 != null) {
				rd1 = change(rd1, x, y, wa, ha);
				rd2 = change(rd2, x, y, wa, ha);
				return new MultiResolutionRendererDescription(rd1, rd2);
			}
		}

		throw new UnsupportedOperationException("Renderer description cannot be changed");
	}

	private static @NotNull BasicRendererDescription change(@NotNull BasicRendererDescription brd, float x, float y, float wa, float ha)
	{
		float nx = x == NO_CHANGE ? brd.getXOffset() : x;
		float ny = y == NO_CHANGE ? brd.getYOffset() : y;
		float nwa = wa == NO_CHANGE ? brd.getWidthAdjustment() : wa;
		float nha = ha == NO_CHANGE ? brd.getHeightAdjustment(): ha;
		return new BasicRendererDescription(nx, ny, nwa, nha);
	}
}
