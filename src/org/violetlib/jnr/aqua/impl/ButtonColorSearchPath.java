/*
 * Copyright (c) 2025-2026 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.aqua.impl;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.violetlib.jnr.aqua.AquaUIPainter;
import org.violetlib.jnr.impl.Colors;

import java.awt.*;

import static org.violetlib.jnr.aqua.AquaUIPainter.State.*;

/**

*/

public class ButtonColorSearchPath
{
    private final @NotNull Colors colors;
    private final @NotNull String root;
    private final @Nullable String root2;
    private final @NotNull AquaUIPainter.State state;
    private final @Nullable AquaUIPainter.ButtonState buttonState;

    public enum Role { BACKGROUND, BORDER, INNER_BORDER }

    public ButtonColorSearchPath(@NotNull String root,
                                 @Nullable String root2,
                                 @NotNull Colors colors,
                                 @NotNull AquaUIPainter.State state)
    {
        this(root, root2, colors, state, null);
    }

    public ButtonColorSearchPath(@NotNull String root,
                                 @Nullable String root2,
                                 @NotNull Colors colors,
                                 @NotNull AquaUIPainter.State state,
                                 @Nullable AquaUIPainter.ButtonState buttonState)
    {
        this.root = root;
        this.root2 = root2;
        this.colors = colors;
        this.state = state;
        this.buttonState = buttonState;
    }

    public @Nullable Color getColor(@NotNull Role role)
    {
        String roleName = roleText(role);
        if (roleName == null) {
            return null;
        }

        Color color = rootSearch(root, roleName);
        if (color != null || root2 == null) {
            return color;
        }
        return rootSearch(root2, roleName);
    }

    private @Nullable Color rootSearch(@NotNull String root, @NotNull String roleName)
    {
        String base = root + roleName;
        if (buttonState == AquaUIPainter.ButtonState.ON || buttonState == AquaUIPainter.ButtonState.MIXED) {
            Color c = stateSearch(base + "_selected");
            if (c != null) {
                return c;
            }
        }
        return stateSearch(base);
    }

    private @Nullable String roleText(@NotNull Role role)
    {
        switch (role) {
            case BACKGROUND: return "Background";
            case BORDER: return "Border";
            case INNER_BORDER: return "InnerBorder";
        }
        return null;
    }

    private @Nullable Color stateSearch(@NotNull String base)
    {
        Color c;
        if (state == DISABLED_INACTIVE) {
            c = colors.getOptional(base + "_disabled_inactive");
            if (c != null) {
                return c;
            }
        }
        if (state == DISABLED_INACTIVE || state == DISABLED) {
            c = colors.getOptional(base + "_disabled");
            if (c != null) {
                return c;
            }
        }
        if (state == INACTIVE) {
            c = colors.getOptional(base + "_inactive");
            if (c != null) {
                return c;
            }
        }
        if (state == ROLLOVER) {
            c = colors.getOptional(base + "_rollover");
            if (c != null) {
                return c;
            }
        }
        if (state == PRESSED) {
            c = colors.getOptional(base + "_pressed");
            if (c != null) {
                return c;
            }
        }
        return colors.getOptional(base);
    }
}
