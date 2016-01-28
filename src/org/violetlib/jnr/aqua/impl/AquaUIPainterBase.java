/*
 * Copyright (c) 2015-2016 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.aqua.impl;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;

import org.jetbrains.annotations.*;

import org.violetlib.jnr.LayoutInfo;
import org.violetlib.jnr.Painter;
import org.violetlib.jnr.SliderPainter;
import org.violetlib.jnr.aqua.*;
import org.violetlib.jnr.impl.BasicRendererDescription;
import org.violetlib.jnr.impl.MultiResolutionRendererDescription;
import org.violetlib.jnr.NullPainter;
import org.violetlib.jnr.impl.OffsetPainter;
import org.violetlib.jnr.impl.Renderer;
import org.violetlib.jnr.impl.RendererDebugInfo;
import org.violetlib.jnr.impl.RendererDescription;

import static org.violetlib.jnr.impl.JNRUtils.*;

/**
	A common base class for native painters. The assumption of this base class is that most if not all layout related
	information is common to all implementations of native rendering.

	<p>
	This class currently supports only the UI for OS 10.10 (Yosemite). In the future, this class may provide results that
	depend upon the current platform version. (Because this class is designed to support native rendering, it is not a
	goal to support UIs other than that of the current platform version.)
	</p>
*/

public abstract class AquaUIPainterBase
	extends AquaUIPainterAbstractBase
	implements AquaUIPainter
{
	public static final Renderer NULL_RENDERER = Renderer.createCompositeRenderer();

	protected final @NotNull RendererDescriptions rendererDescriptions;

	protected AquaUIPainterBase(@NotNull RendererDescriptions rds)
	{
		this.rendererDescriptions = rds;
	}

	@Override
	public @Nullable Shape getOutline(@NotNull LayoutConfiguration g)
		throws UnsupportedOperationException
	{
		LayoutInfo layoutInfo = uiLayout.getLayoutInfo(g);
		Rectangle2D bounds = getCenteredBounds(layoutInfo);
		return getOutline(bounds, g);
	}

	public @Nullable Shape getOutline(@NotNull Rectangle2D bounds, @NotNull LayoutConfiguration g)
		throws UnsupportedOperationException
	{
		return uiOutliner.getOutline(bounds, g);
	}

	@Override
	public @NotNull Painter getPainter(@NotNull Configuration g)
		throws UnsupportedOperationException
	{
		LayoutInfo layoutInfo = uiLayout.getLayoutInfo((LayoutConfiguration) g);
		Renderer r = getRenderer(g);
		Painter p = getPainter(layoutInfo, g, r);
		return customizePainter(p, g, layoutInfo);
	}

	protected @NotNull Painter customizePainter(@NotNull Painter p, @NotNull Configuration g, @NotNull LayoutInfo layoutInfo)
	{
		if (g instanceof SliderConfiguration) {
			SliderConfiguration sg = (SliderConfiguration) g;
			Rectangle2D bounds = getCenteredBounds(layoutInfo);
			return new SliderPainterImpl(sg, layoutInfo, bounds, sg.getValue(), p);
		}
		return p;
	}

	// public to support evaluation
	public @NotNull Renderer getRenderer(@NotNull Configuration g)
	{
		if (g instanceof ButtonConfiguration) {
			ButtonConfiguration gg = (ButtonConfiguration) g;
			return getButtonRenderer(gg);
		}

		if (g instanceof ComboBoxConfiguration) {
			ComboBoxConfiguration gg = (ComboBoxConfiguration) g;
			return getComboBoxButtonRenderer(gg);
		}

		if (g instanceof PopupButtonConfiguration) {
			PopupButtonConfiguration gg = (PopupButtonConfiguration) g;
			return getPopupButtonRenderer(gg);
		}

		if (g instanceof TitleBarConfiguration) {
			TitleBarConfiguration gg = (TitleBarConfiguration) g;
			return getTitleBarRenderer(gg);
		}

		if (g instanceof SliderConfiguration) {
			SliderConfiguration gg = (SliderConfiguration) g;
			return getSliderRenderer(gg);
		}

		if (g instanceof SpinnerArrowsConfiguration) {
			SpinnerArrowsConfiguration gg = (SpinnerArrowsConfiguration) g;
			return getSpinnerArrowsRenderer(gg);
		}

		if (g instanceof SplitPaneDividerConfiguration) {
			SplitPaneDividerConfiguration gg = (SplitPaneDividerConfiguration) g;
			return getSplitPaneDividerRenderer(gg);
		}

		if (g instanceof SegmentedButtonConfiguration) {
			SegmentedButtonConfiguration gg = (SegmentedButtonConfiguration) g;
			return getSegmentedButtonRenderer(gg);
		}

		if (g instanceof ToolBarItemWellConfiguration) {
			ToolBarItemWellConfiguration gg = (ToolBarItemWellConfiguration) g;
			return getToolBarItemWellRenderer(gg);
		}

		if (g instanceof GroupBoxConfiguration) {
			GroupBoxConfiguration gg = (GroupBoxConfiguration) g;
			return getGroupBoxRenderer(gg);
		}

		if (g instanceof ListBoxConfiguration) {
			ListBoxConfiguration gg = (ListBoxConfiguration) g;
			return getListBoxRenderer(gg);
		}

		if (g instanceof TextFieldConfiguration) {
			TextFieldConfiguration gg = (TextFieldConfiguration) g;
			return getTextFieldRenderer(gg);
		}

		if (g instanceof ScrollBarConfiguration) {
			ScrollBarConfiguration gg = (ScrollBarConfiguration) g;
			return getScrollBarRenderer(gg);
		}

		if (g instanceof ScrollColumnSizerConfiguration) {
			ScrollColumnSizerConfiguration gg = (ScrollColumnSizerConfiguration) g;
			return getScrollColumnSizerRenderer(gg);
		}

		if (g instanceof IndeterminateProgressIndicatorConfiguration) {
			IndeterminateProgressIndicatorConfiguration gg = (IndeterminateProgressIndicatorConfiguration) g;
			return getIndeterminateProgressIndicatorRenderer(gg);
		}

		if (g instanceof ProgressIndicatorConfiguration) {
			ProgressIndicatorConfiguration gg = (ProgressIndicatorConfiguration) g;
			return getProgressIndicatorRenderer(gg);
		}

		if (g instanceof TableColumnHeaderConfiguration) {
			TableColumnHeaderConfiguration gg = (TableColumnHeaderConfiguration) g;
			return getTableColumnHeaderRenderer(gg);
		}

		if (g instanceof GradientConfiguration) {
			GradientConfiguration gg = (GradientConfiguration) g;
			return getGradientRenderer(gg);
		}

		if (g instanceof SliderThumbConfiguration) {
			SliderThumbConfiguration gg = (SliderThumbConfiguration) g;
			return getSliderThumbRenderer(gg.getSliderConfiguration());
		}

		if (g instanceof PopupArrowConfiguration) {
			PopupArrowConfiguration gg = (PopupArrowConfiguration) g;
			Renderer r = getPopupArrowRenderer(gg.getPopupButtonConfiguration());
			if (r != null) {
				return r;
			}
		}

		if (g instanceof SearchFieldFindButtonConfiguration) {
			SearchFieldFindButtonConfiguration gg = (SearchFieldFindButtonConfiguration) g;
			Renderer r = getSearchFieldFindButtonRenderer(gg.getTextFieldConfiguration());
			if (r != null) {
				return r;
			}
		}

		if (g instanceof SearchFieldCancelButtonConfiguration) {
			SearchFieldCancelButtonConfiguration gg = (SearchFieldCancelButtonConfiguration) g;
			Renderer r = getSearchFieldCancelButtonRenderer(gg.getTextFieldConfiguration());
			if (r != null) {
				return r;
			}
		}

		throw new UnsupportedOperationException();	// TBD
	}

	public @Nullable RendererDebugInfo getRendererDebugInfo(@NotNull Configuration g, int scaleFactor, int width, int height)
	{
		if (g instanceof SegmentedButtonConfiguration) {
			SegmentedButtonConfiguration gg = (SegmentedButtonConfiguration) g;
			return getSegmentedButtonRendererDebugInfo(gg, scaleFactor, width, height);
		}

		return null;
	}

	/**
		Map a button widget to a canonical equivalent. This mapping addresses the fact that certain styles have become
		obsolete and are best supported by using a similar style.
	*/

	protected @NotNull ButtonWidget toCanonicalButtonStyle(ButtonWidget bw)
	{
//		switch (bw) {
//			case BUTTON_ROUND_INSET:
//				return ButtonWidget.BUTTON_ROUND;
//			case BUTTON_ROUND_TEXTURED:
//				return ButtonWidget.BUTTON_ROUND;
//		}
		return bw;
	}

	protected @Nullable Renderer getSearchFieldFindButtonRenderer(@NotNull TextFieldConfiguration g)
	{
		return null;
	}

	protected @Nullable Renderer getSearchFieldCancelButtonRenderer(@NotNull TextFieldConfiguration g)
	{
		return null;
	}

	protected @Nullable RendererDescription getSearchButtonRendererDescription(@NotNull TextFieldLayoutConfiguration g)
	{
		TextFieldWidget w = g.getWidget();

		if (!w.isSearch()) {
			return null;
		}

		Size sz = g.getSize();
		boolean hasMenu = w.hasMenu();

		if (hasMenu) {
			float x1 = size2D(sz, -5, -6, -4);
			float y1 = size(sz, -1, -1, -2);

			float x2 = size2D(sz, -5.5f, -6, -4.5f);
			float y2 = size2D(sz, -1.5f, -1.5f, -2);

			float wa = size(sz, 6, 7, 4);
			float ha = size(sz, 1, 1, 2);

			if (!g.isLeftToRight()) {
				x1 += 4;
				x2 += 4;
			}

			RendererDescription rd1 = new BasicRendererDescription(x1, y1, wa, ha);
			RendererDescription rd2 = new BasicRendererDescription(x2, y2, wa, ha);
			return new MultiResolutionRendererDescription(rd1, rd2);

		} else {
			float x1 = 0;
			float y1 = size(sz, -1, -1, -2);

			float wa1 = 0;
			float ha1 = size(sz, 1, 1, 1);

			float x2 = -0.5f;
			float y2 = size2D(sz, -1.5f, -2, -2);

			float wa2 = 0;
			float ha2 = size2D(sz, 1, 1.5f, 1.5f);

			RendererDescription rd1 = new BasicRendererDescription(x1, y1, wa1, ha1);
			RendererDescription rd2 = new BasicRendererDescription(x2, y2, wa2, ha2);
			return new MultiResolutionRendererDescription(rd1, rd2);
		}
	}

	/**
		Create a widget painter based on a renderer.

		@param info The layout information for the widget being painted.
		@param g The widget configuration, which may be used to cache the rendered image.
		@param r The renderer used to paint the widget.
		@return the painter.

		<p>
		Offsets are applied when rendering. These offsets have three sources: the insets of the native rendering, centering
		applied when a widget with a fixed width or height is painted in a larger space, and the client specified offset.
		</p>
	*/

	protected @NotNull Painter getPainter(@Nullable LayoutInfo info,
																				@NotNull Configuration g,
																				@NotNull Renderer r)
	{
		configureLayout(info);

		if (pWidth <= 0 || pHeight <= 0) {
			return new NullPainter(info);
		}

		Painter p = getPainter(g, r, pWidth, pHeight);

		if (xOffset != 0 || yOffset != 0) {
			p = new OffsetPainter(p, xOffset, yOffset);
		}

		return p;
	}

	/**
		Create a widget painter based on a renderer.

		@param g The widget configuration, which may be used to cache the rendered image.
		@param r The renderer used to paint the widget.
		@param width The width of the rendering, in device independent pixels.
		@param height The height of the rendering, in device independent pixels.
		@return the painter.
	*/

	protected @NotNull Painter getPainter(@NotNull Configuration g,
																				@NotNull Renderer r,
																				float width,
																				float height)
	{
		return new AquaRenderedPainter(g, r, width, height);
	}

	protected abstract @NotNull Renderer getButtonRenderer(@NotNull ButtonConfiguration g);

	protected abstract @NotNull Renderer getTableColumnHeaderRenderer(@NotNull TableColumnHeaderConfiguration g);

	protected abstract @NotNull Renderer getScrollColumnSizerRenderer(@NotNull ScrollColumnSizerConfiguration g);

	protected abstract @NotNull Renderer getScrollBarRenderer(@NotNull ScrollBarConfiguration g);

	protected abstract @NotNull Renderer getToolBarItemWellRenderer(@NotNull ToolBarItemWellConfiguration g);

	protected abstract @NotNull Renderer getGroupBoxRenderer(@NotNull GroupBoxConfiguration g);

	protected abstract @NotNull Renderer getListBoxRenderer(@NotNull ListBoxConfiguration g);

	protected abstract @NotNull Renderer getTextFieldRenderer(@NotNull TextFieldConfiguration g);

	protected abstract @NotNull Renderer getComboBoxButtonRenderer(@NotNull ComboBoxConfiguration g);

	protected abstract @NotNull Renderer getSegmentedButtonRenderer(@NotNull SegmentedButtonConfiguration g);

	protected abstract @NotNull Renderer getPopupButtonRenderer(@NotNull PopupButtonConfiguration g);

	protected abstract @NotNull Renderer getTitleBarRenderer(@NotNull TitleBarConfiguration g);

	protected abstract @NotNull Renderer getIndeterminateProgressIndicatorRenderer(@NotNull IndeterminateProgressIndicatorConfiguration g);

	protected abstract @NotNull Renderer getProgressIndicatorRenderer(@NotNull ProgressIndicatorConfiguration g);

	protected abstract @NotNull Renderer getSliderRenderer(@NotNull SliderConfiguration g);

	protected abstract @NotNull Renderer getSliderThumbRenderer(@NotNull SliderConfiguration g);

	protected abstract @NotNull Renderer getSpinnerArrowsRenderer(@NotNull SpinnerArrowsConfiguration g);

	protected abstract @NotNull Renderer getSplitPaneDividerRenderer(@NotNull SplitPaneDividerConfiguration g);

	protected abstract @NotNull Renderer getGradientRenderer(@NotNull GradientConfiguration g);

	protected @Nullable RendererDebugInfo getSegmentedButtonRendererDebugInfo(@NotNull SegmentedButtonConfiguration g, int scaleFactor, int width, int height)
	{
		return null;
	}

	/**
		Return the renderer used to draw the arrows of pop up button, if any.
	*/

	protected @Nullable Renderer getPopupArrowRenderer(@NotNull PopupButtonConfiguration g)
	{
		return null;
	}

	private class SliderPainterImpl
		implements SliderPainter
	{
		private final @NotNull SliderConfiguration sg;
		private final @Nullable LayoutInfo layoutInfo;
		private final @NotNull Rectangle2D bounds;
		private final @NotNull Painter p;
		private final double thumbPosition;

		private @Nullable Rectangle2D thumbBounds;
		private @Nullable Shape thumbOutline;

		public SliderPainterImpl(@NotNull SliderConfiguration sg,
														 @Nullable LayoutInfo layoutInfo,
														 @NotNull Rectangle2D bounds,
														 double thumbPosition,
														 @NotNull Painter p)
		{
			this.sg = sg;
			this.layoutInfo = layoutInfo;
			this.bounds = bounds;
			this.thumbPosition = thumbPosition;
			this.p = p;
		}

		@Override
		public @NotNull Rectangle2D getThumbBounds()
		{
			if (thumbBounds == null) {
				thumbBounds = uiLayout.getSliderThumbBounds(bounds, sg, thumbPosition);
			}

			return thumbBounds;
		}

		@Override
		public @NotNull Shape getThumbOutline()
		{
			if (thumbOutline == null) {
				SliderThumbLayoutConfiguration tg = new SliderThumbLayoutConfiguration(sg, thumbPosition);
				thumbOutline = AquaUIPainterBase.this.getOutline(bounds, tg);
			}
			return thumbOutline;
		}

		@Override
		public double getThumbPosition(int x, int y)
		{
			return uiLayout.getSliderThumbPosition(bounds, sg, x, y);
		}

		@Override
		public @NotNull Rectangle2D getLabelBounds(double value, @NotNull Dimension size)
		{
			return uiLayout.getSliderLabelBounds(bounds, sg, value, size);
		}

		@Override
		public float getFixedWidth()
		{
			return layoutInfo != null ? layoutInfo.getFixedVisualWidth() : 0;
		}

		@Override
		public float getFixedHeight()
		{
			return layoutInfo != null ? layoutInfo.getFixedVisualHeight() : 0;
		}

		@Override
		public void paint(@NotNull Graphics g, float x, float y)
		{
			p.paint(g, x, y);
		}
	}
}
