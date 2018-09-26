/*
 * Copyright (c) 2018 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.impl;

import org.jetbrains.annotations.*;

import org.violetlib.vappearances.VAppearance;

/**
	A basic renderer that creates a dark mode group box renderer from a light mode group box renderer.
*/

public class DarkGroupBoxRenderer
	implements BasicRenderer
{
	private final @NotNull BasicRenderer renderer;

	public DarkGroupBoxRenderer(@NotNull BasicRenderer boxRenderer, @NotNull VAppearance appearance)
	{
		float multiplier = appearance.isHighContrast() ? 1f : 1.2f;
		renderer = new ScaledAlphaRenderer(boxRenderer, multiplier);
	}

	@Override
	public void render(@NotNull int[] data, int rw, int rh, float w, float h)
	{
		renderer.render(data, rw, rh, w, h);
	}
}
