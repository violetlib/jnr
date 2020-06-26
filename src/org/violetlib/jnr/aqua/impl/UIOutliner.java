/*
 * Copyright (c) 2015 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.aqua.impl;

import java.awt.Shape;
import java.awt.geom.Rectangle2D;

import org.violetlib.jnr.aqua.*;

import org.jetbrains.annotations.*;

/**
  Provides outlines for widgets that can be used to draw focus rings.
*/

public abstract class UIOutliner
{
    public @Nullable Shape getOutline(@NotNull Rectangle2D bounds, @NotNull LayoutConfiguration g)
      throws UnsupportedOperationException
    {
        if (g instanceof ButtonLayoutConfiguration) {
            ButtonLayoutConfiguration gg = (ButtonLayoutConfiguration) g;
            return getButtonOutline(bounds, gg);
        }

        if (g instanceof ComboBoxLayoutConfiguration) {
            ComboBoxLayoutConfiguration gg = (ComboBoxLayoutConfiguration) g;
            return getComboBoxOutline(bounds, gg);
        }

        if (g instanceof PopupButtonLayoutConfiguration) {
            PopupButtonLayoutConfiguration gg = (PopupButtonLayoutConfiguration) g;
            return getPopUpButtonOutline(bounds, gg);
        }

        if (g instanceof TitleBarLayoutConfiguration) {
            TitleBarLayoutConfiguration gg = (TitleBarLayoutConfiguration) g;
            return getTitleBarOutline(bounds, gg);
        }

        if (g instanceof SliderThumbLayoutConfiguration) {
            SliderThumbLayoutConfiguration gg = (SliderThumbLayoutConfiguration) g;
            return getSliderThumbOutline(bounds, gg);
        }

        if (g instanceof SliderLayoutConfiguration) {
            SliderLayoutConfiguration gg = (SliderLayoutConfiguration) g;
            return getSliderOutline(bounds, gg);
        }

        if (g instanceof SpinnerArrowsLayoutConfiguration) {
            SpinnerArrowsLayoutConfiguration gg = (SpinnerArrowsLayoutConfiguration) g;
            return getSpinnerArrowsOutline(bounds, gg);
        }

        if (g instanceof SplitPaneDividerLayoutConfiguration) {
            SplitPaneDividerLayoutConfiguration gg = (SplitPaneDividerLayoutConfiguration) g;
            return getSplitPaneDividerOutline(bounds, gg);
        }

        if (g instanceof SegmentedButtonLayoutConfiguration) {
            SegmentedButtonLayoutConfiguration gg = (SegmentedButtonLayoutConfiguration) g;
            return getSegmentedButtonOutline(bounds, gg);
        }

        if (g instanceof ToolBarItemWellLayoutConfiguration) {
            ToolBarItemWellLayoutConfiguration gg = (ToolBarItemWellLayoutConfiguration) g;
            return getToolBarItemWellOutline(bounds, gg);
        }

        if (g instanceof GroupBoxLayoutConfiguration) {
            GroupBoxLayoutConfiguration gg = (GroupBoxLayoutConfiguration) g;
            return getGroupBoxOutline(bounds, gg);
        }

        if (g instanceof ListBoxLayoutConfiguration) {
            ListBoxLayoutConfiguration gg = (ListBoxLayoutConfiguration) g;
            return getListBoxOutline(bounds, gg);
        }

        if (g instanceof TextFieldLayoutConfiguration) {
            TextFieldLayoutConfiguration gg = (TextFieldLayoutConfiguration) g;
            return getTextFieldOutline(bounds, gg);
        }

        if (g instanceof ScrollBarLayoutConfiguration) {
            ScrollBarLayoutConfiguration gg = (ScrollBarLayoutConfiguration) g;
            return getScrollBarOutline(bounds, gg);
        }

        if (g instanceof ScrollColumnSizerLayoutConfiguration) {
            ScrollColumnSizerLayoutConfiguration gg = (ScrollColumnSizerLayoutConfiguration) g;
            return getScrollColumnSizerOutline(bounds, gg);
        }

        if (g instanceof ProgressIndicatorLayoutConfiguration) {
            ProgressIndicatorLayoutConfiguration gg = (ProgressIndicatorLayoutConfiguration) g;
            return getProgressIndicatorOutline(bounds, gg);
        }

        if (g instanceof TableColumnHeaderLayoutConfiguration) {
            TableColumnHeaderLayoutConfiguration gg = (TableColumnHeaderLayoutConfiguration) g;
            return getTableColumnHeaderOutline(bounds, gg);
        }

        throw new UnsupportedOperationException();
    }

    protected abstract @Nullable Shape getSliderThumbOutline(@NotNull Rectangle2D bounds, @NotNull SliderThumbLayoutConfiguration g);

    protected abstract @Nullable Shape getButtonOutline(@NotNull Rectangle2D bounds, @NotNull ButtonLayoutConfiguration g);

    protected abstract @Nullable Shape getSegmentedButtonOutline(@NotNull Rectangle2D bounds, @NotNull SegmentedButtonLayoutConfiguration g);

    protected abstract @Nullable Shape getComboBoxOutline(@NotNull Rectangle2D bounds, @NotNull ComboBoxLayoutConfiguration g);

    protected abstract @Nullable Shape getPopUpButtonOutline(@NotNull Rectangle2D bounds, @NotNull PopupButtonLayoutConfiguration g);

    protected abstract @Nullable Shape getToolBarItemWellOutline(@NotNull Rectangle2D bounds, @NotNull ToolBarItemWellLayoutConfiguration g);

    protected abstract @Nullable Shape getTitleBarOutline(@NotNull Rectangle2D bounds, @NotNull TitleBarLayoutConfiguration g);

    protected abstract @Nullable Shape getSliderOutline(@NotNull Rectangle2D bounds, @NotNull SliderLayoutConfiguration g);

    protected abstract @Nullable Shape getSpinnerArrowsOutline(@NotNull Rectangle2D bounds, @NotNull SpinnerArrowsLayoutConfiguration g);

    protected abstract @Nullable Shape getSplitPaneDividerOutline(@NotNull Rectangle2D bounds, @NotNull SplitPaneDividerLayoutConfiguration g);

    protected abstract @Nullable Shape getGroupBoxOutline(@NotNull Rectangle2D bounds, @NotNull GroupBoxLayoutConfiguration g);

    protected abstract @Nullable Shape getListBoxOutline(@NotNull Rectangle2D bounds, @NotNull ListBoxLayoutConfiguration g);

    protected abstract @Nullable Shape getTextFieldOutline(@NotNull Rectangle2D bounds, @NotNull TextFieldLayoutConfiguration g);

    protected abstract @Nullable Shape getScrollBarOutline(@NotNull Rectangle2D bounds, @NotNull ScrollBarLayoutConfiguration g);

    protected abstract @Nullable Shape getScrollColumnSizerOutline(@NotNull Rectangle2D bounds, @NotNull ScrollColumnSizerLayoutConfiguration g);

    protected abstract @Nullable Shape getProgressIndicatorOutline(@NotNull Rectangle2D bounds, @NotNull ProgressIndicatorLayoutConfiguration g);

    protected abstract @Nullable Shape getTableColumnHeaderOutline(@NotNull Rectangle2D bounds, @NotNull TableColumnHeaderLayoutConfiguration g);

    public static int size(@NotNull AquaUIPainter.Size sz, int regular, int small, int mini)
    {
        switch (sz) {
            case SMALL:
                return small;
            case MINI:
                return mini;
            default:
                return regular;
        }
    }

    public static float size2D(@NotNull AquaUIPainter.Size sz, float regular, float small, float mini)
    {
        switch (sz) {
            case SMALL:
                return small;
            case MINI:
                return mini;
            default:
                return regular;
        }
    }
}
