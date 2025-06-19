/*
 * Copyright (c) 2015-2025 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.aqua.impl;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.violetlib.jnr.Insetter;
import org.violetlib.jnr.LayoutInfo;
import org.violetlib.jnr.aqua.*;
import org.violetlib.jnr.impl.Colors;
import org.violetlib.jnr.impl.JNRPlatformUtils;
import org.violetlib.vappearances.VAppearance;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.Map;

/**
  An abstract base class containing common code that supports layout but not rendering.
*/

public abstract class AquaUIPainterAbstractBase
  implements AquaUIPainter
{
    /*
      Because all paint requests are issued from the UI event thread, we can use a separate method to set the generic
      parameters.
    */

    protected int w;
    protected int h;
    protected @Nullable VAppearance appearance;
    protected boolean forceVertical;  // for evaluation mode only
    protected boolean isAlignmentEnabled = true;

    // Layout related state
    protected @Nullable LayoutInfo layoutInfo;

    // The following variables define the current rendering bounds. The bounds may be different only if the widget
    // specifies a fixed width or height that is smaller than the client specified bounds. The client specified bounds
    // always have a zero origin.
    protected float xOffset;  // the X offset introduced to center a fixed width widget in a larger space
    protected float yOffset;  // the Y offset introduced to center a fixed height widget in a larger space
    protected float pWidth;   // the width of the painted area, limited by the widget fixed width (if any)
    protected float pHeight;  // the height of the painted area, limited by the widget fixed height (if any)

    protected final @NotNull AquaUILayoutInfo uiLayout;
    protected final @NotNull UIOutliner uiOutliner;

    protected static @NotNull AquaUILayoutInfo createLayout()
    {
        int platformVersion = JNRPlatformUtils.getPlatformVersion();
        if (platformVersion >= 260000) {
            int version = AquaUIPainterBase.internalGetSegmentedButtonRenderingVersion();
            if (version == AquaUIPainterBase.SEGMENTED_26_OLD) {
                return new LayoutInfo_26old();
            }
            return new LayoutInfo_26();
        }
        if (platformVersion >= 150000) {
            return new LayoutInfo_15();
        }
        if (platformVersion >= 120000) {
            return new LayoutInfo_12();
        }
        if (platformVersion >= 101600) {
            return new LayoutInfo_11();
        }
        if (platformVersion >= 101100) {
            return new LayoutInfo_10_11();
        }
            return new LayoutInfo10_10();

    }

    protected static @NotNull UIOutliner createOutliner(@NotNull AquaUILayoutInfo uiLayout)
    {
        int platformVersion = JNRPlatformUtils.getPlatformVersion();
        return platformVersion >= 101600
                 ? new BigSurOutliner((LayoutInfo_11) uiLayout)
                 : new YosemiteOutliner((LayoutInfo10_10) uiLayout);
    }

    protected AquaUIPainterAbstractBase()
    {
        this.uiLayout = createLayout();
        this.uiOutliner = createOutliner(uiLayout);
    }

    public void setAlignmentEnabled(boolean b)
    {
        isAlignmentEnabled = b;
    }

    @Override
    public @NotNull Map<String,Color> getColors(@NotNull VAppearance appearance)
    {
        Colors colors = Colors.getColors(appearance);
        return colors.getColors();
    }

    @Override
    public void configureAppearance(@NotNull VAppearance appearance)
    {
        this.appearance = appearance;
    }

    @Override
    public void configure(int w, int h)
    {
        this.w = w;
        this.h = h;

        forceVertical = false;
    }

    @Override
    public @NotNull AquaUILayoutInfo getLayoutInfo()
    {
        return uiLayout;
    }

    @Override
    public final @NotNull Rectangle2D getComboBoxEditorBounds(@NotNull ComboBoxLayoutConfiguration g)
    {
        LayoutInfo layoutInfo = uiLayout.getLayoutInfo(g);
        Rectangle2D bounds = getCenteredBounds(layoutInfo);
        Insetter s = uiLayout.getComboBoxEditorInsets(g);
        return s.applyToBounds2D(bounds);
    }

    @Override
    public final @NotNull Rectangle2D getComboBoxIndicatorBounds(@NotNull ComboBoxLayoutConfiguration g)
    {
        LayoutInfo layoutInfo = uiLayout.getLayoutInfo(g);
        Rectangle2D bounds = getCenteredBounds(layoutInfo);
        Insetter s = uiLayout.getComboBoxIndicatorInsets(g);
        return s.applyToBounds2D(bounds);
    }

    @Override
    public final @NotNull Rectangle2D getPopupButtonContentBounds(@NotNull PopupButtonLayoutConfiguration g)
    {
        LayoutInfo layoutInfo = uiLayout.getLayoutInfo(g);
        Rectangle2D bounds = getCenteredBounds(layoutInfo);
        Insetter s = uiLayout.getPopupButtonContentInsets(g);
        return s.applyToBounds2D(bounds);
    }

    @Override
    public float getScrollBarThumbPosition(@NotNull ScrollBarThumbLayoutConfiguration g, boolean useExtent)
    {
        LayoutInfo layoutInfo = uiLayout.getLayoutInfo(g);
        Rectangle2D bounds = getCenteredBounds(layoutInfo);
        return uiLayout.getScrollBarThumbPosition(bounds, g, useExtent);
    }

    @Override
    public int getScrollBarThumbHit(@NotNull ScrollBarThumbConfiguration g)
    {
        LayoutInfo layoutInfo = uiLayout.getLayoutInfo(g);
        Rectangle2D bounds = getCenteredBounds(layoutInfo);
        return uiLayout.getScrollBarThumbHit(bounds, g);
    }

    @Override
    public final @NotNull Rectangle2D getSliderThumbBounds(@NotNull SliderLayoutConfiguration g, double thumbPosition)
    {
        LayoutInfo layoutInfo = uiLayout.getLayoutInfo(g);
        Rectangle2D bounds = getCenteredBounds(layoutInfo);
        return uiLayout.getSliderThumbBounds(bounds, g, thumbPosition);
    }

    @Override
    public final @NotNull Rectangle2D getSliderLabelBounds(@NotNull SliderLayoutConfiguration g,
                                                           double thumbPosition,
                                                           @NotNull Dimension size)
    {
        if (thumbPosition < 0 || thumbPosition > 1) {
            return new Rectangle(0, 0, 0, 0);
        }

        LayoutInfo layoutInfo = uiLayout.getLayoutInfo(g);
        Rectangle2D bounds = getCenteredBounds(layoutInfo);
        return uiLayout.getSliderLabelBounds(bounds, g, thumbPosition, size);
    }

    /**
      Return the location along the major axis of the center of the thumb for a given thumb position. This method is
      appropriate only for linear sliders.

      @param bounds The slider bounds.
      @param g The slider layout configuration.
      @param thumbPosition The thumb position.
      @return the X coordinate of the thumb center, if the slider is horizontal, or the Y coordinate of the thumb
      center, if the slider is vertical.
    */

    public final double getSliderThumbCenter(@NotNull Rectangle2D bounds,
                                             @NotNull SliderLayoutConfiguration g,
                                             double thumbPosition)
    {
        return uiLayout.getSliderThumbCenter(bounds, g, thumbPosition);
    }

    @Override
    public final double getSliderThumbPosition(@NotNull SliderLayoutConfiguration g, int x, int y)
    {
        LayoutInfo layoutInfo = uiLayout.getLayoutInfo(g);
        Rectangle2D bounds = getCenteredBounds(layoutInfo);
        return uiLayout.getSliderThumbPosition(bounds, g, x, y);
    }

    /**
      Update the rendering configuration based on the widget layout.

      @param info The layout info for the widget being rendered. Null if not available.
    */

    protected void configureLayout(@Nullable LayoutInfo info)
    {
        xOffset = 0;
        yOffset = 0;
        pWidth = w;
        pHeight = h;
        layoutInfo = info;

        if (layoutInfo != null) {
            // If the widget rendering cannot expand to fill the requested space, we center the rendering in that space.

            float fixedWidth = layoutInfo.getFixedVisualWidth();
            if (fixedWidth > 0) {
                if (w > fixedWidth && isAlignmentEnabled) {
                    xOffset = (w - fixedWidth) / 2;
                }
                pWidth = fixedWidth;
            }

            float fixedHeight = layoutInfo.getFixedVisualHeight();
            if (fixedHeight > 0) {
                if (h > fixedHeight && isAlignmentEnabled) {
                    yOffset = (h - fixedHeight) / 2;
                }
                pHeight = fixedHeight;
            }
        }
    }

    /**
      Return the configured bounds adjusted for the alignment that is required when the layout bounds exceeds one or
      more fixed sizes.
    */

    protected @NotNull Rectangle2D getCenteredBounds(@Nullable LayoutInfo layoutInfo)
    {
        float x = 0;
        float y = 0;
        float width = w;
        float height = h;

        if (layoutInfo != null) {
            float fixedWidth = layoutInfo.getFixedVisualWidth();
            if (fixedWidth > 0) {
                float extra = width - fixedWidth;
                if (extra > 0) {
                    x += extra / 2;
                    width -= extra;
                }
            }

            float fixedHeight = layoutInfo.getFixedVisualHeight();
            if (fixedHeight > 0) {
                float extra = height - fixedHeight;
                if (extra > 0) {
                    y += extra / 2;
                    height -= extra;
                }
            }
        }

        return new Rectangle2D.Float(x, y, width, height);
    }
}
