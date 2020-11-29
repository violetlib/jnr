/*
 * Copyright (c) 2020 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.aqua.impl;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.Map;

import org.violetlib.jnr.aqua.AquaUILayoutInfo;
import org.violetlib.jnr.aqua.AquaUIPainter;
import org.violetlib.jnr.aqua.IndeterminateProgressIndicatorConfiguration;
import org.violetlib.jnr.impl.PainterExtension;
import org.violetlib.vappearances.VAppearance;

import org.jetbrains.annotations.*;

/**

*/

public class IndeterminateProgressBarPainterExtension
  implements PainterExtension
{
    protected final @NotNull AquaUILayoutInfo uiLayout;
    protected final @NotNull IndeterminateProgressIndicatorConfiguration pg;
    protected final @Nullable Map<String,Color> colors;
    protected final @NotNull Color background;

    public IndeterminateProgressBarPainterExtension(@NotNull AquaUILayoutInfo uiLayout,
                                                    @NotNull IndeterminateProgressIndicatorConfiguration g,
                                                    @Nullable VAppearance appearance)
    {
        this.uiLayout = uiLayout;
        this.pg = g;
        this.colors = appearance != null ? appearance.getColors() : null;
        this.background = appearance != null && appearance.isDark()
                            ? new Color(255, 255, 255, 24) : new Color(0, 0, 0, 16);
    }

    @Override
    public void paint(@NotNull Graphics2D g, float width, float height)
    {
        boolean isVertical = pg.getOrientation() == AquaUIPainter.Orientation.VERTICAL;

        g = (Graphics2D) g.create();
        g.clip(new Rectangle2D.Float(0, 0, width, height));

        g.setColor(background);
        g.fill(new RoundRectangle2D.Float(0, 0, width, height, 3, 3));

        Color color = colors != null ? colors.get("controlAccent") : null;
        if (color == null) {
            color = Color.LIGHT_GRAY;
        }
        g.setColor(color);

        int frameCount = 90; // must agree with VAqua (AquaProgressBarUI)
        float length = isVertical ? height : width;
        int segment = (int) (length / 4);
        float interval = (length + segment) / frameCount;
        int animationFrame = pg.getAnimationFrame();
        int position1 = (int) Math.floor(interval * animationFrame - segment);
        int position2 = (int) Math.ceil(position1 + segment);
        int thick = Math.round(isVertical ? width : height);
        float arc = pg.getSize() == AquaUIPainter.Size.SMALL ? 3 : 6;
        if (isVertical) {
            g.fill(new RoundRectangle2D.Float(0, position1, thick, position2 - position1, arc, arc));
        } else {
            g.fill(new RoundRectangle2D.Float(position1, 0, position2 - position1, thick, arc, arc));
        }

        g.dispose();
    }
}
