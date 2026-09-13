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
