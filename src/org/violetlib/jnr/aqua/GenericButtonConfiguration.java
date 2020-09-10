/*
 * Copyright (c) 2020 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.aqua;

import org.jetbrains.annotations.*;

/**

*/

public interface GenericButtonConfiguration
  extends Configuration
{
    @NotNull AquaUIPainter.State getState();

    boolean isTextured();
}
