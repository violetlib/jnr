/*
 * Copyright (c) 2020-2025 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.aqua;

import org.jetbrains.annotations.NotNull;

/**
 * A configuration for a button-like control.
 */

public interface GenericButtonConfiguration
  extends Configuration
{
    @NotNull AquaUIPainter.State getState();

    boolean isTextured();

    @NotNull LayoutConfiguration getLayoutConfiguration();
}
