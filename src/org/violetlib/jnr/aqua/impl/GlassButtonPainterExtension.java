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
import org.violetlib.jnr.aqua.ButtonConfiguration;
import org.violetlib.jnr.impl.Colors;
import org.violetlib.vappearances.VAppearance;

import java.awt.*;

import static org.violetlib.jnr.aqua.impl.ColorSearchPath.Role.*;

/**
  Simulates the rendering of glass buttons in macOS 26+.
*/

public class GlassButtonPainterExtension
  extends ButtonPainterExtensionBase
{
    public GlassButtonPainterExtension(@NotNull ButtonConfiguration g,
                                       @Nullable VAppearance appearance,
                                       @Nullable Shape outline)
    {
        super("glass button", g.getState(), g.getButtonState(), appearance, outline, new MyColorSearchPath(appearance, g));
    }

    private final static class MyColorSearchPath
      implements ColorSearchPath
    {
        private final @NotNull ColorSearchPath base;
        private final @Nullable VAppearance appearance;
        private final @NotNull AquaUIPainter.State state;
        private final @NotNull AquaUIPainter.ButtonState buttonState;

        public MyColorSearchPath(@Nullable VAppearance appearance, @NotNull ButtonConfiguration g)
        {
            this.appearance = appearance;
            this.state = g.getState();
            this.buttonState = g.getButtonState();
            base = new ButtonColorSearchPath("glassButton", null, Colors.getColors(appearance), state, buttonState);
        }

        @Override
        public @Nullable Color getColor(@NotNull Role role)
        {
            if (role == BACKGROUND && buttonState == AquaUIPainter.ButtonState.ON) {
                if (appearance != null && state == AquaUIPainter.State.ACTIVE) {
                    Color c = appearance.getColors().get("controlAccent");
                    if (c != null) {
                        return c;
                    }
                    // emergency backup
                    return new Color(128, 128, 128);
                }
            }
            return base.getColor(role);
        }
    }
}
