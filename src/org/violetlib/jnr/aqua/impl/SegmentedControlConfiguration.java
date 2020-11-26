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

*/

public abstract class SegmentedControlConfiguration
{
    public final @NotNull AquaUIPainter.SegmentedButtonWidget widget;
    public final boolean isToolbar;
    public final @NotNull AquaUIPainter.Size size;
    public final @NotNull AquaUIPainter.State state;

    protected SegmentedControlConfiguration(@NotNull AquaUIPainter.SegmentedButtonWidget widget,
                                            boolean isToolbar,
                                            @NotNull AquaUIPainter.Size size,
                                            @NotNull AquaUIPainter.State state)
    {
        this.widget = widget;
        this.isToolbar = isToolbar;
        this.size = size;
        this.state = state;

        if (widget.isToolbar()) {
            throw new UnsupportedOperationException("Toolbar widget not supported");
        }
    }

    protected static void validateSegmentWidth(float w)
    {
        if (w < 1 || w > 10000) {
            throw new IllegalArgumentException("Invalid or unsupported segment width");
        }
    }
}
