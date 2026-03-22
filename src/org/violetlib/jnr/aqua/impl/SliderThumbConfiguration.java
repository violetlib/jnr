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
import org.violetlib.jnr.aqua.AquaUIPainter;
import org.violetlib.jnr.aqua.Configuration;
import org.violetlib.jnr.aqua.LayoutConfiguration;
import org.violetlib.jnr.aqua.SliderConfiguration;

import java.util.Objects;

/**
  A pseudo configuration for internal and evaluation use. Should not be used by clients.
*/

public class SliderThumbConfiguration
  extends LayoutConfiguration
  implements Configuration
{
    private final @NotNull SliderConfiguration g;

    public SliderThumbConfiguration(@NotNull SliderConfiguration g)
    {
        this.g = g;
    }

    @Override
    public @NotNull Object getWidget()
    {
        return g.getWidget();
    }

    @Override
    public @Nullable AquaUIPainter.Size getSize()
    {
        return g.getSize();
    }

    public @NotNull SliderConfiguration getSliderConfiguration()
    {
        return g;
    }

    @Override
    public boolean equals(@Nullable Object o)
    {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SliderThumbConfiguration that = (SliderThumbConfiguration) o;
        return g == that.g;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(g);
    }
}
