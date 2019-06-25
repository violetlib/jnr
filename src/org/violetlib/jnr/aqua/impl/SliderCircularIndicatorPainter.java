/*
 * Copyright (c) 2015-2018 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.aqua.impl;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;

import org.violetlib.jnr.impl.Colors;
import org.violetlib.jnr.impl.JNRPlatformUtils;
import org.violetlib.vappearances.VAppearance;

import org.jetbrains.annotations.*;

/**
  Paint a circular slider dimple.
*/

public class SliderCircularIndicatorPainter
{
    protected final double x;
    protected final double y;
    protected final double radius;
    protected final double zeroAngle;
    protected final double p;

    protected final @NotNull Colors colors;

    public SliderCircularIndicatorPainter(double x,
                                          double y,
                                          double radius,
                                          double zeroAngle,
                                          double p,
                                          @Nullable VAppearance appearance)
    {
        this.x = x;
        this.y = y;
        this.radius = radius;
        this.zeroAngle = zeroAngle;
        this.p = p;
        this.colors = Colors.getColors(appearance);
    }

    public void paint(@NotNull Graphics2D g)
    {
        double d = 6;
        double angle = zeroAngle - p * 2 * Math.PI;
        double x0 = x + Math.cos(angle) * radius;
        double y0 = y - Math.sin(angle) * radius;
        Shape s = new Ellipse2D.Double(x0 - d / 2, y0 - d / 2, d, d);

        g.setColor(colors.get("circularSliderDimple"));
        g.fill(s);

        Color top = colors.getOptional("circularSliderDimpleTop");
        if (top != null) {
            int platformVersion = JNRPlatformUtils.getPlatformVersion();
            if (platformVersion < 101400) {
                // TBD: looks more like a gradient
                s = new Ellipse2D.Double(x0 - d / 4, y0 - d / 2, d / 2, d / 2);
                g.setColor(top);
                g.fill(s);
            }
        }
    }
}
