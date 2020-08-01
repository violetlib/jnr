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
  Information for debugging a renderer.
*/

public class RendererDebugInfo
{
    protected final @NotNull BufferedImage fullImage;
    protected final @Nullable Rectangle2D imageBounds;
    protected final @NotNull String info;

    public RendererDebugInfo(@NotNull BufferedImage fullImage, @Nullable Rectangle2D imageBounds, @NotNull String info)
    {
        this.fullImage = fullImage;
        this.imageBounds = imageBounds;
        this.info = info;
    }

    /**
      Return the full output of the renderer as an image.
    */

    public @NotNull BufferedImage getFullImage()
    {
        return fullImage;
    }

    /**
      If the renderer subsets a larger image, return the bounds of the intended rendering within that image.
      @return the bounds, or null if none.
    */

    public @Nullable Rectangle2D getImageBounds()
    {
        return imageBounds;
    }

    /**
      Return debugging information for display.
    */

    public @NotNull String getDebugInfo()
    {
        return info;
    }
}
