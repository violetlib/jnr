/*
 * Copyright (c) 2020 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.aqua.impl;

/**
  A configuration defining the parameters that must be specified to render a segmented button by rendering a control
  with one segment.
*/

public class SegmentButtonRenderingConfiguration1
  extends SegmentButtonRenderingConfiguration
{
    public final boolean isSelected;

    public SegmentButtonRenderingConfiguration1(float scale,
                                                boolean isSelected,
                                                float segmentWidth,
                                                int rasterWidth,
                                                int rasterHeight)
    {
        super(scale, segmentWidth, rasterWidth, rasterHeight);

        this.isSelected = isSelected;
    }
}
