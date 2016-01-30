/*
 * Copyright (c) 2016 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.aqua.impl;

import org.jetbrains.annotations.*;

import org.violetlib.jnr.LayoutInfo;
import org.violetlib.jnr.aqua.AquaUIPainter;
import org.violetlib.jnr.aqua.SegmentedButtonLayoutConfiguration;
import org.violetlib.jnr.impl.BasicLayoutInfo;

import static org.violetlib.jnr.impl.JNRUtils.size;

/**
	Layout information for OS 10.10 widgets.
*/

public class ElCapitanLayoutInfo
	extends YosemiteLayoutInfo
{
	@Override
	protected @NotNull LayoutInfo getSegmentedButtonLayoutInfo(@NotNull SegmentedButtonLayoutConfiguration g)
	{
		AquaUIPainter.SegmentedButtonWidget bw = g.getWidget();
		AquaUIPainter.Size sz = g.getSize();

		switch (bw) {
			case BUTTON_TAB:
			case BUTTON_SEGMENTED:
				return BasicLayoutInfo.createFixedHeight(size(sz, 22, 19, 16));

			case BUTTON_SEGMENTED_INSET:
				return BasicLayoutInfo.createFixedHeight(size(sz, 18, 16, 14));

			case BUTTON_SEGMENTED_SCURVE:
			case BUTTON_SEGMENTED_TEXTURED:
			case BUTTON_SEGMENTED_TOOLBAR:
				return BasicLayoutInfo.createFixedHeight(size(sz, 23, 19, 16));

			case BUTTON_SEGMENTED_TEXTURED_SEPARATED:
				return BasicLayoutInfo.createFixedHeight(size(sz, 24, 20, 17));

			case BUTTON_SEGMENTED_SMALL_SQUARE:
				return BasicLayoutInfo.createFixedHeight(size(sz, 21, 19, 17));

			default:
				throw new UnsupportedOperationException();
		}
	}
}
