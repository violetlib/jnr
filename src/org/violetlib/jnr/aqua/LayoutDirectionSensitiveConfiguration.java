/*
 * Copyright (c) 2025 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.aqua;

import org.jetbrains.annotations.NotNull;

/**
  A configuration for a component whose layout or rendering may depend upon the layout direction (LTR or RTL).
*/

public interface LayoutDirectionSensitiveConfiguration
{
    @NotNull AquaUIPainter.UILayoutDirection getLayoutDirection();

    boolean isLeftToRight();
}
