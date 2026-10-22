/*
 * Copyright (c) 2026 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.aqua.impl;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.violetlib.jnr.aqua.AquaUIPainter;
import org.violetlib.jnr.aqua.SegmentedButtonLayoutConfiguration;

import java.awt.*;
import java.awt.geom.Rectangle2D;

/**

*/

public class Outliner_27
  extends Outliner_26
{
    public Outliner_27(@NotNull LayoutInfo_26 uiLayout)
    {
        super(uiLayout);
    }

    @Override
    protected @Nullable Shape getSegmentedButtonOutline(@NotNull Rectangle2D bounds,
                                                        @NotNull SegmentedButtonLayoutConfiguration g)
    {
        AquaUIPainter.SegmentedButtonWidget w = g.getWidget();
        if (w == AquaUIPainter.SegmentedButtonWidget.BUTTON_TAB) {
            return getGlassButtonOutline(bounds, g.getSize(), GLASS_DELTA);
        }
        return super.getSegmentedButtonOutline(bounds, g);
    }
}
