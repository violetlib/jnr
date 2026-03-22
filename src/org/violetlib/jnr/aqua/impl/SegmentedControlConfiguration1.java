/*
 * Copyright (c) 2020-2025 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.aqua.impl;

import org.jetbrains.annotations.NotNull;
import org.violetlib.jnr.aqua.AquaUIPainter;

/**
  A configuration defining the parameters that must be specified to render a segmented control with one segment.
*/

public class SegmentedControlConfiguration1
  extends SegmentedControlConfiguration
{
    public final float w;
    public final boolean isSelected;

    /**
      Create a configuration for a single segment control.
      @param widget A widget defining the control style. Toolbar styles should not be used.
      @param isToolbar True if and only if the control should be rendered as it would in a toolbar.
      @param sz The size variant.
      @param st The control state.
      @param w The nominal width (in points) of the segment.
      @param isSelected True if the segment should be selected.
      @param tracking The tracking mode (select one or select any). Although there is no behavioral difference
      between select one and select any, the rendering can be different.
    */

    public SegmentedControlConfiguration1(@NotNull AquaUIPainter.SegmentedButtonWidget widget,
                                          boolean isToolbar,
                                          @NotNull AquaUIPainter.Size sz,
                                          @NotNull AquaUIPainter.State st,
                                          float w,
                                          boolean isSelected,
                                          @NotNull AquaUIPainter.SwitchTracking tracking
                                          )
      throws IllegalArgumentException
    {
        super(widget, isToolbar, sz, st, tracking);

        validateSegmentWidth(w);
        this.w = w;
        this.isSelected = isSelected;
    }
}
