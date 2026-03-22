/*
 * Copyright (c) 2015-2026 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.aqua.impl;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.violetlib.jnr.aqua.AquaNativeRendering;
import org.violetlib.jnr.aqua.AquaUILayoutInfo;
import org.violetlib.jnr.aqua.AquaUIPainter;
import org.violetlib.jnr.aqua.ScrollBarConfiguration;
import org.violetlib.jnr.impl.Colors;
import org.violetlib.jnr.impl.JNRPlatformUtils;
import org.violetlib.jnr.impl.PainterExtension;
import org.violetlib.vappearances.VAppearance;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

import static org.violetlib.jnr.aqua.AquaUIPainter.macOS26;

/**
  Paints a rounded scroll bar (used for a fixed sidebar scroll bar).
*/

public class RoundedScrollBarPainterExtension
  implements PainterExtension
{
    private final @NotNull AquaUILayoutInfo uiLayout;
    private final @NotNull Colors colors;
    private final @NotNull ScrollBarConfiguration sg;

    public RoundedScrollBarPainterExtension(@NotNull AquaUILayoutInfo uiLayout,
                                            @NotNull ScrollBarConfiguration g,
                                            @Nullable VAppearance appearance)
    {
        this.uiLayout = uiLayout;
        boolean isDark = useDarkColors(appearance);
        boolean isHighContrast = appearance != null && appearance.isHighContrast();
        this.colors = Colors.getColors(isDark, isHighContrast);
        this.sg = g;
    }

    private boolean useDarkColors(@Nullable VAppearance appearance)
    {
        int version = JNRPlatformUtils.getPlatformVersion();
        if (version < 101400) {
            // Dark mode did not exist. Use the "light" style.
            return true;
        }
        return appearance != null && appearance.isDark();
    }

    @Override
    public void paint(@NotNull Graphics2D g, float width, float height)
    {
        if (!sg.isTrackSuppressed()) {
            g.setColor(getTrackBackgroundColor());
            float arc = getArc(width, height);
            RoundRectangle2D.Float shape = new RoundRectangle2D.Float(0, 0, width, height, arc, arc);
            g.fill(shape);
        }
        paintThumb(g, width, height);
    }

    private void paintThumb(@NotNull Graphics2D g, float width, float height)
    {
        Shape thumbShape = createThumbShape(width, height);
        g.setColor(getThumbColor());
        g.fill(thumbShape);
    }

    protected @NotNull Shape createThumbShape(float width, float height)
    {
        boolean isVertical = height > width;

        double leftTop = 0;
        double rightBottom = 0;

        Rectangle2D bounds = new Rectangle2D.Float(0, 0, width, height);
        Rectangle2D thumbBounds = uiLayout.getScrollBarThumbBounds(bounds, sg);

        if (isVertical) {
            double w = width - leftTop - rightBottom;
            return new RoundRectangle2D.Double(leftTop, thumbBounds.getY(), w, thumbBounds.getHeight(), w, w);
        } else {
            double h = height - leftTop - rightBottom;
            return new RoundRectangle2D.Double(thumbBounds.getX(), leftTop, thumbBounds.getWidth(), h, h, h);
        }
    }

    private @NotNull Color getThumbColor()
    {
        if (sg.getState() == AquaUIPainter.State.ROLLOVER) {
            Color c = colors.getOptional("sidebarThumbRollover");
            if (c != null) {
                return c;
            }
        }
        return colors.get("sidebarThumb");
    }

    private float getArc(float width, float height)
    {
        float d = Math.min(width, height);
        if (AquaNativeRendering.getSystemRenderingVersion() >= macOS26) {
            return d;
        }
        float leftTop = 3.5f;
        float rightBottom = 1.5f;
        return d - leftTop - rightBottom;
    }

    protected @NotNull Color getTrackBackgroundColor()
    {
        return colors.get("legacyScrollBarTrack");
    }
}
