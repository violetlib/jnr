/*
 * Copyright (c) 2015 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.aqua;

import java.util.Objects;

import org.violetlib.jnr.aqua.AquaUIPainter.ComboBoxWidget;
import org.violetlib.jnr.aqua.AquaUIPainter.Size;
import org.violetlib.jnr.aqua.AquaUIPainter.UILayoutDirection;

import org.jetbrains.annotations.*;

/**
  A layout configuration for an editable combo box.
*/

public class ComboBoxLayoutConfiguration
  extends AbstractComboBoxLayoutConfiguration
{
    private final @NotNull ComboBoxWidget widget;
    private final @NotNull Size size;
    private final @NotNull UILayoutDirection ld;

    public ComboBoxLayoutConfiguration(@NotNull ComboBoxWidget widget, @NotNull Size size, @NotNull UILayoutDirection ld)
    {
        this.widget = widget;
        this.size = size;
        this.ld = ld;
    }

    @Override
    public @NotNull ComboBoxWidget getWidget()
    {
        return widget;
    }

    public @NotNull Size getSize()
    {
        return size;
    }

    public @NotNull UILayoutDirection getLayoutDirection()
    {
        return ld;
    }

    @Override
    public boolean isCell()
    {
        return widget == ComboBoxWidget.BUTTON_COMBO_BOX_CELL;
    }

    public boolean isLeftToRight()
    {
        return ld == UILayoutDirection.LEFT_TO_RIGHT;
    }

    @Override
    public boolean equals(@Nullable Object o)
    {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ComboBoxLayoutConfiguration that = (ComboBoxLayoutConfiguration) o;
        return widget == that.widget && size == that.size && ld == that.ld;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(widget, size, ld);
    }

    @Override
    public @NotNull String toString()
    {
        String lds = ld == UILayoutDirection.RIGHT_TO_LEFT ? " RTL" : "";
        return widget + " " + size + lds;
    }
}
