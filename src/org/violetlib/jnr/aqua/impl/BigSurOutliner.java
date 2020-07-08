/*
 * Copyright (c) 2020 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.aqua.impl;

import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

import org.violetlib.geom.GeneralRoundRectangle;
import org.violetlib.jnr.aqua.AquaUIPainter.Position;
import org.violetlib.jnr.aqua.AquaUIPainter.SegmentedButtonWidget;
import org.violetlib.jnr.aqua.AquaUIPainter.Size;
import org.violetlib.jnr.aqua.SegmentedButtonLayoutConfiguration;

import org.jetbrains.annotations.*;

/**

*/
public class BigSurOutliner
  extends YosemiteOutliner
{
    public BigSurOutliner(@NotNull YosemiteLayoutInfo uiLayout)
    {
        super(uiLayout);
    }

    @Override
    protected @Nullable Shape getSegmentedButtonOutline(@NotNull Rectangle2D bounds, @NotNull SegmentedButtonLayoutConfiguration g)
    {
        SegmentedButtonWidget bw = g.getWidget();
        Size sz = g.getSize();
        Position pos = g.getPosition();

        double x = bounds.getX();
        double y = bounds.getY();
        double width = bounds.getWidth();
        double height = bounds.getHeight();

        boolean isLeft = pos == Position.FIRST;

        double corner = 8;

        switch (bw)
        {
            case BUTTON_TAB:
            case BUTTON_SEGMENTED:
            case BUTTON_SEGMENTED_SEPARATED:
                corner = 6;

                x += size2D(sz, isLeft ? 0.5f : 0, 0, 0);
                y += size2D(sz, 0.5f, 0.5f, 0.5f);
                height += size2D(sz, 1, -2, -2);  // height changed for regular size
                width += size2D(sz, -0.5f, 0, 0);

                if (sz == Size.SMALL || sz == Size.MINI) {
                    if (pos == Position.FIRST) {
                        x += 0.5;
                        width -= 0.5;
                    } else if (pos == Position.LAST) {
                        width -= 0.5;
                    } else if (pos == Position.ONLY) {
                        x += 0.5;
                        width -= 1;
                    }
                }

                break;

            case BUTTON_SEGMENTED_INSET:
                break;

            case BUTTON_SEGMENTED_SCURVE:
            case BUTTON_SEGMENTED_TEXTURED:
            case BUTTON_SEGMENTED_TOOLBAR:
                width += size2D(sz, 0, 0, 0);
                height += size2D(sz, -1, -1, -1);
                break;

            case BUTTON_SEGMENTED_TEXTURED_TOOLBAR:
                width += size2D(sz, 0, 0, 0);
                height += size2D(sz, -1, -2, -1);
                break;

            case BUTTON_SEGMENTED_TEXTURED_SEPARATED:
            case BUTTON_SEGMENTED_TEXTURED_SEPARATED_TOOLBAR:
                if (pos == Position.ONLY || pos == Position.FIRST) {
                    width -= 0.5;
                }
                if (pos == Position.MIDDLE) {
                    x -= 0.5;
                }
                height -= size2D(sz, 1, 0.5f, 1);
                break;

            case BUTTON_SEGMENTED_SMALL_SQUARE:
                corner = 0;
                break;
        }

        if (pos == Position.ONLY || bw.isSeparated()) {
            return new RoundRectangle2D.Double(x, y, width, height, corner, corner);
        }

        if (pos == Position.FIRST) {
            return new GeneralRoundRectangle(x, y, width, height, corner, corner, 0, 0, 0, 0, corner, corner);
        }

        if (pos == Position.LAST) {
            return new GeneralRoundRectangle(x, y, width, height, 0, 0, corner, corner, corner, corner, 0, 0);
        }

        return new Rectangle2D.Double(x, y, width, height);
    }
}
