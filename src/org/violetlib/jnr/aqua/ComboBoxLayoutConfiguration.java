/*
 * Copyright (c) 2015-2026 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.aqua;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.violetlib.jnr.aqua.AquaUIPainter.ComboBoxWidget;
import org.violetlib.jnr.aqua.AquaUIPainter.Size;
import org.violetlib.jnr.aqua.AquaUIPainter.UILayoutDirection;

import java.util.Objects;

import static org.violetlib.jnr.aqua.AquaUIPainter.macOS26;

/**
  A layout configuration for an editable combo box.
*/

public class ComboBoxLayoutConfiguration
  extends AbstractComboBoxLayoutConfiguration
{
    private final @NotNull ComboBoxWidget widget;
    private final @NotNull Size size;

    public ComboBoxLayoutConfiguration(@NotNull ComboBoxWidget widget, @NotNull Size size, @NotNull UILayoutDirection ld)
    {
        super(ld);

        if (!AquaNativeRendering.isRaw()) {
            if (widget == ComboBoxWidget.BUTTON_COMBO_BOX_TEXTURED
              && (size == Size.LARGE || size == Size.EXTRA_LARGE)) {
                size = Size.REGULAR;
            }
            if (size == Size.EXTRA_LARGE) {
                int version = AquaNativeRendering.getSystemRenderingVersion();
                if (version < macOS26) {
                    size = Size.LARGE;
                }
            }
        }

        this.widget = widget;
        this.size = size;
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

    @Override
    public boolean isCell()
    {
        return widget == ComboBoxWidget.BUTTON_COMBO_BOX_CELL;
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
        if (!super.equals(o)) {
            return false;
        }
        ComboBoxLayoutConfiguration that = (ComboBoxLayoutConfiguration) o;
        return widget == that.widget && size == that.size;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(super.hashCode(), widget, size);
    }

    @Override
    public @NotNull String toString()
    {
        String lds = getLayoutDirection() == UILayoutDirection.RIGHT_TO_LEFT ? " RTL" : "";
        return widget + " " + size + lds;
    }
}
