/*
 * Copyright (c) 2015-2020 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.aqua;

import java.util.Objects;

import org.violetlib.jnr.aqua.AquaUIPainter.Size;
import org.violetlib.jnr.aqua.AquaUIPainter.SliderWidget;
import org.violetlib.jnr.aqua.AquaUIPainter.TickMarkPosition;
import org.violetlib.jnr.impl.JNRPlatformUtils;

import org.jetbrains.annotations.*;

/**
  A layout configuration for a slider.
*/

public class SliderLayoutConfiguration
  extends LayoutConfiguration
{
    private final @NotNull SliderWidget sw;
    private final @NotNull Size size;
    private final int numberOfTickMarks;
    private final @NotNull TickMarkPosition position;

    public SliderLayoutConfiguration(@NotNull SliderWidget sw,
                                     @NotNull Size size,
                                     int numberOfTickMarks,
                                     @NotNull TickMarkPosition position)
    {
        // Ensure that the tick mark position is compatible with the type of slider.
        if (sw == SliderWidget.SLIDER_HORIZONTAL || sw == SliderWidget.SLIDER_HORIZONTAL_RIGHT_TO_LEFT) {
            if (position == TickMarkPosition.LEFT || position == TickMarkPosition.RIGHT) {
                position = TickMarkPosition.BELOW;
            }
        } else if (sw == SliderWidget.SLIDER_VERTICAL || sw == SliderWidget.SLIDER_UPSIDE_DOWN) {
            if (position == TickMarkPosition.ABOVE || position == TickMarkPosition.BELOW) {
                position = TickMarkPosition.LEFT;
            }
        }

        // Mini circular sliders are not supported
        // Mini linear sliders are not supported before 10.14 (or so)
        int platformVersion = JNRPlatformUtils.getPlatformVersion();
        if (size == Size.MINI && (sw == SliderWidget.SLIDER_CIRCULAR || platformVersion < 101400)) {
            size = Size.SMALL;
        }

        this.sw = sw;
        this.size = size;
        this.numberOfTickMarks = numberOfTickMarks;
        this.position = position;
    }

    protected SliderLayoutConfiguration(@NotNull SliderLayoutConfiguration g)
    {
        this.sw = g.getWidget();
        this.size = g.getSize();
        this.numberOfTickMarks = g.getNumberOfTickMarks();
        this.position = g.getTickMarkPosition();
    }

    @Override
    public @NotNull SliderWidget getWidget()
    {
        return sw;
    }

    public @NotNull Size getSize()
    {
        return size;
    }

    public int getNumberOfTickMarks()
    {
        return numberOfTickMarks;
    }

    public boolean hasTickMarks()
    {
        return numberOfTickMarks > 0;
    }

    public @NotNull TickMarkPosition getTickMarkPosition()
    {
        return position;
    }

    public boolean isVertical()
    {
        return sw == SliderWidget.SLIDER_VERTICAL || sw == SliderWidget.SLIDER_UPSIDE_DOWN;
    }

    public boolean isHorizontal()
    {
        return sw == SliderWidget.SLIDER_HORIZONTAL || sw == SliderWidget.SLIDER_HORIZONTAL_RIGHT_TO_LEFT;
    }

    public boolean isLinear()
    {
        return sw != SliderWidget.SLIDER_CIRCULAR;
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
        SliderLayoutConfiguration that = (SliderLayoutConfiguration) o;
        return sw == that.sw && size == that.size && numberOfTickMarks == that.numberOfTickMarks && position == that.position;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(sw, size, numberOfTickMarks, position);
    }

    @Override
    public @NotNull String toString()
    {
        String ts = "";
        if (numberOfTickMarks > 0) {
            ts = " " + position;
        }
        return sw + " " + size + ts;
    }
}
