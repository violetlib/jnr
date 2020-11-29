/*
 * Copyright (c) 2015-2020 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.aqua.impl;

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

import org.violetlib.geom.GeneralRoundRectangle;
import org.violetlib.jnr.Insetter;
import org.violetlib.jnr.aqua.AquaUIPainter.ButtonWidget;
import org.violetlib.jnr.aqua.AquaUIPainter.ComboBoxWidget;
import org.violetlib.jnr.aqua.AquaUIPainter.PopupButtonWidget;
import org.violetlib.jnr.aqua.AquaUIPainter.Position;
import org.violetlib.jnr.aqua.AquaUIPainter.SegmentedButtonWidget;
import org.violetlib.jnr.aqua.AquaUIPainter.Size;
import org.violetlib.jnr.aqua.AquaUIPainter.SliderWidget;
import org.violetlib.jnr.aqua.AquaUIPainter.TextFieldWidget;
import org.violetlib.jnr.aqua.*;
import org.violetlib.jnr.impl.JNRPlatformUtils;

import org.jetbrains.annotations.*;

import static org.violetlib.jnr.aqua.AquaUIPainter.ButtonWidget.*;
import static org.violetlib.jnr.aqua.AquaUIPainter.PopupButtonWidget.*;

/**
  Provides outlines for widgets that can be used to draw focus rings. This version for macOS 10.10+.
*/

public class YosemiteOutliner
  extends UIOutliner
{
    protected final @NotNull YosemiteLayoutInfo uiLayout;

    public YosemiteOutliner(@NotNull YosemiteLayoutInfo uiLayout)
    {
        this.uiLayout = uiLayout;
    }

    @Override
    protected @Nullable Shape getSliderThumbOutline(@NotNull Rectangle2D bounds,
                                                    @NotNull SliderThumbLayoutConfiguration g)
    {
        Insetter insets = uiLayout.getSliderThumbInsets(g, g.getThumbPosition());
        Rectangle2D tb = insets.applyToBounds2D(bounds);

        SliderWidget sw = g.getWidget();

        if (sw == SliderWidget.SLIDER_CIRCULAR) {
            // TBD: circular slider only supported by NSView at this time
            return new Rectangle(0, 0, 0, 0);
        } else {
            if (!g.hasTickMarks()) {
                // No tick marks - a circular knob
                return new Ellipse2D.Double(tb.getX(), tb.getY(), tb.getWidth(), tb.getHeight());
            } else {
                // TBD: construct the correct shape for the pointer knob
                return tb;
            }
        }
    }

    @Override
    protected @Nullable Shape getButtonOutline(@NotNull Rectangle2D bounds, @NotNull ButtonLayoutConfiguration g)
    {
        ButtonWidget bw = g.getButtonWidget();
        Size sz = g.getSize();

        double x = bounds.getX();
        double y = bounds.getY();
        double width = bounds.getWidth();
        double height = bounds.getHeight();

        // circle buttons

        if (bw == BUTTON_RADIO
              || bw == BUTTON_HELP
              || bw == BUTTON_ROUND
              || bw == BUTTON_ROUND_INSET
              || bw == BUTTON_ROUND_TEXTURED
              || bw == BUTTON_ROUND_TEXTURED_TOOLBAR) {

            switch (bw)
            {
                case BUTTON_HELP:
                    width -= 1;
                    height -= 1;
                    x += 0.5;
                    break;
                case BUTTON_ROUND:
                    width -= 1;
                    height -= 1;
                    x += 0.5;
                    break;
            }

            // The width and height are usually equal, but in some cases the height is larger

            if (height > width && !bw.isTextured()) {
                double excess = height - width;
                y += excess/2;
                height = width;
            }
            return new Ellipse2D.Double(x, y, width, height);
        }

        // rectangular buttons

        if (bw == BUTTON_BEVEL || bw == BUTTON_GRADIENT || bw == BUTTON_COLOR_WELL) {
            return bounds;
        }

        if (bw == BUTTON_TOOLBAR_ITEM) {
            double corner = 7;
            return new GeneralRoundRectangle(x, y, width, height,
              corner, corner, corner, corner, 0, 0, 0, 0);
        }

        // all others are rounded rectangles

        double corner = 8;

        if (bw == BUTTON_PUSH) {
            corner = 6;
            x += size2D(sz, 0.5f, 0.5f, 0.5f);
            y += size2D(sz, 0.5f, 0.5f, 0.5f);
            height += size2D(sz, -2, -2, -2);
            width += size2D(sz, -1, -1, -1);
        } else if (bw == BUTTON_BEVEL_ROUND) {
            x += size2D(sz, 0.5f, 0.5f, 0.5f);
            y += size2D(sz, 0.5f, 0.5f, 0.5f);
            height += size2D(sz, -2, -2, -2);
            width += size2D(sz, -1, -1, -1);
        } else if (bw == BUTTON_CHECK_BOX) {
            corner = 5;
        } else if (bw == BUTTON_DISCLOSURE) {
            x += size2D(sz, 1, 1, 0.5f);
            y += size2D(sz, 0.5f, 0.5f, 0.5f);
            width += size2D(sz, -2, -2, -1);
            height += size2D(sz, -2, -2, -2);
        } else if (bw == BUTTON_INLINE) {
            height -= 1;
            corner = 16;
        } else if (bw == BUTTON_RECESSED) {
            corner = 6;
        } else if (bw == BUTTON_TEXTURED || bw == BUTTON_TEXTURED_TOOLBAR || bw == BUTTON_TEXTURED_TOOLBAR_ICONS) {
            corner = 6;
            height -= 0.5;
        } else if (bw == BUTTON_DISCLOSURE_TRIANGLE) {
            // disclosure triangle
            width -= 0.5;
            corner = 3;
        } else if (bw == BUTTON_ROUNDED_RECT) {
            height -= 0.5;
        } else if (bw == BUTTON_PUSH_INSET2) {
            y += 0.5;
            height -= 1;
            corner = size(sz, 16, 15, 14);
        }

        return new RoundRectangle2D.Double(x, y, width, height, corner, corner);
    }

    @Override
    protected @Nullable Shape getSegmentedButtonOutline(@NotNull Rectangle2D bounds, @NotNull SegmentedButtonLayoutConfiguration g)
    {
        SegmentedButtonWidget bw = g.getWidget();
        Size sz = g.getSize();
        Position pos = g.getPosition();

        double x = bounds.getX();
        double y = bounds.getY();
        double width = bounds.getWidth();
        double height = bounds.getHeight();

        boolean isLeft = pos == Position.FIRST;

        double corner = 8;

        switch (bw)
        {
            case BUTTON_TAB:
            case BUTTON_SEGMENTED:
            case BUTTON_SEGMENTED_SEPARATED:
            case BUTTON_SEGMENTED_SLIDER:
            case BUTTON_SEGMENTED_SLIDER_TOOLBAR:
            case BUTTON_SEGMENTED_SLIDER_TOOLBAR_ICONS:
                corner = 6;

                x += size2D(sz, isLeft ? 0.5f : 0, 0, 0);
                y += size2D(sz, 0.5f, 0.5f, 0.5f);
                height += size2D(sz, -2, -2, -2);
                width += size2D(sz, -0.5f, 0, 0);

                if (sz == Size.SMALL || sz == Size.MINI) {
                    if (pos == Position.FIRST) {
                        x += 0.5;
                        width -= 0.5;
                    } else if (pos == Position.LAST) {
                        width -= 0.5;
                    } else if (pos == Position.ONLY) {
                        x += 0.5;
                        width -= 1;
                    }
                }

                break;

            case BUTTON_SEGMENTED_INSET:
                break;

            case BUTTON_SEGMENTED_SCURVE:
            case BUTTON_SEGMENTED_TEXTURED:
            case BUTTON_SEGMENTED_TOOLBAR:
                width += size2D(sz, 0, 0, 0);
                height += size2D(sz, -1, -1, -1);
                break;

            case BUTTON_SEGMENTED_TEXTURED_TOOLBAR:
            case BUTTON_SEGMENTED_TEXTURED_TOOLBAR_ICONS:
                width += size2D(sz, 0, 0, 0);
                height += size2D(sz, -1, -2, -1);
                break;

            case BUTTON_SEGMENTED_TEXTURED_SEPARATED:
            case BUTTON_SEGMENTED_TEXTURED_SEPARATED_TOOLBAR:
            case BUTTON_SEGMENTED_TEXTURED_SEPARATED_TOOLBAR_ICONS:
                if (pos == Position.ONLY || pos == Position.FIRST) {
                    width -= 0.5;
                }
                if (pos == Position.MIDDLE) {
                    x -= 0.5;
                }
                height -= size2D(sz, 1, 0.5f, 1);
                break;

            case BUTTON_SEGMENTED_SMALL_SQUARE:
                corner = 0;
                break;
        }

        if (pos == Position.ONLY || bw.isSeparated()) {
            return new RoundRectangle2D.Double(x, y, width, height, corner, corner);
        }

        if (pos == Position.FIRST) {
            return new GeneralRoundRectangle(x, y, width, height, corner, corner, 0, 0, 0, 0, corner, corner);
        }

        if (pos == Position.LAST) {
            return new GeneralRoundRectangle(x, y, width, height, 0, 0, corner, corner, corner, corner, 0, 0);
        }

        return new Rectangle2D.Double(x, y, width, height);
    }

    @Override
    protected @Nullable Shape getComboBoxOutline(@NotNull Rectangle2D bounds, @NotNull ComboBoxLayoutConfiguration g)
    {
        int platformVersion = JNRPlatformUtils.getPlatformVersion();

        double x = bounds.getX();
        double y = bounds.getY();
        double width = bounds.getWidth();
        double height = bounds.getHeight();

        ComboBoxWidget widget = g.getWidget();
        if (widget == ComboBoxWidget.BUTTON_COMBO_BOX) {
            Size sz = g.getSize();
            y += size2D(sz, 0.5f, 0.5f, 0.5f);
            height += size2D(sz, -2, -2, -1);
            x += size2D(sz, 0, 0, 0);
            width += size2D(sz, -1, -1, -1);
            double corner = 6;

            // TBD: support right to left

            return new GeneralRoundRectangle(x, y, width, height, 0, 0, corner, corner, corner, corner, 0, 0);

        } else if (widget == ComboBoxWidget.BUTTON_COMBO_BOX_CELL) {
            Insetter insets = uiLayout.getComboBoxEditorInsets(g);
            return insets.applyToBounds2D(bounds);

        } else if (widget == ComboBoxWidget.BUTTON_COMBO_BOX_TEXTURED || widget == ComboBoxWidget.BUTTON_COMBO_BOX_TEXTURED_TOOLBAR) {
            double corner = 4;
            if (platformVersion >= 101300) {
                x += 0.5f;
                width -= 1;
                height -= 1;
                corner = 8;
            } else if (platformVersion >= 101100) {
                corner = 8;
            }
            return new GeneralRoundRectangle(x, y, width, height, corner, corner, corner, corner, corner, corner, corner, corner);

        } else {
            return null;
        }
    }

    @Override
    protected @Nullable Shape getPopUpButtonOutline(@NotNull Rectangle2D bounds, @NotNull PopupButtonLayoutConfiguration g)
    {
        // On Yosemite, the square style bombs if the mini size is selected.
        // See rendering code, which must be consistent.

        PopupButtonWidget bw = g.getPopupButtonWidget();
        Size sz = g.getSize();

        boolean isSquare = bw == BUTTON_POP_UP_SQUARE || bw == BUTTON_POP_DOWN_SQUARE;
        boolean isArrowsOnly = bw == BUTTON_POP_UP_CELL || bw == BUTTON_POP_DOWN_CELL;

        if ((isSquare || isArrowsOnly) && sz == Size.MINI) {
            sz = Size.SMALL;
        }

        double x = bounds.getX();
        double y = bounds.getY();
        double width = bounds.getWidth();
        double height = bounds.getHeight();

        if (bw == BUTTON_POP_UP_SQUARE) {
            x += size2D(sz, 0, 0, -2);
            width += size2D(sz, 0, 0, 1.5f);
            height += size2D(sz, 0, 0, -0.5f);
            return new Rectangle2D.Double(x, y, width, height);

        } else if (bw == BUTTON_POP_DOWN_SQUARE) {
            x += size2D(sz, 0, 0, -0.5f);
            width += size2D(sz, 0, 0, 1);
            return new Rectangle2D.Double(x, y, width, height);

        } else if (bw == BUTTON_POP_UP_GRADIENT) {
            return new Rectangle2D.Double(x, y, width, height);
        } else if (bw == BUTTON_POP_DOWN_GRADIENT) {
            return new Rectangle2D.Double(x, y, width, height);

        } else if (bw == BUTTON_POP_UP_CELL) {
            x += size2D(sz, 0, 0, -2);
            return new Rectangle2D.Double(x, y, width, height);

        } else if (bw == BUTTON_POP_DOWN_CELL) {
            x += size2D(sz, 0, 0, -0.5f);
            return new Rectangle2D.Double(x, y, width, height);

        } else if (bw == BUTTON_POP_UP_TEXTURED
                     || bw == BUTTON_POP_DOWN_TEXTURED
                     || bw == BUTTON_POP_UP_TEXTURED_TOOLBAR
                     || bw == BUTTON_POP_DOWN_TEXTURED_TOOLBAR) {

            height -= 0.5;
            double corner = 6;
            return new RoundRectangle2D.Double(x, y, width, height, corner, corner);

        } else {
            x += 0.5;
            width -= 1;
            double corner = 8;

            if (bw == BUTTON_POP_DOWN) {
                height += size2D(sz, -2, -2, -2);
                y += size2D(sz, 0.5f, 0.5f, 0.5f);
            } else if (bw == BUTTON_POP_UP) {
                height += size2D(sz, -2, -2, -2);
                y += size2D(sz, 0, 0, 0.5f);
            } else if (bw == BUTTON_POP_UP_BEVEL || bw == BUTTON_POP_DOWN_BEVEL) {
                height += size2D(sz, -2, -2, -1);
                y += size2D(sz, 0.5f, 0.5f, 0.5f);
            }

            return new RoundRectangle2D.Double(x, y, width, height, corner, corner);
        }
    }

    @Override
    protected @Nullable Shape getToolBarItemWellOutline(@NotNull Rectangle2D bounds, @NotNull ToolBarItemWellLayoutConfiguration g)
    {
        return null;
    }

    @Override
    protected @Nullable Shape getTitleBarOutline(@NotNull Rectangle2D bounds, @NotNull TitleBarLayoutConfiguration g)
    {
        return null;
    }

    @Override
    protected @Nullable Shape getSliderOutline(@NotNull Rectangle2D bounds, @NotNull SliderLayoutConfiguration g)
    {
        return null;
    }

    @Override
    protected @Nullable Shape getSpinnerArrowsOutline(@NotNull Rectangle2D bounds, @NotNull SpinnerArrowsLayoutConfiguration g)
    {
        Size sz = g.getSize();
        double x = bounds.getX() + size2D(sz, 1, 1, 1);
        double y = bounds.getY() + size2D(sz, 1, 0.5f, 0.5f);
        double w = bounds.getWidth() + size2D(sz, -2.5f, -2.5f, -2.5f);
        double h = bounds.getHeight() + size2D(sz, -2.5f, -2.5f, -2.5f);
        double corner = 9;
        return new RoundRectangle2D.Double(x, y, w, h, corner, corner);
    }

    @Override
    protected @Nullable Shape getSplitPaneDividerOutline(@NotNull Rectangle2D bounds, @NotNull SplitPaneDividerLayoutConfiguration g)
    {
        return null;
    }

    @Override
    protected @Nullable Shape getGroupBoxOutline(@NotNull Rectangle2D bounds, @NotNull GroupBoxLayoutConfiguration g)
    {
        return null;
    }

    @Override
    protected @Nullable Shape getListBoxOutline(@NotNull Rectangle2D bounds, @NotNull ListBoxLayoutConfiguration g)
    {
        return null;
    }

    @Override
    protected @Nullable Shape getTextFieldOutline(@NotNull Rectangle2D bounds, @NotNull TextFieldLayoutConfiguration g)
    {
        double x = bounds.getX();
        double y = bounds.getY();
        double width = bounds.getWidth();
        double height = bounds.getHeight();

        TextFieldWidget w = g.getWidget();
        if (w.isSearch() || w.isRound()) {
            double corner = 6;
            if (w.isToolbar()) {
                height -= 0.5f;
            } else {
                x += 0.5;
                y += 0.5;
                width -= 1;
                height -= 1;
            }
            return new RoundRectangle2D.Double(x, y, width, height, corner, corner);
        } else {
            x += 0.5;
            y += 0.5;
            width -= 1.5;
            height -= 1.5;
            return new Rectangle2D.Double(x, y, width, height);
        }
    }

    @Override
    protected @Nullable Shape getScrollBarOutline(@NotNull Rectangle2D bounds, @NotNull ScrollBarLayoutConfiguration g)
    {
        return null;
    }

    @Override
    protected @Nullable Shape getScrollColumnSizerOutline(@NotNull Rectangle2D bounds, @NotNull ScrollColumnSizerLayoutConfiguration g)
    {
        return null;
    }

    @Override
    protected @Nullable Shape getProgressIndicatorOutline(@NotNull Rectangle2D bounds, @NotNull ProgressIndicatorLayoutConfiguration g)
    {
        return null;
    }

    @Override
    protected @Nullable Shape getTableColumnHeaderOutline(@NotNull Rectangle2D bounds, @NotNull TableColumnHeaderLayoutConfiguration g)
    {
        return null;
    }
}
