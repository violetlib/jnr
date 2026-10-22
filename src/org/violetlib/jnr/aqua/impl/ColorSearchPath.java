/*
 * Copyright (c) 2026 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.aqua.impl;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;

/**

*/

public interface ColorSearchPath
{
    enum Role { BACKGROUND, BORDER, INNER_BORDER }

    @Nullable Color getColor(@NotNull Role role);
}
