/*
 * Copyright (c) 2015-2026 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.aqua.impl;

import org.jetbrains.annotations.NotNull;
import org.violetlib.jnr.aqua.AquaNativeRendering;
import org.violetlib.jnr.aqua.SegmentedButtonConfiguration;
import org.violetlib.jnr.impl.JNRPlatformUtils;

import static org.violetlib.jnr.aqua.AquaUIPainter.*;
import static org.violetlib.jnr.aqua.impl.SegmentedControl4LayoutInfo.DividerPosition;
import static org.violetlib.jnr.aqua.impl.SegmentedControl4LayoutInfo.DividerPosition.*;
import static org.violetlib.jnr.impl.JNRUtils.size;
import static org.violetlib.jnr.impl.JNRUtils.size2D;

/**
  Data for NSView based rendering of segmented controls. Descriptions can be dependent upon widget, size, switch
  tracking and scale. A single segment control can have different parameters than a multiple segment control. Other than
  that, the parameters are the same for First, Middle, and Last positions.
*/

public class SegmentedControlDescriptions
{
    /**
      Indicate whether the rendering is different for Select Any.
    */

    public boolean isSwitchTrackingDependent(@NotNull SegmentedButtonWidget w, @NotNull Size sz)
    {
        int version = AquaNativePainter.getSegmentedButtonRenderingVersion();
        if (version == AquaUIPainterBase.SEGMENTED_11_0) {
            return isSelectAnySpecial11(w, sz);
        } else if (version == AquaUIPainterBase.SEGMENTED_15) {
            return isSelectAnySpecial15(w, sz);
        } else if (version == AquaUIPainterBase.SEGMENTED_26_OLD) {
            return isSelectAnySpecial26old(w, sz);
        }
        return false;
    }

    public @NotNull RenderInsets getInsets(@NotNull SegmentedButtonConfiguration g, int scale)
    {
        int version = AquaNativePainter.getSegmentedButtonRenderingVersion();
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

    private @NotNull RenderInsets getInsets10_10(@NotNull SegmentedButtonConfiguration g, int scale)
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
            case BUTTON_SEGMENTED_TEXTURED_SEPARATED:
            case BUTTON_SEGMENTED_TEXTURED_SEPARATED_TOOLBAR:
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

    protected @NotNull RenderInsets getInsets10_11(@NotNull SegmentedButtonConfiguration g, int scale)
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
                top = size2D(sz, 0, 0, 0.51);
                ha = size2D(sz, 2, 1, 2);
                break;

            case BUTTON_SEGMENTED_TEXTURED_TOOLBAR:
            case BUTTON_SEGMENTED_TEXTURED_SEPARATED_TOOLBAR:
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

    protected @NotNull RenderInsets getInsets10_13new(@NotNull SegmentedButtonConfiguration g, int scale)
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
            case BUTTON_SEGMENTED_TEXTURED_SEPARATED_TOOLBAR:
                top = 0.49;
                ha = 0;
                break;

            default:
                throw new UnsupportedOperationException();
        }

        return createRenderInsets(left, top, left * 2, ha, scale);
    }

    protected @NotNull RenderInsets getInsets10_13old(@NotNull SegmentedButtonConfiguration g, int scale)
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
            case BUTTON_SEGMENTED_TEXTURED_SEPARATED_TOOLBAR:
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

    protected @NotNull RenderInsets getInsets10_14old(@NotNull SegmentedButtonConfiguration g, int scale)
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
            case BUTTON_SEGMENTED_TEXTURED_SEPARATED_TOOLBAR:
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

    protected @NotNull RenderInsets getInsets10_14new(@NotNull SegmentedButtonConfiguration g, int scale)
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
            case BUTTON_SEGMENTED_SEPARATED:
                left = size2D(sz, 2, 2, 1);
                top = size2D(sz, 1, 1, 0);
                ha = size2D(sz, 1, 1, 0);
                break;

            case BUTTON_SEGMENTED_SLIDER_TOOLBAR:
                left = size2D(sz, 1, 1, 1);
                top = size2D(sz, 1, 0, 0);
                ha = size2D(sz, 1, 0, 0);
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
            case BUTTON_SEGMENTED_TEXTURED_SEPARATED_TOOLBAR:
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

    protected @NotNull RenderInsets  getInsets11(@NotNull SegmentedButtonConfiguration g, int scale)
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

            case BUTTON_SEGMENTED_TOOLBAR:
                left = size(sz, 5, 2, 2, 1);
                ha = top = size(sz, 5, 1, 0, 0);
                break;

            case BUTTON_SEGMENTED_TEXTURED:
            case BUTTON_SEGMENTED_SCURVE:
                left = 1;
                top = size2D(sz, 10, 1, 2, 1);
                ha = size2D(sz, 11, 1, 2, 1);
                break;

            case BUTTON_SEGMENTED_TEXTURED_SEPARATED:
                // mapped to BUTTON_SEGMENTED_TEXTURED_SEPARATED_TOOLBAR in 11+
                left = 1;
                top = size2D(sz, 1, 2, 1);
                ha = 2;
                break;

            case BUTTON_SEGMENTED_TEXTURED_TOOLBAR:
                int version = AquaNativeRendering.getSystemRenderingVersion();
                if (version >= 140000) {
                    left = size2D(sz, 6, 1, 1, 1);
                    top = size2D(sz, 0, 0, 0, 0);
                    ha = size2D(sz, 0, 1, 1, 1);
                } else if (version >= 130000) {
                    left = size2D(sz, 6, 1, 1, 1);
                    top = size2D(sz, 6, 1, 1, 1);
                    ha = size2D(sz, 6, 1, 1, 1);
                } else {
                    left = 1;
                    top = size2D(sz, 10, 0, 1, 0);
                    ha = size2D(sz, 11, 1, 1, 1);
                }
                break;

            case BUTTON_SEGMENTED_TEXTURED_SEPARATED_TOOLBAR:
                left = size2D(sz, 6, 1, 1, 1);
                top = size2D(sz, 6, 1, 1, 1);
                ha = size2D(sz, 6, 1, 1, 1);
                break;

            default:
                throw new UnsupportedOperationException();
        }

        return createRenderInsets(left, top, left * 2, ha, scale);
    }

    protected @NotNull RenderInsets getInsets15(@NotNull SegmentedButtonConfiguration g, int scale)
    {
        // Renderer descriptions for macOS 15 rendering

        SegmentedButtonWidget bw = g.getWidget();
        Size sz = g.getSize();
        boolean isSelectAny = g.getTracking() == SwitchTracking.SELECT_ANY;

        double left = 0;
        double top;
        double ha;
        double wa;

        switch (bw) {
            case BUTTON_TAB:
            case BUTTON_SEGMENTED:
            case BUTTON_SEGMENTED_SLIDER:
            case BUTTON_SEGMENTED_SLIDER_TOOLBAR:
            case BUTTON_SEGMENTED_SEPARATED:
                if (!isSelectAny || g.getPosition() == Position.ONLY) {
                    left = size2D(sz, 5, 2, 2, 1);
                    wa = left * 2;
                    top = size2D(sz, 5, 1, 1, 0);
                    ha = size2D(sz, 5, 1, 1, 0);
                } else {
                    left = size2D(sz, 5, 2, 2, 1);
                    int largeWidthAdjustment = bw == SegmentedButtonWidget.BUTTON_SEGMENTED_SEPARATED ? 10 : 9;
                    wa = size2D(sz, largeWidthAdjustment, 4, 5, 4);
                    top = size2D(sz, 5, 1, 1, 0);
                    ha = size2D(sz, 6, 1, 1, 0);
                }
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
            case BUTTON_SEGMENTED_SCURVE:
                left = size2D(sz, 1, 1, 1, 1);
                wa = size2D(sz, 12, 2, 2, 2);
                top = size2D(sz, 9, 1, 2, 1);
                ha = size2D(sz, 9, 1, 2, 1);
                break;

            case BUTTON_SEGMENTED_TEXTURED_TOOLBAR:
                left = size2D(sz, 6, 1, 1, 1);
                wa = size2D(sz, 12, 2, 2, 2);
                top = size2D(sz, 0, 0, 0, 0);
                ha = size2D(sz, 0, 1, 1, 1);
                break;

            case BUTTON_SEGMENTED_TEXTURED_SEPARATED:
                left = 1;
                wa = 2;
                top = size2D(sz, 9, 1, 2, 1);
                ha = size2D(sz, 9, 2, 2, 2);
                break;

            case BUTTON_SEGMENTED_TEXTURED_SEPARATED_TOOLBAR:
                left = size2D(sz, 6, 1, 1, 1);
                wa = size2D(sz, 12, 2, 2, 2);
                top = size2D(sz, 4, 0, 0, 0);
                ha = size2D(sz, 9, 1, 1, 1);
                break;

            case BUTTON_SEGMENTED_TOOLBAR:
                if (g.getPosition() == Position.ONLY) {
                    left = size2D(sz, 1, 1, 1, 1);
                    wa = size2D(sz, 2, 2, 2, 2);
                    top = size2D(sz, 0, 0, 1, 0);
                    ha = size2D(sz, 0, 0, 1, 0);
                } else if (isSelectAny) {
                    left = size2D(sz, 5, 1, 1, 1);
                    wa = size2D(sz, 12, 2, 2, 2);
                    top = size2D(sz, 5, 0, 1, 0);
                    ha = size2D(sz, 11, 0, 1, 1);
                } else {
                    left = size2D(sz, 5, 2, 2, 1);
                    wa = size2D(sz, 12, 4, 4, 2);
                    top = size2D(sz, 5, 1, 1, 0);
                    ha = size2D(sz, 11, 1, 2, 1);
                }
                break;

            default:
                throw new UnsupportedOperationException();
        }

        return createRenderInsets(left, top, wa, ha, scale);
    }

    protected @NotNull RenderInsets getInsets26old(@NotNull SegmentedButtonConfiguration g, int scale)
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

                if (g.getPosition() == Position.ONLY) {
                    // This is probably a bug in AppKit
                    left = size2D(sz, 1, 1, 1, 1);
                    top = size2D(sz, 9, 1, 2, 1);
                }

                return createRenderInsets(left, top, wa, ha, scale);
        }

        return getInsets15(g, scale);  // placeholder
    }

    protected @NotNull RenderInsets getInsets26(@NotNull SegmentedButtonConfiguration g, int scale)
    {
        // TBD: currently only one style is supported

        int version = JNRPlatformUtils.getPlatformVersion();
        if (version < 260300) {
            Size sz = g.getSize();
            if (sz == Size.MINI || sz == Size.SMALL || sz == Size.REGULAR) {
                return createRenderInsets(0.5, 0, 0, 0, scale);
            }
        }
        return createRenderInsets(1.5, 0, 3, 0, scale);
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

    public @NotNull SegmentedControlLayoutInfo getSegmentLayoutInfo(@NotNull SegmentedButtonConfiguration g, int scale)
    {
        if (g.getPosition() == Position.ONLY) {
            return getSegment1LayoutInfo(g, scale);
        } else {
            return getSegment4LayoutInfo(g, scale);
        }
    }

    public @NotNull SegmentedControl1LayoutInfo getSegment1LayoutInfo(@NotNull SegmentedButtonConfiguration g, int scale)
    {
        SegmentedControl4LayoutInfo layout = getSegment4LayoutInfo(g, scale);
        float divider = layout.dividerVisualWidth;
        float first = layout.firstSegmentWidthAdjustment;
        float last = layout.lastSegmentWidthAdjustment;
        float adjustment = getSegment1WidthAdjustment(g, scale, first + last - divider);
        return new SegmentedControl1LayoutInfo(adjustment);
    }

    public float getSegment1WidthAdjustment(@NotNull SegmentedButtonConfiguration g,
                                            int scale,
                                            float defaultValue)
    {
        int version = AquaNativePainter.getSegmentedButtonRenderingVersion();
        if (version == AquaUIPainterBase.SEGMENTED_10_14) {
            return getSegment1WidthAdjustment_14new(g, scale, defaultValue);
        }
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

    public float getSegment1WidthAdjustment_14new(@NotNull SegmentedButtonConfiguration g,
                                                  int scale,
                                                  float defaultValue)
    {
        SegmentedButtonWidget bw = g.getWidget();
        Size sz = g.getSize();
        switch (bw) {
            case BUTTON_SEGMENTED_SLIDER_TOOLBAR:
                if (sz == Size.REGULAR) {
                    return 4;
                }
                return 0;
        }

        return defaultValue;
    }

    public float getSegment1WidthAdjustment_11(@NotNull SegmentedButtonConfiguration g,
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
            case BUTTON_SEGMENTED_SCURVE:
            case BUTTON_SEGMENTED_TOOLBAR:
                return 4;

            case BUTTON_SEGMENTED_TEXTURED_TOOLBAR:
            {
                int version = AquaNativeRendering.getSystemRenderingVersion();
                if (version >= 130000) {
                    return size2D(sz, 4, 4, 0, 0);
                }
                return 4;
            }

            case BUTTON_SEGMENTED_TEXTURED_SEPARATED:
            case BUTTON_SEGMENTED_TEXTURED_SEPARATED_TOOLBAR:
                int version = AquaNativeRendering.getSystemRenderingVersion();
                if (version < 130000) {
                    return size2D(sz, 4, 4, 0, 0);
                }
                return 4;

        }
        return defaultValue;
    }

    public float getSegment1WidthAdjustment_15(@NotNull SegmentedButtonConfiguration g,
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
            case BUTTON_SEGMENTED_TOOLBAR:
            case BUTTON_SEGMENTED_SCURVE:
                return 4;

            case BUTTON_SEGMENTED_TEXTURED_TOOLBAR:
                return size2D(sz, 10, 0, 0, 0);

            case BUTTON_SEGMENTED_TEXTURED_SEPARATED:
            case BUTTON_SEGMENTED_TEXTURED_SEPARATED_TOOLBAR:
                return size2D(sz, 4, 4, 0, 0);
        }
        return defaultValue;
    }

    public float getSegment1WidthAdjustment_26old(@NotNull SegmentedButtonConfiguration g,
                                                  int scale,
                                                  float defaultValue)
    {
        return getSegment1WidthAdjustment_15(g, scale, defaultValue); // placeholder
    }

    public float getSegment1WidthAdjustment_26(@NotNull SegmentedButtonConfiguration g,
                                               int scale,
                                               float defaultValue)
    {
        return 0;
    }

    public @NotNull SegmentedControl4LayoutInfo getSegment4LayoutInfo(@NotNull SegmentedButtonConfiguration g,
                                                                      int scale)
    {
        int version = AquaNativePainter.getSegmentedButtonRenderingVersion();
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

    public @NotNull SegmentedControl4LayoutInfo getSegment4LayoutInfo10_10(@NotNull SegmentedButtonConfiguration g,
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
                first = 2;
                last = 1;
                break;

            case BUTTON_SEGMENTED_TEXTURED_SEPARATED:
            case BUTTON_SEGMENTED_TEXTURED_SEPARATED_TOOLBAR:
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

    public @NotNull SegmentedControl4LayoutInfo getSegment4LayoutInfo10_11(@NotNull SegmentedButtonConfiguration g,
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
                first = 2;
                last = 1;
                dp2 = RIGHT;
                break;

            case BUTTON_SEGMENTED_TEXTURED_SEPARATED:
            case BUTTON_SEGMENTED_TEXTURED_SEPARATED_TOOLBAR:
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

    public @NotNull SegmentedControl4LayoutInfo getSegment4LayoutInfo10_13new(@NotNull SegmentedButtonConfiguration g,
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
                dp2 = CENTER;
        }

        switch (bw) {
            case BUTTON_TAB:
            case BUTTON_SEGMENTED:
            case BUTTON_SEGMENTED_SLIDER:
            case BUTTON_SEGMENTED_SLIDER_TOOLBAR:
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
                first = size2D(sz, 4, 2, 2);
                last = size2D(sz, 3, 1, 1);
                break;

            case BUTTON_SEGMENTED_TEXTURED:
            case BUTTON_SEGMENTED_SCURVE:
            case BUTTON_SEGMENTED_TOOLBAR:
            case BUTTON_SEGMENTED_TEXTURED_SEPARATED:
            case BUTTON_SEGMENTED_TEXTURED_SEPARATED_TOOLBAR:
                first = 4;
                last = 3;
                break;

            default:
                throw new UnsupportedOperationException();
        }

        DividerPosition dp = scale == 1 ? LEFT : dp2;
        return new SegmentedControl4LayoutInfo(dp, dividerVisualWidth, first, middle, last);
    }

    public @NotNull SegmentedControl4LayoutInfo getSegment4LayoutInfo10_14new(@NotNull SegmentedButtonConfiguration g,
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
                first = size2D(sz, 2, 2, 3);
                last = size2D(sz, 1, 1, 2);
                break;

            case BUTTON_SEGMENTED_SLIDER_TOOLBAR:
                first = size2D(sz, 3, 1, 1);
                last = size2D(sz, 2, 0, 0);
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
                first = size2D(sz, 3, 1, 1);
                last = size2D(sz, 2, 0, 0);
                break;

            case BUTTON_SEGMENTED_TEXTURED_SEPARATED_TOOLBAR:
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

    public @NotNull SegmentedControl4LayoutInfo getSegment4LayoutInfo10_13old(@NotNull SegmentedButtonConfiguration g,
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
                first = 2;
                last = 1;
                break;

            case BUTTON_SEGMENTED_TOOLBAR:
            case BUTTON_SEGMENTED_TEXTURED_SEPARATED:
            case BUTTON_SEGMENTED_TEXTURED_SEPARATED_TOOLBAR:
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

    public @NotNull SegmentedControl4LayoutInfo getSegment4LayoutInfo10_14old(@NotNull SegmentedButtonConfiguration g,
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
                first = size2D(sz, 2, 2, 3);
                last = size2D(sz, 1, 1, 2);
                break;

            case BUTTON_SEGMENTED_TEXTURED_SEPARATED:
            case BUTTON_SEGMENTED_TEXTURED_SEPARATED_TOOLBAR:
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

    public @NotNull SegmentedControl4LayoutInfo getSegment4LayoutInfo11(@NotNull SegmentedButtonConfiguration g,
                                                                        int scale)
    {
        SegmentedButtonWidget bw = g.getWidget();
        Size sz = g.getSize();

        double dividerVisualWidth = 1;
        double first = 0;
        double middle = 0;
        double last = 0;

        boolean isSelectAny = g.getTracking() == SwitchTracking.SELECT_ANY && isSelectAnySpecial11(bw, sz);

        if (isSelectAny) {
            switch (bw) {
                case BUTTON_TAB:
                case BUTTON_SEGMENTED:
                case BUTTON_SEGMENTED_SLIDER:
                case BUTTON_SEGMENTED_SLIDER_TOOLBAR:
                case BUTTON_SEGMENTED_TEXTURED:
                case BUTTON_SEGMENTED_SCURVE:
                    bw = SegmentedButtonWidget.BUTTON_SEGMENTED_SEPARATED;
            }
        }

        switch (bw) {
            case BUTTON_TAB:
            case BUTTON_SEGMENTED:
            case BUTTON_SEGMENTED_SLIDER:
            case BUTTON_SEGMENTED_SLIDER_TOOLBAR:
                first = size2D(sz, 13, 3, 3, 5);
                middle = size2D(sz, 13, 3, 3, 5);
                last = size2D(sz, 12, 2, 2, 4);
                break;

            case BUTTON_SEGMENTED_TOOLBAR:
                if (isSelectAny) {
                    first = size2D(sz, 13, 3, 1, 3);
                    middle = size2D(sz, 13, 3, 1, 3);
                    last = size2D(sz, 12, 2, 1, 3);
                } else {
                    first = size2D(sz, 13, 3, 3, 5);
                    middle = size2D(sz, 13, 3, 3, 5);
                    last = size2D(sz, 12, 2, 2, 4);
                }
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
            case BUTTON_SEGMENTED_SCURVE:
                first = size2D(sz, 16, 5, 5, 5);
                middle = size2D(sz, 11, 5, 5, 5);
                last = size2D(sz, 15, 4, 4, 4);
                break;

            case BUTTON_SEGMENTED_TEXTURED_SEPARATED:
                first = size2D(sz, 11, 5, 5, 5);
                middle = size2D(sz, 11, 5, 5, 5);
                last = size2D(sz, 10, 4, 4, 4);
                break;

            case BUTTON_SEGMENTED_TEXTURED_TOOLBAR:
            case BUTTON_SEGMENTED_TEXTURED_SEPARATED_TOOLBAR:

                int version = AquaNativeRendering.getSystemRenderingVersion();
                if (version >= 130000) {
                    first = size2D(sz, 0, 5, 1, 1);
                    middle = size2D(sz, 4, 5, 1, 1);
                    last = size2D(sz, 0, 4, 0, 0);
                } else {
                    first = size2D(sz, 11, 5, 1, 1);
                    middle = size2D(sz, 11, 5, 1, 1);
                    last = size2D(sz, 10, 4, 0, 0);
                }
                break;

            default:
                throw new UnsupportedOperationException();
        }

        return new SegmentedControl4LayoutInfo(LEFT, dividerVisualWidth, first, middle, last);
    }

    private boolean isSelectAnySpecial11(@NotNull SegmentedButtonWidget w, @NotNull Size size)
    {
        switch (w) {
            case BUTTON_TAB:
            case BUTTON_SEGMENTED:
            case BUTTON_SEGMENTED_SLIDER:
            case BUTTON_SEGMENTED_SLIDER_TOOLBAR:
            case BUTTON_SEGMENTED_TOOLBAR:
                return true;
        }
        return false;
    }

    public @NotNull SegmentedControl4LayoutInfo getSegment4LayoutInfo15(@NotNull SegmentedButtonConfiguration g,
                                                                        int scale)
    {
        SegmentedButtonWidget bw = g.getWidget();
        Size sz = g.getSize();

        double dividerVisualWidth = 1;
        double first = 0;
        double middle = 0;
        double last = 0;

        boolean isSelectAny = g.getTracking() == SwitchTracking.SELECT_ANY && isSelectAnySpecial15(bw, sz);
        if (isSelectAny && bw == SegmentedButtonWidget.BUTTON_SEGMENTED_TOOLBAR) {
            bw = SegmentedButtonWidget.BUTTON_SEGMENTED_SCURVE;
        }

        switch (bw) {
            case BUTTON_TAB:
            case BUTTON_SEGMENTED:
            case BUTTON_SEGMENTED_SLIDER:
            case BUTTON_SEGMENTED_SLIDER_TOOLBAR:
            case BUTTON_SEGMENTED_TOOLBAR:

                if (isSelectAny) {
                    first  = size2D(sz, 7, 2, 2, 4);
                    middle = size2D(sz, 7, 1, 1, 3);
                    last   = size2D(sz, 9, 1, 1, 3);
                } else {
                    first = size2D(sz, 13, 3, 3, 5);
                    middle = size2D(sz, 13, 3, 3, 5);
                    last = size2D(sz, 12, 2, 2, 4);
                }
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
            case BUTTON_SEGMENTED_SCURVE:
                first = size2D(sz, 5, 5, 5, 5);
                middle = size2D(sz, 5, 5, 5, 5);
                last = size2D(sz, 4, 4, 4, 4);
                break;

            case BUTTON_SEGMENTED_TEXTURED_TOOLBAR:
                first = size2D(sz, 1, 5, 1, 1);
                middle = size2D(sz, 4, 5, 1, 1);
                last = size2D(sz, 3, 4, 0, 0);
                break;

            case BUTTON_SEGMENTED_TEXTURED_SEPARATED:
                first = size2D(sz, 3, 5, 0, 0);
                middle = size2D(sz, 4, 5, 1, 1);
                last = size2D(sz, 4, 4, 0, 0);
                break;

            case BUTTON_SEGMENTED_TEXTURED_SEPARATED_TOOLBAR:
                first = size2D(sz, 1, 5, 0, 0);
                middle = size2D(sz, 4, 5, 1, 1);
                last = size2D(sz, 2, 4, 0, 0);
                break;

            default:
                throw new UnsupportedOperationException();
        }

        return new SegmentedControl4LayoutInfo(LEFT, dividerVisualWidth, first, middle, last);
    }

    private boolean isSelectAnySpecial15(@NotNull SegmentedButtonWidget w, @NotNull Size size)
    {
        switch (w) {
            case BUTTON_TAB:
            case BUTTON_SEGMENTED:
            case BUTTON_SEGMENTED_SLIDER:
            case BUTTON_SEGMENTED_SLIDER_TOOLBAR:
            case BUTTON_SEGMENTED_TOOLBAR:
                return true;
        }
        return false;
    }

    public @NotNull SegmentedControl4LayoutInfo
    getSegment4LayoutInfo26old(@NotNull SegmentedButtonConfiguration g, int scale)
    {
        return getSegment4LayoutInfo15(g, scale); // placeholder
    }

    private boolean isSelectAnySpecial26old(@NotNull SegmentedButtonWidget w, @NotNull Size size)
    {
        return isSelectAnySpecial15(w, size); // placeholder
    }

    public @NotNull SegmentedControl4LayoutInfo
    getSegment4LayoutInfo26(@NotNull SegmentedButtonConfiguration g, int scale)
    {
        int version = JNRPlatformUtils.getPlatformVersion();
        if (version < 260300) {
            Size sz = g.getSize();
            if (sz == Size.MINI || sz == Size.SMALL || sz == Size.REGULAR) {
                return new SegmentedControl4LayoutInfo(LEFT, 1, 1, 1, 1);
            }
       }
        return new SegmentedControl4LayoutInfo(LEFT, 1, 1, 1, 0);
    }
}
