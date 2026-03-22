/*
 * Copyright (c) 2015-2025 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.aqua;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
  A configuration for a toolbar item well. For internal use.
*/

public class ToolBarItemWellLayoutConfiguration
  extends LayoutConfiguration
{
    @Override
    public @NotNull Object getWidget()
    {
        return this;
    }

    @Override
    public @Nullable AquaUIPainter.Size getSize()
    {
        return null;
    }

    @Override
    public @NotNull String toString()
    {
        return "Tool Bar Item Well";
    }
}
