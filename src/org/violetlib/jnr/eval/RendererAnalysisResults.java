/*
 * Copyright (c) 2015 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.eval;

/**
  The results of analysis.
*/

public class RendererAnalysisResults
{
    private final float fixedWidth;
    private final float fixedHeight;
    private final float xOffset;
    private final float yOffset;
    private final float widthAdjustment;
    private final float heightAdjustment;

    public RendererAnalysisResults(float fixedWidth,
                                   float fixedHeight,
                                   float xOffset,
                                   float yOffset,
                                   float widthAdjustment,
                                   float heightAdjustment)
    {
        this.fixedWidth = fixedWidth;
        this.fixedHeight = fixedHeight;
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        this.widthAdjustment = widthAdjustment;
        this.heightAdjustment = heightAdjustment;
    }

    public float getFixedWidth()
    {
        return fixedWidth;
    }

    public float getFixedHeight()
    {
        return fixedHeight;
    }

    public float getXOffset()
    {
        return xOffset;
    }

    public float getYOffset()
    {
        return yOffset;
    }

    public float getWidthAdjustment()
    {
        return widthAdjustment;
    }

    public float getHeightAdjustment()
    {
        return heightAdjustment;
    }
}
