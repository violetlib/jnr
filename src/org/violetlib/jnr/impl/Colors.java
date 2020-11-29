/*
 * Copyright (c) 2018-2020 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.impl;

import java.awt.Color;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.violetlib.vappearances.VAppearance;

import org.jetbrains.annotations.*;

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

    public @NotNull Map<String,Color> getColors()
    {
        return colors;
    }

    private final Color DARK_THUMB = new Color(0, 0, 0, 128);
    private final Color DARK_TRACK = new Color(250, 250, 250, 191);
    private final Color DARK_TRACK_BORDER = new Color(220, 220, 220, 200);

    static {
        // light mode colors
        {
            Builder b = new Builder();
            b.add("overlayScrollTrack", 249, 185);
            b.add("overlayScrollTrackBorder", 194, 85);
            b.add("overlayThumb", 20, 140);
            b.add("overlayThumb_rollover", 62, 170);
            b.add("legacyScrollBarThumb", 0, 58);
            b.add("legacyScrollBarThumb_rollover", 0, 128);
            b.add("legacyScrollBarTrack", 250);
            b.add("legacyScrollBarInnerBorder", 231);
            b.add("legacyScrollBarOuterBorder", 237);
            b.add("comboBoxArrow", 0, 176);
            b.add("titleBarCloseButton", 255, 95, 87);
            b.add("titleBarMinimizeButton", 255, 192, 47);
            b.add("titleBarResizeButton", 41, 204, 66);
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
            b.add("toolbarButton", 0, 14);
            b.add("toolbarButtonSelected", 0, 14);
            b.add("toolbarButtonPressed", 0, 28);
            b.add("toolbarButtonSelectedPressed", 0, 56);
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
            b.add("legacyScrollBarInnerBorder", 195);
            b.add("legacyScrollBarOuterBorder", 200);
            b.add("titleBarCloseButton", 255, 45, 64);
            b.add("titleBarMinimizeButton", 246, 177, 1);
            b.add("titleBarResizeButton", 5, 161, 27);
            b.add("searchFieldIcon", 0, 177);
            b.add("searchFieldIcon_inactive", 0, 100);
            b.add("circularSliderBorder", 12, 180);
            b.add("tableHeaderDivider", 0, 48);
            b.add("tableHeaderArrow", 0, 140);
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
            b.add("toolbarButton", 255, 14);
            b.add("toolbarButtonSelected", 255, 14);
            b.add("toolbarButtonPressed", 255, 28);
            b.add("toolbarButtonSelectedPressed", 255, 56);
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
            highContrastDarkColors = b.getColors();
        }
    }

    static class Builder
    {
        private final @NotNull Map<String,Color> colors = new HashMap<>();

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
