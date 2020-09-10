/*
 * Copyright (c) 2020 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.impl;

import org.violetlib.jnr.aqua.SliderConfiguration;

import org.jetbrains.annotations.*;

/**

*/

public interface SliderTickMarkRendererFactory
{
    @NotNull Renderer getSliderTickMarkRenderer(@NotNull SliderConfiguration g, boolean isTinted);
}
