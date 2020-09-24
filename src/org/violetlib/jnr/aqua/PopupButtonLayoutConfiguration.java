/*
 * Copyright (c) 2015-2016 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.aqua;

import java.util.Objects;

import org.violetlib.jnr.aqua.AquaUIPainter.PopupButtonWidget;
import org.violetlib.jnr.aqua.AquaUIPainter.Size;
import org.violetlib.jnr.aqua.AquaUIPainter.UILayoutDirection;
import org.violetlib.jnr.impl.JNRPlatformUtils;

import org.jetbrains.annotations.*;

/**
  A layout configuration for a pop up button.
*/

public class PopupButtonLayoutConfiguration
  extends AbstractComboBoxLayoutConfiguration
{
    private final @NotNull PopupButtonWidget bw;
    private final @NotNull Size size;
    private final @NotNull UILayoutDirection ld;

    public PopupButtonLayoutConfiguration(@NotNull PopupButtonWidget bw,
                                          @NotNull Size size,
                                          @NotNull UILayoutDirection ld)
    {
        // Most popup styles do not work in a mini size, most likely because Core UI does not support mini arrows.
        // Some styles have a fixed height and thus do not support small, either.

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
            int platformVersion = JNRPlatformUtils.getPlatformVersion();
            if (platformVersion < 101600 || !supportsLarge(bw)) {
                size = Size.REGULAR;
            }
        }

        this.bw = bw;
        this.size = size;
        this.ld = ld;
    }

    public @NotNull PopupButtonWidget getPopupButtonWidget()
    {
        return bw;
    }

    public @NotNull Size getSize()
    {
        return size;
    }

    public @NotNull UILayoutDirection getLayoutDirection()
    {
        return ld;
    }

    public boolean isCell()
    {
        return bw == PopupButtonWidget.BUTTON_POP_UP_CELL || bw == PopupButtonWidget.BUTTON_POP_DOWN_CELL;
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
        PopupButtonLayoutConfiguration that = (PopupButtonLayoutConfiguration) o;
        return bw == that.bw && size == that.size && ld == that.ld;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(bw, size, ld);
    }

    @Override
    public @NotNull String toString()
    {
        String lds = ld == UILayoutDirection.RIGHT_TO_LEFT ? " RTL" : "";
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
            case BUTTON_POP_DOWN_GRADIENT:
            case BUTTON_POP_UP_GRADIENT:
                return false;
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
                return true;
            default:
                return false;
        }
    }
}
