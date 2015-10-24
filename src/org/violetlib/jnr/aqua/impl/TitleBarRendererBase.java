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
import org.violetlib.jnr.aqua.AquaUIPainter;
import org.violetlib.jnr.aqua.TitleBarConfiguration;
import org.violetlib.jnr.impl.BasicRenderer;
import org.violetlib.jnr.impl.Renderer;
import org.violetlib.jnr.impl.RendererDescription;
import org.violetlib.jnr.impl.ReusableCompositor;

/**

*/

public abstract class TitleBarRendererBase
	extends Renderer
{
	protected final @NotNull TitleBarConfiguration g;
	protected final @NotNull RendererDescription rd;
	protected final @Nullable Insetter closeButtonInsets;
	protected final @Nullable Insetter minimizeButtonInsets;
	protected final @Nullable Insetter resizeButtonInsets;

	protected TitleBarRendererBase(@NotNull TitleBarConfiguration g,
																 @NotNull RendererDescription rd,
																 @Nullable Insetter closeButtonInsets,
																 @Nullable Insetter minimizeButtonInsets,
																 @Nullable Insetter resizeButtonInsets)
	{
		this.g = g;
		this.rd = rd;
		this.closeButtonInsets = closeButtonInsets;
		this.minimizeButtonInsets = minimizeButtonInsets;
		this.resizeButtonInsets = resizeButtonInsets;
	}

	@Override
	public void composeTo(@NotNull ReusableCompositor compositor)
	{
		float w = compositor.getWidth();
		float h = compositor.getHeight();

		{
			BasicRenderer br = getBasicTitleBarRenderer(w, h);
			Renderer r = Renderer.create(br, rd);
			r.composeTo(compositor);
		}

		if (closeButtonInsets != null) {
			Rectangle2D bounds = closeButtonInsets.apply2D(w, h);
			Renderer br = getButtonRenderer(AquaUIPainter.TitleBarButtonWidget.CLOSE_BOX);
			Renderer r = Renderer.createOffsetRenderer(br, bounds);
			r.composeTo(compositor);
		}

		if (minimizeButtonInsets != null) {
			Rectangle2D bounds = minimizeButtonInsets.apply2D(w, h);
			Renderer br = getButtonRenderer(AquaUIPainter.TitleBarButtonWidget.MINIMIZE_BOX);
			Renderer r = Renderer.createOffsetRenderer(br, bounds);
			r.composeTo(compositor);
		}

		if (resizeButtonInsets != null) {
			Rectangle2D bounds = resizeButtonInsets.apply2D(w, h);
			Renderer br = getButtonRenderer(AquaUIPainter.TitleBarButtonWidget.RESIZE_BOX);
			Renderer r = Renderer.createOffsetRenderer(br, bounds);
			r.composeTo(compositor);
		}
	}

	protected abstract @NotNull BasicRenderer getBasicTitleBarRenderer(float w, float h);

	protected abstract @NotNull Renderer getButtonRenderer(@NotNull AquaUIPainter.TitleBarButtonWidget bw);
}
