/*
 * Copyright (c) 2015-2020 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.aqua;

import java.util.Objects;

import org.violetlib.jnr.aqua.AquaUIPainter.PopupButtonWidget;
import org.violetlib.jnr.aqua.AquaUIPainter.Size;
import org.violetlib.jnr.aqua.AquaUIPainter.State;
import org.violetlib.jnr.aqua.AquaUIPainter.UILayoutDirection;
import org.violetlib.jnr.impl.JNRPlatformUtils;

import org.jetbrains.annotations.*;

/**
  A configuration for a pop up button.
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

        // Many buttons do not alter their appearance when inactive. In many cases using CoreUI, the way to accomplish
        // that is to change the inactive state to the active equivalent. Replacing the state also simplifies selection
        // of a text color and improves caching.

        if (state.isInactive() && !isSensitiveToInactiveState(bw)) {
            state = state.toActive();
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
        int platformVersion = JNRPlatformUtils.getPlatformVersion();

        // Push buttons are sensitive: they lose the accent color
        if (widget.isDefault()) {
            return true;
        }

        // Textured buttons are sensitive before 10.15
        if (widget.isTextured()) {
            return platformVersion < 101500;
        }

        // Other styles are not sensitive
        return false;
    }
}
