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
import org.violetlib.jnr.impl.PainterExtension;
import org.violetlib.vappearances.VAppearance;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.function.Function;

/**
  Simulates the effect of ROLLOVER and PRESSED button states.
*/

public class RolloverPainterExtension
  implements PainterExtension
{
    public enum Effect { ROLLOVER, PRESSED }

    protected final @NotNull Function<@NotNull Rectangle2D,@Nullable Shape> outlineProvider;
    protected final @Nullable Color overlayColor;

    public RolloverPainterExtension(@NotNull AquaUIPainter.State state,
                                    @NotNull VAppearance appearance,
                                    @NotNull Function<@NotNull Rectangle2D,@Nullable Shape> outlineProvider)
    {
        this.outlineProvider = outlineProvider;
        Effect effect = toEffect(state);
        if (effect != null) {
            overlayColor = getOverlayColor(true, effect, appearance);
        } else {
            overlayColor = null;
        }
    }

    private static @Nullable Effect toEffect(@NotNull AquaUIPainter.State state)
    {
        switch (state) {
            case PRESSED: return Effect.PRESSED;
            case ROLLOVER: return Effect.ROLLOVER;
            default: return null;
        }
    }

    public RolloverPainterExtension(boolean isActive,
                                    @NotNull Effect effect,
                                    @NotNull VAppearance appearance,
                                    @NotNull Function<@NotNull Rectangle2D,@Nullable Shape> outlineProvider)
    {
        this.outlineProvider = outlineProvider;
        this.overlayColor = getOverlayColor(isActive, effect, appearance);
    }

    protected @Nullable Color getOverlayColor(boolean isActive, @NotNull Effect effect, @NotNull VAppearance appearance)
    {
        int base = appearance.isDark() ? 255 : 0;
        int alpha = appearance.isDark()
          ? effect == Effect.ROLLOVER ? 30 : 40
          : effect == Effect.ROLLOVER ? 10 : 20;
        return new Color(base, base, base, alpha);
    }

    @Override
    public void paint(@NotNull Graphics2D g, float width, float height)
    {
        if (overlayColor != null) {
            Rectangle2D.Float bounds = new Rectangle2D.Float(0, 0, width, height);
            Shape shape = outlineProvider.apply(bounds);
            if (shape != null) {
                g.setColor(overlayColor);
                g.fill(shape);
            }
        }
    }
}
