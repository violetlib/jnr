/*
 * Copyright (c) 2015-2020 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.aqua;

import org.jetbrains.annotations.*;

/**
  A layout configuration defines the parameters needed to determine layout information.
*/

public abstract class LayoutConfiguration
{
    protected LayoutConfiguration()
    {
    }

    public abstract @NotNull Object getWidget();
}
