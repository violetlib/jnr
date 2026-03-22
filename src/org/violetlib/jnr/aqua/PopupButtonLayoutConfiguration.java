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
import org.violetlib.jnr.aqua.AquaUIPainter.PopupButtonWidget;
import org.violetlib.jnr.aqua.AquaUIPainter.Size;
import org.violetlib.jnr.aqua.AquaUIPainter.UILayoutDirection;

import java.util.Objects;

import static org.violetlib.jnr.aqua.AquaUIPainter.PopupButtonWidget.*;
import static org.violetlib.jnr.aqua.AquaUIPainter.macOS11;
import static org.violetlib.jnr.aqua.AquaUIPainter.macOS26;

/**
  A layout configuration for a popup button.
*/

public class PopupButtonLayoutConfiguration
  extends AbstractComboBoxLayoutConfiguration
{
    private final @NotNull PopupButtonWidget bw;
    private final @NotNull Size size;

    public PopupButtonLayoutConfiguration(@NotNull PopupButtonWidget bw,
                                          @NotNull Size size,
                                          @NotNull UILayoutDirection ld)
    {
        super(ld);

        if (!AquaNativeRendering.isRaw()) {
            int version = AquaNativeRendering.getSystemRenderingVersion();
            if (version >= macOS26) {
                switch (bw) {
                    case BUTTON_POP_UP:
                    case BUTTON_POP_UP_RECESSED:
                    case BUTTON_POP_UP_ROUND_RECT:
                    case BUTTON_POP_UP_BEVEL:
                    case BUTTON_POP_UP_TEXTURED:
                    case BUTTON_POP_UP_TEXTURED_TOOLBAR:
                        bw = BUTTON_POP_UP;
                        break;

                    case BUTTON_POP_DOWN:
                    case BUTTON_POP_DOWN_RECESSED:
                    case BUTTON_POP_DOWN_ROUND_RECT:
                    case BUTTON_POP_DOWN_BEVEL:
                    case BUTTON_POP_DOWN_TEXTURED:
                    case BUTTON_POP_DOWN_TEXTURED_TOOLBAR:
                        bw = BUTTON_POP_DOWN;
                }
            } else if (version >= macOS11) {
                if (bw == BUTTON_POP_UP_TEXTURED_TOOLBAR) {
                    bw = BUTTON_POP_UP_TEXTURED;
                } else if (bw == BUTTON_POP_DOWN_TEXTURED_TOOLBAR) {
                    bw = BUTTON_POP_DOWN_TEXTURED;
                }
            }

            if (version >= 101000 && version < macOS11) {
                if (bw == BUTTON_POP_UP_SQUARE) {
                    bw = BUTTON_POP_UP_GRADIENT;
                } else if (bw == BUTTON_POP_DOWN_SQUARE) {
                    bw = BUTTON_POP_DOWN_GRADIENT;
                }
            }

            // Most popup styles do not work in a mini size, most likely because Core UI does not support mini arrows.
            // Even using NSView renderer, the mini arrow is not painted for gradient styles.
            // Some styles have a fixed height and thus do not support small, either.

            if (size == Size.MINI && (bw == BUTTON_POP_UP_GRADIENT || bw == BUTTON_POP_DOWN_GRADIENT)) {
                size = Size.SMALL;
            }

            if (version < macOS26) {
                if (size == Size.EXTRA_LARGE) {
                    size = Size.LARGE;
                }
                if (size == Size.MINI) {
                    if (!supportsMini(bw)) {
                        if (supportsSmall(bw)) {
                            size = Size.SMALL;
                        } else {
                            size = Size.REGULAR;
                        }
                    }
                } else if (size == Size.SMALL) {
                    if (!supportsSmall(bw)) {
                        size = Size.REGULAR;
                    }
                } else if (size == Size.LARGE) {
                    if (version < macOS11 || !supportsLarge(bw)) {
                        size = Size.REGULAR;
                    }
                }
            }
        }

        this.bw = bw;
        this.size = size;
    }

    @Override
    public @NotNull Object getWidget()
    {
        return bw;
    }

    public @NotNull PopupButtonWidget getPopupButtonWidget()
    {
        return bw;
    }

    public @NotNull Size getSize()
    {
        return size;
    }

    public boolean isCell()
    {
        return bw == BUTTON_POP_UP_CELL || bw == BUTTON_POP_DOWN_CELL;
    }

    public boolean isPopUp()
    {
        switch (bw)
        {
            case BUTTON_POP_UP:
            case BUTTON_POP_UP_CELL:
            case BUTTON_POP_UP_BEVEL:
            case BUTTON_POP_UP_ROUND_RECT:
            case BUTTON_POP_UP_RECESSED:
            case BUTTON_POP_UP_TEXTURED:
            case BUTTON_POP_UP_TEXTURED_TOOLBAR:
            case BUTTON_POP_UP_GRADIENT:
            case BUTTON_POP_UP_SQUARE:
                return true;
            case BUTTON_POP_DOWN:
            case BUTTON_POP_DOWN_CELL:
            case BUTTON_POP_DOWN_BEVEL:
            case BUTTON_POP_DOWN_ROUND_RECT:
            case BUTTON_POP_DOWN_RECESSED:
            case BUTTON_POP_DOWN_TEXTURED:
            case BUTTON_POP_DOWN_TEXTURED_TOOLBAR:
            case BUTTON_POP_DOWN_GRADIENT:
            case BUTTON_POP_DOWN_SQUARE:
                return false;
            default:
                throw new UnsupportedOperationException();
        }
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
        PopupButtonLayoutConfiguration that = (PopupButtonLayoutConfiguration) o;
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

    public static boolean supportsMini(@NotNull PopupButtonWidget w)
    {
        switch (w)
        {
            case BUTTON_POP_UP:
            case BUTTON_POP_DOWN:
            case BUTTON_POP_UP_CELL:
            case BUTTON_POP_DOWN_CELL:
            case BUTTON_POP_UP_TEXTURED:
            case BUTTON_POP_UP_TEXTURED_TOOLBAR:
            case BUTTON_POP_DOWN_TEXTURED:
            case BUTTON_POP_DOWN_TEXTURED_TOOLBAR:
                return true;
            default:
                return false;
        }
    }

    public static boolean supportsSmall(@NotNull PopupButtonWidget w)
    {
        switch (w)
        {
            case BUTTON_POP_DOWN_BEVEL:
            case BUTTON_POP_UP_BEVEL:
                return false;
            case BUTTON_POP_DOWN_GRADIENT:
            case BUTTON_POP_UP_GRADIENT:
                int version = AquaNativeRendering.getSystemRenderingVersion();
                return version >= macOS11;
            default:
                return true;
        }
    }

    public static boolean supportsLarge(@NotNull PopupButtonWidget w)
    {
        switch (w)
        {
            case BUTTON_POP_DOWN:
            case BUTTON_POP_UP:
            case BUTTON_POP_UP_TEXTURED:
            case BUTTON_POP_UP_TEXTURED_TOOLBAR:
            case BUTTON_POP_DOWN_TEXTURED:
            case BUTTON_POP_DOWN_TEXTURED_TOOLBAR:
            case BUTTON_POP_DOWN_RECESSED:
            case BUTTON_POP_UP_RECESSED:
                return true;
             default:
                return false;
        }
    }
}
