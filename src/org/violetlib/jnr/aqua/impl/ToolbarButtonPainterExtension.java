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
import org.violetlib.jnr.aqua.ButtonConfiguration;
import org.violetlib.jnr.impl.Colors;
import org.violetlib.vappearances.VAppearance;

import java.awt.*;

/**
  Simulates the rendering of toolbar buttons in macOS 11+.
*/

public class ToolbarButtonPainterExtension
  extends ButtonPainterExtensionBase
{
    public ToolbarButtonPainterExtension(@NotNull ButtonConfiguration g,
                                         @Nullable VAppearance appearance,
                                         @Nullable Shape outline)
    {
        super("toolbar button", g.getState(), g.getButtonState(), appearance, outline,
          new ButtonColorSearchPath("toolbarButton", "glassButton", Colors.getColors(appearance), g.getState(), g.getButtonState()));
    }
}
