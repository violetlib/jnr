/*
 * Copyright (c) 2015 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.aqua.jrs;

import org.jetbrains.annotations.*;

import apple.laf.JRSUIConstants;
import apple.laf.JRSUIControl;
import apple.laf.JRSUIState;
import org.violetlib.jnr.impl.BasicRenderer;

/**
	A renderer that use the Java Runtime Support framework to perform the rendering.
*/

public class JRSRenderer
	implements BasicRenderer
{
	protected final @NotNull JRSUIControl control;
	protected final @NotNull JRSUIState state;

	public JRSRenderer(@NotNull JRSUIControl control, @NotNull JRSUIState state)
	{
		this.control = control;
		this.state = state;
	}

	public @Nullable JRSUIState getControlState()
	{
		return state;
	}

	public boolean isAnimating()
	{
		return state.is(JRSUIConstants.Animating.YES);
	}

	@Override
	public void render(@NotNull int[] data, int rw, int rh, float w, float h)
	{
		// Apparently JRS does not expect fractional sizes in 1x
		float ww = (float) Math.ceil(w);
		float hh = (float) Math.ceil(h);
		if (ww == rw && hh == rh) {
			w = ww;
			h = hh;
		}

		control.set(state);
		control.paint(data, rw, rh, 0, 0, w, h);
	}
}
