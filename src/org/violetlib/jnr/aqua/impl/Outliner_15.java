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
import org.violetlib.geom.GeneralRoundRectangle;
import org.violetlib.jnr.aqua.AquaUIPainter;
import org.violetlib.jnr.aqua.AquaUIPainter.Position;
import org.violetlib.jnr.aqua.AquaUIPainter.SegmentedButtonWidget;
import org.violetlib.jnr.aqua.AquaUIPainter.Size;
import org.violetlib.jnr.aqua.ButtonLayoutConfiguration;
import org.violetlib.jnr.aqua.SegmentedButtonLayoutConfiguration;
import org.violetlib.jnr.aqua.SpinnerArrowsLayoutConfiguration;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

import static org.violetlib.jnr.aqua.AquaUIPainter.ButtonWidget.BUTTON_DISCLOSURE;
import static org.violetlib.jnr.aqua.AquaUIPainter.Position.*;
import static org.violetlib.jnr.aqua.AquaUIPainter.SegmentedButtonWidget.BUTTON_SEGMENTED_SLIDER;
import static org.violetlib.jnr.aqua.AquaUIPainter.SegmentedButtonWidget.BUTTON_TAB;

/**

*/

public class Outliner_15
  extends Outliner_11
{
    public Outliner_15(@NotNull LayoutInfo_15 uiLayout)
    {
        super(uiLayout);
    }

    @Override
    protected @Nullable Shape getButtonOutline(@NotNull Rectangle2D bounds, @NotNull ButtonLayoutConfiguration g)
    {
        AquaUIPainter.ButtonWidget bw = g.getButtonWidget();
        AquaUIPainter.Size sz = g.getSize();

        if (bw == BUTTON_DISCLOSURE) {
            double x = bounds.getX();
            double y = bounds.getY() + size2D(sz, 0.49f, 0, 0, 0);
            double width = bounds.getWidth();
            double height = width + size2D(sz, 0, 1, 0, 0);
            double corner = size2D(sz, 10, 10, 8, 6);
            return new RoundRectangle2D.Double(x, y, width, height, corner, corner);
        }
        return super.getButtonOutline(bounds, g);
    }

    @Override
    protected @Nullable Shape getSegmentedButtonOutline(@NotNull Rectangle2D bounds,
                                                        @NotNull SegmentedButtonLayoutConfiguration g)
    {
        SegmentedButtonWidget bw = g.getWidget();
        Size sz = g.getSize();
        Position pos = g.getPosition();

        double x = bounds.getX();
        double y = bounds.getY();
        double width = bounds.getWidth();
        double height = bounds.getHeight();

        boolean isLeft = pos == Position.FIRST;
        boolean useOnly = pos == ONLY;

        double corner = 8;

        switch (bw)
        {
            case BUTTON_TAB:
            case BUTTON_SEGMENTED:
            case BUTTON_SEGMENTED_SLIDER:
            case BUTTON_SEGMENTED_SLIDER_TOOLBAR:
            case BUTTON_SEGMENTED_SEPARATED:

                // The outline is used for focus rings. Full Keyboard Access causes one segment to be focusable via the
                // keyboard. For select one controls, including tabs, it is the selected segment. The selected segment
                // is always painted using the "only" style. Therefore, the outline should be "only" style regardless of
                // the position. For select any controls, the first segment is focusable. No special case.

                corner = 6;
                if (bw == BUTTON_TAB || bw == BUTTON_SEGMENTED_SLIDER || bw == SegmentedButtonWidget.BUTTON_SEGMENTED_SEPARATED) {
                    useOnly = true;
                }

                float extraTopForRegular = 0.5f;

                x += size2D(sz, isLeft ? 0.5f : 0, 0, 0);
                y += size2D(sz, 0.5f + extraTopForRegular, 0.5, 0.5);
                height += size2D(sz, -2, -1, -1);
                width += size2D(sz, -0.5, 0, 0);

                if (sz == Size.SMALL || sz == Size.MINI) {
                    if (pos == Position.FIRST) {
                        x += 0.5;
                        width -= 0.5;
                    } else if (pos == Position.LAST) {
                        width -= 0.5;
                    } else if (pos == ONLY) {
                        x += 0.5;
                        width -= 1;
                    }
                }

                if (bw == BUTTON_TAB || bw.isSlider()) {

                    double widthDelta = -1.5;
                    if (sz == Size.REGULAR) {
                        if (pos == Position.FIRST || pos == ONLY) {
                            // This adjustment is needed only when the button to the right is not selected, but that
                            // information is beyond what is available for layout. Therefore, choose the larger outline
                            // and use it for all states.
                            widthDelta = -0.5;
                        }
                    } else if (sz == Size.SMALL) {
                        if (pos == ONLY) {
                            widthDelta = -0.5;
                        }
                    } else if (sz == Size.MINI) {
                        // This adjustment is needed only in certain selection states, but that information is not
                        // available for layout. Therefore, choose the larger outline and use it for all states.
                        widthDelta = -0.5;
                    } else if (sz == Size.LARGE) {
                        corner = 10;
                    }

                    if (!bw.isSeparated()) {
                        // Some outlines are different when selected, cannot implement.
                        if (pos == ONLY && sz == Size.LARGE) {
                            widthDelta += 2;
                        } else if (pos == LAST && sz != Size.MINI) {
                            widthDelta += 2;
                        } else if (pos == FIRST && (sz == Size.SMALL || sz == Size.LARGE)) {
                            widthDelta += 1;
                        }
                    }

                    width += (float) widthDelta;
                }

                break;
            default:
                return super.getSegmentedButtonOutline(bounds, g);
        }

        if (useOnly) {
            return new RoundRectangle2D.Double(x, y, width, height, corner, corner);
        }

        if (pos == AquaUIPainter.Position.FIRST) {
            return new GeneralRoundRectangle(x, y, width, height, corner, corner, 0, 0, 0, 0, corner, corner);
        }

        if (pos == Position.LAST) {
            return new GeneralRoundRectangle(x, y, width, height, 0, 0, corner, corner, corner, corner, 0, 0);
        }

        return new Rectangle2D.Double(x, y, width, height);
    }

    @Override
    protected @Nullable Shape getSpinnerArrowsOutline(@NotNull Rectangle2D bounds, @NotNull SpinnerArrowsLayoutConfiguration g)
    {
        AquaUIPainter.Size sz = g.getSize();
        double x = bounds.getX() + size2D(sz, 0, 0, -0.5f);
        double y = bounds.getY() + size2D(sz, 1, 0, 0);
        double w = bounds.getWidth() + size2D(sz, 0, 0, 1);
        double h = bounds.getHeight() + size2D(sz, -2, -2, -1);
        double corner = 8;
        return new RoundRectangle2D.Double(x, y, w, h, corner, corner);
    }
}
