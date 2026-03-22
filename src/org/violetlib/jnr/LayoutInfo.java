/*
 * Copyright (c) 2015-2026 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr;

import org.jetbrains.annotations.NotNull;
import org.violetlib.jnr.impl.BasicLayoutInfo;

/**
  Layout information that describes fixed and/or minimum sizes for a rendering. This information may be platform UI
  dependent. In rare cases, it may also depend upon the native rendering implementation. All dimensions are specified in
  device independent pixels.
*/

public interface LayoutInfo
{
    /**
       Return a layout info with optional fixed or minimum dimensions.

       @param isWidthFixed If true, the width parameter specifies a fixed width. Otherwise, the width parameter
       specifies a minimum width.
       @param width The fixed or minimum width, or zero if none.
       @param isHeightFixed If true, the height parameter specifies a fixed height. Otherwise, the height parameter
       specifies a minimum height.
       @param height The fixed or minimum height, or zero if none.
       @return the layout info.
       @throws IllegalArgumentException if the specified width or height is negative.
    */

    static @NotNull LayoutInfo create(boolean isWidthFixed, float width, boolean isHeightFixed, float height)
    {
        return BasicLayoutInfo.create(isWidthFixed, width, isHeightFixed, height);
    }

    /**
      Return the width of the visual rendering of the widget, if the width is fixed.

      @return the width, or 0 if the width is not fixed.
    */

    float getFixedVisualWidth();

    /**
      Return the height of the visual rendering of the widget, if the height is fixed.

      @return the height, or 0 if the height is not fixed.
    */

    float getFixedVisualHeight();

    /**
      Return the minimum width of the visual rendering of the widget. If the visual rendering has a fixed width, then
      that width is returned.

      @return the minimum visual width.
    */

    float getMinimumVisualWidth();

    /**
      Return the minimum height of the visual rendering of the widget. If the visual rendering has a fixed height, then
      that height is returned.

      @return the minimum visual height.
    */

    float getMinimumVisualHeight();
}
