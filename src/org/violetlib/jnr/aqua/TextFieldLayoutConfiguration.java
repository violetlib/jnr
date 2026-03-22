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
import org.violetlib.jnr.aqua.AquaUIPainter.Size;
import org.violetlib.jnr.aqua.AquaUIPainter.TextFieldWidget;
import org.violetlib.jnr.aqua.AquaUIPainter.UILayoutDirection;

import java.util.Objects;

import static org.violetlib.jnr.aqua.AquaUIPainter.macOS11;
import static org.violetlib.jnr.aqua.AquaUIPainter.macOS26;

/**
  A layout configuration for a text field or pane.
*/

public class TextFieldLayoutConfiguration
  extends LayoutDirectionSensitiveLayoutConfigurationImpl
{
    private final @NotNull TextFieldWidget tw;
    private final @NotNull Size size;

    public TextFieldLayoutConfiguration(@NotNull TextFieldWidget tw, @NotNull Size size, @NotNull UILayoutDirection ld)
    {
        // Layout direction affects search fields, in particular, the locations of the search and cancel icons
        super(ld);

        if (!AquaNativeRendering.isRaw()) {
            int version = AquaNativeRendering.getSystemRenderingVersion();
            if (size == AquaUIPainter.Size.LARGE || size == AquaUIPainter.Size.EXTRA_LARGE) {
                if (size == Size.EXTRA_LARGE) {
                    size = Size.LARGE;
                }
                if (version < macOS11) {
                    size = Size.REGULAR;
                }
            }
            if (tw.isToolbar() && version >= macOS26) {
                // Toolbar styles render incorrectly
                tw = tw.toBasicWidget();
            }
        }

        this.tw = tw;
        this.size = size;
    }

    @Override
    public @NotNull TextFieldWidget getWidget()
    {
        return tw;
    }

    @Override
    public @NotNull Size getSize()
    {
        return size;
    }

    public boolean isSearchField()
    {
        return tw.isSearch();
    }

    public @NotNull TextFieldLayoutConfiguration withSize(@NotNull Size size)
    {
        if (size == this.size) {
            return this;
        }
        return new TextFieldLayoutConfiguration(tw, size, getLayoutDirection());
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
        TextFieldLayoutConfiguration that = (TextFieldLayoutConfiguration) o;
        return tw == that.tw && size == that.size;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(super.hashCode(), tw, size);
    }

    @Override
    public @NotNull String toString()
    {
        String lds = getLayoutDirection() == UILayoutDirection.RIGHT_TO_LEFT ? " RTL" : "";
        return tw + " " + size + lds;
    }
}
