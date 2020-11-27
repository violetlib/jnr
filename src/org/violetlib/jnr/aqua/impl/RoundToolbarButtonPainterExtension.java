/*
 * Copyright (c) 2020 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.aqua.impl;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;

import org.violetlib.jnr.aqua.AquaUIPainter;
import org.violetlib.jnr.aqua.ButtonConfiguration;
import org.violetlib.jnr.impl.Colors;
import org.violetlib.jnr.impl.PainterExtension;
import org.violetlib.vappearances.VAppearance;

import org.jetbrains.annotations.*;

/**
  Simulates the rendering of round buttons on the toolbar in macOS 11+.
*/

public class RoundToolbarButtonPainterExtension
  implements PainterExtension
{
    protected final @NotNull ButtonConfiguration bg;
    protected final @Nullable VAppearance appearance;
    protected final @NotNull Colors colors;
    protected final @NotNull Color lightColor = new Color(0, 0, 0, 16);
    protected final @NotNull Color darkColor = new Color(255, 255, 255, 16);

    public RoundToolbarButtonPainterExtension(@NotNull ButtonConfiguration g, @Nullable VAppearance appearance)
    {
        this.bg = g;
        this.appearance = appearance;
        this.colors = Colors.getColors(appearance);
    }

    @Override
    public void paint(@NotNull Graphics2D g, float width, float height)
    {
        AquaUIPainter.ButtonState state = bg.getButtonState();
        boolean isFilled = state == AquaUIPainter.ButtonState.ON || state == AquaUIPainter.ButtonState.MIXED;
        double x = width / 2.0;
        double y = height / 2.0;
        double diameter = height - 2;
        double radius = diameter / 2.0;
        Color c = appearance != null && appearance.isDark() ? darkColor : lightColor;
        Shape s = new Ellipse2D.Double(x - radius, y - radius, diameter, diameter);
        g.setColor(c);
        if (isFilled) {
            g.fill(s);
        } else {
            g.setStroke(new BasicStroke(2));
            g.draw(s);
        }
    }
}
