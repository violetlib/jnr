/*
 * Copyright (c) 2015 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.aqua.impl;

import java.awt.Dimension;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;

import org.jetbrains.annotations.*;

import org.violetlib.jnr.Painter;
import org.violetlib.jnr.aqua.*;

/**
	A hybrid painter that uses the best available implementation for each given configuration.
*/

public class HybridAquaUIPainter
	implements AquaUIPainter
{
	protected final @NotNull AquaUIPainter viewPainter;
	protected final @NotNull AquaUIPainter coreUIPainter;
	protected final @Nullable AquaUIPainter jrsPainter;

	private final @NotNull AquaUILayoutInfo layout;
	private int w;
	private int h;

	public HybridAquaUIPainter(@NotNull AquaUIPainter viewPainter,
														 @NotNull AquaUIPainter coreUIPainter,
														 @Nullable AquaUIPainter jrsPainter)
	{
		this.viewPainter = viewPainter;
		this.coreUIPainter = coreUIPainter;
		this.jrsPainter = jrsPainter;

		layout = viewPainter.getLayoutInfo();	// all implementations share the same layout
	}

	@Override
	public @NotNull HybridAquaUIPainter copy()
	{
		return new HybridAquaUIPainter(viewPainter, coreUIPainter, jrsPainter);
	}

	@Override
	public void configure(int w, int h)
	{
		this.w = w;
		this.h = h;
	}

	@Override
	public @NotNull Painter getPainter(@NotNull Configuration g)
		throws UnsupportedOperationException
	{
		AquaUIPainter p = select(g);
		p.configure(w, h);
		return p.getPainter(g);
	}

	protected @NotNull AquaUIPainter select(@NotNull Configuration g)
	{
		// Prefer the JSR painter if defined because it is faster, except where it is not accurate.
		// Otherwise the core UI painter except where it falls down and the view painter is better.

		if (g instanceof ButtonConfiguration) {
			ButtonConfiguration bg = (ButtonConfiguration) g;
			ButtonWidget bw = bg.getButtonWidget();
			if (bw == ButtonWidget.BUTTON_INLINE) {
				return viewPainter;
			} else {
				return coreUIPainter;
			}
		} else if (g instanceof SegmentedButtonConfiguration) {
			SegmentedButtonConfiguration bg = (SegmentedButtonConfiguration) g;
			if (bg.getState() == State.PRESSED) {
				return jrsPainter != null ? jrsPainter : viewPainter;
			}
			return viewPainter;
		} else if (g instanceof GradientConfiguration) {
			return coreUIPainter;
		} else if (g instanceof ComboBoxConfiguration) {
			ComboBoxConfiguration bg = (ComboBoxConfiguration) g;
			State st = bg.getState();
			if (st == State.DISABLED || st == State.DISABLED_INACTIVE || bg.getLayoutDirection() == UILayoutDirection.RIGHT_TO_LEFT) {
				return coreUIPainter;
			}
		} else if (g instanceof PopupButtonConfiguration) {
			PopupButtonConfiguration bg = (PopupButtonConfiguration) g;
			if (bg.getLayoutDirection() == UILayoutDirection.RIGHT_TO_LEFT) {
				return coreUIPainter;
			}
			PopupButtonWidget widget = bg.getPopupButtonWidget();
			if (widget == PopupButtonWidget.BUTTON_POP_UP_TEXTURED || widget == PopupButtonWidget.BUTTON_POP_DOWN_TEXTURED) {
				return coreUIPainter;
			}
		} else if (g instanceof ProgressIndicatorConfiguration) {
			ProgressIndicatorConfiguration bg = (ProgressIndicatorConfiguration) g;
			if (bg.getWidget() == ProgressWidget.BAR && bg.getOrientation() == Orientation.HORIZONTAL) {
				return coreUIPainter;
			}
		} else if (g instanceof IndeterminateProgressIndicatorConfiguration) {
			IndeterminateProgressIndicatorConfiguration bg = (IndeterminateProgressIndicatorConfiguration) g;
			return coreUIPainter;
		} else if (g instanceof TextFieldConfiguration) {
			TextFieldConfiguration bg = (TextFieldConfiguration) g;
			if (bg.isSearchField()) {
				return coreUIPainter;
			}
		} else if (g instanceof SliderConfiguration) {
			SliderConfiguration bg = (SliderConfiguration) g;
			if (!bg.hasTickMarks()) {
				return coreUIPainter;
			}
		} else if (g instanceof TitleBarConfiguration) {
			return coreUIPainter;
		} else if (g instanceof ScrollBarConfiguration) {
			return coreUIPainter;
		}

		return jrsPainter != null ? jrsPainter : coreUIPainter;
	}

	@Override
	public @NotNull AquaUILayoutInfo getLayoutInfo()
	{
		return layout;
	}

	@Override
	public @Nullable Shape getOutline(@NotNull LayoutConfiguration g)
	{
		viewPainter.configure(w, h);
		return viewPainter.getOutline(g);
	}

	@Override
	public @NotNull Rectangle2D getComboBoxEditorBounds(@NotNull ComboBoxLayoutConfiguration g)
	{
		viewPainter.configure(w, h);
		return viewPainter.getComboBoxEditorBounds(g);
	}

	@Override
	public @NotNull Rectangle2D getComboBoxIndicatorBounds(@NotNull ComboBoxLayoutConfiguration g)
	{
		viewPainter.configure(w, h);
		return viewPainter.getComboBoxIndicatorBounds(g);
	}

	@Override
	public @NotNull Rectangle2D getPopupButtonContentBounds(@NotNull PopupButtonLayoutConfiguration g)
	{
		viewPainter.configure(w, h);
		return viewPainter.getPopupButtonContentBounds(g);
	}

	@Override
	public @NotNull Rectangle2D getSliderThumbBounds(@NotNull SliderLayoutConfiguration g, double thumbPosition)
	{
		viewPainter.configure(w, h);
		return viewPainter.getSliderThumbBounds(g, thumbPosition);
	}

	@Override
	public double getSliderThumbPosition(@NotNull SliderLayoutConfiguration g, int x, int y)
	{
		viewPainter.configure(w, h);
		return viewPainter.getSliderThumbPosition(g, x, y);
	}

	@Override
	public float getScrollBarThumbPosition(@NotNull ScrollBarThumbLayoutConfiguration g, boolean useExtent)
	{
		viewPainter.configure(w, h);
		return viewPainter.getScrollBarThumbPosition(g, useExtent);
	}

	@Override
	public int getScrollBarThumbHit(@NotNull ScrollBarThumbConfiguration g)
	{
		viewPainter.configure(w, h);
		return viewPainter.getScrollBarThumbHit(g);
	}

	@Override
	public @NotNull Rectangle2D getSliderLabelBounds(@NotNull SliderLayoutConfiguration g,
																									 double thumbPosition,
																									 @NotNull Dimension size)
	{
		viewPainter.configure(w, h);
		return viewPainter.getSliderLabelBounds(g, thumbPosition, size);
	}
}
