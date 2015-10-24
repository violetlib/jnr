/*
 * Copyright (c) 2015 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.aqua.impl;

import org.jetbrains.annotations.*;

import org.violetlib.jnr.aqua.*;
import org.violetlib.jnr.impl.RendererDescription;

/**
	Provides renderer descriptions. Often shared by multiple painters that use the same underlying renderer.
*/

public interface RendererDescriptions
{
	@NotNull RendererDescription getButtonRendererDescription(@NotNull ButtonConfiguration g);

	@NotNull RendererDescription getSegmentedButtonRendererDescription(@NotNull SegmentedButtonConfiguration g);

	@NotNull RendererDescription getComboBoxRendererDescription(@NotNull ComboBoxConfiguration g);

	@NotNull RendererDescription getPopupButtonRendererDescription(@NotNull PopupButtonConfiguration g);

	@Nullable RendererDescription getBasicPopupButtonRendererDescription(@NotNull PopupButtonConfiguration g);

	@NotNull RendererDescription getToolBarItemWellRendererDescription(@NotNull ToolBarItemWellConfiguration g);

	@NotNull RendererDescription getTitleBarRendererDescription(@NotNull TitleBarConfiguration g);

	@NotNull RendererDescription getSliderRendererDescription(@NotNull SliderConfiguration g);

	@NotNull RendererDescription getSliderTrackRendererDescription(@NotNull SliderConfiguration g);

	@NotNull RendererDescription getSliderThumbRendererDescription(@NotNull SliderConfiguration g);

	@NotNull RendererDescription getSpinnerArrowsRendererDescription(@NotNull SpinnerArrowsConfiguration g);

	@NotNull RendererDescription getSplitPaneDividerRendererDescription(@NotNull SplitPaneDividerConfiguration g);

	@NotNull RendererDescription getGroupBoxRendererDescription(@NotNull GroupBoxConfiguration g);

	@NotNull RendererDescription getListBoxRendererDescription(@NotNull ListBoxConfiguration g);

	@NotNull RendererDescription getTextFieldRendererDescription(@NotNull TextFieldConfiguration g);

	@NotNull RendererDescription getScrollBarRendererDescription(@NotNull ScrollBarConfiguration g);

	@NotNull RendererDescription getScrollColumnSizerRendererDescription(@NotNull ScrollColumnSizerConfiguration g);

	@NotNull RendererDescription getProgressIndicatorRendererDescription(@NotNull ProgressIndicatorConfiguration g);

	@NotNull RendererDescription getIndeterminateProgressIndicatorRendererDescription(@NotNull IndeterminateProgressIndicatorConfiguration g);

	@NotNull RendererDescription getTableColumnHeaderRendererDescription(@NotNull TableColumnHeaderConfiguration g);

	@NotNull RendererDescription getGradientRendererDescription(@NotNull GradientConfiguration g);
}
