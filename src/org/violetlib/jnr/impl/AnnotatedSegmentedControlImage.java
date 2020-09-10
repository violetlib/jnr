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
  extends RendererDebugInfo
{
    protected final @NotNull Rectangle2D[] segmentBounds;

    public AnnotatedSegmentedControlImage(@NotNull BufferedImage fullImage, @NotNull Rectangle2D[] segmentBounds)
    {
        super(fullImage, segmentBounds[0], "");

        this.segmentBounds = segmentBounds;
    }

    /**
      Return the bounds of the individual segments.
    */

    public @NotNull Rectangle2D[] getAllSegmentBounds()
    {
        return segmentBounds;
    }
}
