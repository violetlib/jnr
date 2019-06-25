/*
 * Copyright (c) 2015 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.geom;

import java.awt.Shape;
import java.awt.geom.RoundRectangle2D;

import org.jetbrains.annotations.*;

/**
  An expandable outline in the form of a rounded rectangle.
*/

public final class ExpandableRoundRectOutline
  extends ExpandableOutline
{
    private final double x;
    private final double y;
    private final double width;
    private final double height;
    private final double arcWidth;
    private final double arcHeight;

    public ExpandableRoundRectOutline(double x, double y, double width, double height,
                                      double arcWidth, double arcHeight)
    {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.arcWidth = arcWidth;
        this.arcHeight = arcHeight;
    }

    public ExpandableRoundRectOutline(@NotNull RoundRectangle2D source)
    {
        this.x = source.getX();
        this.y = source.getY();
        this.width = source.getWidth();
        this.height = source.getHeight();
        this.arcWidth = source.getArcWidth();
        this.arcHeight = source.getArcHeight();
    }

    @Override
    public @NotNull Shape getShape(float offset) {
        double nx = x - offset;
        double ny = y - offset;
        double nwidth = width + 2 * offset;
        double nheight = height + 2 * offset;
        double narcWidth = arcWidth + offset;
        double narcHeight = arcHeight + offset;
        return new RoundRectangle2D.Double(nx, ny, nwidth, nheight, narcWidth, narcHeight);
    }
}
