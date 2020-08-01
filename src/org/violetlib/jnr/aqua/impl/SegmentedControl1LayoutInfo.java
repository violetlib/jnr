/*
 * Copyright (c) 2020 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.aqua.impl;

/**
  A specification of the layout of the rendering of a single segment control in a raster. The raster may be larger than
  the visual bounds of the control, a requirement imposed by the native renderer.
*/

public class SegmentedControl1LayoutInfo
{
    public final float widthAdjustment;

    public SegmentedControl1LayoutInfo(float widthAdjustment)
    {
        this.widthAdjustment = widthAdjustment;
    }
}
