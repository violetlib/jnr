/*
 * Copyright (c) 2015 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.aqua;

import java.util.Objects;

import org.jetbrains.annotations.*;

import org.violetlib.jnr.aqua.AquaUIPainter.ButtonWidget;
import org.violetlib.jnr.aqua.AquaUIPainter.Size;
import org.violetlib.jnr.aqua.AquaUIPainter.State;
import org.violetlib.jnr.aqua.AquaUIPainter.ButtonState;
import org.violetlib.jnr.aqua.AquaUIPainter.UILayoutDirection;

/**
	A configuration for a button.
*/

public class ButtonConfiguration
	extends ButtonLayoutConfiguration
	implements Configuration
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

	public @NotNull State getState()
	{
		return state;
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
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;
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
}
