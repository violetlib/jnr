/*
 * Copyright (c) 2015 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.aqua;

import java.util.Objects;

import org.jetbrains.annotations.*;

import org.violetlib.jnr.aqua.AquaUIPainter.ColumnSortArrowDirection;
import org.violetlib.jnr.aqua.AquaUIPainter.State;
import org.violetlib.jnr.aqua.AquaUIPainter.UILayoutDirection;

/**
	A configuration for a table column header.
*/

public class TableColumnHeaderConfiguration
	extends TableColumnHeaderLayoutConfiguration
	implements Configuration
{
	private final @NotNull State state;
	private final @NotNull ColumnSortArrowDirection d;
	private final boolean isSelected;
	private final boolean isFocused;

	// It is assumed that all calls to paint a table header cell will paint a divider on both sides. The calls are
	// arranged so that the divider on the left side either overlaps the right side divider of the cell to the left or is
	// clipped away. This design has potential uses that are not currently supported. For example, it could support a
	// selected cell with a special divider on both sides. That would require painting the cells in the proper order,
	// which the current code does not try to do.

	// It appears that the current design also clips away the top row of pixels produced by the renderer.

	public TableColumnHeaderConfiguration(@NotNull State state,
																				@NotNull ColumnSortArrowDirection d,
																				boolean isSelected,
																				boolean isFocused,
																				@NotNull UILayoutDirection ld)
	{
		super(ld, d != ColumnSortArrowDirection.NONE);

		this.state = state;
		this.d = d;
		this.isSelected = isSelected;
		this.isFocused = isFocused;
	}

	public TableColumnHeaderConfiguration(@NotNull State state,
																				boolean isSortable,
																				boolean isSelected,
																				boolean isFocused,
																				@NotNull UILayoutDirection ld)
	{
		super(ld, isSortable);

		this.state = state;
		this.d = ColumnSortArrowDirection.NONE;
		this.isSelected = isSelected;
		this.isFocused = isFocused;
	}

	public @NotNull State getState()
	{
		return state;
	}

	public @NotNull ColumnSortArrowDirection getSortArrowDirection()
	{
		return d;
	}

	public boolean isSelected()
	{
		return isSelected;
	}

	public boolean isFocused()
	{
		return isFocused;
	}

	@Override
	public boolean equals(@Nullable Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;
		TableColumnHeaderConfiguration that = (TableColumnHeaderConfiguration) o;
		return state == that.state && d == that.d
			&& isSelected == that.isSelected && isFocused == that.isFocused;
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(super.hashCode(), state, d, isSelected, isFocused);
	}

	@Override
	public @NotNull String toString()
	{
		String lds = getLayoutDirection() == UILayoutDirection.RIGHT_TO_LEFT ? " RTL" : "";
		String ss = d == ColumnSortArrowDirection.NONE && isSortable() ? " - Sortable" : "";
		String fs = isFocused ? " focused" : "";
		String sls = isSelected ? " selected" : "";
		return "Table Column Header" + ss + lds + " " + state + " " + d + sls + fs;
	}
}
