/*
 * Copyright (c) 2015-2025 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.impl;

import org.jetbrains.annotations.NotNull;

/**
  A render that does nothing.
*/

public class EmptyRenderer
  implements BasicRenderer
{
    @Override
    public void render(int @NotNull [] data, int rw, int rh, float w, float h)
    {
    }
}
