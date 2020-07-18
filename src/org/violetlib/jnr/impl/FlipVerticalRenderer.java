/*
 * Copyright (c) 2015-2020 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.impl;

import org.jetbrains.annotations.*;

/**
  A renderer that vertically flips the effect of a base renderer.
*/

public class FlipVerticalRenderer
  extends Renderer
{
    private final @NotNull Renderer r;

    /**
      Create a renderer that will invoke the specified renderer on a temporary raster and then copy its output into the
      original raster by flipping it vertically.

      @param r The renderer to invoke.
    */

    public FlipVerticalRenderer(@NotNull Renderer r)
    {
        this.r = r;
    }

    @Override
    public void composeTo(@NotNull ReusableCompositor compositor)
    {
        ReusableCompositor temp = compositor.createSimilar();
        r.composeTo(temp);
        ReusableCompositor flipped = temp.createVerticallyFlippedCopy();
        compositor.compose(flipped);
    }
}
