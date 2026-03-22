/*
 * Copyright (c) 2015-2025 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.aqua;

import org.jetbrains.annotations.NotNull;
import org.violetlib.jnr.aqua.AquaUIPainter.Size;
import org.violetlib.jnr.aqua.AquaUIPainter.UILayoutDirection;

/**
  A common base class for an editable or non-editable combo box layout configuration.
*/

public abstract class AbstractComboBoxLayoutConfiguration
  extends LayoutDirectionSensitiveLayoutConfigurationImpl
{
    public AbstractComboBoxLayoutConfiguration(@NotNull UILayoutDirection ld)
    {
        super(ld);
    }

    public abstract @NotNull Size getSize();

    public abstract boolean isCell();
}
