/*
 * Copyright (c) 2025-2026 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.aqua.impl;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.violetlib.jnr.aqua.LayoutConfiguration;
import org.violetlib.jnr.impl.Colors;
import org.violetlib.jnr.impl.CustomColors;
import org.violetlib.jnr.impl.PainterExtension;
import org.violetlib.vappearances.VAppearance;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.function.Function;

/**
  Simulates the rendering of glass text fields in macOS 26 (the native renderer is broken).
*/

public class GlassTextFieldPainterExtension
  implements PainterExtension
{
    protected final @NotNull LayoutConfiguration gg;
    protected final @Nullable VAppearance appearance;
    protected final @NotNull Function<@NotNull LayoutConfiguration,@Nullable Shape> outlineProvider;
    protected final @NotNull Colors colors;

    public GlassTextFieldPainterExtension(@NotNull LayoutConfiguration gg,
                                          @Nullable VAppearance appearance,
                                          @NotNull Function<@NotNull LayoutConfiguration,@Nullable Shape> outlineProvider)
    {
        this.gg = gg;
        this.appearance = appearance;
        this.outlineProvider = outlineProvider;
        colors = Colors.getColors(appearance);
    }

    @Override
    public void paint(@NotNull Graphics2D g, float width, float height)
    {
        Shape shape = outlineProvider.apply(gg);
        if (shape == null) {
            debug("No outline defined for glass text field");
            return;
        }

        g = (Graphics2D) g.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        shape = fixShape(shape);

        Color background = colors.get("glassTextFieldBackground");
        if (!CustomColors.isClear(background)) {
            g.setColor(background);
            g.fill(shape);
        }

        Color border = colors.get("glassTextFieldBorder");
        if (!CustomColors.isClear(border)) {
            g.setColor(border);
            g.setStroke(new BasicStroke(1));
            g.draw(shape);
        }

        g.dispose();
    }

    private @NotNull Shape fixShape(@NotNull Shape s)
    {
        if (s instanceof RoundRectangle2D) {
            RoundRectangle2D r = (RoundRectangle2D) s;
            return new RoundRectangle2D.Double(r.getX(), r.getY(), r.getWidth()-1, r.getHeight()-1, r.getArcWidth(), r.getArcHeight());
        }
        return s;
    }


    protected void debug(@NotNull String s)
    {
        if (false) {
            System.err.println(s);
        }
    }
}
