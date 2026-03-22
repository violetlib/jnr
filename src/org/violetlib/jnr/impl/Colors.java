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

import static org.violetlib.jnr.aqua.AquaUIPainter.macOS11;
import static org.violetlib.jnr.aqua.AquaUIPainter.macOS26;

/**

*/

public class Colors
{
    private static final @NotNull Colors lightColors;
    private static final @NotNull Colors darkColors;
    private static final @NotNull Colors highContrastLightColors;
    private static final @NotNull Colors highContrastDarkColors;

    public static @NotNull Colors getColors(@Nullable VAppearance appearance)
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

    public static @NotNull Colors getColors(boolean isDark, boolean isHighContrast)
    {
        if (isDark) {
            return isHighContrast ? highContrastDarkColors : darkColors;
        } else {
            return isHighContrast ? highContrastLightColors : lightColors;
        }
    }

    private final @NotNull Map<String,Color> colors;

    Colors(@NotNull Map<String,Color> colors)
    {
        this.colors = Collections.unmodifiableMap(colors);
    }

    public @NotNull Color get(@NotNull String name)
    {
        Color c = colors.get(name);
        if (c == null) {
            throw new UnsupportedOperationException("Missing color: " + name);
        }
        return c;
    }

    public @Nullable Color getOptional(@NotNull String name)
    {
        return colors.get(name);
    }

    public static boolean isClear(@NotNull Color c)
    {
        return c.getAlpha() == 0;
    }

    public @NotNull Map<String,Color> getColors()
    {
        return colors;
    }

    private final Color DARK_THUMB = new Color(0, 0, 0, 128);
    private final Color DARK_TRACK = new Color(250, 250, 250, 191);
    private final Color DARK_TRACK_BORDER = new Color(220, 220, 220, 200);

    static {
        int version = AquaNativeRendering.getSystemRenderingVersion();

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
            b.add("sidebarThumb", 0, 115);
            b.add("sidebarThumbRollover", 0, 115);
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

            b.add("glassButtonBorder", 244);  // should be 255, but visible only because of a surrounding cloud
            b.addClear("glassButtonBorder_inactive");
            b.addClear("glassButtonBorder_disabled_inactive");

            b.add("glassButtonBackground_pressed", 0, 20);   // 233
            b.add("glassButtonBackground_rollover", 0, 12);
            b.add("glassButtonBackground_selected", 0, 24);
            b.add("glassButtonBackground_selected_rollover", 0, 36);
            if (version >= 260200) {
                b.add("glassButtonBackground", 0, 4);
                b.add("glassButtonBackground_disabled", 0, 4);
                b.add("glassButtonBackground_inactive", 0, 8);
                b.add("glassButtonBackground_selected_inactive", 0, 40);
            } else {
                b.addClear("glassButtonBackground");  // 253 (native is 255) same when disabled
                b.add("glassButtonBackground_selected_inactive", 0, 24);
                if (version >= macOS11) {
                    b.addClear("toolbarButtonBackground");
                    b.add("toolbarButtonBackground_rollover", 0, 16);
                    b.add("toolbarButtonBackground_selected_inactive", 234);
                }
            }

            lightColors = b.getColors();
        }

        // light mode high contrast colors
        {
            Builder b = new Builder();
            b.addAll(lightColors);
            b.add("overlayScrollTrack", 249);
            b.add("overlayScrollTrackBorder", 202);
            b.add("overlayThumb", 0, 184);
            b.add("overlayThumb_rollover", 87);
            b.add("legacyScrollBarThumb", 0, 108);
            b.add("legacyScrollBarThumb_rollover", 0, 164);
            b.add("legacyScrollBarTrack", 0, 5);  // was 0/35 — is that needed for some release?
            b.add("legacyScrollBarInnerBorder", 195);
            b.add("legacyScrollBarOuterBorder", 200);
            b.add("sidebarThumb", 0, 185);
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

            b.add("glassButtonBorder", 10);
            b.add("glassButtonBorder_disabled", 142);
            b.add("glassButtonBorder_inactive", 86);
            b.add("glassButtonBorder_disabled_inactive", 86);

            b.add("glassButtonBackground_pressed", 0, 100);  // 155
            b.add("glassButtonBackground_rollover", 0, 32);
            b.add("glassButtonBackground_selected", 0, 72);

            if (version >= macOS26) {
                b.add("glassButtonBackground_selected_rollover", 0, 146);
            }

            if (version >= 260200) {
                b.add("glassButtonBackground", 0, 10);
                b.add("glassButtonBackground_inactive", 0, 5);
                b.add("glassButtonBackground_disabled", 0, 10);
            } else {
                b.add("glassButtonBackground", 0, 20);
                b.add("glassButtonBackground_disabled", 0, 20);
                b.add("glassButtonBackground_inactive", 0, 20);
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
            b.add("sidebarThumb", 166);
            b.add("sidebarThumbRollover", 166);
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

            b.add("glassButtonBorder", 55);
            b.addClear("glassButtonBorder_inactive");
            b.addClear("glassButtonBorder_disabled_inactive");
            b.add("glassButtonBorder_disabled", 49);

            b.add("glassButtonBackground", 255, 16);
            b.add("glassButtonBackground_pressed", 255, 56);
            b.add("glassButtonBackground_disabled", 255, 16);
            b.add("glassButtonBackground_inactive", 255, 16);
            b.add("glassButtonBackground_selected", 255, 42);
            b.add("glassButtonBackground_selected_pressed", 255, 60);  // a guess

            if (version >= macOS26) {
                b.add("glassButtonBackground_selected_rollover", 255, 70);  // no data on 26.1
            }

            if (version >= 260200) {
                b.add("glassButtonBackground_rollover", 255, 28);
            } else {
                b.add("glassButtonBackground_rollover", 255, 45);  // no data
            }

            if (version >= macOS11 && version < 260200) {
                b.addClear("toolbarButtonBackground");
                b.add("toolbarButtonBackground_rollover", 255, 16);
                b.add("toolbarButtonBackground_selected_inactive", 39);
            }

            darkColors = b.getColors();
        }

        // dark mode high contrast colors
        {
            Builder b = new Builder();
            b.addAll(darkColors);
            b.add("overlayScrollTrack", 255, 16);
            b.add("overlayScrollTrackBorder", 114);
            b.add("overlayThumb", 255, 180);
            b.add("overlayThumb_rollover", 199);
            b.add("legacyScrollBarThumb", 255, 180);
            b.add("legacyScrollBarThumb_rollover", 255, 255);
            b.add("legacyScrollBarTrack", 255, 44);
            b.add("legacyScrollBarInnerBorder", 255, 22);
            b.add("legacyScrollBarOuterBorder", 255, 12);
            b.add("searchFieldIcon", 255, 237);
            b.add("searchFieldIcon_inactive", 255, 143);
            b.add("circularSliderBorder", 255, 160);
            b.add("circularSliderDimple", 255);
            b.add("tableHeaderDivider", 255, 76);
            b.add("tableHeaderArrow", 255, 128);
            b.add("texturedButtonBorder", 255, 180);
            b.add("texturedButtonBorder_inactive", 255, 120);
            b.add("texturedButtonBorder_disabled_inactive", 255, 120);

            b.add("glassButtonBorder", 85);
            b.add("glassButtonBorder_inactive", 68);
            b.add("glassButtonBorder_disabled_inactive", 68);
            b.add("glassButtonBorder_disabled", 68);
            b.add("glassButtonInnerBorder", 190);
            b.add("glassButtonInnerBorder_inactive", 150);
            b.add("glassButtonInnerBorder_disabled_inactive", 120);
            b.add("glassButtonInnerBorder_disabled", 150);

            b.add("glassButtonBackground_pressed", 255, 72);
            b.add("glassButtonBackground_selected", 255, 90); // no data on 26.1
            b.add("glassButtonBackground_selected_disabled", 0, 100);
            b.add("glassButtonBackground_selected_rollover", 255, 118);
            if (version >= 260200) {
                b.add("glassButtonBackground", 0, 150);
                b.add("glassButtonBackground_disabled", 0, 50);
                b.add("glassButtonBackground_inactive", 0, 150);
                b.add("glassButtonBackground_rollover", 255, 40);
            } else {
                b.add("glassButtonBackground", 23, 170);
                b.add("glassButtonBackground_disabled", 23, 170);
                b.add("glassButtonBackground_inactive", 23, 170);
                b.add("glassButtonBackground_rollover", 255, 32);
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

            highContrastDarkColors = b.getColors();
        }
    }

    static class Builder
    {
        private final @NotNull Map<String,Color> colors = new HashMap<>();
        public static final @NotNull Color CLEAR = new Color(0, 0, 0, 0);

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

        public void add(@NotNull String name, @NotNull String existingName)
        {
            Color c = colors.get(existingName);
            if (c != null) {
                internalAdd(name, c);
            } else {
                throw new UnsupportedOperationException("Missing color: " + existingName);
            }
        }

        public void addAll(@NotNull Colors colors)
        {
            Map<String,Color> cs = colors.getColors();
            for (String name : cs.keySet()) {
                Color c = cs.get(name);
                internalAdd(name, c);
            }
        }

        private void internalAdd(@NotNull String name, @NotNull Color c)
        {
            colors.put(name, c);
        }

        public @NotNull Colors getColors()
        {
            return new Colors(colors);
        }
    }
}
