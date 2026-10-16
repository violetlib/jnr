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
import org.violetlib.jnr.aqua.AquaNativeRendering;
import org.violetlib.vappearances.VAppearance;

import java.awt.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.violetlib.jnr.aqua.AquaUIPainter.macOS11;
import static org.violetlib.jnr.aqua.AquaUIPainter.macOS26;

/**
  Definitions of custom colors with appearance-specific values.
*/

public class CustomColors
{
    private static final @NotNull CustomColors lightColors;
    private static final @NotNull CustomColors darkColors;
    private static final @NotNull CustomColors highContrastLightColors;
    private static final @NotNull CustomColors highContrastDarkColors;

    public static @NotNull CustomColors getColors(@Nullable VAppearance appearance)
    {
        if (appearance != null) {
            if (appearance.isDark()) {
                if (appearance.isHighContrast()) {
                    return highContrastDarkColors;
                } else {
                    return darkColors;
                }
            } else {
                if (appearance.isHighContrast()) {
                    return highContrastLightColors;
                } else {
                    return lightColors;
                }
            }
        } else {
            return lightColors;
        }
    }

    public static @NotNull CustomColors getColors(boolean isDark, boolean isHighContrast)
    {
        if (isDark) {
            return isHighContrast ? highContrastDarkColors : darkColors;
        } else {
            return isHighContrast ? highContrastLightColors : lightColors;
        }
    }

    private final @NotNull Map<String,Color> namedColors;
    private final @NotNull Map<String,String> systemColorReferences;

    private CustomColors(@NotNull Map<String,Color> namedColors, @NotNull Map<String,String> systemColorReferences)
    {
        this.namedColors = Collections.unmodifiableMap(namedColors);
        this.systemColorReferences = Collections.unmodifiableMap(systemColorReferences);
    }

    public @NotNull Color get(@NotNull String name)
    {
        Color c = getOptional(name);
        if (c != null) {
            return c;
        }
        throw new UnsupportedOperationException("Missing color: " + name);
    }

    /**
      Return the custom color for the specified color name.
      @param name The color name.
      @return the corresponding custom color, or null if the color name is not defined.
    */

    public @Nullable Color getOptional(@NotNull String name)
    {
        return getOptional(name, null);
    }

    /**
      Return the custom color for the specified color name. If an appearance is provided, it is used to
      identify system colors.
      @param name The color name.
      @return the corresponding custom color or system color, or null if the color name is not defined.
    */

    public @Nullable Color getOptional(@NotNull String name, @Nullable VAppearance appearance)
    {
        Color c = namedColors.get(name);
        if (c != null) {
            return c;
        }
        if (appearance != null) {
            String systemColorName = systemColorReferences.get(name);
            if (systemColorName != null) {
                return appearance.getColors().get(systemColorName);
            }
        }
        return null;
    }

    public static boolean isClear(@NotNull Color c)
    {
        return c.getAlpha() == 0;
    }

    public @NotNull Map<String,Color> getCustomColors()
    {
        return namedColors;
    }

    static {
        int version = AquaNativeRendering.getSystemRenderingVersion();

        Builder lightColorsBuilder;
        Builder darkColorsBuilder;

        // light mode colors
        {
            Builder b = new Builder();
            b.add("overlayScrollTrack", 249, 185);
            b.add("overlayScrollTrackBorder", 194, 85);
            b.add("overlayThumb", 20, 140);
            b.add("overlayThumb_rollover", 62, 170);
            b.add("legacyScrollBarThumb", 0, 58);
            b.add("legacyScrollBarThumb_rollover", 0, 128);
            b.add("legacyScrollBarTrack", 0, 5);  // was 0/10 — is that needed for some release?
            b.add("legacyScrollBarInnerBorder", 231);
            b.add("legacyScrollBarOuterBorder", 237);
            if (version >= 270000) {
                b.add("sidebarThumb", 0, 150);
                b.add("sidebarThumbRollover", 0, 150);
                b.add("sidebarScrollBarTrack", 0, 72);
            } else if (version >= macOS26) {
                b.add("sidebarThumb", 0, 120);
                b.add("sidebarThumbRollover", 0, 120);
                b.add("sidebarScrollBarTrack", 0, 8);
            } else {
                b.add("sidebarThumb", 0, 115);
                b.add("sidebarThumbRollover", 0, 115);
                b.add("sidebarScrollBarTrack", 0, 12);
            }
            b.add("comboBoxButton", 210);
            b.add("comboBoxArrow", 0, 176);
            b.add("titleBarCloseButton", 255, 95, 87);
            b.add("titleBarMinimizeButton", 255, 192, 47);
            b.add("titleBarResizeButton", 41, 204, 66);
            b.add("titleBarInactiveButton", 255, 89);
            b.add("searchFieldIcon", 0, 155);
            b.add("searchFieldIcon_inactive", 0, 72);
            b.add("linearSliderTick", 10, 110);
            b.add("circularSliderInterior", 255);
            b.add("circularSliderBorder", 32, 48);
            b.add("circularSliderDimple", 75);
            b.add("circularSliderDimpleTop", 105);
            b.add("circularSliderTick", 10, 110);
            b.add("thickDividerDimple", 220);
            b.add("thickDividerDimpleBorder", 191);
            b.add("tableHeaderDivider", 0, 24);
            b.add("tableHeaderArrow", 0, 92);

            b.add("texturedButtonBorder", 0, 20);
            b.add("texturedButtonBorder_inactive", 0, 10);

            {
                int base = 0;
                b.add("texturedButtonBackground_rollover", base, 36);
                b.add("texturedButtonBackground_pressed", base, 48);
                b.add("texturedButtonBackground_selected", base, 20);
                b.add("texturedButtonBackground_selected_pressed", base, 64);
                b.add("texturedButtonBackground_selected_rollover", base, 48);
            }

            b.add("glassButtonBackground_disabled", "glassButtonBackground");

            b.add("glassButtonBackground_selected_disabled_inactive", "glassButtonBackground_selected_inactive");
            if (version >= 270000) {
                b.add("glassButtonBackground", 250, 97);
                b.add("glassButtonBackground_inactive", 230, 130);
                b.add("glassButtonBackground_rollover", 210, 60);
                b.add("glassButtonBackground_pressed", 210, 96);
                b.add("glassButtonBackground_selected", "controlAccent");
                b.add("glassButtonBackground_selected_rollover", "controlAccent_rollover");
                b.add("glassButtonBackground_selected_pressed", "controlAccent_pressed");
                b.add("glassButtonBackground_selected_inactive", 230, 120);
            } else {
                b.add("glassButtonBackground", 253, 200);
                b.add("glassButtonBackground_inactive", 245, 200);
                b.add("glassButtonBackground_pressed", 240, 200);
                b.add("glassButtonBackground_rollover", 245, 200);
                b.add("glassButtonBackground_selected", "controlAccent");
                b.add("glassButtonBackground_selected_rollover", "controlAccent_rollover");
                b.add("glassButtonBackground_selected_pressed", "controlAccent_pressed");
                b.add("glassButtonBackground_selected_inactive", 230, 165);
            }

            if (version >= macOS11 && version < 260200) {
                b.addClear("toolbarButtonBackground");
                b.add("toolbarButtonBackground_rollover", 0, 16);
                b.add("toolbarButtonBackground_selected_inactive", 234);
            }

            if (version >= 270000) {
                b.add("tabBackground_selected", 249);
                b.add("tabBackground_selected_inactive", 241);
                b.add("tabControlBackground", 238);
                b.add("tabBorder_selected", 204);
                b.add("glassButtonBorder", 238);
                b.add("glassButtonBorder_disabled", 238);
                b.add("glassButtonBorder_inactive", 221);
                b.add("glassButtonBorder_disabled_inactive", 221);
            } else {
                b.add("glassButtonBorder", 244);  // should be 255, but visible only because of a surrounding cloud
                b.add("glassButtonBorder_disabled", 244);
                b.addClear("glassButtonBorder_inactive");
                b.addClear("glassButtonBorder_disabled_inactive");
            }

            b.add("textFieldBorder", 0, 64);
            b.add("glassTextFieldBorder", 0, 32);  // should be 0/14, but that is too light
            b.add("glassTextFieldBackground", 255, 130);

            lightColors = b.getColors();
            lightColorsBuilder = b;
        }

        // light mode high contrast colors
        {
            Builder b = new Builder(lightColorsBuilder);
            b.add("overlayScrollTrack", 249);
            b.add("overlayScrollTrackBorder", 202);
            b.add("overlayThumb", 0, 184);
            b.add("overlayThumb_rollover", 87);
            b.add("legacyScrollBarThumb", 0, 108);
            b.add("legacyScrollBarThumb_rollover", 0, 164);
            b.add("legacyScrollBarTrack", 0, 5);  // was 0/35 — is that needed for some release?
            b.add("legacyScrollBarInnerBorder", 195);
            b.add("legacyScrollBarOuterBorder", 200);
            if (version >= 270000) {
                b.add("sidebarThumb", 0, 132);
                b.add("sidebarThumbRollover", 0, 132);
                b.add("sidebarScrollBarTrack", 0, 56);
            } else if (version >= macOS26) {
                b.add("sidebarThumb", 0, 192);
                b.add("sidebarThumbRollover", 0, 192);
                b.add("sidebarScrollBarTrack", 0, 40);
            } else {
                b.add("sidebarThumb", 185);
                b.add("sidebarThumbRollover", 185);
                b.add("sidebarScrollBarTrack", 0, 38);
            }
            b.add("titleBarCloseButton", 255, 45, 64);
            b.add("titleBarMinimizeButton", 246, 177, 1);
            b.add("titleBarResizeButton", 5, 161, 27);
            b.add("searchFieldIcon", 0, 177);
            b.add("searchFieldIcon_inactive", 0, 100);
            b.add("circularSliderBorder", 12, 180);
            b.add("tableHeaderDivider", 0, 48);
            b.add("tableHeaderArrow", 0, 140);

            b.add("texturedButtonBorder", 0, 180);
            b.add("texturedButtonBorder_inactive", 0, 120);

            b.add("glassButtonBackground_disabled", "glassButtonBackground");

            if (version >= 270000) {
                b.add("glassButtonBackground", 250, 186);
                b.add("glassButtonBackground_inactive", 244, 190);
                b.add("glassButtonBackground_rollover", 244, 190);
                b.add("glassButtonBackground_pressed", 230, 190);  // guess
                b.add("glassButtonBackground_selected", "controlAccent");
                b.add("glassButtonBackground_selected_rollover", "controlAccent_pressed");  // controlAccent_rollover is too dark
                b.add("glassButtonBackground_selected_pressed", "controlAccent_pressed");
                b.add("glassButtonBackground_selected_disabled", "controlAccent_disabled");
                b.add("glassButtonBackground_selected_inactive", 229);  // guess
            } else {
                b.add("glassButtonBackground", 250);
                b.add("glassButtonBackground_inactive", 252);
                b.add("glassButtonBackground_disabled", 250);
                b.add("glassButtonBackground_rollover", 238);
                b.add("glassButtonBackground_pressed", 227);
                b.add("glassButtonBackground_selected_rollover", "controlAccent_pressed");  // controlAccent_rollover is too dark
                b.add("glassButtonBackground_selected_pressed", "controlAccent_pressed");
                b.add("glassButtonBackground_selected_disabled", "controlAccent_disabled");
                b.add("glassButtonBackground_selected_inactive", 229);
            }

            if (version >= 270000) {
                b.add("tabBackground_selected", 255);
                b.add("tabBackground_selected_inactive", 248);
                b.add("tabControlBackground", 216);
                b.add("tabBorder_selected", 177);
                // glassButtonBorder same as light mode
            } else {
                b.add("glassButtonBorder", 10);
                b.add("glassButtonBorder_disabled", 142);
                b.add("glassButtonBorder_inactive", 86);
                b.add("glassButtonBorder_disabled_inactive", 86);
            }

            if (version >= macOS11) {
                int base = 0;
                b.add("texturedButtonBackground_pressed", base, 60);
                b.add("texturedButtonBackground_selected", base, 182);
                b.add("texturedButtonBackground_selected_inactive", base, 60);
                b.add("texturedButtonBackground_selected_disabled_inactive", base, 60);
                b.add("texturedButtonBackground_selected_pressed", base, 140);
                if (version >= macOS26) {
                    b.add("texturedButtonBackground_rollover", base, 100);
                    b.add("texturedButtonBackground_selected_rollover", base, 100);
                } else {
                    b.addClear("texturedButtonBackground_rollover");
                    b.addClear("texturedButtonBackground_selected_rollover");
                    b.addClear("toolbarButtonBackground_rollover");
                    b.addClear("toolbarButtonBackground");
                    b.addClear("toolbarButtonBackground_disabled");
                    b.addClear("toolbarButtonBackground_inactive");
                    b.addClear("toolbarButtonBackground_disabled_inactive");
                    b.add("toolbarButtonBackground_selected", 86);
                    b.add("toolbarButtonBackground_selected_inactive", 172);
                }
            }

            b.add("textFieldBorder", 0, 160);
            b.add("glassTextFieldBorder", 0, 220);
            b.add("glassTextFieldBackground", 255);

            highContrastLightColors = b.getColors();
        }

        // dark mode colors
        {
            Builder b = new Builder();
            b.add("overlayScrollTrack", 255, 32);
            b.add("overlayScrollTrackBorder", 149, 28);
            b.add("overlayThumb", 255, 128);
            b.add("overlayThumb_rollover", 237, 140);
            b.add("overlayThumbBorder", 0, 26);
            b.add("overlayThumbBorder_rollover", 59, 217);
            b.add("legacyScrollBarThumb", 255, 78);
            b.add("legacyScrollBarThumb_rollover", 255, 128);
            b.add("legacyScrollBarTrack", 255, 16);
            b.add("legacyScrollBarInnerBorder", 255, 24);
            b.add("legacyScrollBarOuterBorder", 255, 14);
            if (version >= 270000) {
                b.add("sidebarThumb", 255, 123);
                b.add("sidebarThumbRollover", 255, 123);
                b.add("sidebarScrollBarTrack", 255, 68);
            } else if (version >= macOS26) {
                b.add("sidebarThumb", 255, 113);
                b.add("sidebarThumbRollover", 255, 113);
                b.add("sidebarScrollBarTrack", 0, 59);
            } else {
                b.add("sidebarThumb", 166);
                b.add("sidebarThumbRollover", 166);
                b.add("sidebarScrollBarTrack", 255, 12);
            }
            b.add("comboBoxButton", 70);
            b.add("comboBoxArrow", 255, 164);
            b.add("titleBarCloseButton", 255, 90, 82);
            b.add("titleBarMinimizeButton", 230, 192, 41);
            b.add("titleBarResizeButton", 84, 194, 43);
            b.add("searchFieldIcon", 255, 213);
            b.add("searchFieldIcon_inactive", 255, 58);
            b.add("linearSliderTick", 255, 64);
            b.add("circularSliderInterior", 255, 64);
            b.add("circularSliderBorder", 32, 48);
            b.add("circularSliderDimple", 255, 218);
            b.add("circularSliderTick", 255, 64);
            b.add("thickDividerDimple", 255, 32);
            b.add("thickDividerDimpleBorder", 0, 48);
            b.add("tableHeaderDivider", 255, 24);
            b.add("tableHeaderArrow", 255, 102);

            b.add("texturedButtonBorder", 255, 20);
            b.add("texturedButtonBorder_inactive", 255, 10);

            {
                int base = 255;
                b.add("texturedButtonBackground_rollover", base, 20);
                b.add("texturedButtonBackground_pressed", base, 36);
                b.add("texturedButtonBackground_selected", base, 12);
                b.add("texturedButtonBackground_selected_pressed", base, 48);
                b.add("texturedButtonBackground_selected_rollover", base, 36);
            }

            b.add("glassButtonBackground_selected_disabled_inactive", "glassButtonBackground_selected_inactive");
            if (version >= 270000) {
                b.add("glassButtonBackground", 60, 130);
                b.add("glassButtonBackground_disabled", 100, 130);
                b.add("glassButtonBackground_inactive", 254, 24);
                b.add("glassButtonBackground_rollover", 255, 40);  // guess
                b.add("glassButtonBackground_pressed", 255, 56);
                b.add("glassButtonBackground_selected", "controlAccent");
                b.add("glassButtonBackground_selected_disabled", "controlAccent_disabled");
                b.add("glassButtonBackground_selected_rollover", "controlAccent");  // rollover too bright
                b.add("glassButtonBackground_selected_pressed", "controlAccent");  // pressed too bright
                b.add("glassButtonBackground_selected_inactive", 254, 56);  // a guess
            } else {
                b.add("glassButtonBackground", 10, 195);
                b.add("glassButtonBackground_disabled", 10, 195);
                b.add("glassButtonBackground_inactive", 10, 195);
                b.add("glassButtonBackground_rollover", 44, 192);
                b.add("glassButtonBackground_pressed", 44, 192);
                b.add("glassButtonBackground_selected", "controlAccent");
                b.add("glassButtonBackground_selected_disabled", "controlAccent_disabled");
                b.add("glassButtonBackground_selected_rollover", "controlAccent_rollover");
                b.add("glassButtonBackground_selected_pressed", "controlAccent_pressed");
                b.add("glassButtonBackground_selected_inactive", 40, 70);
            }

            if (version >= 270000) {
                b.add("tabBackground_selected", 61);
                b.add("tabBackground_selected_inactive", 54);
                b.add("tabControlBackground", 48);
                b.add("tabBorder_selected", 92);
                b.add("glassButtonBorder", 117);
                b.add("glassButtonBorder_disabled", 117);
                b.add("glassButtonBorder_inactive", 92);
                b.add("glassButtonBorder_disabled_inactive", 92);
            } else {
                b.add("glassButtonBorder", 55);
                b.add("glassButtonBorder_disabled", 49);
                b.addClear("glassButtonBorder_inactive");
                b.addClear("glassButtonBorder_disabled_inactive");
            }

            if (version >= macOS11 && version < 260200) {
                b.addClear("toolbarButtonBackground");
                b.add("toolbarButtonBackground_rollover", 255, 16);
                b.add("toolbarButtonBackground_selected_inactive", 39);
            }

            b.add("textFieldBorder", 255, 64);
            b.add("glassTextFieldBorder", 255, 32);  // should be 255/14, but that is too dark
            b.add("glassTextFieldBackground", 20);

            darkColors = b.getColors();
            darkColorsBuilder = b;
        }

        // dark mode high contrast colors
        {
            Builder b = new Builder(darkColorsBuilder);
            b.add("overlayScrollTrack", 255, 16);
            b.add("overlayScrollTrackBorder", 114);
            b.add("overlayThumb", 255, 180);
            b.add("overlayThumb_rollover", 199);
            b.add("legacyScrollBarThumb", 255, 180);
            b.add("legacyScrollBarThumb_rollover", 255, 255);
            b.add("legacyScrollBarTrack", 255, 44);
            b.add("legacyScrollBarInnerBorder", 255, 22);
            b.add("legacyScrollBarOuterBorder", 255, 12);
            if (version >= 270000) {
                b.add("sidebarThumb", 255, 110);
                b.add("sidebarThumbRollover", 255, 110);
                b.add("sidebarScrollBarTrack", 255, 56);
            } else if (version >= macOS26) {
                b.add("sidebarThumb", 255, 178);
                b.add("sidebarThumbRollover", 255, 178);
                b.add("sidebarScrollBarTrack", 255, 38);
            } else {
                b.add("sidebarScrollBarTrack", 255, 38);
            }
            b.add("searchFieldIcon", 255, 237);
            b.add("searchFieldIcon_inactive", 255, 143);
            b.add("circularSliderBorder", 255, 160);
            b.add("circularSliderDimple", 255);
            b.add("tableHeaderDivider", 255, 76);
            b.add("tableHeaderArrow", 255, 128);
            b.add("texturedButtonBorder", 255, 180);
            b.add("texturedButtonBorder_inactive", 255, 120);
            b.add("texturedButtonBorder_disabled_inactive", 255, 120);

            if (version >= macOS11) {
                b.add("toolbarButtonBorder", 190);
                b.add("toolbarButtonBorder_inactive", 138);
                b.add("toolbarButtonBorder_disabled_inactive", 138);
                b.addClear("toolbarButtonInnerBorder");
                b.addClear("toolbarButtonBackground");
                b.addClear("toolbarButtonBackground_rollover");
                b.add("toolbarButtonBackground_selected", 255, 150);
                b.add("toolbarButtonBackground_selected_disabled", 255, 100);
                b.add("toolbarButtonBackground_selected_pressed", 255, 200);
                b.add("toolbarButtonBackground_selected_inactive", 255, 60);
            }

            b.add("glassButtonBackground_disabled", "glassButtonBackground");

            if (version >= 270000) {
                b.add("glassButtonBackground", 28, 148);
                b.add("glassButtonBackground_inactive", 180, 32);
                b.add("glassButtonBackground_disabled", 40, 148);
                b.add("glassButtonBackground_rollover", 255, 40);  // guess
                b.add("glassButtonBackground_selected", "controlAccent");
                b.add("glassButtonBackground_selected_rollover", "controlAccent_rollover");
                b.add("glassButtonBackground_selected_pressed", "controlAccent_deepPressed");
                b.add("glassButtonBackground_selected_inactive", 180, 50);  // guess
            } else {
                if (version >= 260200) {
                    b.add("glassButtonBackground", 0, 150);
                    b.add("glassButtonBackground_inactive", 0, 150);
                    b.add("glassButtonBackground_rollover", 255, 40);
                } else {
                    b.add("glassButtonBackground", 23, 170);
                    b.add("glassButtonBackground_inactive", 23, 170);
                    b.add("glassButtonBackground_rollover", 255, 32);

                }
                b.add("glassButtonBackground_pressed", 255, 72);
                b.add("glassButtonBackground_selected", 255, 90); // no data on 26.1
                b.add("glassButtonBackground_selected_disabled", 0, 100);
                b.add("glassButtonBackground_selected_rollover", 255, 118);
            }

            if (version >= 270000) {
                b.add("tabBackground_selected", 54);
                b.add("tabBackground_selected_inactive", 47);
                b.add("tabControlBackground", 74);
                b.add("tabBorder_selected", 86);
                b.add("glassButtonBorder", 92);
                b.add("glassButtonBorder_disabled", 92);
                b.add("glassButtonBorder_inactive", 14);
                b.add("glassButtonBorder_disabled_inactive", 14);
            } else {
                b.add("glassButtonBorder", 85);
                b.add("glassButtonBorder_disabled", 68);
                b.add("glassButtonBorder_inactive", 68);
                b.add("glassButtonBorder_disabled_inactive", 68);
                b.add("glassButtonInnerBorder", 190);
                b.add("glassButtonInnerBorder_disabled", 150);
                b.add("glassButtonInnerBorder_inactive", 150);
                b.add("glassButtonInnerBorder_disabled_inactive", 120);
            }

            if (version >= macOS11) {
                int base = 255;
                b.add("texturedButtonBackground_pressed", base, 100);
                b.add("texturedButtonBackground_selected", base, 140);
                b.add("texturedButtonBackground_selected_inactive", base, 60);
                b.add("texturedButtonBackground_selected_disabled_inactive", base, 60);
                b.add("texturedButtonBackground_selected_pressed", base, 100);
                b.add("texturedButtonBorder", 190);
                if (version >= macOS26) {
                    b.add("texturedButtonBackground_rollover", base, 120);
                    b.add("texturedButtonBackground_selected_rollover", base, 60);
                } else {
                    b.addClear("texturedButtonBackground_rollover");
                    b.addClear("texturedButtonBackground_selected_rollover");
                }
            }

            b.add("textFieldBorder", 255, 210);
            b.add("glassTextFieldBorder", 255, 160);
            b.add("glassTextFieldBackground", 23);

            highContrastDarkColors = b.getColors();
        }
    }

    /**
      Build a collection of named colors. Color names may resolve to specific colors or to named system colors.
    */

    static class Builder
    {
        private final @NotNull Map<String,Object> definitions;  // map to Color or String
        public static final @NotNull Color CLEAR = new Color(0, 0, 0, 0);

        Builder()
        {
            this.definitions = new HashMap<>();
        }

        Builder(@NotNull Builder seed)
        {
            this.definitions = new HashMap<>(seed.definitions);
        }

        public void add(@NotNull String name, int color)
        {
            Color c = new Color(color, color, color);
            internalAdd(name, c);
        }

        public void add(@NotNull String name, int red, int green, int blue)
        {
            Color c = new Color(red, green, blue);
            internalAdd(name, c);
        }

        public void add(@NotNull String name, int red, int green, int blue, int alpha)
        {
            Color c = new Color(red, green, blue, alpha);
            internalAdd(name, c);
        }

        public void add(@NotNull String name, int intensity, int alpha)
        {
            Color c = new Color(intensity, intensity, intensity, alpha);
            internalAdd(name, c);
        }

        public void addClear(@NotNull String name)
        {
            internalAdd(name, CLEAR);
        }

        public void add(@NotNull String name, @NotNull Color color)
        {
            internalAdd(name, color);
        }

        public void add(@NotNull String name, @NotNull String replacement)
        {
            internalAdd(name, replacement);
        }

        private void internalAdd(@NotNull String name, @NotNull Object c)
        {
            definitions.put(name, c);
        }

        public @NotNull CustomColors getColors()
        {
            // Process synonyms. Collect references to system colors (which are recognized only by being undefined).

            Set<String> colorNames = definitions.keySet();
            Map<String,String> systemColorReferences = new HashMap<>();
            Map<String,Color> namedColors = new HashMap<>();
            for (String name : colorNames) {
                Object o = getColorDefinition(name);
                if (o instanceof Color) {
                    namedColors.put(name, (Color) o);
                } else if (o instanceof String) {
                    systemColorReferences.put(name, (String) o);
                } else {
                    // should not happen
                }
            }
            return new CustomColors(namedColors, systemColorReferences);
        }

        private @Nullable Object getColorDefinition(@NotNull String name)
        {
            boolean nameFound = false;
            int limit = 10;
            String s = name;
            while (true) {
                Object o = definitions.get(s);
                if (o instanceof Color) {
                    return o;
                }
                if (o instanceof String) {
                    nameFound = true;
                    s = (String) o;
                    if (--limit == 0) {
                        throw new UnsupportedOperationException("Color synonym limit reached for " + name);
                    }
                } else if (nameFound) {
                    // Assume it color is mapped to a system color
                    return s;
                } else {
                    // The color is not defined (should not happen)
                    return null;
                }
            }
        }
    }
}
