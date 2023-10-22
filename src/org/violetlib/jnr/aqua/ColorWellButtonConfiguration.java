/*
 * Copyright (c) 2023 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.aqua;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.Objects;

import static org.violetlib.jnr.aqua.AquaUIPainter.*;

/**
  A configuration for a color well button.
*/

public class ColorWellButtonConfiguration
  extends ButtonConfiguration
{
    public static @Nullable Color getColor(@NotNull ButtonConfiguration g)
    {
        if (g instanceof ColorWellButtonConfiguration) {
            ColorWellButtonConfiguration cg = (ColorWellButtonConfiguration) g;
            return cg.getSelectedColor();
        }
        return null;
    }

    private final @NotNull Color selectedColor;

    public ColorWellButtonConfiguration(@NotNull ButtonWidget bw,
                                        @NotNull Size size,
                                        @NotNull State state,
                                        boolean isFocused,
                                        @NotNull ButtonState buttonState,
                                        @NotNull UILayoutDirection ld,
                                        @NotNull Color selectedColor)
    {
        super(bw, size, state, isFocused, buttonState, ld);

        this.selectedColor = selectedColor;
    }

    public ColorWellButtonConfiguration(@NotNull ButtonLayoutConfiguration g,
                                        @NotNull State state,
                                        boolean isFocused,
                                        @NotNull ButtonState buttonState,
                                        @NotNull Color selectedColor)
    {
        super(g, state, isFocused, buttonState);

        this.selectedColor = selectedColor;
    }

    public ColorWellButtonConfiguration(@NotNull ButtonConfiguration g,
                                        @NotNull Color selectedColor)
    {
        super(g);

        this.selectedColor = selectedColor;
    }

    public @NotNull Color getSelectedColor()
    {
        return selectedColor;
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
        ColorWellButtonConfiguration that = (ColorWellButtonConfiguration) o;
        return selectedColor.equals(that.selectedColor);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(super.hashCode(), selectedColor);
    }
}
