/*
 * Copyright (c) 2015-2026 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.aqua;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.violetlib.jnr.aqua.AquaUIPainter.Size;
import org.violetlib.jnr.aqua.AquaUIPainter.State;
import org.violetlib.jnr.aqua.AquaUIPainter.TextFieldWidget;
import org.violetlib.jnr.aqua.AquaUIPainter.UILayoutDirection;

import java.util.Objects;

/**
  A configuration for a text field or pane.
*/

public class TextFieldConfiguration
  extends TextFieldLayoutConfiguration
  implements Configuration
{
    private final @NotNull State state;
    private final boolean isFocused;

    public TextFieldConfiguration(@NotNull TextFieldWidget tw,
                                  @NotNull Size sz,
                                  @NotNull State state,
                                  boolean isFocused,
                                  @NotNull UILayoutDirection ld)
    {
        super(tw, sz, ld);

        this.state = state;
        this.isFocused = isFocused;
    }

    public @NotNull State getState()
    {
        return state;
    }

    public boolean isFocused()
    {
        return isFocused;
    }

    @Override
    public @NotNull TextFieldConfiguration withSize(@NotNull Size size)
    {
        if (size == this.getSize()) {
            return this;
        }
        return new TextFieldConfiguration(getWidget(), size, state, isFocused, getLayoutDirection());
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
        if (!super.equals(o)) {
            return false;
        }
        TextFieldConfiguration that = (TextFieldConfiguration) o;
        return state == that.state && isFocused == that.isFocused;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(super.hashCode(), state, isFocused);
    }

    @Override
    public @NotNull String toString()
    {
        String fs = isFocused ? " focused" : "";
        return super.toString() + " " + state + fs;
    }
}
