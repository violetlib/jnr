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
  A layout configuration for a list box.
*/

public class ListBoxLayoutConfiguration
  extends LayoutConfiguration
{
    @Override
    public @NotNull String toString()
    {
        return "List Box";
    }

    @Override
    public @NotNull Object getWidget()
    {
        return this;
    }
}
