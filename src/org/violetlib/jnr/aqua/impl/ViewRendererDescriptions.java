/*
 * Copyright (c) 2015-2020 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.aqua.impl;

import org.violetlib.jnr.aqua.AquaUIPainter;
import org.violetlib.jnr.aqua.ComboBoxConfiguration;
import org.violetlib.jnr.aqua.SegmentedButtonConfiguration;
import org.violetlib.jnr.aqua.SplitPaneDividerConfiguration;
import org.violetlib.jnr.impl.BasicRendererDescription;
import org.violetlib.jnr.impl.JNRPlatformUtils;
import org.violetlib.jnr.impl.JNRUtils;
import org.violetlib.jnr.impl.MultiResolutionRendererDescription;
import org.violetlib.jnr.impl.RendererDescription;

import org.jetbrains.annotations.*;

import static org.violetlib.jnr.impl.JNRUtils.*;

/**
 Renderer descriptions for NSView based rendering on macOS 10.10 and later.
 */

public class ViewRendererDescriptions
  extends RendererDescriptionsBase
{
    @Override
    public @NotNull RendererDescription getSegmentedButtonRendererDescription(@NotNull SegmentedButtonConfiguration g)
    {
        int platformVersion = JNRPlatformUtils.getPlatformVersion();

        RendererDescription rd = super.getSegmentedButtonRendererDescription(g);
        if (platformVersion < 101300) {
            return rd;
        }

        int version = AquaUIPainterBase.internalGetSegmentedButtonRenderingVersion();
        if (version == AquaUIPainterBase.SEGMENTED_10_13) {
            return getSegmentedButtonRendererDescription13new(g, rd);
        } else if (version == AquaUIPainterBase.SEGMENTED_10_14) {
            return getSegmentedButtonRendererDescription14new(g, rd);
        } else if (version == AquaUIPainterBase.SEGMENTED_10_13_OLD) {
            return getSegmentedButtonRendererDescription13old(g, rd);
        } else if (version == AquaUIPainterBase.SEGMENTED_10_14_OLD) {
            return getSegmentedButtonRendererDescription14old(g, rd);
        } else if (version == AquaUIPainterBase.SEGMENTED_11_0) {
            return getSegmentedButtonRendererDescription16(g, rd);
        }

        throw new UnsupportedOperationException();
    }

    protected @NotNull RendererDescription getSegmentedButtonRendererDescription13new(@NotNull SegmentedButtonConfiguration g,
                                                                                      @NotNull RendererDescription rd)
    {
        // Adjust renderer descriptions for macOS 10.13 new rendering

        AquaUIPainter.SegmentedButtonWidget bw = g.getWidget();
        AquaUIPainter.Size sz = g.getSize();

        float y;

        switch (bw) {
            case BUTTON_TAB:
            case BUTTON_SEGMENTED:
            case BUTTON_SEGMENTED_SEPARATED:
            case BUTTON_SEGMENTED_SLIDER:
                y = size2D(sz, -1, -2.49f, -4);
                break;

            case BUTTON_SEGMENTED_INSET:
                y = size2D(sz, -3, -4, -5);
                break;

            case BUTTON_SEGMENTED_SMALL_SQUARE:
                y = size2D(sz, -1, -2, -3);
                break;

            case BUTTON_SEGMENTED_TEXTURED_SEPARATED_TOOLBAR:
            case BUTTON_SEGMENTED_TEXTURED_TOOLBAR:
                y = size2D(sz, -1.49f, -2, -3.49f);
                break;

            case BUTTON_SEGMENTED_SCURVE:
            case BUTTON_SEGMENTED_TOOLBAR:
            case BUTTON_SEGMENTED_TEXTURED:
            case BUTTON_SEGMENTED_TEXTURED_SEPARATED:
                y = size2D(sz, -1.49f, -2, -4.49f);
                break;

            default:
                throw new UnsupportedOperationException();
        }

        return JNRUtils.changeRendererDescription(rd, NO_CHANGE, y, NO_CHANGE, NO_CHANGE);
    }

    protected @NotNull RendererDescription getSegmentedButtonRendererDescription13old(@NotNull SegmentedButtonConfiguration g,
                                                                                      @NotNull RendererDescription rd)
    {
        // Adjust renderer descriptions for macOS 10.13 old rendering

        AquaUIPainter.SegmentedButtonWidget bw = g.getWidget();
        AquaUIPainter.Size sz = g.getSize();

        float y;

        switch (bw) {
            case BUTTON_TAB:
            case BUTTON_SEGMENTED:
            case BUTTON_SEGMENTED_SEPARATED:
            case BUTTON_SEGMENTED_SLIDER:
                y = size2D(sz, -1, -2.49f, -4);
                break;

            case BUTTON_SEGMENTED_INSET:
                y = size2D(sz, -3, -4, -5);
                break;

            case BUTTON_SEGMENTED_SMALL_SQUARE:
                y = size2D(sz, -1, -2, -3);
                break;

            case BUTTON_SEGMENTED_TEXTURED:
            case BUTTON_SEGMENTED_TEXTURED_SEPARATED_TOOLBAR:
            case BUTTON_SEGMENTED_TEXTURED_TOOLBAR:
                y = size2D(sz, -1.49f, -2.49f, -3.49f);
                break;

            case BUTTON_SEGMENTED_TOOLBAR:
                y = size2D(sz, -1.49f, -2.49f, -4.49f);
                break;

            case BUTTON_SEGMENTED_TEXTURED_SEPARATED:
                y = size2D(sz, -1.49f, -2, -1.49f);
                break;

            case BUTTON_SEGMENTED_SCURVE:
                y = size2D(sz, -1, -2.49f, -4.49f);
                break;

            default:
                throw new UnsupportedOperationException();
        }

        return JNRUtils.changeRendererDescription(rd, NO_CHANGE, y, NO_CHANGE, NO_CHANGE);
    }

    protected @NotNull RendererDescription getSegmentedButtonRendererDescription14old(@NotNull SegmentedButtonConfiguration g,
                                                                                      @NotNull RendererDescription rd)
    {
        // Adjust renderer descriptions for macOS 10.14 old rendering

        AquaUIPainter.SegmentedButtonWidget bw = g.getWidget();
        AquaUIPainter.Size sz = g.getSize();

        float y;

        switch (bw) {
            case BUTTON_TAB:
            case BUTTON_SEGMENTED:
            case BUTTON_SEGMENTED_SEPARATED:
            case BUTTON_SEGMENTED_SLIDER:
                y = size2D(sz, -1, -2.49f, -4);
                break;

            case BUTTON_SEGMENTED_INSET:
                y = size2D(sz, -3, -4, -5);
                break;

            case BUTTON_SEGMENTED_SMALL_SQUARE:
                y = size2D(sz, -1, -2, -3);
                break;

            case BUTTON_SEGMENTED_TEXTURED:
            case BUTTON_SEGMENTED_TEXTURED_SEPARATED_TOOLBAR:
            case BUTTON_SEGMENTED_TEXTURED_TOOLBAR:
                y = size2D(sz, -1.49f, -2.49f, -3.49f);
                break;

            case BUTTON_SEGMENTED_TOOLBAR:
                y = size2D(sz, -1.49f, -2.49f, -4.49f);
                break;

            case BUTTON_SEGMENTED_TEXTURED_SEPARATED:
                y = size2D(sz, -1.49f, -2, -1.49f);
                break;

            case BUTTON_SEGMENTED_SCURVE:
                y = size2D(sz, -1, -2.49f, -4.49f);
                break;

            default:
                throw new UnsupportedOperationException();
        }

        return JNRUtils.changeRendererDescription(rd, NO_CHANGE, y, NO_CHANGE, NO_CHANGE);
    }

    protected @NotNull RendererDescription getSegmentedButtonRendererDescription14new(@NotNull SegmentedButtonConfiguration g,
                                                                                      @NotNull RendererDescription rd)
    {
        // Adjust renderer descriptions for macOS 10.14 new rendering

        AquaUIPainter.SegmentedButtonWidget bw = g.getWidget();
        AquaUIPainter.Size sz = g.getSize();

        float y;

        switch (bw) {
            case BUTTON_TAB:
            case BUTTON_SEGMENTED:
            case BUTTON_SEGMENTED_SEPARATED:
            case BUTTON_SEGMENTED_SLIDER:
                y = size2D(sz, -1, -2, -4);
                break;

            case BUTTON_SEGMENTED_INSET:
                y = size2D(sz, -3, -4, -5);
                break;

            case BUTTON_SEGMENTED_SMALL_SQUARE:
                y = size2D(sz, -1, -2, -3);
                break;

            case BUTTON_SEGMENTED_TEXTURED_TOOLBAR:
            case BUTTON_SEGMENTED_TEXTURED_SEPARATED_TOOLBAR:
                y = size2D(sz, -1.49f, -2.49f, -3.49f);
                break;

            case BUTTON_SEGMENTED_TEXTURED_SEPARATED:
            case BUTTON_SEGMENTED_TEXTURED:
            case BUTTON_SEGMENTED_SCURVE:
            case BUTTON_SEGMENTED_TOOLBAR:
                y = size2D(sz, -1.49f, -2, -4.49f);
                break;

            default:
                throw new UnsupportedOperationException();
        }

        return JNRUtils.changeRendererDescription(rd, NO_CHANGE, y, NO_CHANGE, NO_CHANGE);
    }

    protected @NotNull RendererDescription getSegmentedButtonRendererDescription16(@NotNull SegmentedButtonConfiguration g,
                                                                                   @NotNull RendererDescription rd)
    {
        // Adjust renderer descriptions for macOS 11 new rendering

        AquaUIPainter.SegmentedButtonWidget bw = g.getWidget();
        AquaUIPainter.Size sz = g.getSize();
        AquaUIPainter.Position position = g.getPosition();

        float y;

        switch (bw) {
            case BUTTON_TAB:
            case BUTTON_SEGMENTED:
            case BUTTON_SEGMENTED_SEPARATED:
            case BUTTON_SEGMENTED_SLIDER:
                y = size2D(sz, -1, -2, -4);
                break;

            case BUTTON_SEGMENTED_INSET:
                y = size2D(sz, -3, -4, -5);
                break;

            case BUTTON_SEGMENTED_SMALL_SQUARE:
                y = size2D(sz, -1, -2, -3);
                break;

            case BUTTON_SEGMENTED_TEXTURED_TOOLBAR:
            case BUTTON_SEGMENTED_TEXTURED_SEPARATED_TOOLBAR:
                y = size2D(sz, -1.49f, -2.49f, -3.49f);
                break;

            case BUTTON_SEGMENTED_TEXTURED_SEPARATED:
            case BUTTON_SEGMENTED_TEXTURED:
            case BUTTON_SEGMENTED_SCURVE:
            case BUTTON_SEGMENTED_TOOLBAR:
                y = size2D(sz, -1.49f, -2, -4.49f);
                break;

            default:
                throw new UnsupportedOperationException();
        }

        return JNRUtils.changeRendererDescription(rd, NO_CHANGE, y, NO_CHANGE, NO_CHANGE);
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
