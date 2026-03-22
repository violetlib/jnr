/*
 * Copyright (c) 2025 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.aqua;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
  A convenient base class for a layout configuration for a component whose layout may depend upon the layout direction (LTR or RTL).
*/

public abstract class LayoutDirectionSensitiveLayoutConfigurationImpl
  extends LayoutConfiguration
  implements LayoutDirectionSensitiveConfiguration
{
    private final @NotNull AquaUIPainter.UILayoutDirection ld;

    protected LayoutDirectionSensitiveLayoutConfigurationImpl(@NotNull AquaUIPainter.UILayoutDirection ld)
    {
        this.ld = ld;
    }

    @Override
    public @NotNull AquaUIPainter.UILayoutDirection getLayoutDirection()
    {
        return ld;
    }

    @Override
    public boolean isLeftToRight()
    {
        return ld == AquaUIPainter.UILayoutDirection.LEFT_TO_RIGHT;
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
        LayoutDirectionSensitiveLayoutConfigurationImpl that = (LayoutDirectionSensitiveLayoutConfigurationImpl) o;
        return ld == that.ld;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(ld);
    }
}
