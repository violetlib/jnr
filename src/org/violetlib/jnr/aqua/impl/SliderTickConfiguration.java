/*
 * Copyright (c) 2020 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.aqua.impl;

import java.util.Objects;

import org.violetlib.jnr.aqua.Configuration;
import org.violetlib.jnr.aqua.LayoutConfiguration;
import org.violetlib.jnr.aqua.SliderConfiguration;

import org.jetbrains.annotations.*;

/**
  A pseudo configuration for internal and evaluation use. Should not be used by clients.
*/

public class SliderTickConfiguration
  extends LayoutConfiguration
  implements Configuration
{
    private final @NotNull SliderConfiguration g;
    private final boolean isTinted;

    public SliderTickConfiguration(@NotNull SliderConfiguration g, boolean isTinted)
    {
        this.g = g;
        this.isTinted = isTinted;
    }

    @Override
    public @NotNull Object getWidget()
    {
        return g.getWidget();
    }

    public @NotNull SliderConfiguration getSliderConfiguration()
    {
        return g;
    }

    public boolean isTinted()
    {
        return isTinted;
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
        SliderTickConfiguration that = (SliderTickConfiguration) o;
        return g == that.g && isTinted == that.isTinted;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(g, isTinted);
    }
}
