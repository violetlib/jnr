/*
 * Copyright (c) 2015-2020 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.aqua;

import java.util.Objects;

import org.violetlib.jnr.aqua.AquaUIPainter.ButtonState;
import org.violetlib.jnr.aqua.AquaUIPainter.ButtonWidget;
import org.violetlib.jnr.aqua.AquaUIPainter.Size;
import org.violetlib.jnr.aqua.AquaUIPainter.State;
import org.violetlib.jnr.aqua.AquaUIPainter.UILayoutDirection;
import org.violetlib.jnr.impl.JNRPlatformUtils;

import org.jetbrains.annotations.*;

/**
  A configuration for a button.
*/

public class ButtonConfiguration
  extends ButtonLayoutConfiguration
  implements GenericButtonConfiguration
{
    private final @NotNull State state;
    private final boolean isFocused;
    private final @NotNull ButtonState buttonState;

    public ButtonConfiguration(@NotNull ButtonWidget bw,
                               @NotNull Size size,
                               @NotNull State state,
                               boolean isFocused,
                               @NotNull ButtonState buttonState,
                               @NotNull UILayoutDirection ld
    )
    {
        super(bw, size, ld);

        // On Yosemite, a selected color well displays the same as a pressed color well.

        if (bw == ButtonWidget.BUTTON_COLOR_WELL && buttonState != ButtonState.OFF) {
            if (state == State.ACTIVE) {
                state = State.PRESSED;
            }
        }

        // Many buttons do not alter their appearance when inactive. In many cases using CoreUI, the way to accomplish
        // that is to change the inactive state to the active equivalent. Replacing the state also simplifies selection
        // of a text color and improves caching.

        if (state.isInactive() && !isSensitiveToInactiveState(bw, buttonState)) {
            state = state.toActive();
        }

        this.state = state;
        this.isFocused = isFocused;
        this.buttonState = buttonState;
    }

    public ButtonConfiguration(@NotNull ButtonLayoutConfiguration g,
                               @NotNull State state,
                               boolean isFocused,
                               @NotNull ButtonState buttonState)
    {
        this(g.getButtonWidget(), g.getSize(), state, isFocused, buttonState, g.getLayoutDirection());
    }

    @Override
    public @NotNull State getState()
    {
        return state;
    }

    @Override
    public boolean isTextured()
    {
        ButtonWidget w = getButtonWidget();
        return w.isTextured();
    }

    public boolean isFocused()
    {
        return isFocused;
    }

    public @NotNull ButtonState getButtonState()
    {
        return buttonState;
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
        ButtonConfiguration that = (ButtonConfiguration) o;
        return isFocused == that.isFocused && state == that.state && buttonState == that.buttonState;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(super.hashCode(), state, isFocused, buttonState);
    }

    @Override
    public @NotNull String toString()
    {
        String fs = isFocused ? " focused" : "";
        return super.toString() + " " + state + " " + buttonState + fs;
    }

   private static boolean isSensitiveToInactiveState(@NotNull ButtonWidget bw, @NotNull ButtonState bs)
    {
        int platformVersion = JNRPlatformUtils.getPlatformVersion();

        // Push buttons are sensitive if they are in the "on" state
        if (bw == ButtonWidget.BUTTON_PUSH) {
            return bs == ButtonState.ON;
        }

        // Round buttons are sensitive if they are in the "on" state, starting with macOS 10.15
        if (bw == ButtonWidget.BUTTON_ROUND) {
            return bs == ButtonState.ON && platformVersion >= 101500;
        }

        // Checkboxes and radio buttons are sensitive
        if (bw == ButtonWidget.BUTTON_CHECK_BOX || bw == ButtonWidget.BUTTON_RADIO) {
            return true;
        }

        // Inline buttons are sensitive in 11.0
        if (bw == ButtonWidget.BUTTON_INLINE && platformVersion >= 101600) {
            return true;
        }

        // Starting with macOS 10.15, there are no additional sensitive buttons
        if (platformVersion >= 101500) {
            return false;
        }

        // Textured buttons are sensitive, both stateless buttons and toggle buttons.
        if (bw.isTextured()) {
            return true;
        }

        // Stateless buttons of other types are insensitive.
        if (bs == ButtonState.STATELESS) {
            return false;
        }

        // Recessed buttons in the "on" state are sensitive
        if (bw == ButtonWidget.BUTTON_RECESSED && bs == ButtonState.ON) {
            return true;
        }

        // Everything else is insensitive
        return false;
    }
}
