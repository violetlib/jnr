/*
 * Copyright (c) 2015 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.eval;

import java.awt.Rectangle;
import java.awt.Shape;

import org.jetbrains.annotations.*;

import org.violetlib.jnr.LayoutInfo;
import org.violetlib.jnr.NullPainter;
import org.violetlib.jnr.Painter;
import org.violetlib.jnr.aqua.AquaUIPainter;
import org.violetlib.jnr.aqua.Configuration;
import org.violetlib.jnr.aqua.LayoutConfiguration;
import org.violetlib.jnr.aqua.impl.AquaUIPainterAbstractBase;

/**
	This painter does not paint. It provides the renderer to an evaluator.
*/

public abstract class AquaEvaluatingPainter
	extends AquaUIPainterAbstractBase
	implements AquaUIPainter
{
	@Override
	public @NotNull Shape getOutline(@NotNull LayoutConfiguration g)
	{
		return new Rectangle(0, 0, 0, 0);
	}

	@Override
	public @NotNull Painter getPainter(@NotNull Configuration g)
		throws UnsupportedOperationException
	{
		LayoutInfo layoutInfo = uiLayout.getLayoutInfo((LayoutConfiguration) g);
		evaluate(g, layoutInfo);
		return new NullPainter(layoutInfo);
	}

	protected abstract void evaluate(@NotNull Configuration g, @NotNull LayoutInfo layoutInfo);
}
