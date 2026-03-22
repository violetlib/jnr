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
import org.violetlib.jnr.aqua.AquaNativeRendering;
import org.violetlib.jnr.aqua.AquaUIPainter;
import org.violetlib.jnr.impl.Colors;
import org.violetlib.jnr.impl.PainterExtension;
import org.violetlib.vappearances.VAppearance;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.function.Function;

import static org.violetlib.jnr.aqua.AquaUIPainter.macOS26;
import static org.violetlib.jnr.aqua.impl.ButtonColorSearchPath.Role.BACKGROUND;
import static org.violetlib.jnr.aqua.impl.ButtonColorSearchPath.Role.BORDER;

/**
  Simulates the rendering of a textured button bezel.
*/

public class TexturedButtonPainterExtension
  implements PainterExtension
{
    protected final @Nullable VAppearance appearance;
    protected final @NotNull Function<@NotNull Rectangle2D, @Nullable Shape> outlineProvider;
    protected final @NotNull ButtonColorSearchPath colorSearch;
    protected final boolean isSpecialColor;

    public TexturedButtonPainterExtension(@NotNull AquaUIPainter.State state,
                                          @Nullable AquaUIPainter.ButtonState buttonState,
                                          @Nullable VAppearance appearance,
                                          @NotNull Function<@NotNull Rectangle2D, @Nullable Shape> outlineProvider)
    {
        this.appearance = appearance;
        this.outlineProvider = outlineProvider;
        Colors colors = Colors.getColors(appearance);
        this.colorSearch = new ButtonColorSearchPath("texturedButton", null, colors, state, buttonState);
        this.isSpecialColor = determineSpecialColor(state, buttonState, appearance);
    }

    private boolean determineSpecialColor(@NotNull AquaUIPainter.State state,
                                          @Nullable AquaUIPainter.ButtonState buttonState,
                                          @Nullable VAppearance appearance)
    {
        int version = AquaNativeRendering.getSystemRenderingVersion();
        if (version >= macOS26 && appearance != null) {
            return state == AquaUIPainter.State.ACTIVE
              && (buttonState == AquaUIPainter.ButtonState.ON || buttonState == AquaUIPainter.ButtonState.MIXED);
        }
        return false;
    }

    private @Nullable Color getSpecialColor()
    {
        assert appearance != null;
        return appearance.getColors().get("controlAccent");
    }

    @Override
    public void paint(@NotNull Graphics2D g, float width, float height)
    {
        Color background = isSpecialColor ? getSpecialColor() : colorSearch.getColor(BACKGROUND);
        if (background != null && !Colors.isClear(background)) {
            Shape outline = outlineProvider.apply(new Rectangle2D.Float(0, 0, width, height));
            if (outline != null) {
                g.setColor(background);
                g.fill(outline);
            }
        }

        Color border = colorSearch.getColor(BORDER);
        if (border != null && !Colors.isClear(border)) {
            Shape outline = outlineProvider.apply(new Rectangle2D.Float(0, 0, width, height));
            if (outline != null) {
                g.setColor(border);
                g.setStroke(new BasicStroke(1));
                g.draw(outline);
            }
        }
    }
}
