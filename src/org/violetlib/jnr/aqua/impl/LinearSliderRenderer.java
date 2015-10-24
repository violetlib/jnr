/*
 * Copyright (c) 2015 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.aqua.impl;

import java.awt.geom.Rectangle2D;

import org.jetbrains.annotations.*;

import org.violetlib.jnr.Insetter;
import org.violetlib.jnr.aqua.SliderConfiguration;
import org.violetlib.jnr.impl.Renderer;
import org.violetlib.jnr.impl.ReusableCompositor;

/**
	A renderer for linear sliders using CoreUI based renderers for the track and thumb. It repositions the track and thumb
	to match what NSSlider paints (more or less).
*/

public class LinearSliderRenderer
	extends Renderer
{
	protected final @NotNull SliderConfiguration g;
	protected final @NotNull Renderer trackRenderer;
	protected final @NotNull Insetter trackInsets;
	protected final @Nullable Renderer tickMarkRenderer;
	protected final @NotNull Renderer thumbRenderer;
	protected final @NotNull Insetter thumbInsets;

	public LinearSliderRenderer(@NotNull SliderConfiguration g,
															@NotNull Renderer trackRenderer,
															@NotNull Insetter trackInsets,
															@Nullable Renderer tickMarkRenderer,
															@NotNull Renderer thumbRenderer,
															@NotNull Insetter thumbInsets)
	{
		this.g = g;
		this.trackRenderer = trackRenderer;
		this.trackInsets = trackInsets;
		this.tickMarkRenderer = tickMarkRenderer;
		this.thumbRenderer = thumbRenderer;
		this.thumbInsets = thumbInsets;
	}

	@Override
	public void composeTo(@NotNull ReusableCompositor compositor)
	{
		float w = compositor.getWidth();
		float h = compositor.getHeight();

		{
			Rectangle2D trackBounds = trackInsets.apply2D(w, h);
			Renderer r = Renderer.createOffsetRenderer(trackRenderer, trackBounds);
			r.composeTo(compositor);
			if (tickMarkRenderer != null) {
				tickMarkRenderer.composeTo(compositor);
			}
		}

		{
			Rectangle2D thumbBounds = thumbInsets.apply2D(w, h);
			double x = thumbBounds.getX();
			double y = thumbBounds.getY();
			Renderer r = Renderer.createOffsetRenderer(thumbRenderer, x, y, thumbBounds.getWidth(), thumbBounds.getHeight());
			r.composeTo(compositor);
		}
	}
}
