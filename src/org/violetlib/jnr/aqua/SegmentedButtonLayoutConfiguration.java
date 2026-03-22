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
import org.violetlib.jnr.aqua.AquaUIPainter.Position;
import org.violetlib.jnr.aqua.AquaUIPainter.SegmentedButtonWidget;
import org.violetlib.jnr.aqua.AquaUIPainter.Size;

import java.util.Objects;

import static org.violetlib.jnr.aqua.AquaUIPainter.SegmentedButtonWidget.*;
import static org.violetlib.jnr.aqua.AquaUIPainter.macOS11;
import static org.violetlib.jnr.aqua.AquaUIPainter.macOS26;

/**
  A layout configuration for a segmented button.
*/

public class SegmentedButtonLayoutConfiguration
  extends LayoutConfiguration
{
    private final @NotNull SegmentedButtonWidget bw;
    private final @NotNull Size size;
    private final @NotNull Position position;

    public SegmentedButtonLayoutConfiguration(@NotNull SegmentedButtonWidget bw,
                                              @NotNull Size size,
                                              @NotNull Position position)
    {
        int version = AquaNativeRendering.getSystemRenderingVersion();

        if (!AquaNativeRendering.isRaw()) {

            // Map unsupported styles to the closest equivalent.
            // Note: Rounded (BUTTON_SEGMENTED) is obsolete as an exclusive segmented control, but it is still used for
            // default toggle buttons and default select any segmented buttons.
            // The general rule is to avoid special toolbar and icon styles, as CoreUI does not handle them
            // properly. (NSView does not handle any textured styles properly.)

            if (version >= macOS11) {

                switch (bw) {
                    case BUTTON_TAB:
                    case BUTTON_SEGMENTED_SLIDER:
                    case BUTTON_SEGMENTED_SLIDER_TOOLBAR:
                        bw = BUTTON_SEGMENTED_SLIDER;
                        break;

                    case BUTTON_SEGMENTED_TOOLBAR:
                    case BUTTON_SEGMENTED_SCURVE:
                        bw = BUTTON_SEGMENTED_TEXTURED;
                        break;
                }

                // Textured toolbar is incorrectly rendered as separated on macOS 11 and 12.
                if (bw == BUTTON_SEGMENTED_TEXTURED_TOOLBAR && version < 130000) {
                    bw = BUTTON_SEGMENTED_TEXTURED_SEPARATED_TOOLBAR;
                }
            }

            // Textured separated toolbar is incorrectly rendered as non-separated on macOS 13, 14, 15.
            if (bw == BUTTON_SEGMENTED_TEXTURED_SEPARATED_TOOLBAR && version >= 130000) {
                bw = BUTTON_SEGMENTED_TEXTURED_TOOLBAR;
            }

            if (size == Size.EXTRA_LARGE && !isExtraLargeSizeSupported(version, bw)) {
                size = Size.LARGE;
            }

            if (size == Size.LARGE && !isLargeSizeSupported(version, bw)) {
                size = Size.REGULAR;
            }
        }

        this.bw = bw;
        this.size = size;
        this.position = position;
    }

    private static boolean isExtraLargeSizeSupported(int version, @NotNull SegmentedButtonWidget bw)
    {
        if (version < macOS26) {
            return false;
        }
        if (bw == BUTTON_SEGMENTED_SEPARATED) {
            return false;
        }
        return true;
    }

    private static boolean isLargeSizeSupported(int version, @NotNull SegmentedButtonWidget bw)
    {
        if (version < macOS11) {
            return false;
        }

        if (version >= macOS26 && bw.isTextured()) {
            return false;
        }

        if (bw == BUTTON_SEGMENTED_TEXTURED) {
            return false;
        }

        if (bw == BUTTON_SEGMENTED_TEXTURED_TOOLBAR) {
            return version >= 130000;
        }

        if (bw == BUTTON_SEGMENTED_TEXTURED_SEPARATED || bw == BUTTON_SEGMENTED_TEXTURED_SEPARATED_TOOLBAR) {
            return false;
        }

        if (bw == BUTTON_SEGMENTED_INSET || bw == BUTTON_SEGMENTED_SMALL_SQUARE) {
            return false;
        }

        // No renderer does large capsule or toolbar (obsolete styles)
        if (bw == BUTTON_SEGMENTED_SCURVE || bw == BUTTON_SEGMENTED_TOOLBAR) {
            return false;
        }

        return true;
    }

    @Override
    public @NotNull SegmentedButtonWidget getWidget()
    {
        return bw;
    }

    @Override
    public @NotNull Size getSize()
    {
        return size;
    }

    public @NotNull Position getPosition()
    {
        return position;
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
        SegmentedButtonLayoutConfiguration that = (SegmentedButtonLayoutConfiguration) o;
        return layoutEquals(that);
    }

    protected boolean layoutEquals(@NotNull SegmentedButtonLayoutConfiguration that)
    {
        return bw == that.bw && size == that.size && position == that.position;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(bw, size, position);
    }

    @Override
    public @NotNull String toString()
    {
        return bw + " " + size + " " + position;
    }
}
