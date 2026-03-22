/*
 * Copyright (c) 2015-2025 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;

/**
  A painter renders a fixed size rendering at a client specified origin.
*/

public interface Painter
{
    /**
      Draw the rendering.

      @param g The graphics context where the rendering should be drawn.
      @param x The X coordinate of the origin of the rendering.
      @param y The Y coordinate of the origin of the rendering.
    */

    void paint(@NotNull Graphics g, float x, float y);

    /**
      Return the rendering as an image. This method supports development.
    */

    @Nullable Image getImage(int scaleFactor, int width, int height);
}
