/*
 * Copyright (c) 2015-2025 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.eval;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.violetlib.jnr.LayoutInfo;
import org.violetlib.jnr.NullPainter;
import org.violetlib.jnr.Painter;
import org.violetlib.jnr.aqua.AquaUIPainter;
import org.violetlib.jnr.aqua.Configuration;
import org.violetlib.jnr.aqua.LayoutConfiguration;
import org.violetlib.jnr.aqua.impl.AquaUIPainterAbstractBase;
import org.violetlib.jnr.impl.RendererDescription;

import java.awt.*;

/**
  This painter does not paint. It provides the renderer to an evaluator.
*/

public abstract class AquaEvaluatingPainter
  extends AquaUIPainterAbstractBase
  implements AquaUIPainter
{
    public AquaEvaluatingPainter()
    {
    }

    @Override
    public @NotNull Shape getOutline(@NotNull LayoutConfiguration g)
    {
        return new Rectangle(0, 0, 0, 0);
    }

    @Override
    public @NotNull Painter getPainter(@NotNull Configuration g)
      throws UnsupportedOperationException
    {
        return getPainter(g, null);
    }

    @Override
    public @NotNull Painter getPainter(@NotNull Configuration g, @Nullable RendererDescription rd)
      throws UnsupportedOperationException
    {
        LayoutInfo layoutInfo = uiLayout.getLayoutInfo((LayoutConfiguration) g);
        evaluate(g, layoutInfo);
        return new NullPainter(layoutInfo);
    }

    protected abstract void evaluate(@NotNull Configuration g, @NotNull LayoutInfo layoutInfo);
}
