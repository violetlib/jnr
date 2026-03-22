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
import org.violetlib.jnr.aqua.AquaUIPainter.ButtonWidget;
import org.violetlib.jnr.aqua.AquaUIPainter.Size;
import org.violetlib.jnr.aqua.AquaUIPainter.UILayoutDirection;

import java.util.Objects;

import static org.violetlib.jnr.aqua.AquaUIPainter.ButtonWidget.*;
import static org.violetlib.jnr.aqua.AquaUIPainter.macOS11;
import static org.violetlib.jnr.aqua.AquaUIPainter.macOS26;

/**
  A layout configuration for a button.
*/

public class ButtonLayoutConfiguration
  extends LayoutDirectionSensitiveLayoutConfigurationImpl
{
    private final @NotNull ButtonWidget bw;
    private final @NotNull Size size;

    public ButtonLayoutConfiguration(@NotNull ButtonWidget bw, @NotNull Size size, @NotNull UILayoutDirection ld)
    {
        super(ld);

        if (!AquaNativeRendering.isRaw()) {
            int version = AquaNativeRendering.getSystemRenderingVersion();

            if (version >= 150000) {
                if (bw == BUTTON_PUSH_INSET2) {
                    bw = BUTTON_PUSH;
                } else if (bw == BUTTON_ROUND_INSET) {
                    bw = BUTTON_ROUND;
                }
            }

            if (version >= macOS26) {
                if (size == Size.EXTRA_LARGE) {
                    switch (bw) {
                        case BUTTON_PUSH:
                        case BUTTON_ROUNDED_RECT:
                        case BUTTON_BEVEL_ROUND:
                        case BUTTON_GLASS:
                        case BUTTON_TOOLBAR_ITEM:
                        case BUTTON_TOOLBAR:
                            break;
                        default:
                            size = Size.LARGE;
                    }
                }
            } else {
                if (bw == BUTTON_GLASS) {
                    bw = BUTTON_BEVEL_ROUND;
                }
                // Extra large size is not supported
                if (size == Size.EXTRA_LARGE) {
                    size = Size.LARGE;
                }
                if (size == Size.LARGE && !isLargeSizeSupported(version, bw)) {
                    size = Size.REGULAR;
                }
            }
        }

        this.bw = bw;
        this.size = size;
    }

    private static boolean isLargeSizeSupported(int version, @NotNull ButtonWidget bw)
    {
        if (version < macOS11) {
            return false;
        }
        // Large size is supported only for certain styles (with some painter-specific exceptions).
        switch (bw) {
            case BUTTON_PUSH:
            case BUTTON_CHECK_BOX:
            case BUTTON_RADIO:
            case BUTTON_DISCLOSURE:
            case BUTTON_RECESSED:
            case BUTTON_TEXTURED_TOOLBAR:
            case BUTTON_TOOLBAR:
            case BUTTON_HELP:
            case BUTTON_ROUND:
            case BUTTON_ROUND_INSET:
            case BUTTON_ROUND_TEXTURED:
            case BUTTON_ROUND_TEXTURED_TOOLBAR:
                return true;
        }
        return false;
    }

    @Override
    public @NotNull Object getWidget()
    {
        return bw;
    }

    public @NotNull ButtonWidget getButtonWidget()
    {
        return bw;
    }

    @Override
    public @NotNull Size getSize()
    {
        return size;
    }

    public @NotNull ButtonLayoutConfiguration with(@NotNull ButtonWidget widget)
    {
        if (widget == this.bw) {
            return this;
        }
        return new ButtonLayoutConfiguration(widget, size, getLayoutDirection());
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
        ButtonLayoutConfiguration that = (ButtonLayoutConfiguration) o;
        return bw == that.bw && size == that.size;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(super.hashCode(), bw, size);
    }

    @Override
    public @NotNull String toString()
    {
        String lds = getLayoutDirection() == UILayoutDirection.RIGHT_TO_LEFT ? " RTL" : "";
        return bw + " " + size + lds;
    }
}
