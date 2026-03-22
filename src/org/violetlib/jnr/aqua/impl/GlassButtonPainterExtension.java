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
import org.violetlib.jnr.aqua.AquaUIPainter;
import org.violetlib.jnr.aqua.ButtonConfiguration;
import org.violetlib.jnr.aqua.LayoutConfiguration;
import org.violetlib.jnr.impl.Colors;
import org.violetlib.jnr.impl.PainterExtension;
import org.violetlib.vappearances.VAppearance;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.function.Function;

import static org.violetlib.jnr.aqua.impl.ButtonColorSearchPath.Role.*;

/**
  Simulates the rendering of glass buttons in macOS 26+ and toolbar buttons in macOS 11+.
*/

public class GlassButtonPainterExtension
  implements PainterExtension
{
    protected final @NotNull ButtonConfiguration bg;
    protected final @Nullable VAppearance appearance;
    protected final @NotNull Function<@NotNull LayoutConfiguration,@Nullable Shape> outlineProvider;
    protected final @NotNull ButtonColorSearchPath colorSearch;

    public GlassButtonPainterExtension(@NotNull ButtonConfiguration g,
                                       @Nullable VAppearance appearance,
                                       @NotNull Function<@NotNull LayoutConfiguration,@Nullable Shape> outlineProvider)
    {
        this.bg = g;
        this.appearance = appearance;
        this.outlineProvider = outlineProvider;
        Colors colors = Colors.getColors(appearance);
        boolean isToolbar = bg.getButtonWidget().isToolbar();
        String root1 = isToolbar ? "toolbarButton" : "glassButton";
        String root2 = isToolbar ? "glassButton" : null;
        this.colorSearch = new ButtonColorSearchPath(root1, root2, colors, g.getState(), g.getButtonState());
    }

    @Override
    public void paint(@NotNull Graphics2D g, float width, float height)
    {
        Shape shape = outlineProvider.apply(bg);
        if (shape == null) {
            debug("No outline defined for glass/toolbar button in state " + bg.getState());
            return;
        }

        Color background = getBackgroundColor();
        if (background == null) {
            debug("No background defined for glass/toolbar button in state " + bg.getState());
        } else if (!Colors.isClear(background)) {
            g.setColor(background);
            g.fill(shape);
        }

        Color border = colorSearch.getColor(BORDER);
        if (border != null && !Colors.isClear(border)) {
            g.setColor(border);
            g.setStroke(new BasicStroke(1));
            g.draw(shape);
        }

        Color innerBorder = colorSearch.getColor(INNER_BORDER);
        if (innerBorder != null && !Colors.isClear(innerBorder)) {
            g.setColor(innerBorder);
            g.setStroke(new BasicStroke(1));
            g.draw(shrink(shape));
        }
    }

    private @Nullable Color getBackgroundColor()
    {
        if (bg.getButtonState() == AquaUIPainter.ButtonState.ON) {
            if (!bg.getButtonWidget().isToolbar() && appearance != null && bg.getState() == AquaUIPainter.State.ACTIVE) {
                Color c = appearance.getColors().get("controlAccent");
                if (c != null) {
                    return c;
                }
                // emergency backup
                return new Color(128, 128, 128);
            }
        }
        return colorSearch.getColor(BACKGROUND);
    }

    private @NotNull Shape shrink(@NotNull Shape s)
    {
        if (s instanceof RoundRectangle2D.Double) {
            RoundRectangle2D.Double r = (RoundRectangle2D.Double) s;
            double d = 1;
            return new RoundRectangle2D.Double(r.x+d , r.y+d, r.width-2*d, r.height-2*d, r.arcwidth-d, r.archeight-d);
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
