/*
 * Copyright (c) 2020 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.aqua.impl;

import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.lang.annotation.Native;

import org.violetlib.jnr.aqua.AquaUIPainter;
import org.violetlib.jnr.aqua.SegmentedButtonConfiguration;
import org.violetlib.jnr.aqua.SegmentedButtonLayoutConfiguration;
import org.violetlib.jnr.impl.AnnotatedSegmentedControlImage;
import org.violetlib.jnr.impl.BasicImageSupport;
import org.violetlib.jnr.impl.PixelRaster;
import org.violetlib.jnr.impl.PixelRasterImpl;
import org.violetlib.jnr.impl.Renderer;
import org.violetlib.jnr.impl.RendererDebugInfo;
import org.violetlib.jnr.impl.ReusableCompositor;

import org.jetbrains.annotations.*;

import static org.violetlib.jnr.aqua.SegmentedButtonConfiguration.*;
import static org.violetlib.jnr.aqua.impl.AquaNativePainter.*;
import static org.violetlib.jnr.aqua.impl.SegmentedControl4LayoutInfo.*;
import static org.violetlib.jnr.aqua.impl.SegmentedControl4LayoutInfo.DividerPosition.*;

/**
  Support for rendering buttons as components of a simulated segmented control, implemented using a native segmented
  control. This class is public for debugging use. Normally, it is used internally to AquaNativePainter.
*/

public class AquaNativeSegmentedControlPainter
{
    protected final SegmentedControlDescriptions scds = new SegmentedControlDescriptions();

    /**
      Create a renderer for a segmented button.
      @param g The button configuration.
      @return the renderer.
    */

    public @NotNull Renderer createSegmentedButtonRenderer(@NotNull SegmentedButtonConfiguration g)
    {
        return new SegmentedButtonRenderer(g);
    }

    private class SegmentedButtonRenderer
      extends Renderer
    {
        private final @NotNull SegmentedButtonConfiguration g;

        public SegmentedButtonRenderer(@NotNull SegmentedButtonConfiguration g)
        {
            this.g = g;
        }

        @Override
        public void composeTo(@NotNull ReusableCompositor compositor)
        {
            int scale = compositor.getScaleFactor();
            int buttonWidth = Math.round(compositor.getWidth());
            int buttonHeight = Math.round(compositor.getHeight());
            RenderInsets s = scds.getInsets(g, scale);
            int rasterWidth;
            int rasterHeight;
            int[] raster;
            int x;
            int y;
            int w;
            int h;
            if (g.getPosition() == Position.ONLY) {
                SegmentedControl1LayoutInfo layout = scds.getSegment1LayoutInfo(g, scale);
                SegmentButtonRenderingConfiguration1 bc
                  = getRenderConfiguration(g.isSelected(), s, layout, scale, buttonWidth, buttonHeight);
                SegmentedControlConfiguration1 cc = createControlConfiguration(g, bc);
                rasterWidth = Math.round(scale * bc.rasterWidth);
                rasterHeight = Math.round(scale * bc.rasterHeight);
                raster = new int[rasterWidth * rasterHeight];
                paintSegmentedControl1(raster, rasterWidth, rasterHeight, scale, cc, false);
                x = Math.round(scale * s.left);
                y = Math.round(scale * s.top);
                w = Math.round(scale * buttonWidth);
                h = Math.round(scale * buttonHeight);
            } else {
                SegmentedControl4LayoutInfo layout = scds.getSegment4LayoutInfo(g, scale);
                SegmentButtonRenderingConfiguration4 bc
                  = getRenderConfiguration(g, s, layout, scale, buttonWidth, buttonHeight);
                SegmentedControlConfiguration4 cc = createControlConfiguration(g, bc);
                rasterWidth = Math.round(scale * bc.rasterWidth);
                rasterHeight = Math.round(scale * bc.rasterHeight);
                raster = new int[rasterWidth * rasterHeight];
                paintSegmentedControl4(raster, rasterWidth, rasterHeight, scale, cc, false);
                Rectangle2D bounds = bc.bounds;
                x = Math.round(scale * (float) bounds.getX());
                y = Math.round(scale * (float) bounds.getY());
                w = Math.round(scale * (float) bounds.getWidth());
                h = Math.round(scale * (float) bounds.getHeight());
            }
            PixelRaster px = new PixelRasterImpl(raster, rasterWidth, rasterHeight);
            compositor.composeFrom(px, x, y, 0, 0, w, h);
        }
    }

    private static @NotNull Rectangle2D extractBounds(@NotNull float[] fs, int start)
    {
        return new Rectangle2D.Float(fs[start], fs[start+1], fs[start+2], fs[start+3]);
    }

    public @Nullable RendererDebugInfo getSegmentedButtonRendererDebugInfo(
      @NotNull SegmentedButtonConfiguration g, int scale, int buttonWidth, int buttonHeight)
    {
        RenderInsets s = scds.getInsets(g, scale);
        if (g.getPosition() == Position.ONLY) {
            SegmentedControl1LayoutInfo layout = scds.getSegment1LayoutInfo(g, scale);
            SegmentButtonRenderingConfiguration1 bc
              = getRenderConfiguration(g.isSelected(), s, layout, scale, buttonWidth, buttonHeight);
            SegmentedControlConfiguration1 cc = createControlConfiguration(g, bc);
            int rasterWidth = Math.round(scale * bc.rasterWidth);
            int rasterHeight = Math.round(scale * bc.rasterHeight);
            int[] raster = new int[rasterWidth * rasterHeight];
            return paintSegmentedControl1(raster, rasterWidth, rasterHeight, scale, cc, true);
        } else {
            SegmentedControl4LayoutInfo layout = scds.getSegment4LayoutInfo(g, scale);
            SegmentButtonRenderingConfiguration4 bc
              = getRenderConfiguration(g, s, layout, scale, buttonWidth, buttonHeight);
            SegmentedControlConfiguration4 cc = createControlConfiguration(g, bc);
            int rasterWidth = Math.round(scale * bc.rasterWidth);
            int rasterHeight = Math.round(scale * bc.rasterHeight);
            int[] raster = new int[rasterWidth * rasterHeight];
            return paintSegmentedControl4(raster, rasterWidth, rasterHeight, scale, cc, true);
        }
    }

    // supports debugging, called using reflection
    public @Nullable float[] getSegmentedButtonLayoutParameters(@NotNull SegmentedButtonLayoutConfiguration g)
    {
        int size = toSize(g.getSize());
        int segmentStyle = toSegmentedStyle(g.getWidget());
        float[] debugOutput = new float[8];
        int result = nativeDetermineSegmentedButtonLayoutParameters(segmentStyle, size, debugOutput);
        return result == 0 ? debugOutput : null;
    }

    @Native
    private static final int SELECT_SEGMENT_1 = 1 << 3;
    @Native private static final int SELECT_SEGMENT_2 = 1 << 2;
    @Native private static final int SELECT_SEGMENT_3 = 1 << 1;
    @Native private static final int SELECT_SEGMENT_4 = 1 << 0;

    @Native private static final int CONTEXT_WINDOW = 1;
    @Native private static final int CONTEXT_TOOLBAR = 2;

    /**
      Return the layout information for rendering a segmented control.
      @param g The segmented button layout configuration.
      @param scale The display scale factor (1 or 2).
      @return the layout information.
    */

    public @NotNull SegmentedControlLayoutInfo getSegmentLayoutInfo(
      @NotNull SegmentedButtonLayoutConfiguration g, int scale)
    {
        return scds.getSegmentLayoutInfo(g, scale);
    }

    /**
      Return the insets to use when rendering a segmented control.
      @param g The segmented button layout configuration.
      @param scale The display scale factor (1 or 2).
      @return the render insets.
    */

    public @NotNull RenderInsets getSegmentedControlInsets(
      @NotNull SegmentedButtonLayoutConfiguration g, int scale)
    {
        return scds.getInsets(g, scale);
    }

    /**
      Compute a configuration used to render a segmented button.

      @param isSelected True if and only if the button state is selected (on).
      @param s The render insets.
      @param layout The segmented control layout information.
      @param scale The display scale.
      @param buttonWidth The desired button width (in points).
      @param buttonHeight The desired button height (in points).
      @return a rendering configuration.
    */

    public @NotNull SegmentButtonRenderingConfiguration1 getRenderConfiguration(
      boolean isSelected,
      @NotNull RenderInsets s,
      @NotNull SegmentedControl1LayoutInfo layout,
      int scale,
      int buttonWidth,
      int buttonHeight)
    {
        float segmentWidth = buttonWidth - layout.widthAdjustment;
        int rasterWidth = (int) Math.ceil(buttonWidth + s.widthAdjust);
        int rasterHeight = (int) Math.ceil(buttonHeight + s.heightAdjust);
        return new SegmentButtonRenderingConfiguration1(scale, isSelected, segmentWidth, rasterWidth, rasterHeight);
    }

    /**
      Compute a configuration used to render a segmented button.

      @param g The button configuration. The currently required information is: whether the button style is a separated
      style; whether the button is in the selected state; the position of the button in the control;
      @param s The render insets.
      @param layout The segmented control layout information.
      @param scale The display scale.
      @param segmentWidth The desired segment width (in points).
      @param controlHeight The desired control height (in points).
      @return a rendering configuration.
    */

    public @NotNull SegmentButtonRenderingConfiguration4
    getRenderConfiguration(@NotNull SegmentedButtonConfiguration g,
                           @NotNull RenderInsets s,
                           @NotNull SegmentedControl4LayoutInfo layout,
                           int scale, int segmentWidth, int controlHeight)
    {
        boolean isSeparated = g.getWidget().isSeparated();
        boolean isSelected = g.isSelected();
        Position pos = g.getPosition();
        DividerState leftDivider = g.getLeftDividerState();
        DividerState rightDivider = g.getRightDividerState();
        return getRenderConfiguration(isSeparated, isSelected, pos, leftDivider, rightDivider,
          s, layout, scale, segmentWidth, controlHeight);
    }

    /**
      Compute a configuration used to render a segmented button.

      @param isSeparated True if and only if the button style is a separated style.
      @param isSelected True if and only if the button state is selected (on).
      @param pos The position of the button in the control. Must not be ONLY.
      @param leftDivider The requested state of the divider on the left side of the segment.
      @param rightDivider The requested state of the divider on the right side of the segment.
      @param s The render insets.
      @param layout The segmented control layout information.
      @param scale The display scale.
      @param segmentWidth The desired segment width (in points).
      @param controlHeight The desired control height (in points).
      @return a rendering configuration.
    */

    public @NotNull SegmentButtonRenderingConfiguration4 getRenderConfiguration(
      boolean isSeparated,
      boolean isSelected,
      @NotNull Position pos,
      @NotNull DividerState leftDivider,
      @NotNull DividerState rightDivider,
      @NotNull RenderInsets s,
      @NotNull SegmentedControl4LayoutInfo layout,
      int scale,
      int segmentWidth,
      int controlHeight)
    {
        DividerPosition dividerPosition = layout.dividerPosition;
        float dividerVisualWidth = layout.dividerVisualWidth;
        float firstExtra = layout.firstSegmentWidthAdjustment;
        float middleExtra = layout.middleSegmentWidthAdjustment;
        float lastExtra = layout.lastSegmentWidthAdjustment;
        float otherSegmentNominalWidth = 20;
        float renderedSegmentNominalWidth;
        int segmentIndex;
        int selectedSegmentIndex = -1;
        float xOffset = 0;
        float widthAdjustment = 0;

        if (pos == Position.FIRST) {
            segmentIndex = 0;
            renderedSegmentNominalWidth = segmentWidth - firstExtra;
        } else if (pos == Position.MIDDLE) {
            segmentIndex = 1;
            renderedSegmentNominalWidth = segmentWidth - middleExtra;
        } else if (pos == Position.LAST) {
            segmentIndex = 3;
            renderedSegmentNominalWidth = segmentWidth - lastExtra;
        } else {
            throw new AssertionError("Unexpected segment position");
        }

        if (isSelected) {
            selectedSegmentIndex = segmentIndex;
        } else if (leftDivider == SegmentedButtonConfiguration.DividerState.SELECTED) {
            if (segmentIndex > 0) {
                selectedSegmentIndex = segmentIndex - 1;
            }
        } else if (rightDivider == SegmentedButtonConfiguration.DividerState.SELECTED) {
            if (segmentIndex == 1) {
                segmentIndex = 2;
                selectedSegmentIndex = 3;
            } else if (segmentIndex == 0) {
                selectedSegmentIndex = 1;
            }
        }

        if (segmentIndex > 0) {
            // the width of the first segment is otherSegmentWidth + firstExtra
            xOffset = segmentIndex * (otherSegmentNominalWidth + middleExtra) + (firstExtra - middleExtra);
            widthAdjustment += firstExtra;
        }

        if (segmentIndex < 3) {
            // the width of the last segment is otherSegmentWidth + lastExtra
            widthAdjustment += lastExtra;
        }

        if (segmentIndex == 0 || segmentIndex == 3) {
            widthAdjustment += 2 * middleExtra;
        } else {
            widthAdjustment += middleExtra;
        }

        // The next step is to adjust the segment width and extracted region location to either hide or reveal dividers,
        // as needed to satisfy the requested set of dividers. This adjustment is inhibited in the case of a
        // separated style in 2x when the "divider" is centered. What this basically means
        // is that we always want to show the entire segment, as the half divider on either side is the border or
        // space that we want to be visible.

        boolean isSeparatedCentered2X = isSeparated && scale != 1 && dividerPosition == CENTER;
        if (!isSeparatedCentered2X && dividerVisualWidth > 0) {
            if (segmentIndex > 0) {
                // adjust so that the left divider is not visible by default
                if (dividerPosition != LEFT) {
                    //NSLog(@"Shifting and widening to hide left divider");
                    xOffset += dividerVisualWidth;
                    renderedSegmentNominalWidth += dividerVisualWidth;
                    widthAdjustment += dividerVisualWidth;
                }
            }

            if (segmentIndex < 3) {
                // adjust so that the right divider is not visible by default
                if (dividerPosition != RIGHT) {
                    //NSLog(@"Widening to hide right divider");
                    renderedSegmentNominalWidth += dividerVisualWidth;
                    widthAdjustment += dividerVisualWidth;
                }
            }

            boolean drawLeadingDivider = segmentIndex > 0 && leftDivider != DividerState.NONE;
            boolean drawTrailingDivider = segmentIndex < 3 && rightDivider != DividerState.NONE;
            if (drawLeadingDivider) {
                xOffset -= dividerVisualWidth;
                int adjustment = (int) Math.ceil(dividerVisualWidth);
                if (dividerPosition == CENTER) {
                    xOffset -= dividerVisualWidth;
                    adjustment += dividerVisualWidth;
                }
                renderedSegmentNominalWidth -= adjustment;
                widthAdjustment -= adjustment;
                //System.err.println("Shifting to reveal left divider");
            }

            if (drawTrailingDivider) {
                renderedSegmentNominalWidth -= dividerVisualWidth;
                widthAdjustment -= dividerVisualWidth;
                if (dividerPosition == CENTER) {
                    renderedSegmentNominalWidth -= dividerVisualWidth;
                    widthAdjustment -= dividerVisualWidth;
                }
                //System.err.println("Shrinking to reveal right divider");
            }
        }

        float controlWidth = 3 * otherSegmentNominalWidth + segmentWidth + widthAdjustment;
        int rasterWidth = (int) Math.ceil(controlWidth + s.widthAdjust);
        int rasterHeight = (int) Math.ceil(controlHeight + s.heightAdjust);

        // TBD: the only novelty in the segment bounds is the xOffset.
        // The rest is based on provided inputs and the raster insets.

        Rectangle2D segmentBounds = createPixelBounds(scale, s.left + xOffset, s.top, segmentWidth, controlHeight);

        return new SegmentButtonRenderingConfiguration4(scale, segmentIndex + 1, selectedSegmentIndex + 1,
          renderedSegmentNominalWidth, otherSegmentNominalWidth, rasterWidth, rasterHeight, segmentBounds);
    }

    private static @NotNull Rectangle2D createPixelBounds(int scale, double x, double y, double w, double h)
    {
        double x2 = round(x + w, scale);
        double y2 = round(y + h, scale);
        x = round(x, scale);
        y = round(y, scale);
        w = x2 - x;
        h = y2 - y;
        return new Rectangle2D.Double(x, y, w, h);
    }

    private static double round(double d, int scale)
    {
        if (scale == 1) {
            return Math.round(d);
        }
        return Math.round(d * scale) / (double) scale;
    }

    /**
      Create a configuration for rendering a segmented control.
      @param g The segmented button configuration.
      @param b The segmented button rendering configuration.
      @return the control configuration.
    */

    public @NotNull SegmentedControlConfiguration1
    createControlConfiguration(@NotNull SegmentedButtonConfiguration g, @Nullable SegmentButtonRenderingConfiguration1 b)
    {
        SegmentedButtonWidget widget = g.getWidget();
        boolean isToolbar = widget.isToolbar();
        AquaUIPainter.Size sz = g.getSize();
        AquaUIPainter.State st = g.getState();
        AquaUIPainter.SwitchTracking tracking = g.getTracking();
        SegmentedButtonWidget basicWidget = widget.toBasicWidget();
        float w = b != null ? b.segmentWidth : 20;
        boolean isSelected = b != null && b.isSelected;
        return new SegmentedControlConfiguration1(basicWidget, isToolbar, sz, st, w, isSelected);
    }

    /**
      Create a configuration for rendering a segmented control.
      @param g The segmented button configuration.
      @param b The segmented button rendering configuration.
      @return the control configuration.
    */

    public @NotNull SegmentedControlConfiguration4
    createControlConfiguration(@NotNull SegmentedButtonConfiguration g, @Nullable SegmentButtonRenderingConfiguration4 b)
    {
        SegmentedButtonWidget widget = g.getWidget();
        boolean isToolbar = widget.isToolbar();
        AquaUIPainter.Size sz = g.getSize();
        AquaUIPainter.State st = g.getState();
        AquaUIPainter.SwitchTracking tracking = g.getTracking();
        SegmentedButtonWidget basicWidget = widget.toBasicWidget();

        float sw1 = b != null ? b.otherSegmentWidth : 20;
        float sw2 = b != null ? b.otherSegmentWidth : 20;
        float sw3 = b != null ? b.otherSegmentWidth : 20;
        float sw4 = b != null ? b.otherSegmentWidth : 20;
        boolean sel1 = false;
        boolean sel2 = false;
        boolean sel3 = false;
        boolean sel4 = false;

        if (b != null) {
            int i = b.designatedSegment;
            if (i == 1) {
                sw1 = b.segmentWidth;
            } else if (i == 2) {
                sw2 = b.segmentWidth;
            } else if (i == 3) {
                sw3 = b.segmentWidth;
            } else if (i == 4) {
                sw4 = b.segmentWidth;
            }
            int leftSelectedIndex = g.getLeftDividerState() == SegmentedButtonConfiguration.DividerState.SELECTED
              && b.selectedSegment > 0 ? b.selectedSegment - 1 : 0;
            int rightSelectedIndex = g.getRightDividerState() == SegmentedButtonConfiguration.DividerState.SELECTED
              && b.selectedSegment > 0 ? b.selectedSegment + 1 : 0;

            i = b.selectedSegment;
            if (i == 1 || leftSelectedIndex == 1) {
                sel1 = true;
            }
            if (i == 2 || leftSelectedIndex == 2 || rightSelectedIndex == 2) {
                sel2 = true;
            }
            if (i == 3 || leftSelectedIndex == 3 || rightSelectedIndex == 3) {
                sel3 = true;
            }
            if (i == 4 || rightSelectedIndex == 4) {
                sel4 = true;
            }
        }

        return new SegmentedControlConfiguration4(basicWidget, isToolbar, sz, st, tracking, sw1, sw2, sw3, sw4, sel1, sel2, sel3, sel4);
    }

    /**
      Paint a segmented control into a raster buffer.

      @param data The raster buffer where 32-bit RGBA pixels will be written.
      @param rw The width of the raster buffer, in pixels.
      @param rh The height of the raster buffer, in pixels.
      @param scale The scale factor, typically 1 or 2.
      @param g The control configuration.
      @param requestDebugOutput True to request debug information.
      @return the requested debug information, or null if not requested.
    */

    public @Nullable AnnotatedSegmentedControlImage paintSegmentedControl(
      @NotNull int[] data,
      int rw,
      int rh,
      float scale,
      SegmentedControlConfiguration g,
      boolean requestDebugOutput
    )
    {
        if (g instanceof SegmentedControlConfiguration1) {
            return paintSegmentedControl1(data, rw, rh, scale, (SegmentedControlConfiguration1) g, requestDebugOutput);
        } else {
            return paintSegmentedControl4(data, rw, rh, scale, (SegmentedControlConfiguration4) g, requestDebugOutput);
        }
    }

    /**
      Paint a segmented control with one segment into a raster buffer.

      @param data The raster buffer where 32-bit RGBA pixels will be written.
      @param rw The width of the raster buffer, in pixels.
      @param rh The height of the raster buffer, in pixels.
      @param scale The scale factor, typically 1 or 2.
      @param g The control configuration.
      @param requestDebugOutput True to request debug information.
      @return the requested debug information, or null if not requested.
    */

    private @Nullable AnnotatedSegmentedControlImage paintSegmentedControl1(
      @NotNull int[] data,
      int rw,
      int rh,
      float scale,
      SegmentedControlConfiguration1 g,
      boolean requestDebugOutput
    )
    {
        if (rw < 1 || rh < 1 || data.length < rw * rh) {
            throw new IllegalArgumentException("Invalid raster size or length");
        }
        if (scale < 0.01 || scale > 100) {
            throw new IllegalArgumentException("Invalid or unsupported scale factor");
        }

        int size = toSize(g.size);
        int state = toState(g.state);
        int style = toSegmentedStyle(g.widget);
        boolean isSelected = g.isSelected;
        int context = g.isToolbar ? CONTEXT_TOOLBAR : CONTEXT_WINDOW;

        float[] debugData = requestDebugOutput ? new float[4] : null;

        nativePaintSegmentedControl1(data, rw, rh, scale, g.w, style, isSelected, context, size, state, debugData);

        if (debugData != null) {
            int count = debugData.length / 4;
            Rectangle2D[] segmentBounds = new Rectangle2D[count];
            int offset = 0;
            for (int i = 0; i < count; i++) {
                segmentBounds[i] = extractBounds(debugData, offset);
                offset += 4;
            }
            BufferedImage im = BasicImageSupport.createImage(data, rw, rh);
            return new AnnotatedSegmentedControlImage(im, segmentBounds);
        } else {
            return null;
        }
    }

    /**
      Paint a segmented control with 4 segments into a raster buffer.

      @param data The raster buffer where 32-bit RGBA pixels will be written.
      @param rw The width of the raster buffer, in pixels.
      @param rh The height of the raster buffer, in pixels.
      @param scale The scale factor, typically 1 or 2.
      @param g The control configuration.
      @param requestDebugOutput True to request debug information.
      @return the requested debug information, or null if not requested.
    */

    private @Nullable AnnotatedSegmentedControlImage paintSegmentedControl4(
      @NotNull int[] data,
      int rw,
      int rh,
      float scale,
      SegmentedControlConfiguration4 g,
      boolean requestDebugOutput
    )
    {
        if (rw < 1 || rh < 1 || data.length < rw * rh) {
            throw new IllegalArgumentException("Invalid raster size or length");
        }
        if (scale < 0.01 || scale > 100) {
            throw new IllegalArgumentException("Invalid or unsupported scale factor");
        }

        int size = toSize(g.size);
        int state = toState(g.state);
        int style = toSegmentedStyle(g.widget);
        int tracking = toTracking(g.tracking);
        int selectionFlags = toSelectionFlags(g.s1, g.s2, g.s3, g.s4);
        int context = g.isToolbar ? CONTEXT_TOOLBAR : CONTEXT_WINDOW;

        float[] debugData = requestDebugOutput ? new float[16] : null;

        nativePaintSegmentedControl4(data, rw, rh, scale, g.w1, g.w2, g.w3, g.w4, style, tracking,
          selectionFlags, context, size, state, debugData);

        if (debugData != null) {
            int count = debugData.length / 4;
            Rectangle2D[] segmentBounds = new Rectangle2D[count];
            int offset = 0;
            for (int i = 0; i < count; i++) {
                segmentBounds[i] = extractBounds(debugData, offset);
                offset += 4;
            }
            BufferedImage im = BasicImageSupport.createImage(data, rw, rh);
            return new AnnotatedSegmentedControlImage(im, segmentBounds);
        } else {
            return null;
        }
    }

    private static int toSelectionFlags(boolean s1, boolean s2, boolean s3, boolean s4)
    {
        return (s1 ? SELECT_SEGMENT_1 : 0)
          | (s2 ? SELECT_SEGMENT_2 : 0)
          | (s3 ? SELECT_SEGMENT_3 : 0)
          | (s4 ? SELECT_SEGMENT_4 : 0);
    }
    /**
      Paint a segmented control with 4 segments into a raster buffer.

      @param data The raster buffer where 32-bit RGBA pixels will be written.
      @param rw The width of the raster buffer, in pixels.
      @param rh The height of the raster buffer, in pixels.
      @param scale The scale factor, typically 1 or 2.
      @param w1 the nominal width (in points) of the 1st segment
      @param w2 the nominal width (in points) of the 2nd segment
      @param w3 the nominal width (in points) of the 3rd segment
      @param w4 the nominal width (in points) of the 4th segment
      @param style the control style
      @param tracking the tracking mode (select one or select any)
      @param selections a bitmask indicating which segments are to be selected (lower 4 bits as 1,2,3,4)
      @param context the context of the control (toolbar = 1, normal = 0)
      @param sz the size variant
      @param st the control state
      @return 0 if successful, an error code otherwise.
    */

    private static native int nativePaintSegmentedControl4(
      int[] data,
      int rw,
      int rh,
      float scale,
      float w1,
      float w2,
      float w3,
      float w4,
      int style,
      int tracking,
      int selections,
      int context,
      int sz,
      int st,
      @Nullable float[] debugOutput
    );

    /**
      Paint a segmented control with one segment into a raster buffer.

      @param data The raster buffer where 32-bit RGBA pixels will be written.
      @param rw The width of the raster buffer, in pixels.
      @param rh The height of the raster buffer, in pixels.
      @param scale The scale factor, typically 1 or 2.
      @param w the nominal width (in points) of the segment
      @param style the control style
      @param isSelected true if the segment should be selected
      @param context the context of the control (toolbar = 1, normal = 0)
      @param sz the size variant
      @param st the control state
      @return 0 if successful, an error code otherwise.
    */

    private static native int nativePaintSegmentedControl1(
      int[] data,
      int rw,
      int rh,
      float scale,
      float w,
      int style,
      boolean isSelected,
      int context,
      int sz,
      int st,
      @Nullable float[] debugOutput
    );

    private static native int nativeDetermineSegmentedButtonLayoutParameters(int segmentStyle, int size, float[] data);
    public static native int nativeDetermineSegmentedButtonRenderingVersion();

    // The following method represents a failed experiment. Although I am not sure why, asking a view for its size does
    // not always provide the information needed here.

    private static native int nativeDetermineSegmentedButtonFixedHeight(int segmentStyle, int size);
}
