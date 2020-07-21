/*
 * Copyright (c) 2015-2020 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.aqua;

import java.util.Objects;

import org.violetlib.jnr.aqua.AquaUIPainter.Direction;
import org.violetlib.jnr.aqua.AquaUIPainter.Position;
import org.violetlib.jnr.aqua.AquaUIPainter.SegmentedButtonWidget;
import org.violetlib.jnr.aqua.AquaUIPainter.Size;
import org.violetlib.jnr.aqua.AquaUIPainter.State;
import org.violetlib.jnr.impl.JNRPlatformUtils;

import org.jetbrains.annotations.*;

/**
  A configuration for a segmented button.

  <p>
  The painting model for segmented buttons follows the Java model. The buttons are abutted with no overlap. For each
  divider between two buttons, the client code decides which button owns the divider and sets the leftDividerState
  and rightDividerState parameters appropriately. The divider is painted in the space allocated to the button that
  owns the divider.

  <p>
  Because Java normally does not know which buttons are part of a segmented control, the normal policy is for the button
  on the left to own the divider. The divider state is set to selected if that button is selected.

  <p>
  Code that knows about the buttons in a segmented control can get slightly better results by having the selected button
  own the dividers on either side, buttons to the left of the selected button own the dividers to their left, and
  buttons to the right of the selected button own the dividers to their right.

  <p>
  We currently assume that writing direction does not affect the appearance of the buttons and dividers, so that left
  always means left or top and right always means right or bottom.
*/

public class SegmentedButtonConfiguration
  extends SegmentedButtonLayoutConfiguration
  implements GenericButtonConfiguration
{
    private final @NotNull State state;
    private final boolean isSelected;
    private final boolean isFocused;
    private final @NotNull Direction d;  // the direction that the "top" of the button faces
    private final @NotNull DividerState leftDividerState;
    private final @NotNull DividerState rightDividerState;

    /**
      The display configuration of a divider between two segments in a segmented control.
    */

    public enum DividerState
    {
        NONE,
        ORDINARY,
        SELECTED
    }

    public SegmentedButtonConfiguration(@NotNull SegmentedButtonWidget bw,
                                        @NotNull Size size,
                                        @NotNull State state,
                                        boolean isSelected,
                                        boolean isFocused,
                                        @NotNull Direction d,
                                        @NotNull Position position,
                                        @NotNull DividerState leftDividerState,
                                        @NotNull DividerState rightDividerState)
    {
        super(bw, size, position);

        // Many buttons do not alter their appearance when inactive. In many cases using CoreUI, the way to accomplish
        // that is to change the inactive state to the active equivalent. Replacing the state also simplifies selection
        // of a text color and improves caching.

        if (state.isInactive() && !isSensitiveToInactiveState(bw)) {
            state = state.toActive();
        }

        this.state = state;
        this.isSelected = isSelected;
        this.isFocused = isFocused;
        this.d = d;
        this.leftDividerState = leftDividerState;
        this.rightDividerState = rightDividerState;
    }

    public SegmentedButtonConfiguration(@NotNull SegmentedButtonLayoutConfiguration g,
                                        @NotNull State state,
                                        boolean isSelected,
                                        boolean isFocused,
                                        @NotNull Direction d,
                                        @NotNull DividerState leftDividerState,
                                        @NotNull DividerState rightDividerState)
    {
        this(g.getWidget(), g.getSize(), state, isSelected, isFocused, d, g.getPosition(), leftDividerState, rightDividerState);
    }

    public @NotNull SegmentedButtonConfiguration withWidget(@NotNull SegmentedButtonWidget widget)
    {
        return new SegmentedButtonConfiguration(widget,
          getSize(), state, isSelected, isFocused, d, getPosition(), leftDividerState, rightDividerState);
    }

    @Override
    public @NotNull State getState()
    {
        return state;
    }

    @Override
    public boolean isTextured()
    {
        SegmentedButtonWidget w = getWidget();
        return w.isTextured();
    }

    public boolean isSelected()
    {
        return isSelected;
    }

    public boolean isFocused()
    {
        return isFocused;
    }

    public @NotNull Direction getDirection()
    {
        return d;
    }

    public @NotNull DividerState getLeftDividerState()
    {
        return leftDividerState;
    }

    public @NotNull DividerState getRightDividerState()
    {
        return rightDividerState;
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
        SegmentedButtonConfiguration that = (SegmentedButtonConfiguration) o;
        return state == that.state
                 && isSelected == that.isSelected
                 && isFocused == that.isFocused
                 && d == that.d
                 && leftDividerState == that.leftDividerState
                 && rightDividerState == that.rightDividerState;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(super.hashCode(), state, isSelected, isFocused, d, leftDividerState, rightDividerState);
    }

    @Override
    public @NotNull String toString()
    {
        String fs = isFocused ? " focused" : "";
        String ss = isSelected ? "S" : "-";
        String ls = leftDividerState == DividerState.NONE ? "" : leftDividerState == DividerState.ORDINARY ? "<" : "[";
        String rs = rightDividerState == DividerState.NONE ? "" : rightDividerState == DividerState.ORDINARY ? ">" : "]";
        return super.toString() + " " + d + " " + state + fs + " " + ls + ss + rs;
    }

    private static boolean isSensitiveToInactiveState(@NotNull SegmentedButtonWidget bw)
    {
        int platformVersion = JNRPlatformUtils.getPlatformVersion();

        // Starting with macOS 10.15, textured buttons are not sensitive
        if (platformVersion >= 101500 && bw.isTextured()) {
            return false;
        }

        // Starting with macOS 11, tab buttons are not sensitive
        if (platformVersion >= 101600 && bw == SegmentedButtonWidget.BUTTON_TAB) {
            return false;
        }

        return true;
    }
}
