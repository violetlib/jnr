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

import org.violetlib.jnr.aqua.AquaUIPainter.PopupButtonWidget;
import org.violetlib.jnr.aqua.AquaUIPainter.Size;
import org.violetlib.jnr.aqua.AquaUIPainter.State;
import org.violetlib.jnr.aqua.AquaUIPainter.UILayoutDirection;

/**
	A configuration for a pop up button.
*/

public class PopupButtonConfiguration
	extends PopupButtonLayoutConfiguration
	implements Configuration
{
	private final @NotNull State state;

	public PopupButtonConfiguration(@NotNull PopupButtonWidget bw,
																	@NotNull Size size,
																	@NotNull State state,
																	@NotNull UILayoutDirection ld)
	{
		super(bw, size, ld);

		this.state = state;
	}

	public PopupButtonConfiguration(@NotNull PopupButtonLayoutConfiguration g, @NotNull State state)
	{
		this(g.getPopupButtonWidget(), g.getSize(), state, g.getLayoutDirection());
	}

	public @NotNull State getState()
	{
		return state;
	}

	@Override
	public boolean equals(@Nullable Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;
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
}
