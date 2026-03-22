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
import org.violetlib.jnr.aqua.AquaUIPainter.ComboBoxWidget;
import org.violetlib.jnr.aqua.AquaUIPainter.Size;
import org.violetlib.jnr.aqua.AquaUIPainter.State;
import org.violetlib.jnr.aqua.AquaUIPainter.UILayoutDirection;

import java.util.Objects;

import static org.violetlib.jnr.aqua.AquaUIPainter.macOS11;

/**
  A configuration for an editable combo box.
*/

public class ComboBoxConfiguration
  extends ComboBoxLayoutConfiguration
  implements GenericButtonConfiguration
{
    private final @NotNull State state;
    private final boolean isFocused;

    public ComboBoxConfiguration(@NotNull ComboBoxWidget widget,
                                 @NotNull Size size,
                                 @NotNull State state,
                                 boolean isFocused,
                                 @NotNull UILayoutDirection ld)
    {
        super(widget, size, ld);

        if (!AquaNativeRendering.isRaw()) {
            // Many buttons do not alter their appearance when inactive. In many cases using CoreUI, the way to
            // accomplish that is to change the inactive state to the active equivalent. Replacing the state also
            // simplifies selection of a text color and improves caching.
            if (state.isInactive() && !isSensitiveToInactiveState(widget)) {
                state = state.toActive();
            }
        }

        this.isFocused = isFocused;
        this.state = state;
    }

    public ComboBoxConfiguration(@NotNull ComboBoxLayoutConfiguration g, @NotNull State state, boolean isFocused)
    {
        this(g.getWidget(), g.getSize(), state, isFocused, g.getLayoutDirection());
    }

    @Override
    public @NotNull State getState()
    {
        return state;
    }

    @Override
    public boolean isTextured()
    {
        ComboBoxWidget w = getWidget();
        return w.isTextured();
    }

    public boolean isFocused()
    {
        return isFocused;
    }

    @Override
    public @NotNull LayoutConfiguration getLayoutConfiguration()
    {
        return this;
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
        ComboBoxConfiguration that = (ComboBoxConfiguration) o;
        return isFocused == that.isFocused && state == that.state;
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

    private static boolean isSensitiveToInactiveState(@NotNull ComboBoxWidget widget)
    {
        int version = AquaNativeRendering.getSystemRenderingVersion();

        // Push buttons are sensitive: they lose the accent color
        if (widget == ComboBoxWidget.BUTTON_COMBO_BOX) {
            return true;
        }

        // toolbar buttons are generally sensitive, at least for 11+
        if (widget == ComboBoxWidget.BUTTON_COMBO_BOX_TEXTURED_TOOLBAR) {
            return version >= macOS11;
        }

        // Textured buttons are sensitive before 10.15
        if (widget.isTextured()) {
            return version < 101500;
        }

        // Other styles are not sensitive
        return false;
    }
}
