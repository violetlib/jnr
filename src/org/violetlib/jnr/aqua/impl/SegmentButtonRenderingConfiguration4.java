/*
 * Copyright (c) 2020 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.aqua.impl;

import java.awt.geom.Rectangle2D;

/**
  A configuration defining the parameters that must be specified to render a segmented button by rendering a control
  with four segments.
*/

public class SegmentButtonRenderingConfiguration4
  extends SegmentButtonRenderingConfiguration
{
    public final int designatedSegment; // 1 to 4
    public final int selectedSegment; // 0 or 1 to 4
    public final float otherSegmentWidth; // the nominal width of other segments
    public final Rectangle2D bounds; // the bounds of the button rendering relative to the raster

    public SegmentButtonRenderingConfiguration4(float scale,
                                                int designatedSegment,
                                                int selectedSegment,
                                                float designatedSegmentWidth,
                                                float otherSegmentWidth,
                                                int rasterWidth,
                                                int rasterHeight,
                                                Rectangle2D bounds)
    {
        super(scale, designatedSegmentWidth, rasterWidth, rasterHeight);

        this.designatedSegment = designatedSegment;
        this.selectedSegment = selectedSegment;
        this.otherSegmentWidth = otherSegmentWidth;
        this.bounds = bounds;
    }
}
