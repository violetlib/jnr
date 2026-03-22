/*
 * Copyright (c) 2025 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.aqua.impl;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.violetlib.jnr.aqua.AquaUIPainter;
import org.violetlib.jnr.aqua.ComboBoxConfiguration;
import org.violetlib.jnr.impl.Colors;
import org.violetlib.jnr.impl.JNRUtils;
import org.violetlib.jnr.impl.PainterExtension;
import org.violetlib.vappearances.VAppearance;

import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;

import static org.violetlib.jnr.impl.JNRUtils.size2D;

/**
  Simulates the rendering of a combo box button cell (a combo box in a table cell) using the macOS 26+ UI.
*/

public class ComboBoxButtonCell26PainterExtension
  implements PainterExtension
{
    protected final @NotNull ComboBoxConfiguration gg;
    protected final @NotNull Colors colors;

    public ComboBoxButtonCell26PainterExtension(@NotNull ComboBoxConfiguration g, @Nullable VAppearance appearance)
    {
        this.gg = g;
        this.colors = Colors.getColors(appearance);
    }

    @Override
    public void paint(@NotNull Graphics2D g, float width, float height)
    {
        AquaUIPainter.Size sz = gg.getSize();
        float right = size2D(sz, 5, 4, 4, 4, 4);
        float w = size2D(sz, 29, 24, 24, 20, 16);
        float h = size2D(sz, 26, 20, 18, 14, 9);
        float x = Math.max(0, width - w - right);
        float y = Math.max(0, (height - h) / 2);
        float arc = JNRUtils.size2D(sz, 12, 10, 8, 6, 4);
        RoundRectangle2D shape = new RoundRectangle2D.Float(x, y, w, h, arc, arc);
        Color background = colors.get("comboBoxButton");
        g.setColor(background);
        g.fill(shape);
        paintArrow(g, x, y, w, h, sz);
    }

    private void paintArrow(@NotNull Graphics2D g, float x, float y, float width, float height, AquaUIPainter.Size sz)
    {
        double w = size2D(sz, 7, 7, 6, 6, 5);
        double h = size2D(sz, 4, 4, 3, 3, 2);

        double x1 = x + width/2 - w/2;
        double y1 = y + height/2 - h/2;
        double x2 = x1 + w/2;
        double y2 = y1 + h;
        double x3 = x1 + w;

        Path2D p1 = new Path2D.Double();
        p1.moveTo(x1, y1);
        p1.lineTo(x2, y2);
        p1.lineTo(x3, y1);
        g.setColor(colors.get("comboBoxArrow"));
        g.setStroke(new BasicStroke(1.5f));
        g.draw(p1);
    }
}
