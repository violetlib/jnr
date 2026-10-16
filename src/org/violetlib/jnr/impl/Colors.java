/*
 * Copyright (c) 2018-2026 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.impl;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.violetlib.vappearances.VAppearance;
import org.violetlib.vappearances.VAppearances;

import java.awt.*;
import java.io.IOException;
import java.util.Map;

/**
  A collection of custom and system colors for a specific appearance.
*/

public class Colors
{
    /**
      Return colors for the specified appearance.
      @param appearance The appearance. This parameter should not be null. If it is, the Aqua (light) appearance is
      used.
    */

    public static @NotNull Colors getColors(@Nullable VAppearance appearance)
    {
        if (appearance == null) {
            String name = VAppearances.aquaAppearance;
            try {
                appearance = VAppearances.getAppearance(name);
            } catch (IOException e) {
                throw new UnsupportedOperationException("Missing appearance: " + name);
            }
        }
        CustomColors cc = CustomColors.getColors(appearance);
        return new Colors(appearance, cc);
    }

    private final @NotNull VAppearance appearance;
    private final @NotNull CustomColors customColors;

    private Colors(@NotNull VAppearance appearance, @NotNull CustomColors customColors)
    {
        this.appearance = appearance;
        this.customColors = customColors;
    }

    public @NotNull Color get(@NotNull String name)
    {
        Color c = getOptional(name);
        if (c != null) {
            return c;
        }
        throw new UnsupportedOperationException("Undefined color: " + name);
    }

    public @Nullable Color getOptional(@NotNull String name)
    {
        return customColors.getOptional(name, appearance);
    }

    @NotNull Map<String,Color> getCustomColors()
    {
        return customColors.getCustomColors();
    }
}
