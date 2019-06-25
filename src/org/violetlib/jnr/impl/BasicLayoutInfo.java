/*
 * Copyright (c) 2015 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.impl;

import org.violetlib.jnr.LayoutInfo;

import org.jetbrains.annotations.*;

/**
  A basic implementation of layout information.
*/

public class BasicLayoutInfo
  implements LayoutInfo
{
    private final float fixedWidth;
    private final float fixedHeight;
    private final float minimumWidth;
    private final float minimumHeight;

    private static final @NotNull LayoutInfo EMPTY = new BasicLayoutInfo(0, 0, 0, 0);

    /**
      Return a layout info with no fixed or minimum dimensions.
    */

    public static @NotNull LayoutInfo getInstance()
    {
        return EMPTY;
    }

    /**
      Return a layout info with an optional fixed width.

      @param fixedWidth The fixed width, or zero if not fixed.
      @return the layout info.
      @throws IllegalArgumentException if the specified width is negative.
    */

    public static @NotNull LayoutInfo createFixedWidth(float fixedWidth)
    {
        return new BasicLayoutInfo(fixedWidth, 0, 0, 0);
    }

    /**
      Return a layout info with an optional fixed height.

      @param fixedHeight The fixed height, or zero if not fixed.
      @return the layout info.
      @throws IllegalArgumentException if the specified height is negative.
    */

    public static @NotNull LayoutInfo createFixedHeight(float fixedHeight)
    {
        return new BasicLayoutInfo(0, fixedHeight, 0, 0);
    }

    /**
      Return a layout info with optional fixed dimensions.

      @param fixedWidth The fixed width, or zero if not fixed.
      @param fixedHeight The fixed height, or zero if not fixed.
      @return the layout info.
      @throws IllegalArgumentException if the specified width or height is negative.
    */

    public static @NotNull LayoutInfo createFixed(float fixedWidth, float fixedHeight)
    {
        return new BasicLayoutInfo(fixedWidth, fixedHeight, 0, 0);
    }

    /**
      Return a layout info with an optional minimum width.

      @param minWidth The minimum width, or zero if no minimum.
      @return the layout info.
      @throws IllegalArgumentException if the specified width is negative.
    */

    public static @NotNull LayoutInfo createMinimumWidth(float minWidth)
    {
        return new BasicLayoutInfo(0, 0, minWidth, 0);
    }

    /**
      Return a layout info with an optional minimum height.

      @param minHeight The minimum height, or zero if no minimum.
      @return the layout info.
      @throws IllegalArgumentException if the specified height is negative.
    */

    public static @NotNull LayoutInfo createMinimumHeight(float minHeight)
    {
        return new BasicLayoutInfo(0, 0, 0, minHeight);
    }

    /**
      Return a layout info with optional minimum dimensions.

      @param minWidth The minimum width, or zero if no minimum.
      @param minHeight The minimum height, or zero if no minimum.
      @return the layout info.
      @throws IllegalArgumentException if the specified width or height is negative.
    */

    public static @NotNull LayoutInfo createMinimum(float minWidth, float minHeight)
    {
        return new BasicLayoutInfo(0, 0, minWidth, minHeight);
    }

    /**
      Return a layout info with optional fixed or minimum dimensions.

      @param isWidthFixed If true, the width parameter specifies a fixed width. Otherwise, the width
      parameter specifies a minimum width.
      @param width The fixed or minimum width, or zero if none.
      @param isHeightFixed If true, the height parameter specifies a fixed height. Otherwise, the height
      parameter specifies a minimum height.
      @param height The fixed or minimum height, or zero if none.
      @return the layout info.
      @throws IllegalArgumentException if the specified width or height is negative.
    */

    public static @NotNull LayoutInfo create(boolean isWidthFixed, float width, boolean isHeightFixed, float height)
    {
        float fixedWidth = isWidthFixed ? width : 0;
        float fixedHeight = isHeightFixed ? height : 0;
        float minWidth = isWidthFixed ? 0 : width;
        float minHeight = isHeightFixed ? 0 : height;
        return new BasicLayoutInfo(fixedWidth, fixedHeight, minWidth, minHeight);
    }

    private BasicLayoutInfo(float fixedWidth, float fixedHeight, float minimumWidth, float minimumHeight)
      throws IllegalArgumentException
    {
        if (fixedWidth < 0) {
            throw new IllegalArgumentException("Invalid negative fixed width");
        }

        if (fixedHeight < 0) {
            throw new IllegalArgumentException("Invalid negative fixed height");
        }

        if (minimumWidth < 0) {
            throw new IllegalArgumentException("Invalid negative minimum width");
        }

        if (minimumHeight < 0) {
            throw new IllegalArgumentException("Invalid negative minimum height");
        }

        if (minimumWidth > 0 && fixedWidth > 0 && minimumWidth != fixedWidth) {
            throw new IllegalArgumentException("Incompatible fixed and minimum width");
        }

        if (minimumHeight > 0 && fixedHeight > 0 && minimumHeight != fixedHeight) {
            throw new IllegalArgumentException("Incompatible fixed and minimum height");
        }

        this.fixedWidth = fixedWidth;
        this.fixedHeight = fixedHeight;
        this.minimumWidth = minimumWidth > 0 ? minimumWidth : fixedWidth;
        this.minimumHeight = minimumHeight > 0 ? minimumHeight : fixedHeight;
    }

    @Override
    public float getFixedVisualWidth()
    {
        return fixedWidth;
    }

    @Override
    public float getFixedVisualHeight()
    {
        return fixedHeight;
    }

    @Override
    public float getMinimumVisualWidth()
    {
        return minimumWidth;
    }

    @Override
    public float getMinimumVisualHeight()
    {
        return minimumHeight;
    }
}
