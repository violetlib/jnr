/*
 * Copyright (c) 2015-2018 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.impl;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.*;

/**
	A generic renderer. This class supports native renderers as well as renderers implemented using Java graphics.
*/

public abstract class Renderer
	implements ReusableCompositor.PixelSource
{
	private final static Renderer NULL_RENDERER = new NullRenderer();
	private final static BasicRenderer NULL_BASIC_RENDERER = new NullBasicRenderer();

	/**
		Create a renderer using a basic (native) renderer.
		@param r The basic renderer.
		@param rd The renderer description.
		@return the renderer.
	*/

	public static @NotNull Renderer create(@NotNull BasicRenderer r, @Nullable RendererDescription rd)
	{
		if (rd == null) {
			rd = TrivialRendererDescription.getInstance();
		}

		return new BasicRendererRenderer(r, rd);
	}

	/**
		Create a renderer using a painter extension.
		@param px The painter extension.
		@return the renderer.
	*/

	public static @NotNull Renderer create(@NotNull PainterExtension px)
	{
		return new PainterExtensionRenderer(px);
	}

	/**
		Create a composite renderer.
		@param rs The renderers to be composed.
		@return the composite renderer.
	*/

	public static @NotNull Renderer createCompositeRenderer(@Nullable Renderer... rs)
	{
		if (rs == null || rs.length == 0) {
			return NULL_RENDERER;
		}

		List<Renderer> renderers  = new ArrayList<>();
		for (Renderer r : rs) {
			if (r != null) {
				renderers.add(r);
			}
		}

		switch (renderers.size()) {
			case 0:
				return NULL_RENDERER;
			case 1:
				return renderers.get(0);
			default:
				return new CompositeRenderer(renderers);
		}
	}

	/**
		Create a composite basic renderer.
		@param brs The basic renderers to be composed.
	*/

	public static @NotNull BasicRenderer createCompositeBasicRenderer(@Nullable BasicRenderer... brs)
	{
		if (brs == null || brs.length == 0) {
			return NULL_BASIC_RENDERER;
		}

		List<BasicRenderer> renderers  = new ArrayList<>();
		for (BasicRenderer r : brs) {
			if (r != null) {
				renderers.add(r);
			}
		}

		switch (renderers.size()) {
			case 0:
				return NULL_BASIC_RENDERER;
			case 1:
				return renderers.get(0);
			default:
				return new CompositeBasicRenderer(renderers);
		}
	}

	/**
		Create a renderer that renders into a region of the target raster. The region is specified in device independent
		pixels.

		@param source The source renderer.
		@param x The X offset of the destination region.
		@param y The Y offset of the destination region.
		@param w The width of the destination region.
		@param h The height of the destination region.
		@return the renderer.
	*/

	public static @NotNull Renderer createOffsetRenderer(@NotNull Renderer source, float x, float y, float w, float h)
	{
		return new OffsetRendererX(source, x, y, w, h);
	}

	public static @NotNull Renderer createOffsetRenderer(@NotNull Renderer source, double x, double y, double w, double h)
	{
		return new OffsetRendererX(source, (float) x, (float) y, (float) w, (float) h);
	}

	public static @NotNull Renderer createOffsetRenderer(@NotNull Renderer source, @NotNull Rectangle2D bounds)
	{
		return new OffsetRendererX(source, (float) bounds.getX(), (float) bounds.getY(), (float) bounds.getWidth(), (float) bounds.getHeight());
	}

	/**
		Create a renderer that renders into a region of the target raster. The region is specified in raster pixels.

		@param source The source renderer.
		@param x The X offset of the destination region.
		@param y The Y offset of the destination region.
		@param w The width of the destination region.
		@param h The height of the destination region.
		@return the renderer.
	*/

	public static @NotNull Renderer createRasterOffsetRenderer(@NotNull Renderer source, int x, int y, int w, int h)
	{
		return new OffsetRasterRendererX(source, x, y, w, h);
	}

	public @Nullable BasicRenderer getBasicRenderer()
	{
		return null;
	}

	public @Nullable RendererDescription getRendererDescription()
	{
		return null;
	}
}

class BasicRendererRenderer
	extends Renderer
{
	private final @NotNull BasicRenderer r;
	private final @NotNull RendererDescription rd;

	public BasicRendererRenderer(@NotNull BasicRenderer r, @NotNull RendererDescription rd)
	{
		this.r = r;
		this.rd = rd;
	}

	@Override
	public void composeTo(@NotNull ReusableCompositor compositor)
	{
		if (rd.isTrivial()) {
			compositor.composeRenderer(r);
		} else {
			// Use the renderer description to determine if the raster size needs to be increased or if the rendering needs
			// to be offset. Increasing the raster size works only if the compositor has not yet been used.

			int scaleFactor = compositor.getScaleFactor();
			float width = compositor.getWidth();
			float height = compositor.getHeight();
			Rectangle2D bounds = new Rectangle2D.Float(0, 0, width, height);
			RasterDescription sd = rd.getRasterBounds(bounds, scaleFactor);
			int x = Math.round(scaleFactor * sd.getX());
			int y = Math.round(scaleFactor * sd.getY());
			int w = (int) Math.ceil(scaleFactor * sd.getWidth());
			int h = (int) Math.ceil(scaleFactor * sd.getHeight());
			compositor.composeRenderer(r, x, y, w, h);
		}
	}

	public @NotNull BasicRenderer getBasicRenderer()
	{
		return r;
	}

	public @NotNull RendererDescription getRendererDescription()
	{
		return rd;
	}
}

class PainterExtensionRenderer
	extends Renderer
{
	private final @NotNull PainterExtension px;

	public PainterExtensionRenderer(@NotNull PainterExtension px)
	{
		this.px = px;
	}

	@NotNull PainterExtension getPainterExtension()
	{
		return px;
	}

	@Override
	public void composeTo(@NotNull ReusableCompositor compositor)
	{
		compositor.composePainter(px, 0, 0);
	}
}

class CompositeRenderer
	extends Renderer
{
	private final @NotNull List<Renderer> renderers;

	public CompositeRenderer(@NotNull List<Renderer> renderers)
	{
		this.renderers = renderers;
	}

	@Override
	public void composeTo(@NotNull ReusableCompositor compositor)
	{
		for (Renderer r : renderers) {
			r.composeTo(compositor);
		}
	}
}

class CompositeBasicRenderer
	implements BasicRenderer
{
	private final @NotNull List<BasicRenderer> renderers;

	public CompositeBasicRenderer(@NotNull List<BasicRenderer> renderers)
	{
		this.renderers = renderers;
	}

	@Override
	public void render(@NotNull int[] data, int rw, int rh, float w, float h)
	{
		int scaleFactor = (int) Math.ceil(rw / w);
		ReusableCompositor compositor = new ReusableCompositor(data, rw, rh, scaleFactor);
		for (BasicRenderer renderer : renderers) {
			compositor.composeRenderer(renderer);
		}
	}
}

class NullRenderer
	extends Renderer
{
	@Override
	public void composeTo(@NotNull ReusableCompositor compositor)
	{
	}
}

class NullBasicRenderer
	implements BasicRenderer
{
	@Override
	public void render(@NotNull int[] data, int rw, int rh, float w, float h)
	{
	}
}

class OffsetRendererX
	extends Renderer
{
	private final @NotNull Renderer source;
	private final float x;
	private final float y;
	private final float w;
	private final float h;

	public OffsetRendererX(@NotNull Renderer source, float x, float y, float w, float h)
	{
		this.source = source;
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
	}

	@Override
	public void composeTo(@NotNull ReusableCompositor compositor)
	{
		if (source instanceof BasicRendererRenderer) {
			BasicRendererRenderer brr = (BasicRendererRenderer) source;
			BasicRenderer r = brr.getBasicRenderer();
			RendererDescription rd = brr.getRendererDescription();
			if (rd.isTrivial()) {
				int scaleFactor = compositor.getScaleFactor();
				int rx = Math.round(scaleFactor * x);
				int ry = Math.round(scaleFactor * y);
				int rw = (int) Math.ceil(scaleFactor * w);
				int rh = (int) Math.ceil(scaleFactor * h);
				compositor.composeRenderer(r, rx, ry, rw, rh);
			} else {
				int scaleFactor = compositor.getScaleFactor();
				Rectangle2D bounds = new Rectangle2D.Float(x, y, w, h);
				RasterDescription sd = rd.getRasterBounds(bounds, scaleFactor);
				int rx = Math.round(scaleFactor * sd.getX());
				int ry = Math.round(scaleFactor * sd.getY());
				int rw = (int) Math.ceil(scaleFactor * sd.getWidth());
				int rh = (int) Math.ceil(scaleFactor * sd.getHeight());
				compositor.composeRenderer(r, rx, ry, rw, rh);
			}
		} else if (source instanceof PainterExtensionRenderer) {
			PainterExtensionRenderer pxr = (PainterExtensionRenderer) source;
			PainterExtension px = pxr.getPainterExtension();
			int scaleFactor = compositor.getScaleFactor();
			int rx = Math.round(scaleFactor * x);
			int ry = Math.round(scaleFactor * y);
			int rw = (int) Math.ceil(scaleFactor * w);
			int rh = (int) Math.ceil(scaleFactor * h);
			compositor.composePainter(px, rx, ry, rw, rh);
		}
	}
}

class OffsetRasterRendererX
	extends Renderer
{
	private final @NotNull Renderer source;
	private final int x;
	private final int y;
	private final int w;
	private final int h;

	public OffsetRasterRendererX(@NotNull Renderer source, int x, int y, int w, int h)
	{
		this.source = source;
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
	}

	@Override
	public void composeTo(@NotNull ReusableCompositor compositor)
	{
		if (source instanceof BasicRendererRenderer) {
			BasicRendererRenderer brr = (BasicRendererRenderer) source;
			BasicRenderer r = brr.getBasicRenderer();
			RendererDescription rd = brr.getRendererDescription();
			if (rd.isTrivial()) {
				compositor.composeRenderer(r, x, y, w, h);
			} else {
				int scaleFactor = compositor.getScaleFactor();
				float sf = scaleFactor;
				Rectangle2D bounds = new Rectangle2D.Float(x / sf, y / sf, w / sf, h / sf);
				RasterDescription sd = rd.getRasterBounds(bounds, scaleFactor);
				int rx = Math.round(scaleFactor * sd.getX());
				int ry = Math.round(scaleFactor * sd.getY());
				int rw = (int) Math.ceil(scaleFactor * sd.getWidth());
				int rh = (int) Math.ceil(scaleFactor * sd.getHeight());
				compositor.composeRenderer(r, rx, ry, rw, rh);
			}
		} else if (source instanceof PainterExtensionRenderer) {
			PainterExtensionRenderer pxr = (PainterExtensionRenderer) source;
			PainterExtension px = pxr.getPainterExtension();
			compositor.composePainter(px, x, y, w, h);
		}
	}
}
