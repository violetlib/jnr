/*
 * Copyright (c) 2015 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.aqua.impl;

import java.awt.Color;

import org.jetbrains.annotations.*;

import org.violetlib.jnr.aqua.AquaUIPainter.PopupButtonWidget;
import org.violetlib.jnr.aqua.AquaUIPainter.State;
import org.violetlib.jnr.aqua.PopupButtonConfiguration;

/**

*/

public abstract class PopUpArrowPainterBase
{
	protected @NotNull Color ACTIVE_COLOR = new Color(7, 7, 7, 150);
	protected @NotNull Color DISABLED_COLOR = new Color(0, 0, 0, 64);

	protected final @NotNull PopupButtonConfiguration gg;

	public PopUpArrowPainterBase(@NotNull PopupButtonConfiguration gg)
	{
		this.gg = gg;
	}

	protected @NotNull Color getColor()
	{
		State st = gg.getState();
		PopupButtonWidget w = gg.getPopupButtonWidget();

		if (st == State.ROLLOVER && (w == PopupButtonWidget.BUTTON_POP_UP_RECESSED || w == PopupButtonWidget.BUTTON_POP_DOWN_RECESSED)) {
			return Color.WHITE;
		}

		return st == State.DISABLED || st == State.DISABLED_INACTIVE ? DISABLED_COLOR : ACTIVE_COLOR;
	}
}
