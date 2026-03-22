/*
 * Copyright (c) 2015-2025 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.aqua;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.violetlib.jnr.aqua.AquaUIPainter.UILayoutDirection;

import java.util.Objects;

/**
  A layout configuration for a table column header.
*/

public class TableColumnHeaderLayoutConfiguration
  extends LayoutDirectionSensitiveLayoutConfigurationImpl
{
    private final boolean isSortable;

    public TableColumnHeaderLayoutConfiguration(@NotNull UILayoutDirection ld, boolean isSortable)
    {
        super(ld);

        this.isSortable = isSortable;
    }

    @Override
    public @NotNull Object getWidget()
    {
        return this;
    }

    @Override
    public @Nullable AquaUIPainter.Size getSize()
    {
        return null;
    }

    public boolean isSortable()
    {
        return isSortable;
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
        TableColumnHeaderLayoutConfiguration that = (TableColumnHeaderLayoutConfiguration) o;
        return isSortable == that.isSortable;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(super.hashCode(), isSortable);
    }

    @Override
    public @NotNull String toString()
    {
        String lds = getLayoutDirection() == UILayoutDirection.RIGHT_TO_LEFT ? " RTL" : "";
        String ss = isSortable ? " - Sortable" : "";
        return "Table Column Header" + ss + lds;
    }
}
