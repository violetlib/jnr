/*
 * Copyright (c) 2020 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.aqua.impl;

import org.violetlib.jnr.aqua.AquaUIPainter;

import org.jetbrains.annotations.*;

/**
  A configuration defining the parameters that must be specified to render a segmented control with four segments.
*/

public class SegmentedControlConfiguration4
  extends SegmentedControlConfiguration
{
    public final float w1;
    public final float w2;
    public final float w3;
    public final float w4;
    public final @NotNull AquaUIPainter.SwitchTracking tracking;
    public final boolean s1;
    public final boolean s2;
    public final boolean s3;
    public final boolean s4;

    /**
      Create a configuration for a 4 segment control.
      @param widget A widget defining the control style. Toolbar styles should not be used.
      @param isToolbar True if and only if the control should be rendered as it would in a toolbar.
      @param sz The size variant.
      @param st The control state.
      @param tr The tracking mode (select one or select any).
      @param w1 The nominal width (in points) of the 1st (leftmost) segment.
      @param w2 The nominal width (in points) of the 2nd segment.
      @param w3 The nominal width (in points) of the 3rd segment.
      @param w4 The nominal width (in points) of the 4th (rightmost) segment.
      @param s1 True if the 1st segment should be selected.
      @param s2 True if the 2nd segment should be selected.
      @param s3 True if the 3rd segment should be selected.
      @param s4 True if the 4th segment should be selected.
    */

    public SegmentedControlConfiguration4(@NotNull AquaUIPainter.SegmentedButtonWidget widget,
                                          boolean isToolbar,
                                          @NotNull AquaUIPainter.Size sz,
                                          @NotNull AquaUIPainter.State st,
                                          @NotNull AquaUIPainter.SwitchTracking tr,
                                          float w1, float w2, float w3, float w4,
                                          boolean s1, boolean s2, boolean s3, boolean s4
    )
      throws IllegalArgumentException
    {
        super(widget, isToolbar, sz, st);

        validateSegmentWidth(w1);
        validateSegmentWidth(w2);
        validateSegmentWidth(w3);
        validateSegmentWidth(w4);

        this.w1 = w1;
        this.w2 = w2;
        this.w3 = w3;
        this.w4 = w4;
        this.tracking = tr;
        this.s1 = s1;
        this.s2 = s2;
        this.s3 = s3;
        this.s4 = s4;
    }
}
