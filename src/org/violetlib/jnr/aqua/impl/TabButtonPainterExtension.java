package org.violetlib.jnr.aqua.impl;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.violetlib.jnr.aqua.AquaUIPainter;
import org.violetlib.jnr.impl.Colors;
import org.violetlib.vappearances.VAppearance;

import java.awt.*;

/**
  Simulates the rendering of tabs in macOS 27+.
*/

public class TabButtonPainterExtension
  extends ButtonPainterExtensionBase
{
    public TabButtonPainterExtension(@NotNull AquaUIPainter.State state,
                                     @NotNull AquaUIPainter.ButtonState buttonState,
                                     @Nullable VAppearance appearance,
                                     @Nullable Shape outline
                                     )
    {
        super("tab", state, buttonState, appearance, outline,
          new ButtonColorSearchPath("tab", "glassButton", Colors.getColors(appearance), state, buttonState));
    }
}
