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
import org.violetlib.jnr.aqua.AquaUIPainter.PopupButtonWidget;
import org.violetlib.jnr.aqua.AquaUIPainter.Size;
import org.violetlib.jnr.aqua.AquaUIPainter.State;
import org.violetlib.jnr.aqua.AquaUIPainter.UILayoutDirection;

import java.util.Objects;

/**
  A configuration for a popup button.
*/

public class PopupButtonConfiguration
  extends PopupButtonLayoutConfiguration
  implements GenericButtonConfiguration
{
    private final @NotNull State state;

    public PopupButtonConfiguration(@NotNull PopupButtonWidget bw,
                                    @NotNull Size size,
                                    @NotNull State state,
                                    @NotNull UILayoutDirection ld)
    {
        super(bw, size, ld);

        if (!AquaNativeRendering.isRaw()) {
            // Many buttons do not alter their appearance when inactive. In many cases using CoreUI, the way to
            // accomplish that is to change the inactive state to the active equivalent. Replacing the state also
            // simplifies selection of a text color and improves caching.

            if (state.isInactive() && !isSensitiveToInactiveState(bw)) {
                state = state.toActive();
            }
        }

        this.state = state;
    }

    public PopupButtonConfiguration(@NotNull PopupButtonLayoutConfiguration g, @NotNull State state)
    {
        this(g.getPopupButtonWidget(), g.getSize(), state, g.getLayoutDirection());
    }

    @Override
    public @NotNull State getState()
    {
        return state;
    }

    @Override
    public boolean isTextured()
    {
        PopupButtonWidget w = getPopupButtonWidget();
        return w.isTextured();
    }

    @Override
    public @NotNull LayoutConfiguration getLayoutConfiguration()
    {
        return this;
    }

    public @NotNull PopupButtonConfiguration with(@NotNull PopupButtonWidget bw)
    {
        return new PopupButtonConfiguration(bw, getSize(), getState(), getLayoutDirection());
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
        PopupButtonConfiguration that = (PopupButtonConfiguration) o;
        return state == that.state;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(super.hashCode(), state);
    }

    @Override
    public @NotNull String toString()
    {
        return super.toString() + " " + state;
    }

    private static boolean isSensitiveToInactiveState(@NotNull PopupButtonWidget widget)
    {
        int version = AquaNativeRendering.getSystemRenderingVersion();

        // Push buttons are sensitive: they lose the accent color
        if (widget.isDefault()) {
            return true;
        }

        // Textured buttons are sensitive
        if (widget.isTextured()) {
            return true;
        }

        // Other styles are not sensitive
        return false;
    }
}
