/*
 * Copyright (c) 2020 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.aqua.impl;

/**
  A configuration defining the parameters that must be specified to render a segmented button.
*/

public abstract class SegmentButtonRenderingConfiguration
{
    public final float scale; // the display scale factor
    public final float segmentWidth; // the nominal width of the segment representing the button
    public final int rasterWidth; // the required width of the raster
    public final int rasterHeight; // the required height of the raster

    protected SegmentButtonRenderingConfiguration(float scale, float segmentWidth, int rasterWidth, int rasterHeight)
    {
        this.scale = scale;
        this.segmentWidth = segmentWidth;
        this.rasterWidth = rasterWidth;
        this.rasterHeight = rasterHeight;
    }
}
