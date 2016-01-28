/*
 * Copyright (c) 2015-2016 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.aqua.coreui;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.*;

import org.violetlib.jnr.aqua.ButtonConfiguration;
import org.violetlib.jnr.aqua.PopupButtonConfiguration;
import org.violetlib.jnr.aqua.SegmentedButtonConfiguration;
import org.violetlib.jnr.aqua.SliderConfiguration;
import org.violetlib.jnr.aqua.SplitPaneDividerConfiguration;
import org.violetlib.jnr.aqua.TableColumnHeaderConfiguration;
import org.violetlib.jnr.aqua.impl.CircularSliderPainterExtension;
import org.violetlib.jnr.aqua.impl.LinearSliderPainterExtension;
import org.violetlib.jnr.aqua.impl.PopUpArrowPainter;
import org.violetlib.jnr.aqua.impl.PullDownArrowPainter;
import org.violetlib.jnr.aqua.impl.TableColumnHeaderCellPainterExtension;
import org.violetlib.jnr.aqua.impl.ThinSplitPaneDividerPainterExtension;
import org.violetlib.jnr.impl.BasicRenderer;
import org.violetlib.jnr.impl.BasicRendererDescription;
import org.violetlib.jnr.impl.PainterExtension;
import org.violetlib.jnr.impl.Renderer;
import org.violetlib.jnr.impl.RendererDescription;
import org.violetlib.jnr.impl.ReusableCompositor;
import org.violetlib.jnr.impl.TrivialRendererDescription;

import static org.violetlib.jnr.aqua.AquaUIPainter.PopupButtonWidget.*;

/**
	This class augments the Core UI native painting code to work around its deficiencies.
*/

public class AugmentedCoreUIPainter
	extends CoreUIPainter
{
	/**
		Create a painter that uses Core UI rendering by way of the Java Runtime Support framework, supplemented with
		Java rendering.
	*/

	public AugmentedCoreUIPainter()
	{
	}

	/**
		Create a painter that uses Core UI rendering, supplemented with Java rendering.

		@param useJRS If true, the Java Runtime Support framework is used to access Core UI rendering. If false, a private
			method is used to access Core UI rendering.
	*/

	public AugmentedCoreUIPainter(boolean useJRS)
	{
		super(useJRS);
	}

	@Override
	public @NotNull AugmentedCoreUIPainter copy()
	{
		return new AugmentedCoreUIPainter(useJRS);
	}

	@Override
	protected @NotNull Renderer getButtonRenderer(@NotNull ButtonConfiguration g)
	{
		Renderer r = super.getButtonRenderer(g);
		if (g.getButtonWidget() == ButtonWidget.BUTTON_COLOR_WELL) {
			return new ColorWellRenderer(g, r);
		}
		return r;
	}

	@Override
	protected @NotNull Renderer getSegmentedButtonRenderer(@NotNull SegmentedButtonConfiguration g)
	{
		Renderer r = super.getSegmentedButtonRenderer(g);
		if (isCustomSegmentedButtonRendererNeeded(g)) {
			return createCustomSegmentedButtonRenderer(g, r);
		} else {
			return r;
		}
	}

	protected boolean isCustomSegmentedButtonRendererNeeded(@NotNull SegmentedButtonConfiguration g)
	{
		// The CoreUI rendering of segmented cells assumes a particular style of use, the result of which is that dividers
		// are painted one pixel wide and at 1x only right side dividers are painted. We want dividers to be one point wide,
		// which means they must be extended at 2x. Also, we want the ability to request a divider on the left side, so
		// that at 1x we must conjure an appropriate divider.

		// No special treatment is required for small square buttons.

		// We cannot tell until use whether we are at 1x or 2x, so we use a custom renderer whenever any divider is
		// requested.

		SegmentedButtonWidget w = g.getWidget();
		return w != SegmentedButtonWidget.BUTTON_SEGMENTED_SMALL_SQUARE
			&& (g.getLeftDividerState() != SegmentedButtonConfiguration.DividerState.NONE
			|| g.getRightDividerState() != SegmentedButtonConfiguration.DividerState.NONE);
	}

	protected @NotNull Renderer createCustomSegmentedButtonRenderer(@NotNull SegmentedButtonConfiguration g,
																																	@NotNull Renderer r)
	{
		return new MySegmentedButtonRenderer(g, r);
	}

	protected class MySegmentedButtonRenderer
		extends Renderer
	{
		private final @NotNull SegmentedButtonConfiguration g;
		private final @NotNull Renderer r;

		public MySegmentedButtonRenderer(@NotNull SegmentedButtonConfiguration g, @NotNull Renderer r)
		{
			this.g = g;
			this.r = r;
		}

		@Override
		public void composeTo(@NotNull ReusableCompositor compositor)
		{
			// This code is sensitive to the behavior of the CoreUI segment cell rendering, which is resolution dependent.
			// It may not work for displays with a scale factor other than 1 or 2, which is all we have tested.

			boolean isLeftExtensionNeeded = g.getLeftDividerState() != SegmentedButtonConfiguration.DividerState.NONE;
			boolean isRightExtensionNeeded = g.getRightDividerState() != SegmentedButtonConfiguration.DividerState.NONE
				&& compositor.getScaleFactor() > 1;
			if (!isLeftExtensionNeeded && !isRightExtensionNeeded) {
				// This case arises at 1x if only a right divider is requested. No special treatment needed.
				r.composeTo(compositor);
			} else {
				int leftInset = isLeftExtensionNeeded ? 1 : 0;
				int rightInset = isRightExtensionNeeded ? 1 : 0;
				int w = compositor.getRasterWidth();
				int h = compositor.getRasterHeight();

				// Paint the basic rendering
				Renderer basic = Renderer.createRasterOffsetRenderer(r, leftInset, 0, w-leftInset-rightInset, h);
				basic.composeTo(compositor);

				// To extend the left or right side with a divider, we copy the divider at the opposite end of an appropriately
				// configured rendering of a middle cell.

				SegmentedButtonConfiguration rg = new SegmentedButtonConfiguration(g.getWidget(),
					g.getSize(),
					g.getState(),
					g.isSelected(),
					g.isFocused(),
					g.getDirection(),
					Position.MIDDLE,
					g.getRightDividerState(),
					g.getLeftDividerState());

				int sourceWidth = 100;
				Renderer unconfiguredSource = AugmentedCoreUIPainter.super.getSegmentedButtonRenderer(rg);
				ReusableCompositor source = new ReusableCompositor();
				source.reset(sourceWidth, h, compositor.getScaleFactor());
				unconfiguredSource.composeTo(source);

				if (leftInset > 0) {
					// paint the rightmost raster column(s) of the native segment at the left side
					compositor.composeFrom(source, sourceWidth-1, 0, 0, 0, leftInset, h);
				}

				if (rightInset > 0) {
					// paint the leftmost raster column(s) of the native segment at the right side
					compositor.composeFrom(source, w-rightInset, 0, rightInset, h);
				}
			}
		}
	}

	@Override
	protected @NotNull Renderer getSplitPaneDividerRenderer(@NotNull SplitPaneDividerConfiguration g)
	{
		if (g.getWidget() == DividerWidget.THIN_DIVIDER) {
			PainterExtension px = new ThinSplitPaneDividerPainterExtension(g);
			return Renderer.create(px);
		} else {
			return super.getSplitPaneDividerRenderer(g);
		}
	}

	@Override
	public @NotNull Renderer getTableColumnHeaderRenderer(@NotNull TableColumnHeaderConfiguration g)
	{
		// Do not use the native renderer. Use our simulation instead.

		PainterExtension px = new TableColumnHeaderCellPainterExtension(g);
		return Renderer.create(px);
	}

	@Override
	protected @NotNull Renderer getSliderRenderer(@NotNull SliderConfiguration g)
	{
		Renderer r = super.getSliderRenderer(g);
		if (g.getWidget() == SliderWidget.SLIDER_CIRCULAR) {
			Renderer pr = Renderer.create(new CircularSliderPainterExtension(g));
			return Renderer.createCompositeRenderer(r, pr);
		}
		return r;
	}

	@Override
	protected @Nullable Renderer getSliderTickMarkRenderer(@NotNull SliderConfiguration g)
	{
		if (g.getWidget() != SliderWidget.SLIDER_CIRCULAR && g.hasTickMarks()) {
			return Renderer.create(new LinearSliderPainterExtension(uiLayout, g));
		} else {
			return null;
		}
	}

	@Override
	public @Nullable Renderer getPopupArrowRenderer(@NotNull PopupButtonConfiguration g)
	{
		Renderer r = super.getPopupArrowRenderer(g);
		if (isArrowNeeded(g)) {
			if (g.isPopUp()) {
				return Renderer.create(new PopUpArrowPainter(g));
			} else {
				return Renderer.create(new PullDownArrowPainter(g));
			}
		}
		return r;
	}

	private boolean isArrowNeeded(@NotNull PopupButtonConfiguration g)
	{
		PopupButtonWidget w = g.getPopupButtonWidget();
		// Correct arrow color for recessed style
		return w == BUTTON_POP_UP_RECESSED || w == BUTTON_POP_DOWN_RECESSED;
	}
}
