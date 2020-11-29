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
import org.violetlib.jnr.aqua.AquaUIPainter.ButtonState;
import org.violetlib.jnr.aqua.AquaUIPainter.State;
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
    // This extension should not be needed, but I get incorrect results using CoreUI in dark mode.

    protected final @NotNull ButtonConfiguration bg;
    protected final @Nullable VAppearance appearance;
    protected final @NotNull Colors colors;

    public RoundToolbarButtonPainterExtension(@NotNull ButtonConfiguration g, @Nullable VAppearance appearance)
    {
        this.bg = g;
        this.appearance = appearance;
        this.colors = Colors.getColors(appearance);
    }

    @Override
    public void paint(@NotNull Graphics2D g, float width, float height)
    {
        State state = bg.getState();
        ButtonState buttonState = bg.getButtonState();
        boolean isFilled = state == State.PRESSED
                             || buttonState == AquaUIPainter.ButtonState.ON
                             || buttonState == AquaUIPainter.ButtonState.MIXED;
        double x = width / 2.0;
        double y = height / 2.0;
        double diameter = height - 2;
        double radius = diameter / 2.0;
        Color c = getColor();
        Shape s = new Ellipse2D.Double(x - radius, y - radius, diameter, diameter);
        g.setColor(c);
        if (isFilled) {
            g.fill(s);
        } else {
            g.setStroke(new BasicStroke(2));
            g.draw(s);
        }
    }

    protected @NotNull Color getColor()
    {
        String colorName = "toolbarButton";
        State state = bg.getState();
        ButtonState buttonState = bg.getButtonState();
        if (buttonState == ButtonState.ON || buttonState == ButtonState.MIXED) {
            colorName = colorName + "Selected";
        }
        if (state == AquaUIPainter.State.PRESSED) {
            colorName = colorName + "Pressed";
        }
        return colors.get(colorName);
    }
}
