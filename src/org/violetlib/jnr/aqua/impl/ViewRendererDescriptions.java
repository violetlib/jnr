/*
 * Copyright (c) 2015-2021 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.aqua.impl;

import org.violetlib.jnr.aqua.AquaUIPainter;
import org.violetlib.jnr.aqua.ButtonConfiguration;
import org.violetlib.jnr.aqua.ComboBoxConfiguration;
import org.violetlib.jnr.aqua.PopupButtonConfiguration;
import org.violetlib.jnr.aqua.SegmentedButtonConfiguration;
import org.violetlib.jnr.aqua.SplitPaneDividerConfiguration;
import org.violetlib.jnr.impl.BasicRendererDescription;
import org.violetlib.jnr.impl.JNRPlatformUtils;
import org.violetlib.jnr.impl.MultiResolutionRendererDescription;
import org.violetlib.jnr.impl.RendererDescription;

import org.jetbrains.annotations.*;

import static org.violetlib.jnr.aqua.AquaUIPainter.ButtonWidget.*;
import static org.violetlib.jnr.aqua.AquaUIPainter.ComboBoxWidget.*;
import static org.violetlib.jnr.aqua.AquaUIPainter.Orientation.*;
import static org.violetlib.jnr.aqua.AquaUIPainter.PopupButtonWidget.*;

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

        if (platformVersion >= 120000) {
            AquaUIPainter.ButtonWidget bw = g.getButtonWidget();
            AquaUIPainter.Size sz = g.getSize();
            if (bw == BUTTON_BEVEL_ROUND) {
                if (sz == AquaUIPainter.Size.MINI) {
                    return new BasicRendererDescription(-1, -1, 2, 1);
                } else {
                    return new BasicRendererDescription(-2, -2, 4, 4);
                }
            }
        }

        if (platformVersion >= 101600) {
            AquaUIPainter.ButtonWidget bw = g.getButtonWidget();
            AquaUIPainter.Size sz = g.getSize();
            if (bw == BUTTON_CHECK_BOX) {
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
            } else if (bw == BUTTON_RADIO) {
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
            } else if (bw == BUTTON_TEXTURED) {
                return new BasicRendererDescription(-1, 0, 2, 0);
            } else if (bw == BUTTON_ROUND) {
                if (platformVersion >= 101600) {
                    return new BasicRendererDescription(0, 0, 0, 0);
                }
            } else if (bw == BUTTON_HELP) {
                if (platformVersion >= 101600) {
                    switch (sz) {
                        case LARGE:
                            return new BasicRendererDescription(0.49f, 0, 0, 6);
                        case REGULAR:
                            return new BasicRendererDescription(-0.49f, 0, 0, 2);
                        case SMALL:
                            return new BasicRendererDescription(0, 0, 0, 0);
                        case MINI:
                            return new BasicRendererDescription(0.49f, 0, 0, 0);
                        default:
                            throw new UnsupportedOperationException();
                    }
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
    public @NotNull RendererDescription getPopupButtonRendererDescription(@NotNull PopupButtonConfiguration g)
    {
        int platformVersion = JNRPlatformUtils.getPlatformVersion();
        if (platformVersion >= 101600) {
            AquaUIPainter.PopupButtonWidget w = g.getPopupButtonWidget();
            AquaUIPainter.Size sz = g.getSize();
            if (w == BUTTON_POP_DOWN || w == BUTTON_POP_UP) {
                switch (sz) {
                    case LARGE:
                        return new BasicRendererDescription(-4, -1, 8, 1);
                    case REGULAR:
                        return new BasicRendererDescription(-2, -1, 5, 4);
                    case SMALL:
                        return new BasicRendererDescription(-3, 0.51f, 5.51f, 1);
                    case MINI:
                        return new BasicRendererDescription(-1, 0, 3, 0);
                    default:
                        throw new UnsupportedOperationException();
                }
            } else if (w == BUTTON_POP_DOWN_TEXTURED || w == BUTTON_POP_UP_TEXTURED) {
                switch (sz) {
                    case LARGE:
                    case REGULAR:
                        return new BasicRendererDescription(-2, 0, 5, 2);
                    case SMALL:
                        return new BasicRendererDescription(-2.51f, 0, 5.51f, 2);
                    case MINI:
                        return new BasicRendererDescription(-1, -1, 3, 2);
                    default:
                        throw new UnsupportedOperationException();
                }
            }
        }
        return super.getPopupButtonRendererDescription(g);
    }

    @Override
    public @NotNull RendererDescription getComboBoxRendererDescription(@NotNull ComboBoxConfiguration g)
    {
        AquaUIPainter.ComboBoxWidget bw = g.getWidget();
        AquaUIPainter.Size sz = g.getSize();

        if (bw == BUTTON_COMBO_BOX_CELL) {
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
        } else if (bw == BUTTON_COMBO_BOX_TEXTURED || bw == BUTTON_COMBO_BOX_TEXTURED_TOOLBAR){
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
                return o == HORIZONTAL ? new BasicRendererDescription(0, 0, 0, 9) : new BasicRendererDescription(-1, 0, 2, 0);

            case THICK_DIVIDER:
                // At 2x, the native view painter requires a "divider width" of at least 10 points.
                // At 1x, a larger width works better for vertical dividers.
                // We should only be given a "divider width" of 9 points, as that is the fixed logical divider width.
                return o == HORIZONTAL ? new BasicRendererDescription(0, 0, 0, 1) :
                         new MultiResolutionRendererDescription(new BasicRendererDescription(-4, 0, 6, 0), new BasicRendererDescription(-3, 0, 6, 0));

            case PANE_SPLITTER:
                // At 2x, the native view painter requires a "divider width" of at least 11 points.
                // At 1x, a larger width works better for vertical dividers.
                // We should only be given a "divider width" of 10 points, as that is the fixed logical divider width.
                return o == HORIZONTAL ? new BasicRendererDescription(0, 0, 0, 1) : new BasicRendererDescription(-5, 0, 10, 0);

            default:
                return null;
        }
    }
}
