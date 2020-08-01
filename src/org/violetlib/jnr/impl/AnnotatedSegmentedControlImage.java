/*
 * Copyright (c) 2016-2020 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.impl;

import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import org.jetbrains.annotations.*;

/**
  Information for debugging a segmented control renderer.
*/

public class AnnotatedSegmentedControlImage
{
    protected final @NotNull BufferedImage fullImage;
    protected final @NotNull Rectangle2D[] segmentBounds;

    public AnnotatedSegmentedControlImage(@NotNull BufferedImage fullImage,
                                          @NotNull Rectangle2D[] segmentBounds)
    {
        this.fullImage = fullImage;
        this.segmentBounds = segmentBounds;
    }

    /**
      If the renderer subsets a larger image, return the larger image.
      @return the image, or null if none.
    */

    public @NotNull BufferedImage getFullImage()
    {
        return fullImage;
    }

    /**
      Return the bounds of the individual segments.
    */

    public @NotNull Rectangle2D[] getSegmentBounds()
    {
        return segmentBounds;
    }
}
