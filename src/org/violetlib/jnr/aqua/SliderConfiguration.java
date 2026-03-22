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
import org.violetlib.jnr.aqua.AquaUIPainter.SliderWidget;
import org.violetlib.jnr.aqua.AquaUIPainter.State;
import org.violetlib.jnr.aqua.AquaUIPainter.TickMarkPosition;
import org.violetlib.jnr.impl.JNRUtils;

import java.util.Objects;

import static org.violetlib.jnr.aqua.AquaUIPainter.macOS26;

/**
  A configuration for a slider.
*/

public class SliderConfiguration
  extends SliderLayoutConfiguration
  implements Configuration
{
    private final @NotNull State state;
    private final boolean isFocused;
    private final double value;
    private final double neutralValue;

    public static final double NO_NEUTRAL_VALUE = Double.MAX_VALUE;

    public SliderConfiguration(@NotNull SliderWidget sw,
                               @NotNull Size sz,
                               @NotNull State state,
                               boolean isFocused,
                               double value,
                               int numberOfTickMarks,
                               @NotNull TickMarkPosition position
    )
    {
        this(sw, sz, state, isFocused, value, numberOfTickMarks, position, NO_NEUTRAL_VALUE);
    }

    public SliderConfiguration(@NotNull SliderWidget sw,
                               @NotNull Size sz,
                               @NotNull State state,
                               boolean isFocused,
                               double value,
                               int numberOfTickMarks,
                               @NotNull TickMarkPosition position,
                               double neutralValue
    )
    {
        super(sw, sz, numberOfTickMarks, position);

        if (!AquaNativeRendering.isRaw()) {
            state = fixState(state);
        }

        this.state = state;
        this.isFocused = isFocused;
        this.value = value;
        this.neutralValue = fixNeutralValue(neutralValue);
    }

    private static @NotNull State fixState(@NotNull State state)
    {
        // The following workaround causes problems because the simulation of inactive window is not working, which
        // means that disabled sliders might paint as enabled.

//        if ((state == State.DISABLED_INACTIVE || state == State.DISABLED) && JNRPlatformUtils.getPlatformVersion() >= macOS26) {
//            // avoid an apparent painting bug (as of 26.0)
//            return State.INACTIVE;
//        }
        return state;
    }

    private static double fixNeutralValue(double neutralValue)
    {
        if (neutralValue != NO_NEUTRAL_VALUE && AquaNativeRendering.getSystemRenderingVersion() < macOS26) {
            return NO_NEUTRAL_VALUE;
        }
        return neutralValue;
    }

    public @NotNull State getState()
    {
        return state;
    }

    public boolean isFocused()
    {
        return isFocused;
    }

    public double getValue()
    {
        return value;
    }

    public boolean isNeutralValueDefined()
    {
        return neutralValue != NO_NEUTRAL_VALUE;
    }

    public double getNeutralValue()
      throws IllegalStateException
    {
        if (neutralValue == NO_NEUTRAL_VALUE) {
            throw new IllegalStateException("No neutral value is defined");
        }
        return neutralValue;
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
        SliderConfiguration that = (SliderConfiguration) o;
        return state == that.state && isFocused == that.isFocused && value == that.value && neutralValue != that.neutralValue;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(super.hashCode(), state, isFocused, value, neutralValue);
    }

    @Override
    public @NotNull String toString()
    {
        String fs = isFocused ? " focused" : "";
        String s = super.toString() + " " + state + fs + " " + JNRUtils.format2(value);
        if (isNeutralValueDefined()) {
            s = s + " neutral=" + JNRUtils.format2(neutralValue);
        }
        return s;
    }
}
