/*
 * Copyright (c) 2018 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.impl;

import java.awt.Color;

import org.jetbrains.annotations.*;

/**
  A basic renderer that paints a color using an alpha mask defined by a basic renderer.
*/

public class PaintUsingMaskRenderer
  extends PostProcessedRenderer
{
    private final int redPaint;
    private final int greenPaint;
    private final int bluePaint;
    private final int alphaPaint;

    public PaintUsingMaskRenderer(@NotNull BasicRenderer r, @NotNull Color c)
    {
        super(r);

        this.redPaint = c.getRed();
        this.greenPaint = c.getGreen();
        this.bluePaint = c.getBlue();
        this.alphaPaint = c.getAlpha();
    }

    @Override
    protected int processPixel(int row, int col, int red, int green, int blue, int alpha)
    {
        if (alpha > 0) {
            alpha = alpha * alphaPaint / 255;
            return createPixel(redPaint, greenPaint, bluePaint, alpha);
        } else {
            return 0;
        }
    }
}
