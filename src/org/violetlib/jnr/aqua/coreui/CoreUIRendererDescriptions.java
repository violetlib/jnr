/*
 * Copyright (c) 2015-2016 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.aqua.coreui;

import org.jetbrains.annotations.*;

import org.violetlib.jnr.aqua.AquaUIPainter;
import org.violetlib.jnr.aqua.ButtonConfiguration;
import org.violetlib.jnr.aqua.ComboBoxConfiguration;
import org.violetlib.jnr.aqua.PopupButtonConfiguration;
import org.violetlib.jnr.aqua.ScrollBarConfiguration;
import org.violetlib.jnr.aqua.SegmentedButtonConfiguration;
import org.violetlib.jnr.aqua.SplitPaneDividerConfiguration;
import org.violetlib.jnr.aqua.impl.NativeSupport;
import org.violetlib.jnr.aqua.impl.ViewRendererDescriptions;
import org.violetlib.jnr.impl.BasicRendererDescription;
import org.violetlib.jnr.impl.JNRUtils;
import org.violetlib.jnr.impl.RendererDescription;

import static org.violetlib.jnr.impl.JNRUtils.*;

/**
	Renderer descriptions for Core UI based rendering on OS X 10.10 (Yosemite). This mostly includes rendering via the
	Java Runtime Support framework.
*/

public class CoreUIRendererDescriptions
	extends ViewRendererDescriptions
{
	@Override
	public @NotNull RendererDescription getSplitPaneDividerRendererDescription(@NotNull SplitPaneDividerConfiguration g)
	{
		AquaUIPainter.DividerWidget dw = g.getWidget();
		AquaUIPainter.Orientation o = g.getOrientation();

		switch (g.getWidget())
		{
			case THIN_DIVIDER:
			case THICK_DIVIDER:
				return new BasicRendererDescription(0, 0, 0, 0);
			case PANE_SPLITTER:
				return o == AquaUIPainter.Orientation.HORIZONTAL ? new BasicRendererDescription(0, -1, 0, 2) : new BasicRendererDescription(-1, 0, 2, 0);
			default:
				return null;
		}
	}

	@Override
	public @NotNull RendererDescription getButtonRendererDescription(@NotNull ButtonConfiguration g)
	{
		AquaUIPainter.ButtonWidget bw = toCanonicalButtonStyle(g.getButtonWidget());
		AquaUIPainter.Size sz = g.getSize();

		if (bw == AquaUIPainter.ButtonWidget.BUTTON_ROUND) {
			switch (sz) {
				case LARGE:
				case REGULAR:
					return new BasicRendererDescription(0, 0, 0, 2);
				case SMALL:
					return new BasicRendererDescription(0, 0, 0, 4);
				case MINI:
					return new BasicRendererDescription(0, 0, 0, 2);
				default:
					throw new UnsupportedOperationException();
			}
		} else if (bw == AquaUIPainter.ButtonWidget.BUTTON_TEXTURED) {
			return new BasicRendererDescription(-1, -1, 2, 2);
		} else {
			return super.getButtonRendererDescription(g);
		}
	}

	@Override
	public @NotNull RendererDescription getSegmentedButtonRendererDescription(@NotNull SegmentedButtonConfiguration g)
	{
		AquaUIPainter.Position pos = g.getPosition();
		AquaUIPainter.SegmentedButtonWidget bw = g.getWidget();
		AquaUIPainter.Size sz = g.getSize();

		boolean atLeftEdge = pos == AquaUIPainter.Position.FIRST || pos == AquaUIPainter.Position.ONLY;
		boolean atRightEdge = pos == AquaUIPainter.Position.LAST || pos == AquaUIPainter.Position.ONLY;

		boolean isLeftDividerPossible = pos == AquaUIPainter.Position.MIDDLE || pos == AquaUIPainter.Position.LAST;
		boolean isRightDividerPossible = pos == AquaUIPainter.Position.FIRST || pos == AquaUIPainter.Position.MIDDLE;

		RendererDescription rd = super.getSegmentedButtonRendererDescription(g);

		float extraWidth = 0;
		float xOffset = 0;
		float yOffset = 0;

		float leftOffset = 0;
		float leftExtraWidth = 0;
		float rightExtraWidth = 0;
		float extraHeight = 0;

		switch (bw) {
			case BUTTON_TAB:
			case BUTTON_SEGMENTED:
				yOffset = size2D(sz, -0.51f, -1, -2);	// regular size should be -1 at 1x
				leftOffset = size(sz, -2, -2, -1);
				leftExtraWidth = rightExtraWidth = size(sz, 2, 2, 1);
				break;

			case BUTTON_SEGMENTED_INSET:
				yOffset = size2D(sz, -1, -1.51f, -2);	// small size should be -2 at 1x
				leftOffset = -1;
				leftExtraWidth = 1;
				rightExtraWidth = 1;
				break;

			case BUTTON_SEGMENTED_SCURVE:
			case BUTTON_SEGMENTED_TEXTURED:
			case BUTTON_SEGMENTED_TOOLBAR:
				if (sz == AquaUIPainter.Size.MINI) {
					rd = createVertical(0, 4);
				}
				yOffset = size(sz, 0, -1, -2);
				break;

			case BUTTON_SEGMENTED_TEXTURED_SEPARATED:
				if (sz == AquaUIPainter.Size.MINI) {
					rd = createVertical(0, 4);
				}
				yOffset = size(sz, 0, -1, -2);
				extraWidth = -0.49f;	// decrease the raster width at 2x so that the gap is 1 point
				isLeftDividerPossible = false;
				isRightDividerPossible = false;
				break;

			case BUTTON_SEGMENTED_SMALL_SQUARE:
				yOffset = -1;
				extraHeight = size(sz, 0, -1, -2);
				break;

			default:
				throw new UnsupportedOperationException();
		}

		if (atLeftEdge) {
			xOffset += leftOffset;
			extraWidth += leftExtraWidth;
		}

		if (atRightEdge) {
			extraWidth += rightExtraWidth;
		}

		// If a left divider is possible and not requested, widen the rendering and shift it left by one point so that no
		// divider is painted in our raster. This is necessary because the divider space is allocated even if it is not
		// painted. Similar for a right divider.

		if (isLeftDividerPossible && g.getLeftDividerState() == SegmentedButtonConfiguration.DividerState.NONE) {
			xOffset -= 1;
			extraWidth += 1;
		}

		if (isRightDividerPossible && g.getRightDividerState() == SegmentedButtonConfiguration.DividerState.NONE) {
			extraWidth += 1;
		}

		try {
			return JNRUtils.adjustRendererDescription(rd, xOffset, yOffset, extraWidth, extraHeight);
		} catch (UnsupportedOperationException ex) {
			NativeSupport.log("Unable to adjust segmented button renderer description for " + g);
			return rd;
		}
	}

	@Override
	public @NotNull RendererDescription getComboBoxRendererDescription(@NotNull ComboBoxConfiguration g)
	{
		AquaUIPainter.ComboBoxWidget bw = g.getWidget();
		AquaUIPainter.Size sz = g.getSize();

		if (bw == AquaUIPainter.ComboBoxWidget.BUTTON_COMBO_BOX_CELL) {
			switch (sz) {
				case LARGE:
				case REGULAR:
					return new BasicRendererDescription(0, -3, 2, 3);
				case SMALL:
					return new BasicRendererDescription(0, -3, 2, 3);
				case MINI:
					return new BasicRendererDescription(0, -1.5f, 2, 2);
				default:
					throw new UnsupportedOperationException();
			}
		} else {
			switch (sz) {
				case LARGE:
				case REGULAR:
					return new BasicRendererDescription(-0.5f, 0, 2, 1);
				case SMALL:
					return new BasicRendererDescription(-0.5f, 0, 1, 0);
				case MINI:
					return new BasicRendererDescription(-0.5f, -0.51f, 2, 1);
				default:
					throw new UnsupportedOperationException();
			}
		}
	}

	@Override
	public @NotNull RendererDescription getPopupButtonRendererDescription(@NotNull PopupButtonConfiguration g)
	{
		AquaUIPainter.PopupButtonWidget bw = g.getPopupButtonWidget();
		AquaUIPainter.Size sz = g.getSize();

		if (bw == AquaUIPainter.PopupButtonWidget.BUTTON_POP_UP_CELL) {
				// extra height not needed for Core UI renderer
				switch (sz) {
					case LARGE:
					case REGULAR:
						return new BasicRendererDescription(0, 0, 0, 0);
					case SMALL:
						return new BasicRendererDescription(0, 0, 0, 0);
					case MINI:
						return new BasicRendererDescription(0, 0, 0, 0);
					default:
						throw new UnsupportedOperationException();
				}
		}

		return super.getPopupButtonRendererDescription(g);
	}

	public @Nullable RendererDescription getPopUpArrowRendererDescription(@NotNull PopupButtonConfiguration g,
																																				@NotNull AquaUIPainter.Size arrowSize)
	{
		float left = JNRUtils.size2D(arrowSize, 1, 0, 2);
		float top = JNRUtils.size2D(arrowSize, 3, 1, 1);
		int w = JNRUtils.size(arrowSize, 1, 0, 1);
		int h = JNRUtils.size(arrowSize, 3, 1, 0);
		return new BasicRendererDescription(-left, -top, w, h);
	}

	public @NotNull RendererDescription getPullDownArrowRendererDescription(@NotNull PopupButtonConfiguration g)
	{
		return new BasicRendererDescription(0, 0, 0, 0);
	}

	public @NotNull RendererDescription getScrollBarThumbRendererDescription(@NotNull ScrollBarConfiguration g)
	{
		// NSScroller leaves a 3 point gap at the ends for legacy, apparently 2 points for overlay.
		// CoreUI leaves a 1 point gap at the ends.
		// Here we compensate.

		int extra = g.getWidget() == AquaUIPainter.ScrollBarWidget.LEGACY ? 2 : 1;

		if (g.getOrientation() == AquaUIPainter.Orientation.VERTICAL) {
			return new BasicRendererDescription(0, extra, 0, -2*extra);
		} else {
			return new BasicRendererDescription(extra, 0, -2*extra, 0);
		}
	}
}
