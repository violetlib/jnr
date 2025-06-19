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
import org.violetlib.jnr.aqua.AquaUIPainter.Position;
import org.violetlib.jnr.aqua.AquaUIPainter.SegmentedButtonWidget;
import org.violetlib.jnr.aqua.AquaUIPainter.Size;
import org.violetlib.jnr.impl.JNRPlatformUtils;

import java.util.Objects;

import static org.violetlib.jnr.aqua.AquaUIPainter.SegmentedButtonWidget.*;

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
        int platformVersion = JNRPlatformUtils.getPlatformVersion();

        // Map unsupported styles to the closest equivalent.
        // Note: Rounded (BUTTON_SEGMENTED) is obsolete as an exclusive segmented control, but it is still used for
        // default toggle buttons and default select any segmented buttons.

        if (platformVersion >= 101600) {
            if (platformVersion >= 150000) {
                switch (bw) {
                    case BUTTON_TAB:
                    case BUTTON_SEGMENTED_SLIDER:
                    case BUTTON_SEGMENTED_SLIDER_TOOLBAR:
                    case BUTTON_SEGMENTED_SLIDER_TOOLBAR_ICONS:
                        bw = BUTTON_SEGMENTED_SLIDER;
                        break;

                    case BUTTON_SEGMENTED_TEXTURED:
                    case BUTTON_SEGMENTED_TEXTURED_TOOLBAR:
                    case BUTTON_SEGMENTED_TEXTURED_TOOLBAR_ICONS:
                        bw = BUTTON_SEGMENTED_SCURVE;
                        break;

                    case BUTTON_SEGMENTED_TEXTURED_SEPARATED:
                    case BUTTON_SEGMENTED_TEXTURED_SEPARATED_TOOLBAR:
                    case BUTTON_SEGMENTED_TEXTURED_SEPARATED_TOOLBAR_ICONS:
                        bw = BUTTON_SEGMENTED_SEPARATED;
                        break;
                }
            } else {
                switch (bw) {
                    case BUTTON_TAB:
                    case BUTTON_SEGMENTED_TEXTURED:
                        bw = BUTTON_SEGMENTED_SLIDER;
                        break;
                    case BUTTON_SEGMENTED_TEXTURED_SEPARATED:
                        bw = BUTTON_SEGMENTED_SEPARATED;
                        break;
                    case BUTTON_SEGMENTED_TOOLBAR:
                        bw = BUTTON_SEGMENTED_SCURVE;
                }
            }
        }

        if (size == Size.LARGE) {
            if (platformVersion < 101600) {
                size = Size.REGULAR;
            } else if (platformVersion < 150000){
                // No renderer does large textured and textured separated, but CoreUI can do the toolbar equivalent.
                bw = bw.toToolbarWidget();
            }
        }

        this.bw = bw;
        this.size = size;
        this.position = position;
    }

    @Override
    public @NotNull SegmentedButtonWidget getWidget()
    {
        return bw;
    }

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
