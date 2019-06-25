/*
 * Copyright (c) 2015-2018 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.aqua.impl;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;

import org.violetlib.jnr.aqua.AquaUIPainter;
import org.violetlib.jnr.aqua.PopupButtonConfiguration;
import org.violetlib.jnr.impl.PainterExtension;
import org.violetlib.vappearances.VAppearance;

import org.jetbrains.annotations.*;

/**
  Simulates the rendering of a Yosemite pop up menu button arrow.
*/

public class PopUpArrowPainter
  extends PopUpArrowPainterBase
  implements PainterExtension
{
    public PopUpArrowPainter(@NotNull PopupButtonConfiguration g, @Nullable VAppearance appearance)
    {
        super(g, appearance);
    }

    @Override
    public void paint(@NotNull Graphics2D g, float width, float height)
    {
        AquaUIPainter.Size sz = gg.getSize();
        double h = sz == AquaUIPainter.Size.REGULAR ? 2 : 2;
        double w = sz == AquaUIPainter.Size.REGULAR ? 5 : 4;
        double sep = sz == AquaUIPainter.Size.REGULAR ? 5 : 4;
        double stroke = sz == AquaUIPainter.Size.REGULAR ? 1.5 : 1.2;

        double hh = 2 * h + sep;

        double x1 = (width - w) / 2;
        double y1 = (height - hh) / 2;
        double x2 = x1 + w/2;
        double y2 = y1 + h;
        double x3 = x1 + w;
        double y4 = y2 + h + sep;
        double y5 = y4 - h;

        Path2D p = new Path2D.Double();
        p.moveTo(x1, y2);
        p.lineTo(x2, y1);
        p.lineTo(x3, y2);
        p.moveTo(x1, y5);
        p.lineTo(x2, y4);
        p.lineTo(x3, y5);
        g.setColor(color);
        g.setStroke(new BasicStroke((float) stroke));
        g.draw(p);
    }
}
