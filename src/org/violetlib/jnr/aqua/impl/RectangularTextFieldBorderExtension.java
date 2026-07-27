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
import org.violetlib.jnr.aqua.TextFieldConfiguration;
import org.violetlib.jnr.impl.Colors;
import org.violetlib.jnr.impl.PainterExtension;
import org.violetlib.vappearances.VAppearance;

import java.awt.*;
import java.awt.geom.Rectangle2D;

/**
  Simulates the rendering of a rectangular text field border.
*/

public class RectangularTextFieldBorderExtension
  implements PainterExtension
{
    private final @NotNull TextFieldConfiguration g;
    private final @NotNull Colors colors;

    public RectangularTextFieldBorderExtension(@NotNull TextFieldConfiguration g,
                                               @Nullable VAppearance appearance)
    {
        this.g = g;
        colors = Colors.getColors(appearance);
    }

    @Override
    public void paint(@NotNull Graphics2D g, float width, float height)
    {
        g = (Graphics2D) g.create();
        g.clip(new Rectangle2D.Float(0, 0, width, height));

        int w = (int) Math.ceil(width);
        int h = (int) Math.ceil(height);

        Color bc = getBorderColor();
        if (bc != null) {
            g.setColor(bc);
            g.fillRect(0, 0, w, 1);
            g.fillRect(0, 1, 1, h-2);
            g.fillRect(0, h-1, w, 1);
            g.fillRect(w-1, 0, 1, h-2);
        }

        g.dispose();
    }

    protected @Nullable Color getBorderColor()
    {
        return colors.getOptional("textFieldBorder");
    }
}
