/*
 * Copyright (c) 2015-2025 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.aqua.impl;

import org.jetbrains.annotations.NotNull;
import org.violetlib.jnr.aqua.AquaUIPainter;
import org.violetlib.jnr.aqua.SegmentedButtonLayoutConfiguration;

import static org.violetlib.jnr.aqua.AquaUIPainter.*;
import static org.violetlib.jnr.aqua.impl.SegmentedControl4LayoutInfo.DividerPosition;
import static org.violetlib.jnr.aqua.impl.SegmentedControl4LayoutInfo.DividerPosition.*;
import static org.violetlib.jnr.impl.JNRUtils.size2D;

/**
  Data for NSView based rendering of segmented controls.
*/

public class SegmentedControlDescriptions
{
    public @NotNull RenderInsets getInsets(@NotNull SegmentedButtonLayoutConfiguration g, int scale)
    {
        int version = AquaUIPainterBase.internalGetSegmentedButtonRenderingVersion();
        if (version == AquaUIPainterBase.SEGMENTED_10_10) {
            return getInsets10_10(g, scale);
        } else if (version == AquaUIPainterBase.SEGMENTED_10_11) {
            return getInsets10_11(g, scale);
        } else if (version == AquaUIPainterBase.SEGMENTED_10_13) {
            return getInsets10_13new(g, scale);
        } else if (version == AquaUIPainterBase.SEGMENTED_10_14) {
            return getInsets10_14new(g, scale);
        } else if (version == AquaUIPainterBase.SEGMENTED_10_13_OLD) {
            return getInsets10_13old(g, scale);
        } else if (version == AquaUIPainterBase.SEGMENTED_10_14_OLD) {
            return getInsets10_14old(g, scale);
        } else if (version == AquaUIPainterBase.SEGMENTED_11_0) {
            return getInsets11(g, scale);
        } else if (version == AquaUIPainterBase.SEGMENTED_15) {
            return getInsets15(g, scale);
        } else if (version == AquaUIPainterBase.SEGMENTED_26_OLD) {
            return getInsets26old(g, scale);
        } else if (version == AquaUIPainterBase.SEGMENTED_26) {
            return getInsets26(g, scale);
        }
        throw new UnsupportedOperationException();
    }

    private @NotNull RenderInsets getInsets10_10(@NotNull SegmentedButtonLayoutConfiguration g, int scale)
    {
        SegmentedButtonWidget bw = g.getWidget();
        Size sz = g.getSize();

        double left = 0;
        double top;
        double ha;

        switch (bw) {
            case BUTTON_TAB:
            case BUTTON_SEGMENTED:
            case BUTTON_SEGMENTED_SLIDER:
            case BUTTON_SEGMENTED_SLIDER_TOOLBAR:
            case BUTTON_SEGMENTED_SLIDER_TOOLBAR_ICONS:
            case BUTTON_SEGMENTED_SEPARATED:
                left = size2D(sz, 2, 2, 1);
                top = size2D(sz, 0, 1, 0.51);
                ha = size2D(sz, 1, 1, 1);
                break;

            case BUTTON_SEGMENTED_INSET:
                left = 1;
                top = size2D(sz, 0, 0, 0);
                ha = size2D(sz, 3, 2, 2);
                break;

            case BUTTON_SEGMENTED_SMALL_SQUARE:
                top = size2D(sz, 0, 1, 1);
                ha = size2D(sz, 2, 2, 2);
                break;

            case BUTTON_SEGMENTED_TEXTURED:
            case BUTTON_SEGMENTED_TEXTURED_TOOLBAR:
            case BUTTON_SEGMENTED_TEXTURED_TOOLBAR_ICONS:
            case BUTTON_SEGMENTED_TEXTURED_SEPARATED:
            case BUTTON_SEGMENTED_TEXTURED_SEPARATED_TOOLBAR:
            case BUTTON_SEGMENTED_TEXTURED_SEPARATED_TOOLBAR_ICONS:
                top = size2D(sz, 0, 0, 0.51);
                ha = size2D(sz, 1, 0, 1);
                break;

            case BUTTON_SEGMENTED_SCURVE:
                top = size2D(sz, 0, 0, 0);
                ha = size2D(sz, 1, 1, 2);
                break;

            case BUTTON_SEGMENTED_TOOLBAR:
                top = size2D(sz, 0, 0, 0.51);
                ha = size2D(sz, 1, 1, 1);
                break;

            default:
                throw new UnsupportedOperationException();
        }

        return createRenderInsets(left, top, left * 2, ha, scale);
    }

    protected @NotNull RenderInsets getInsets10_11(@NotNull SegmentedButtonLayoutConfiguration g, int scale)
    {
        // Renderer descriptions for macOS 10.11 and 10.12

        SegmentedButtonWidget bw = g.getWidget();
        Size sz = g.getSize();

        double left = 0;
        double top;
        double ha;

        switch (bw) {
            case BUTTON_TAB:
            case BUTTON_SEGMENTED:
            case BUTTON_SEGMENTED_SLIDER:
            case BUTTON_SEGMENTED_SLIDER_TOOLBAR:
            case BUTTON_SEGMENTED_SLIDER_TOOLBAR_ICONS:
            case BUTTON_SEGMENTED_SEPARATED:
                left = size2D(sz, 2, 2, 1);
                top = size2D(sz, 0, 1, 0.51);
                ha = 1;
                break;

            case BUTTON_SEGMENTED_INSET:
                left = 1;
                top = size2D(sz, 0, 0, 0);
                ha = size2D(sz, 3, 2, 2);
                break;

            case BUTTON_SEGMENTED_SMALL_SQUARE:
                top = size2D(sz, 0, 1, 1);
                ha = 2;
                break;

            case BUTTON_SEGMENTED_TEXTURED:
            case BUTTON_SEGMENTED_TEXTURED_SEPARATED:
                top = size2D(sz, 0.49, 0.49, 0);
                ha = size2D(sz, 2, 1, 1);
                break;

            case BUTTON_SEGMENTED_TEXTURED_TOOLBAR:
            case BUTTON_SEGMENTED_TEXTURED_TOOLBAR_ICONS:
            case BUTTON_SEGMENTED_TEXTURED_SEPARATED_TOOLBAR:
            case BUTTON_SEGMENTED_TEXTURED_SEPARATED_TOOLBAR_ICONS:
                top = 0.49;
                ha = 1;
                break;

            case BUTTON_SEGMENTED_SCURVE:
                top = size2D(sz, 0.49, 0, 0);
                ha = size2D(sz, 2, 2, 2);
                break;

            case BUTTON_SEGMENTED_TOOLBAR:
                top = size2D(sz, 0, 0.49, 1);
                ha = size2D(sz, 2, 2, 2);
                break;

            default:
                throw new UnsupportedOperationException();
        }

        return createRenderInsets(left, top, left * 2, ha, scale);
    }

    protected @NotNull RenderInsets getInsets10_13new(@NotNull SegmentedButtonLayoutConfiguration g, int scale)
    {
        // Renderer descriptions for macOS 10.13 new rendering

        SegmentedButtonWidget bw = g.getWidget();
        Size sz = g.getSize();

        double left = 0;
        double top;
        double ha;

        switch (bw) {
            case BUTTON_TAB:
            case BUTTON_SEGMENTED:
            case BUTTON_SEGMENTED_SLIDER:
            case BUTTON_SEGMENTED_SLIDER_TOOLBAR:
            case BUTTON_SEGMENTED_SLIDER_TOOLBAR_ICONS:
                left = size2D(sz, 2, 2, 1);
                top = size2D(sz, 1, 1, 0);
                ha = size2D(sz, 1, 1, 0);
                break;

            case BUTTON_SEGMENTED_SEPARATED:
                left = size2D(sz, 2, 2, 1);
                top = size2D(sz, 1, 1.49, 0);
                ha = size2D(sz, 1, 2, 0);
                break;

            case BUTTON_SEGMENTED_INSET:
                left = 1;
                top = size2D(sz, 3, 2, 1);
                ha = size2D(sz, 3, 2, 1);
                break;

            case BUTTON_SEGMENTED_SMALL_SQUARE:
                top = size2D(sz, 1, 1, 0);
                ha = size2D(sz, 1, 1, 0);
                break;

            case BUTTON_SEGMENTED_TEXTURED:
            case BUTTON_SEGMENTED_TEXTURED_SEPARATED:
            case BUTTON_SEGMENTED_SCURVE:
            case BUTTON_SEGMENTED_TOOLBAR:
                top = size2D(sz, 0.49, 1.49, 0.49);
                ha = size2D(sz, 0, 1, 0);
                break;

            case BUTTON_SEGMENTED_TEXTURED_TOOLBAR:
            case BUTTON_SEGMENTED_TEXTURED_TOOLBAR_ICONS:
            case BUTTON_SEGMENTED_TEXTURED_SEPARATED_TOOLBAR:
            case BUTTON_SEGMENTED_TEXTURED_SEPARATED_TOOLBAR_ICONS:
                top = 0.49;
                ha = 0;
                break;

            default:
                throw new UnsupportedOperationException();
        }

        return createRenderInsets(left, top, left * 2, ha, scale);
    }

    protected @NotNull RenderInsets getInsets10_13old(@NotNull SegmentedButtonLayoutConfiguration g, int scale)
    {
        // Renderer descriptions for macOS 10.13 old rendering

        SegmentedButtonWidget bw = g.getWidget();
        Size sz = g.getSize();

        double left = 0;
        double top;
        double ha;

        switch (bw) {
            case BUTTON_TAB:
            case BUTTON_SEGMENTED:
            case BUTTON_SEGMENTED_SLIDER:
            case BUTTON_SEGMENTED_SLIDER_TOOLBAR:
            case BUTTON_SEGMENTED_SLIDER_TOOLBAR_ICONS:
            case BUTTON_SEGMENTED_SEPARATED:
                left = size2D(sz, 2, 2, 1);
                top = size2D(sz, 1, 1.49, 1);
                ha = size2D(sz, 1, 1, 2);
                break;

            case BUTTON_SEGMENTED_INSET:
                left = 1;
                top = size2D(sz, 3, 2, 1);
                ha = size2D(sz, 3, 2, 1);
                break;

            case BUTTON_SEGMENTED_SMALL_SQUARE:
                top = 1;
                ha = size2D(sz, 1, 1, 2);
                break;

            case BUTTON_SEGMENTED_TEXTURED:
                top =  size2D(sz, 0, 0.49, 0.49);
                ha = size2D(sz, 0, 1, 0);
                break;

            case BUTTON_SEGMENTED_TEXTURED_SEPARATED:
                top = size2D(sz, 0.49, 0, 0.49);
                ha = size2D(sz, 0, 0, 0);
                break;

            case BUTTON_SEGMENTED_TEXTURED_TOOLBAR:
            case BUTTON_SEGMENTED_TEXTURED_TOOLBAR_ICONS:
            case BUTTON_SEGMENTED_TEXTURED_SEPARATED_TOOLBAR:
            case BUTTON_SEGMENTED_TEXTURED_SEPARATED_TOOLBAR_ICONS:
                top = 0.49;
                ha = 0;
                break;

            case BUTTON_SEGMENTED_SCURVE:
                top = size2D(sz, 1, 1.49, 0.49);
                ha = size2D(sz, 1, 1, 0);
                break;

            case BUTTON_SEGMENTED_TOOLBAR:
                top = size2D(sz, 0.49, 1.49, 0.49);
                ha = size2D(sz, 0, 1, 0);
                break;

            default:
                throw new UnsupportedOperationException();
        }

        return createRenderInsets(left, top, left * 2, ha, scale);
    }

    protected @NotNull RenderInsets getInsets10_14old(@NotNull SegmentedButtonLayoutConfiguration g, int scale)
    {
        // Renderer descriptions for macOS 10.14 old rendering

        SegmentedButtonWidget bw = g.getWidget();
        Size sz = g.getSize();

        double left = 0;
        double top;
        double ha;

        switch (bw) {
            case BUTTON_TAB:
            case BUTTON_SEGMENTED:
            case BUTTON_SEGMENTED_SLIDER:
            case BUTTON_SEGMENTED_SLIDER_TOOLBAR:
            case BUTTON_SEGMENTED_SLIDER_TOOLBAR_ICONS:
            case BUTTON_SEGMENTED_SEPARATED:
                left = size2D(sz, 2, 2, 1);
                top = size2D(sz, 1, 1.49, 0);
                ha = size2D(sz, 1, 1, 0);
                break;

            case BUTTON_SEGMENTED_INSET:
                left = 1;
                top = size2D(sz, 3, 2, 1);
                ha = size2D(sz, 3, 2, 1);
                break;

            case BUTTON_SEGMENTED_SMALL_SQUARE:
                top = 1;
                ha = size2D(sz, 1, 1, 2);
                break;

            case BUTTON_SEGMENTED_TEXTURED:
                top = 0.49;
                ha = 0;
                break;

            case BUTTON_SEGMENTED_TEXTURED_SEPARATED:
                top = size2D(sz, 0.49, 0, 0.49);
                ha = 0;
                break;

            case BUTTON_SEGMENTED_TEXTURED_TOOLBAR:
            case BUTTON_SEGMENTED_TEXTURED_TOOLBAR_ICONS:
            case BUTTON_SEGMENTED_TEXTURED_SEPARATED_TOOLBAR:
            case BUTTON_SEGMENTED_TEXTURED_SEPARATED_TOOLBAR_ICONS:
                top = 0.49;
                ha = 0;
                break;

            case BUTTON_SEGMENTED_SCURVE:
                top = size2D(sz, 1, 1.49, 0.49);
                ha = size2D(sz, 1, 1, 0);
                break;

            case BUTTON_SEGMENTED_TOOLBAR:
                top = size2D(sz, 0.49, 1.49, 0.49);
                ha = size2D(sz, 0, 1, 0);
                break;

            default:
                throw new UnsupportedOperationException();
        }

        return createRenderInsets(left, top, left * 2, ha, scale);
    }

    protected @NotNull RenderInsets getInsets10_14new(@NotNull SegmentedButtonLayoutConfiguration g, int scale)
    {
        // Renderer descriptions for macOS 10.14 new rendering

        SegmentedButtonWidget bw = g.getWidget();
        Size sz = g.getSize();

        double left = 0;
        double top;
        double ha;

        switch (bw) {
            case BUTTON_TAB:
            case BUTTON_SEGMENTED:
            case BUTTON_SEGMENTED_SLIDER:
            case BUTTON_SEGMENTED_SLIDER_TOOLBAR:
            case BUTTON_SEGMENTED_SLIDER_TOOLBAR_ICONS:
            case BUTTON_SEGMENTED_SEPARATED:
                left = size2D(sz, 2, 2, 1);
                top = size2D(sz, 1, 1, 0);
                ha = size2D(sz, 1, 1, 0);
                break;

            case BUTTON_SEGMENTED_INSET:
                left = 1;
                top = size2D(sz, 3, 2, 1);
                ha = size2D(sz, 3, 2, 1);
                break;

            case BUTTON_SEGMENTED_SMALL_SQUARE:
                top = size2D(sz, 1, 1, 0);
                ha = size2D(sz, 1, 1, 0);
                break;

            case BUTTON_SEGMENTED_TEXTURED_TOOLBAR:
            case BUTTON_SEGMENTED_TEXTURED_TOOLBAR_ICONS:
            case BUTTON_SEGMENTED_TEXTURED_SEPARATED_TOOLBAR:
            case BUTTON_SEGMENTED_TEXTURED_SEPARATED_TOOLBAR_ICONS:
                left = 1;
                top = 1;
                ha = 1;
                break;

            case BUTTON_SEGMENTED_TEXTURED_SEPARATED:
            case BUTTON_SEGMENTED_TEXTURED:
            case BUTTON_SEGMENTED_SCURVE:
            case BUTTON_SEGMENTED_TOOLBAR:
                top = size2D(sz, 0, 1, 0);
                ha = size2D(sz, 0, 1, 0);
                break;

            default:
                throw new UnsupportedOperationException();
        }

        return createRenderInsets(left, top, left * 2, ha, scale);
    }

    protected @NotNull RenderInsets getInsets11(@NotNull SegmentedButtonLayoutConfiguration g, int scale)
    {
        // Renderer descriptions for macOS 11 (aka 10.16) rendering

        SegmentedButtonWidget bw = g.getWidget();
        Size sz = g.getSize();

        double left = 0;
        double top;
        double ha;

        switch (bw) {
            case BUTTON_TAB:
            case BUTTON_SEGMENTED:
            case BUTTON_SEGMENTED_SLIDER:
            case BUTTON_SEGMENTED_SLIDER_TOOLBAR:
            case BUTTON_SEGMENTED_SLIDER_TOOLBAR_ICONS:
                left = size2D(sz, 5, 2, 2, 1);
                top = size2D(sz, 5, 1, 1, 0);
                ha = size2D(sz, 5, 1, 1, 0);
                break;

            case BUTTON_SEGMENTED_SEPARATED:
                left = size2D(sz, 5, 2, 2, 1);
                top = size2D(sz, 5, 1, 1, 0);
                ha = size2D(sz, 5, 1, 1, 0);
                break;

            case BUTTON_SEGMENTED_INSET:
                left = 1;
                top = size2D(sz, 11, 3, 2, 1);
                ha = size2D(sz, 11, 3, 2, 1);
                break;

            case BUTTON_SEGMENTED_SMALL_SQUARE:
                top = size2D(sz, 9, 1, 1, 0);
                ha = size2D(sz, 9, 1, 1, 0);
                break;

            case BUTTON_SEGMENTED_TEXTURED:
            case BUTTON_SEGMENTED_SCURVE:
            case BUTTON_SEGMENTED_TOOLBAR:
            case BUTTON_SEGMENTED_TEXTURED_SEPARATED:
            case BUTTON_SEGMENTED_TEXTURED_SEPARATED_TOOLBAR:
            case BUTTON_SEGMENTED_TEXTURED_SEPARATED_TOOLBAR_ICONS:
                left = 1;
                top = size2D(sz, 10, 1, 2, 1);
                ha = size2D(sz, 11, 1, 2, 1);
                break;

            case BUTTON_SEGMENTED_TEXTURED_TOOLBAR:
            case BUTTON_SEGMENTED_TEXTURED_TOOLBAR_ICONS:
                // TBD: regular layout H is 20 instead of 24, could be a mistake
                left = 1;
                top = size2D(sz, 10, 1, 2, 1);
                ha = size2D(sz, 11, 0, 0, 0);
                break;

            default:
                throw new UnsupportedOperationException();
        }

        return createRenderInsets(left, top, left * 2, ha, scale);
    }

    protected @NotNull RenderInsets getInsets15(@NotNull SegmentedButtonLayoutConfiguration g, int scale)
    {
        // Renderer descriptions for macOS 15 rendering

        SegmentedButtonWidget bw = g.getWidget();
        Size sz = g.getSize();

        double left = 0;
        double top;
        double ha;
        double wa;

        switch (bw) {
            case BUTTON_TAB:
            case BUTTON_SEGMENTED:
            case BUTTON_SEGMENTED_SLIDER:
            case BUTTON_SEGMENTED_SLIDER_TOOLBAR:
            case BUTTON_SEGMENTED_SLIDER_TOOLBAR_ICONS:
            case BUTTON_SEGMENTED_SEPARATED:
                left = size2D(sz, 5, 2, 2, 1);
                top = size2D(sz, 5, 1, 1, 0);
                ha = size2D(sz, 5, 1, 1, 0);
                wa = left * 2;
                break;

            case BUTTON_SEGMENTED_INSET:
                left = 1;
                top = size2D(sz, 11, 3, 2, 1);
                ha = size2D(sz, 11, 3, 2, 1);
                wa = left * 2;
                break;

            case BUTTON_SEGMENTED_SMALL_SQUARE:
                top = size2D(sz, 9, 1, 1, 0);
                ha = size2D(sz, 9, 1, 1, 0);
                wa = left * 2;
                break;

            case BUTTON_SEGMENTED_TEXTURED:
            case BUTTON_SEGMENTED_TEXTURED_TOOLBAR:
            case BUTTON_SEGMENTED_TEXTURED_TOOLBAR_ICONS:
            case BUTTON_SEGMENTED_SCURVE:
                left = size2D(sz, 1, 1, 1, 1);
                wa = size2D(sz, 12, 2, 2, 2);
                top = size2D(sz, 9, 1, 2, 1);
                ha = size2D(sz, 9, 1, 2, 1);
                break;

            case BUTTON_SEGMENTED_TEXTURED_SEPARATED:
            case BUTTON_SEGMENTED_TEXTURED_SEPARATED_TOOLBAR:
            case BUTTON_SEGMENTED_TEXTURED_SEPARATED_TOOLBAR_ICONS:
                left = size2D(sz, 1, 1, 1, 1);
                wa = size2D(sz, 12, 12, 12, 12);
                top = size2D(sz, 9, 1, 2, 1);
                ha = size2D(sz, 9, 1, 2, 1);
                break;

            case BUTTON_SEGMENTED_TOOLBAR:
                left = size2D(sz, 5, 2, 2, 1);
                wa = size2D(sz, 12, 4, 4, 2);
                top = size2D(sz, 5, 1, 1, 0);
                ha = size2D(sz, 11, 1, 2, 1);

                if (g.getPosition() == AquaUIPainter.Position.ONLY) {
                    // This is probably a bug in AppKit
                    left = size2D(sz, 1, 1, 1, 1);
                    top = size2D(sz, 9, 1, 2, 1);
                }

                break;

            default:
                throw new UnsupportedOperationException();
        }

        return createRenderInsets(left, top, wa, ha, scale);
    }

    protected @NotNull RenderInsets getInsets26old(@NotNull SegmentedButtonLayoutConfiguration g, int scale)
    {
        SegmentedButtonWidget bw = g.getWidget();
        Size sz = g.getSize();

        double left;
        double top;
        double ha;
        double wa;

        switch (bw) {

            case BUTTON_SEGMENTED_TOOLBAR:
                left = size2D(sz, 5, 2, 2, 1);
                wa = size2D(sz, 12, 4, 4, 2);
                top = size2D(sz, 5, 1, 1, 0);
                ha = size2D(sz, 11, 1, 2, 1);

                if (g.getPosition() == AquaUIPainter.Position.ONLY) {
                    // This is probably a bug in AppKit
                    left = size2D(sz, 1, 1, 1, 1);
                    top = size2D(sz, 9, 1, 2, 1);
                }

                return createRenderInsets(left, top, wa, ha, scale);
        }

        return getInsets15(g, scale);  // placeholder
    }

    protected @NotNull RenderInsets getInsets26(@NotNull SegmentedButtonLayoutConfiguration g, int scale)
    {
        return getInsets15(g, scale);  // placeholder
    }

    public static @NotNull RenderInsets createRenderInsets(double left, double top, double wa, double ha, int scale)
    {
        return new RenderInsets(round((float) left, scale), round((float) top, scale), wa, ha);
    }

    private static float round(float v, int scale)
    {
        if (scale == 1) {
            return Math.round(v);
        }
        return Math.round(v * scale) / (float) scale;
    }

    public @NotNull SegmentedControlLayoutInfo getSegmentLayoutInfo(@NotNull SegmentedButtonLayoutConfiguration g, int scale)
    {
        if (g.getPosition() == Position.ONLY) {
            return getSegment1LayoutInfo(g, scale);
        } else {
            return getSegment4LayoutInfo(g, scale);
        }
    }

    public @NotNull SegmentedControl1LayoutInfo getSegment1LayoutInfo(@NotNull SegmentedButtonLayoutConfiguration g, int scale)
    {
        SegmentedControl4LayoutInfo layout = getSegment4LayoutInfo(g, scale);
        float divider = layout.dividerVisualWidth;
        float first = layout.firstSegmentWidthAdjustment;
        float last = layout.lastSegmentWidthAdjustment;
        float adjustment = getSegment1WidthAdjustment(g, scale, first + last - divider);
        return new SegmentedControl1LayoutInfo(adjustment);
    }

    public float getSegment1WidthAdjustment(@NotNull SegmentedButtonLayoutConfiguration g,
                                            int scale,
                                            float defaultValue)
    {
        int version = AquaUIPainterBase.internalGetSegmentedButtonRenderingVersion();
        if (version == AquaUIPainterBase.SEGMENTED_11_0) {
            return getSegment1WidthAdjustment_11(g, scale, defaultValue);
        }
        if (version == AquaUIPainterBase.SEGMENTED_15) {
            return getSegment1WidthAdjustment_15(g, scale, defaultValue);
        }
        if (version == AquaUIPainterBase.SEGMENTED_26_OLD) {
            return getSegment1WidthAdjustment_26old(g, scale, defaultValue);
        }
        if (version == AquaUIPainterBase.SEGMENTED_26) {
            return getSegment1WidthAdjustment_26(g, scale, defaultValue);
        }
        return defaultValue;
    }

    public float getSegment1WidthAdjustment_11(@NotNull SegmentedButtonLayoutConfiguration g,
                                               int scale,
                                               float defaultValue)
    {
        SegmentedButtonWidget bw = g.getWidget();
        Size sz = g.getSize();
        switch (bw) {
            case BUTTON_TAB:
            case BUTTON_SEGMENTED:
            case BUTTON_SEGMENTED_SEPARATED:
            case BUTTON_SEGMENTED_SLIDER:
            case BUTTON_SEGMENTED_SLIDER_TOOLBAR:
            case BUTTON_SEGMENTED_SLIDER_TOOLBAR_ICONS:
            {
                float adjustment = size2D(sz, 12, 2, 2, 4);
                if (scale == 2) {
                    adjustment += 0.5f;
                }
                return adjustment;
            }

            case BUTTON_SEGMENTED_INSET:
            case BUTTON_SEGMENTED_SMALL_SQUARE:
                return 2;

            case BUTTON_SEGMENTED_TEXTURED:
            case BUTTON_SEGMENTED_TEXTURED_TOOLBAR:
            case BUTTON_SEGMENTED_TEXTURED_TOOLBAR_ICONS:
            case BUTTON_SEGMENTED_TOOLBAR:
            case BUTTON_SEGMENTED_SCURVE:
                return 4;

            case BUTTON_SEGMENTED_TEXTURED_SEPARATED:
            case BUTTON_SEGMENTED_TEXTURED_SEPARATED_TOOLBAR:
            case BUTTON_SEGMENTED_TEXTURED_SEPARATED_TOOLBAR_ICONS:
                return 6;
        }
        return defaultValue;
    }

    public float getSegment1WidthAdjustment_15(@NotNull SegmentedButtonLayoutConfiguration g,
                                               int scale,
                                               float defaultValue)
    {
        SegmentedButtonWidget bw = g.getWidget();
        Size sz = g.getSize();
        switch (bw) {
            case BUTTON_TAB:
            case BUTTON_SEGMENTED:
            case BUTTON_SEGMENTED_SEPARATED:
            case BUTTON_SEGMENTED_SLIDER:
            case BUTTON_SEGMENTED_SLIDER_TOOLBAR:
            case BUTTON_SEGMENTED_SLIDER_TOOLBAR_ICONS:
            {
                float adjustment = size2D(sz, 12, 2, 2, 4);
                if (scale == 2) {
                    adjustment += 0.5f;
                }
                return adjustment;
            }

            case BUTTON_SEGMENTED_INSET:
            case BUTTON_SEGMENTED_SMALL_SQUARE:
                return 2;

            case BUTTON_SEGMENTED_TEXTURED:
            case BUTTON_SEGMENTED_TEXTURED_TOOLBAR:
            case BUTTON_SEGMENTED_TEXTURED_TOOLBAR_ICONS:
            case BUTTON_SEGMENTED_TEXTURED_SEPARATED:
            case BUTTON_SEGMENTED_TEXTURED_SEPARATED_TOOLBAR:
            case BUTTON_SEGMENTED_TEXTURED_SEPARATED_TOOLBAR_ICONS:
            case BUTTON_SEGMENTED_TOOLBAR:
            case BUTTON_SEGMENTED_SCURVE:
                return 4;

        }
        return defaultValue;
    }

    public float getSegment1WidthAdjustment_26old(@NotNull SegmentedButtonLayoutConfiguration g,
                                                  int scale,
                                                  float defaultValue)
    {
        return getSegment1WidthAdjustment_15(g, scale, defaultValue); // placeholder
    }

    public float getSegment1WidthAdjustment_26(@NotNull SegmentedButtonLayoutConfiguration g,
                                               int scale,
                                               float defaultValue)
    {
        return getSegment1WidthAdjustment_15(g, scale, defaultValue); // placeholder
    }

    public @NotNull SegmentedControl4LayoutInfo getSegment4LayoutInfo(@NotNull SegmentedButtonLayoutConfiguration g,
                                                                      int scale)
    {
        int version = AquaUIPainterBase.internalGetSegmentedButtonRenderingVersion();
        if (version == AquaUIPainterBase.SEGMENTED_10_10) {
            return getSegment4LayoutInfo10_10(g, scale);
        } else if (version == AquaUIPainterBase.SEGMENTED_10_11) {
            return getSegment4LayoutInfo10_11(g, scale);
        } else if (version == AquaUIPainterBase.SEGMENTED_10_13) {
            return getSegment4LayoutInfo10_13new(g, scale);
        } else if (version == AquaUIPainterBase.SEGMENTED_10_14) {
            return getSegment4LayoutInfo10_14new(g, scale);
        } else if (version == AquaUIPainterBase.SEGMENTED_10_13_OLD) {
            return getSegment4LayoutInfo10_13old(g, scale);
        } else if (version == AquaUIPainterBase.SEGMENTED_10_14_OLD) {
            return getSegment4LayoutInfo10_14old(g, scale);
        } else if (version == AquaUIPainterBase.SEGMENTED_11_0) {
            return getSegment4LayoutInfo11(g, scale);
        } else if (version == AquaUIPainterBase.SEGMENTED_15) {
            return getSegment4LayoutInfo15(g, scale);
        } else if (version == AquaUIPainterBase.SEGMENTED_26_OLD) {
            return getSegment4LayoutInfo26old(g, scale);
        } else if (version == AquaUIPainterBase.SEGMENTED_26) {
            return getSegment4LayoutInfo26(g, scale);
        }
        throw new UnsupportedOperationException();
    }

    public @NotNull SegmentedControl4LayoutInfo getSegment4LayoutInfo10_10(@NotNull SegmentedButtonLayoutConfiguration g,
                                                                           int scale)
    {
        SegmentedButtonWidget bw = g.getWidget();
        Size sz = g.getSize();

        DividerPosition dp2 = CENTER;
        double first = 0;
        double middle = 1;
        double last = 0;

        switch (bw) {
            case BUTTON_TAB:
            case BUTTON_SEGMENTED:
            case BUTTON_SEGMENTED_SEPARATED:
            case BUTTON_SEGMENTED_SLIDER:
            case BUTTON_SEGMENTED_SLIDER_TOOLBAR:
            case BUTTON_SEGMENTED_SLIDER_TOOLBAR_ICONS:
                first = size2D(sz, 2, 2, 3);
                last = size2D(sz, 1, 1, 2);
                break;

            case BUTTON_SEGMENTED_INSET:
                first = 3;
                last = 2;
                break;

            case BUTTON_SEGMENTED_SMALL_SQUARE:
                first = 2;
                last = 1;
                dp2 = LEFT;
                break;

            case BUTTON_SEGMENTED_TEXTURED:
            case BUTTON_SEGMENTED_TEXTURED_TOOLBAR:
            case BUTTON_SEGMENTED_TEXTURED_TOOLBAR_ICONS:
                first = 2;
                last = 1;
                break;

            case BUTTON_SEGMENTED_TEXTURED_SEPARATED:
            case BUTTON_SEGMENTED_TEXTURED_SEPARATED_TOOLBAR:
            case BUTTON_SEGMENTED_TEXTURED_SEPARATED_TOOLBAR_ICONS:
            case BUTTON_SEGMENTED_TOOLBAR:
                first = size2D(sz, 4, 2, 2);
                last = size2D(sz, 3, 1, 1);
                break;

            case BUTTON_SEGMENTED_SCURVE:
                first = 4;
                last = 3;
                break;

            default:
                throw new UnsupportedOperationException();
        }

        DividerPosition dp = scale == 1 ? LEFT : dp2;
        return new SegmentedControl4LayoutInfo(dp, 1, first, middle, last);
    }

    public @NotNull SegmentedControl4LayoutInfo getSegment4LayoutInfo10_11(@NotNull SegmentedButtonLayoutConfiguration g,
                                                                           int scale)
    {
        SegmentedButtonWidget bw = g.getWidget();
        Size sz = g.getSize();

        DividerPosition dp2 = CENTER;
        double first;
        double middle = 1;
        double last;

        switch (bw) {
            case BUTTON_TAB:
            case BUTTON_SEGMENTED:
            case BUTTON_SEGMENTED_SLIDER:
            case BUTTON_SEGMENTED_SLIDER_TOOLBAR:
            case BUTTON_SEGMENTED_SLIDER_TOOLBAR_ICONS:
            case BUTTON_SEGMENTED_SEPARATED:
                first = size2D(sz, 2, 2, 3);
                last = size2D(sz, 1, 1, 2);
                break;

            case BUTTON_SEGMENTED_INSET:
                first = 3;
                last = 2;
                break;

            case BUTTON_SEGMENTED_SMALL_SQUARE:
                first = 2;
                last = 1;
                dp2 = LEFT;
                break;

            case BUTTON_SEGMENTED_TEXTURED:
            case BUTTON_SEGMENTED_TEXTURED_TOOLBAR:
            case BUTTON_SEGMENTED_TEXTURED_TOOLBAR_ICONS:
                first = 2;
                last = 1;
                dp2 = RIGHT;
                break;

            case BUTTON_SEGMENTED_TEXTURED_SEPARATED:
            case BUTTON_SEGMENTED_TEXTURED_SEPARATED_TOOLBAR:
            case BUTTON_SEGMENTED_TEXTURED_SEPARATED_TOOLBAR_ICONS:
                first = size2D(sz, 4, 2, 2);
                last = size2D(sz, 3, 1, 1);
                break;

            case BUTTON_SEGMENTED_SCURVE:
                first = 4;
                last = 3;
                dp2 = RIGHT;
                break;

            case BUTTON_SEGMENTED_TOOLBAR:
                first = size2D(sz, 4, 2, 2);
                last = size2D(sz, 3, 1, 1);
                dp2 = RIGHT;
                break;

            default:
                throw new UnsupportedOperationException();
        }

        DividerPosition dp = scale == 1 ? LEFT : dp2;
        return new SegmentedControl4LayoutInfo(dp, 1, first, middle, last);
    }

    public @NotNull SegmentedControl4LayoutInfo getSegment4LayoutInfo10_13new(@NotNull SegmentedButtonLayoutConfiguration g,
                                                                              int scale)
    {
        double dividerVisualWidth = scale == 2 ? 0.5 : 1;
        Size sz = g.getSize();

        DividerPosition dp2 = LEFT;
        double first = 0;
        double middle = 1;
        double last = 0;

        SegmentedButtonWidget bw = g.getWidget();

        switch (bw) {
            case BUTTON_SEGMENTED_SEPARATED:
            case BUTTON_SEGMENTED_TEXTURED_SEPARATED:
            case BUTTON_SEGMENTED_TEXTURED_SEPARATED_TOOLBAR:
            case BUTTON_SEGMENTED_TEXTURED_SEPARATED_TOOLBAR_ICONS:
                dp2 = CENTER;
        }

        switch (bw) {
            case BUTTON_TAB:
            case BUTTON_SEGMENTED:
            case BUTTON_SEGMENTED_SLIDER:
            case BUTTON_SEGMENTED_SLIDER_TOOLBAR:
            case BUTTON_SEGMENTED_SLIDER_TOOLBAR_ICONS:
                first = size2D(sz, 2, 2, 3);
                last = size2D(sz, 1, 1, 2);
                break;

            case BUTTON_SEGMENTED_SEPARATED:
            case BUTTON_SEGMENTED_INSET:
            case BUTTON_SEGMENTED_SMALL_SQUARE:
                first = 2;
                last = 1;
                break;

            case BUTTON_SEGMENTED_TEXTURED_TOOLBAR:
            case BUTTON_SEGMENTED_TEXTURED_TOOLBAR_ICONS:
                first = size2D(sz, 4, 2, 2);
                last = size2D(sz, 3, 1, 1);
                break;

            case BUTTON_SEGMENTED_TEXTURED:
            case BUTTON_SEGMENTED_SCURVE:
            case BUTTON_SEGMENTED_TOOLBAR:
            case BUTTON_SEGMENTED_TEXTURED_SEPARATED:
            case BUTTON_SEGMENTED_TEXTURED_SEPARATED_TOOLBAR:
            case BUTTON_SEGMENTED_TEXTURED_SEPARATED_TOOLBAR_ICONS:
                first = 4;
                last = 3;
                break;

            default:
                throw new UnsupportedOperationException();
        }

        DividerPosition dp = scale == 1 ? LEFT : dp2;
        return new SegmentedControl4LayoutInfo(dp, dividerVisualWidth, first, middle, last);
    }

    public @NotNull SegmentedControl4LayoutInfo getSegment4LayoutInfo10_14new(@NotNull SegmentedButtonLayoutConfiguration g,
                                                                              int scale)
    {
        SegmentedButtonWidget bw = g.getWidget();
        Size sz = g.getSize();

        float first = 0;
        float last = 0;

        switch (bw) {
            case BUTTON_SEGMENTED_SEPARATED:
                first = 2;
                last = 1;
                break;

            case BUTTON_TAB:
            case BUTTON_SEGMENTED:
            case BUTTON_SEGMENTED_SLIDER:
            case BUTTON_SEGMENTED_SLIDER_TOOLBAR:
            case BUTTON_SEGMENTED_SLIDER_TOOLBAR_ICONS:
                first = size2D(sz, 2, 2, 3);
                last = size2D(sz, 1, 1, 2);
                break;

            case BUTTON_SEGMENTED_INSET:
                first = 2;
                last = 1;
                break;

            case BUTTON_SEGMENTED_SMALL_SQUARE:
                first = 2;
                last = 1;
                break;

            case BUTTON_SEGMENTED_TEXTURED_TOOLBAR:
            case BUTTON_SEGMENTED_TEXTURED_TOOLBAR_ICONS:
                first = size2D(sz, 3, 1, 1);
                last = size2D(sz, 2, 0, 0);
                break;

            case BUTTON_SEGMENTED_TEXTURED_SEPARATED_TOOLBAR:
            case BUTTON_SEGMENTED_TEXTURED_SEPARATED_TOOLBAR_ICONS:
                first = 3;
                last = 2;
                break;

            case BUTTON_SEGMENTED_TEXTURED:
            case BUTTON_SEGMENTED_SCURVE:
            case BUTTON_SEGMENTED_TOOLBAR:
            case BUTTON_SEGMENTED_TEXTURED_SEPARATED:
                first = 4;
                last = 3;
                break;

            default:
                throw new UnsupportedOperationException();
        }

        return new SegmentedControl4LayoutInfo(LEFT, 1, first, 1, last);
    }

    public @NotNull SegmentedControl4LayoutInfo getSegment4LayoutInfo10_13old(@NotNull SegmentedButtonLayoutConfiguration g,
                                                                              int scale)
    {
        SegmentedButtonWidget bw = g.getWidget();
        Size sz = g.getSize();

        DividerPosition dp2 = CENTER;
        double first = 0;
        double middle = 1;
        double last = 0;

        switch (bw) {
            case BUTTON_SEGMENTED_TEXTURED:
            case BUTTON_SEGMENTED_TOOLBAR:
            case BUTTON_SEGMENTED_SCURVE:
                dp2 = RIGHT;
                break;
        }

        switch (bw) {
            case BUTTON_TAB:
            case BUTTON_SEGMENTED:
            case BUTTON_SEGMENTED_SEPARATED:
            case BUTTON_SEGMENTED_SLIDER:
            case BUTTON_SEGMENTED_SLIDER_TOOLBAR:
            case BUTTON_SEGMENTED_SLIDER_TOOLBAR_ICONS:
                first = size2D(sz, 2, 2, 3);
                last = size2D(sz, 1, 1, 2);
                break;

            case BUTTON_SEGMENTED_SCURVE:
                first = 4;
                last = 3;
                break;

            case BUTTON_SEGMENTED_SMALL_SQUARE:
            case BUTTON_SEGMENTED_TEXTURED:
            case BUTTON_SEGMENTED_TEXTURED_TOOLBAR:
            case BUTTON_SEGMENTED_TEXTURED_TOOLBAR_ICONS:
                first = 2;
                last = 1;
                break;

            case BUTTON_SEGMENTED_TOOLBAR:
            case BUTTON_SEGMENTED_TEXTURED_SEPARATED:
            case BUTTON_SEGMENTED_TEXTURED_SEPARATED_TOOLBAR:
            case BUTTON_SEGMENTED_TEXTURED_SEPARATED_TOOLBAR_ICONS:
                first = size2D(sz, 4, 2, 2);
                last = size2D(sz, 3, 1, 1);
                break;

            case BUTTON_SEGMENTED_INSET:
                first = 3;
                last = 2;
                break;

            default:
                throw new UnsupportedOperationException();
        }

        DividerPosition dp = scale == 1 ? LEFT : dp2;
        return new SegmentedControl4LayoutInfo(dp, 1, first, middle, last);
    }

    public @NotNull SegmentedControl4LayoutInfo getSegment4LayoutInfo10_14old(@NotNull SegmentedButtonLayoutConfiguration g,
                                                                              int scale)
    {
        SegmentedButtonWidget bw = g.getWidget();
        Size sz = g.getSize();

        DividerPosition dp2 = CENTER;
        double first = 0;
        double middle = 1;
        double last = 0;

        switch (bw) {
            case BUTTON_TAB:
            case BUTTON_SEGMENTED:
            case BUTTON_SEGMENTED_SEPARATED:
            case BUTTON_SEGMENTED_SLIDER:
            case BUTTON_SEGMENTED_SLIDER_TOOLBAR:
            case BUTTON_SEGMENTED_SLIDER_TOOLBAR_ICONS:
                first = size2D(sz, 2, 2, 3);
                last = size2D(sz, 1, 1, 2);
                break;

            case BUTTON_SEGMENTED_TEXTURED_SEPARATED:
            case BUTTON_SEGMENTED_TEXTURED_SEPARATED_TOOLBAR:
            case BUTTON_SEGMENTED_TEXTURED_SEPARATED_TOOLBAR_ICONS:
            case BUTTON_SEGMENTED_TOOLBAR:
                first = size2D(sz, 4, 2, 2);
                last = size2D(sz, 3, 1, 1);
                break;

            case BUTTON_SEGMENTED_SCURVE:
                first = 4;
                last = 3;
                break;

            case BUTTON_SEGMENTED_SMALL_SQUARE:
            case BUTTON_SEGMENTED_TEXTURED:
            case BUTTON_SEGMENTED_TEXTURED_TOOLBAR:
            case BUTTON_SEGMENTED_TEXTURED_TOOLBAR_ICONS:
                first = 2;
                last = 1;
                break;

            case BUTTON_SEGMENTED_INSET:
                first = 3;
                last = 2;
                break;

            default:
                throw new UnsupportedOperationException();
        }

        return new SegmentedControl4LayoutInfo(RIGHT, 1, first, middle, last);
    }

    public @NotNull SegmentedControl4LayoutInfo getSegment4LayoutInfo11(@NotNull SegmentedButtonLayoutConfiguration g,
                                                                        int scale)
    {
        Size sz = g.getSize();

        double dividerVisualWidth = 1;
        double first = 0;
        double middle = 0;
        double last = 0;

        SegmentedButtonWidget bw = g.getWidget();

        switch (bw) {
            case BUTTON_TAB:
            case BUTTON_SEGMENTED:
            case BUTTON_SEGMENTED_SLIDER:
            case BUTTON_SEGMENTED_SLIDER_TOOLBAR:
            case BUTTON_SEGMENTED_SLIDER_TOOLBAR_ICONS:
            case BUTTON_SEGMENTED_TOOLBAR:
                first = size2D(sz, 13, 3, 3, 5);
                middle = size2D(sz, 13, 3, 3, 5);
                last = size2D(sz, 12, 2, 2, 4);
                break;

            case BUTTON_SEGMENTED_SEPARATED:
                first = size2D(sz, 12, 2, 2, 4);
                middle = size2D(sz, 11, 1, 1, 3);
                last = size2D(sz, 11, 1, 1, 3);
                break;

            case BUTTON_SEGMENTED_INSET:
            case BUTTON_SEGMENTED_SMALL_SQUARE:
                first = 3;
                middle = 3;
                last = 2;
                break;

            case BUTTON_SEGMENTED_TEXTURED:
            case BUTTON_SEGMENTED_TEXTURED_TOOLBAR:
            case BUTTON_SEGMENTED_TEXTURED_TOOLBAR_ICONS:
            case BUTTON_SEGMENTED_SCURVE:
                first = size2D(sz, 16, 5, 5, 5);
                middle = size2D(sz, 11, 5, 5, 5);
                last = size2D(sz, 15, 4, 4, 4);
                break;

            case BUTTON_SEGMENTED_TEXTURED_SEPARATED:
            case BUTTON_SEGMENTED_TEXTURED_SEPARATED_TOOLBAR:
            case BUTTON_SEGMENTED_TEXTURED_SEPARATED_TOOLBAR_ICONS:
                first = size2D(sz, 16, 5, 5, 5);
                middle = size2D(sz, 11, 5, 5, 5);
                last = size2D(sz, 15, 4, 4, 4);
                break;

            default:
                throw new UnsupportedOperationException();
        }

        return new SegmentedControl4LayoutInfo(LEFT, dividerVisualWidth, first, middle, last);
    }

    public @NotNull SegmentedControl4LayoutInfo getSegment4LayoutInfo15(@NotNull SegmentedButtonLayoutConfiguration g,
                                                                        int scale)
    {
        Size sz = g.getSize();

        double dividerVisualWidth = 1;
        double first = 0;
        double middle = 0;
        double last = 0;

        SegmentedButtonWidget bw = g.getWidget();

        switch (bw) {
            case BUTTON_TAB:
            case BUTTON_SEGMENTED:
            case BUTTON_SEGMENTED_SLIDER:
            case BUTTON_SEGMENTED_SLIDER_TOOLBAR:
            case BUTTON_SEGMENTED_SLIDER_TOOLBAR_ICONS:
            case BUTTON_SEGMENTED_TOOLBAR:
                first = size2D(sz, 13, 3, 3, 5);
                middle = size2D(sz, 13, 3, 3, 5);
                last = size2D(sz, 12, 2, 2, 4);
                break;

            case BUTTON_SEGMENTED_SEPARATED:
                first = size2D(sz, 12, 2, 2, 4);
                middle = size2D(sz, 11, 1, 1, 3);
                last = size2D(sz, 11, 1, 1, 3);
                break;

            case BUTTON_SEGMENTED_INSET:
            case BUTTON_SEGMENTED_SMALL_SQUARE:
                first = 3;
                middle = 3;
                last = 2;
                break;

            case BUTTON_SEGMENTED_TEXTURED:
            case BUTTON_SEGMENTED_TEXTURED_TOOLBAR:
            case BUTTON_SEGMENTED_TEXTURED_TOOLBAR_ICONS:
            case BUTTON_SEGMENTED_TEXTURED_SEPARATED:
            case BUTTON_SEGMENTED_TEXTURED_SEPARATED_TOOLBAR:
            case BUTTON_SEGMENTED_TEXTURED_SEPARATED_TOOLBAR_ICONS:
            case BUTTON_SEGMENTED_SCURVE:
                first = size2D(sz, 5, 5, 5, 5);
                middle = size2D(sz, 5, 5, 5, 5);
                last = size2D(sz, 4, 4, 4, 4);
                break;

            default:
                throw new UnsupportedOperationException();
        }

        return new SegmentedControl4LayoutInfo(LEFT, dividerVisualWidth, first, middle, last);
    }

    public @NotNull SegmentedControl4LayoutInfo
    getSegment4LayoutInfo26old(@NotNull SegmentedButtonLayoutConfiguration g, int scale)
    {
        return getSegment4LayoutInfo15(g, scale); // placeholder
    }

    public @NotNull SegmentedControl4LayoutInfo
    getSegment4LayoutInfo26(@NotNull SegmentedButtonLayoutConfiguration g, int scale)
    {
        return getSegment4LayoutInfo15(g, scale); // placeholder
    }
}
