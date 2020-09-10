/*
 * Copyright (c) 2015-2020 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.aqua.impl;

import org.violetlib.jnr.aqua.AquaUIPainter;
import org.violetlib.jnr.aqua.ButtonConfiguration;
import org.violetlib.jnr.aqua.ComboBoxConfiguration;
import org.violetlib.jnr.aqua.SegmentedButtonConfiguration;
import org.violetlib.jnr.aqua.SplitPaneDividerConfiguration;
import org.violetlib.jnr.impl.BasicRendererDescription;
import org.violetlib.jnr.impl.JNRPlatformUtils;
import org.violetlib.jnr.impl.MultiResolutionRendererDescription;
import org.violetlib.jnr.impl.RendererDescription;

import org.jetbrains.annotations.*;

/**
  Renderer descriptions for NSView based rendering on macOS 10.10 and later.
*/

public class ViewRendererDescriptions
  extends RendererDescriptionsBase
{
    @Override
    public @NotNull RendererDescription getButtonRendererDescription(@NotNull ButtonConfiguration g)
    {
        int platformVersion = JNRPlatformUtils.getPlatformVersion();
        if (platformVersion >= 101600) {
            AquaUIPainter.ButtonWidget bw = g.getButtonWidget();
            AquaUIPainter.Size sz = g.getSize();
            if (bw == AquaUIPainter.ButtonWidget.BUTTON_CHECK_BOX) {
                switch (sz) {
                    case LARGE:
                        return new BasicRendererDescription(-1.49f, 0, 0, 0);
                    case REGULAR:
                        return new BasicRendererDescription(0, 0, 1, 0);
                    case SMALL:
                        return new BasicRendererDescription(0, -0.49f, 0, 0);
                    case MINI:
                        return new BasicRendererDescription(0, -1.49f, 0, 1);
                    default:
                        throw new UnsupportedOperationException();
                }
            } else if (bw == AquaUIPainter.ButtonWidget.BUTTON_RADIO) {
                switch (sz) {
                    case LARGE:
                        return new BasicRendererDescription(-1, 0, 1, 0);
                    case REGULAR:
                        return new BasicRendererDescription(0, 0, 0, 0);
                    case SMALL:
                        return new BasicRendererDescription(0, 0, 0, 1);
                    case MINI:
                        return new BasicRendererDescription(0, 0, 0, 1);
                    default:
                        throw new UnsupportedOperationException();
                }
            }
        }

        return super.getButtonRendererDescription(g);
    }

    @Override
    public @NotNull RendererDescription getSegmentedButtonRendererDescription(@NotNull SegmentedButtonConfiguration g)
    {
        // This method is not used.
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull RendererDescription getComboBoxRendererDescription(@NotNull ComboBoxConfiguration g)
    {
        AquaUIPainter.ComboBoxWidget bw = g.getWidget();
        AquaUIPainter.Size sz = g.getSize();

        if (bw == AquaUIPainter.ComboBoxWidget.BUTTON_COMBO_BOX_CELL) {
            switch (sz) {
                case LARGE:
                case REGULAR:
                    return new BasicRendererDescription(0, -3, 3, 3);
                case SMALL:
                    return new BasicRendererDescription(0, -1, 3, 1);
                case MINI:
                    return new BasicRendererDescription(0, 0, 2, 1);
                default:
                    throw new UnsupportedOperationException();
            }
        } else if (bw == AquaUIPainter.ComboBoxWidget.BUTTON_COMBO_BOX_TEXTURED || bw == AquaUIPainter.ComboBoxWidget.BUTTON_COMBO_BOX_TEXTURED_TOOLBAR){
            switch (sz) {
                case LARGE:
                case REGULAR:
                    return new BasicRendererDescription(0, 0, 1, 0);
                case SMALL:
                    return new BasicRendererDescription(0, 0, 2, 2);
                case MINI:
                    return new BasicRendererDescription(0, 0, 0, 2);
                default:
                    throw new UnsupportedOperationException();
            }
        } else {
            switch (sz) {
                case LARGE:
                case REGULAR:
                    return new BasicRendererDescription(-0.5f, 0, 3, 1);
                case SMALL:
                    return new BasicRendererDescription(-0.5f, 0, 3, 2);
                case MINI:
                    return new BasicRendererDescription(-0.5f, 0, 2, 2);
                default:
                    throw new UnsupportedOperationException();
            }
        }
    }

    @Override
    public @NotNull RendererDescription getSplitPaneDividerRendererDescription(@NotNull SplitPaneDividerConfiguration g)
    {
        AquaUIPainter.Orientation o = g.getOrientation();

        switch (g.getWidget())
        {
            case THIN_DIVIDER:
                // At 2x, the native view painter requires a "divider width" of at least 2 points.
                // At 1x, a larger size works better for both horizontal and vertical dividers.
                // We should only be given a "divider width" of one point, as that is the fixed logical divider width.
                return o == AquaUIPainter.Orientation.HORIZONTAL ? new BasicRendererDescription(0, 0, 0, 9) : new BasicRendererDescription(-1, 0, 2, 0);

            case THICK_DIVIDER:
                // At 2x, the native view painter requires a "divider width" of at least 10 points.
                // At 1x, a larger width works better for vertical dividers.
                // We should only be given a "divider width" of 9 points, as that is the fixed logical divider width.
                return o == AquaUIPainter.Orientation.HORIZONTAL ? new BasicRendererDescription(0, 0, 0, 1) :
                         new MultiResolutionRendererDescription(new BasicRendererDescription(-4, 0, 6, 0), new BasicRendererDescription(-3, 0, 6, 0));

            case PANE_SPLITTER:
                // At 2x, the native view painter requires a "divider width" of at least 11 points.
                // At 1x, a larger width works better for vertical dividers.
                // We should only be given a "divider width" of 10 points, as that is the fixed logical divider width.
                return o == AquaUIPainter.Orientation.HORIZONTAL ? new BasicRendererDescription(0, 0, 0, 1) : new BasicRendererDescription(-5, 0, 10, 0);

            default:
                return null;
        }
    }
}
