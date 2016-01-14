/*
 * Copyright (c) 2015-2016 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.aqua.coreui;

import org.jetbrains.annotations.*;

import org.violetlib.jnr.aqua.ButtonConfiguration;
import org.violetlib.jnr.aqua.PopupButtonConfiguration;
import org.violetlib.jnr.aqua.SliderConfiguration;
import org.violetlib.jnr.aqua.SplitPaneDividerConfiguration;
import org.violetlib.jnr.aqua.TableColumnHeaderConfiguration;
import org.violetlib.jnr.aqua.impl.CircularSliderPainterExtension;
import org.violetlib.jnr.aqua.impl.LinearSliderPainterExtension;
import org.violetlib.jnr.aqua.impl.PopUpArrowPainter;
import org.violetlib.jnr.aqua.impl.PullDownArrowPainter;
import org.violetlib.jnr.aqua.impl.TableColumnHeaderCellPainterExtension;
import org.violetlib.jnr.aqua.impl.ThinSplitPaneDividerPainterExtension;
import org.violetlib.jnr.impl.JNRPlatformUtils;
import org.violetlib.jnr.impl.PainterExtension;
import org.violetlib.jnr.impl.Renderer;

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

		// On El Capitan, the arrow color is wrong for the rollover state in several styles.

		if (g.getState() == State.ROLLOVER) {
			int platformVersion = JNRPlatformUtils.getPlatformVersion();
			if (platformVersion >= 101100) {
				if (w == BUTTON_POP_UP_CELL || w == BUTTON_POP_UP_BEVEL || w == BUTTON_POP_UP_GRADIENT || w == BUTTON_POP_UP_SQUARE) {
					return true;
				}
			}
		}

		// Correct arrow color for recessed style
		return w == BUTTON_POP_UP_RECESSED || w == BUTTON_POP_DOWN_RECESSED;
	}
}
