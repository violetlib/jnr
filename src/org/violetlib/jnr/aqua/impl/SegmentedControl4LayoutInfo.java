/*
 * Copyright (c) 2020 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.aqua.impl;

import org.jetbrains.annotations.*;

/**
  A specification of the layout of the rendering of a 4 segment control in a raster. The raster may be larger than the
  visual bounds of the control, a requirement imposed by the native renderer.
*/

public class SegmentedControl4LayoutInfo
{
    public enum DividerPosition { LEFT, RIGHT, CENTER }

    /**
      The divider position identifies the position of each divider relative to the boundary where two segments meet.
      In 1x rendering, the divider is always to the left of the boundary.
      In 2x rendering, there are three options: on the left side, on the right side, and straddling the boundary.
    */

    public final @NotNull DividerPosition dividerPosition;
    public final float dividerVisualWidth;
    public final float firstSegmentWidthAdjustment;
    public final float middleSegmentWidthAdjustment;
    public final float lastSegmentWidthAdjustment;

    public SegmentedControl4LayoutInfo(@NotNull DividerPosition dividerPosition,
                                       float dividerVisualWidth,
                                       float firstSegmentWidthAdjustment,
                                       float middleSegmentWidthAdjustment,
                                       float lastSegmentWidthAdjustment)
    {
        this.dividerPosition = dividerPosition;
        this.dividerVisualWidth = dividerVisualWidth;
        this.firstSegmentWidthAdjustment = firstSegmentWidthAdjustment;
        this.middleSegmentWidthAdjustment = middleSegmentWidthAdjustment;
        this.lastSegmentWidthAdjustment = lastSegmentWidthAdjustment;
    }

    public SegmentedControl4LayoutInfo(@NotNull DividerPosition dividerPosition,
                                       double dividerVisualWidth,
                                       double firstSegmentWidthAdjustment,
                                       double middleSegmentWidthAdjustment,
                                       double lastSegmentWidthAdjustment)
    {
        this.dividerPosition = dividerPosition;
        this.dividerVisualWidth = (float) dividerVisualWidth;
        this.firstSegmentWidthAdjustment = (float) firstSegmentWidthAdjustment;
        this.middleSegmentWidthAdjustment = (float) middleSegmentWidthAdjustment;
        this.lastSegmentWidthAdjustment = (float) lastSegmentWidthAdjustment;
    }

    public @NotNull SegmentedControl4LayoutInfo withDividerPosition(@NotNull DividerPosition pos)
    {
        return new SegmentedControl4LayoutInfo(pos, dividerVisualWidth,
          firstSegmentWidthAdjustment, middleSegmentWidthAdjustment, lastSegmentWidthAdjustment);
    }

    public @NotNull SegmentedControl4LayoutInfo withDividerVisualWidth(double dividerVisualWidth)
    {
        return new SegmentedControl4LayoutInfo(dividerPosition, (float) dividerVisualWidth,
          firstSegmentWidthAdjustment, middleSegmentWidthAdjustment, lastSegmentWidthAdjustment);
    }

    public @NotNull SegmentedControl4LayoutInfo withFirstSegmentWidthAdjustment(double first)
    {
        return new SegmentedControl4LayoutInfo(dividerPosition, dividerVisualWidth,
          first, middleSegmentWidthAdjustment, lastSegmentWidthAdjustment);
    }

    public @NotNull SegmentedControl4LayoutInfo withLastSegmentWidthAdjustment(double last)
    {
        return new SegmentedControl4LayoutInfo(dividerPosition, dividerVisualWidth,
          firstSegmentWidthAdjustment, middleSegmentWidthAdjustment, last);
    }
}
