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
import org.violetlib.jnr.impl.Colors;
import org.violetlib.jnr.impl.PainterExtension;
import org.violetlib.vappearances.VAppearance;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;

import static org.violetlib.jnr.aqua.impl.ColorSearchPath.Role.*;

/**
  Simulates the rendering of buttons with an outline and a background.
*/

public class ButtonPainterExtensionBase
  implements PainterExtension
{
    protected final @NotNull String name;
    protected final @NotNull AquaUIPainter.State state;
    protected final @NotNull AquaUIPainter.ButtonState buttonState;
    protected final @Nullable VAppearance appearance;
    protected final @Nullable Shape outline;
    protected final @NotNull ColorSearchPath colorSearchPath;

    protected ButtonPainterExtensionBase(@NotNull String name,
                                         @NotNull AquaUIPainter.State state,
                                         @NotNull AquaUIPainter.ButtonState buttonState,
                                         @Nullable VAppearance appearance,
                                         @Nullable Shape outline,
                                         @NotNull ColorSearchPath colorSearchPath)
    {
        this.name = name;
        this.state = state;
        this.buttonState = buttonState;
        this.appearance = appearance;
        this.outline = outline;
        this.colorSearchPath = colorSearchPath;

        if (outline == null) {
            debug("No outline defined for " + name + " in state " + state);
        }
    }

    @Override
    public void paint(@NotNull Graphics2D g, float width, float height)
    {
        if (outline == null) {
            return;
        }

        Color background = colorSearchPath.getColor(BACKGROUND);
        if (background == null) {
            debug("No background defined for " + name + " in state " + state);
        } else if (!Colors.isClear(background)) {
            g.setColor(background);
            g.fill(outline);
        }

        Color border = colorSearchPath.getColor(BORDER);
        if (border != null && !Colors.isClear(border)) {
            g.setColor(border);
            g.setStroke(new BasicStroke(1));
            g.draw(outline);
        }

        Color innerBorder = colorSearchPath.getColor(INNER_BORDER);
        if (innerBorder != null && !Colors.isClear(innerBorder)) {
            g.setColor(innerBorder);
            g.setStroke(new BasicStroke(1));
            g.draw(shrink(outline));
        }
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
