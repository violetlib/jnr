/*
 * Copyright (c) 2015-2020 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.aqua.impl;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;

import org.violetlib.geom.LinearBounds;
import org.violetlib.jnr.Insetter;
import org.violetlib.jnr.LayoutInfo;
import org.violetlib.jnr.aqua.*;
import org.violetlib.jnr.impl.BasicLayoutInfo;
import org.violetlib.jnr.impl.CenteredInsetter1;
import org.violetlib.jnr.impl.CombinedInsetter;
import org.violetlib.jnr.impl.DynamicInsetter1;
import org.violetlib.jnr.impl.FixedInsetter1;
import org.violetlib.jnr.impl.FloatingInsetter1;
import org.violetlib.jnr.impl.Insetter1;
import org.violetlib.jnr.impl.Insetters;
import org.violetlib.jnr.impl.JNRUtils;

import org.jetbrains.annotations.*;

import static org.violetlib.jnr.aqua.AquaUIPainter.*;
import static org.violetlib.jnr.impl.JNRUtils.*;

/**
  Layout information for OS 10.10 widgets.
*/

public class YosemiteLayoutInfo
  extends AquaUILayoutInfo
{
    public YosemiteLayoutInfo()
    {
    }

    @Override
    protected @NotNull LayoutInfo getButtonLayoutInfo(@NotNull ButtonLayoutConfiguration g)
    {
        ButtonWidget bw = g.getButtonWidget();

        if (bw == ButtonWidget.BUTTON_TOOLBAR_ITEM) {
            ToolBarItemWellLayoutConfiguration tg = new ToolBarItemWellLayoutConfiguration();
            return getToolBarItemWellLayoutInfo(tg);
        }

        Size sz = g.getSize();

        if (bw == ButtonWidget.BUTTON_PUSH) {
            return BasicLayoutInfo.createFixedHeight(size(sz, 22, 19, 16));

        } else if (bw == ButtonWidget.BUTTON_BEVEL) {
            return BasicLayoutInfo.getInstance();

        } else if (bw == ButtonWidget.BUTTON_BEVEL_ROUND) {
            return BasicLayoutInfo.getInstance();

        } else if (bw == ButtonWidget.BUTTON_CHECK_BOX) {
            return BasicLayoutInfo.createFixed(size(sz, 14, 12, 10), size(sz, 14, 12, 10));

        } else if (bw == ButtonWidget.BUTTON_RADIO) {
            return BasicLayoutInfo.createFixed(size(sz, 16, 14, 10), size(sz, 16, 14, 10));

        } else if (bw == ButtonWidget.BUTTON_DISCLOSURE) {
            return BasicLayoutInfo.createFixed(size(sz, 21, 19, 15), size(sz, 22, 19, 16));

        } else if (bw == ButtonWidget.BUTTON_HELP) {
            return BasicLayoutInfo.createFixed(size(sz, 21, 18, 15), size(sz, 22, 19, 16));

        } else if (bw == ButtonWidget.BUTTON_GRADIENT) {
            return BasicLayoutInfo.getInstance();

        } else if (bw == ButtonWidget.BUTTON_RECESSED) {
            return BasicLayoutInfo.createFixedHeight(size(sz, 18, 16, 14));

        } else if (bw == ButtonWidget.BUTTON_INLINE) {
            // Note that the NSView does not limit the size, but there seems to be an intended fixed size.
            return BasicLayoutInfo.createFixedHeight(17);

        } else if (bw == ButtonWidget.BUTTON_ROUNDED_RECT) {
            return BasicLayoutInfo.createFixedHeight(size(sz, 18, 16, 14));

        } else if (bw == ButtonWidget.BUTTON_TEXTURED || bw == ButtonWidget.BUTTON_TEXTURED_TOOLBAR) {
            return BasicLayoutInfo.createFixedHeight(size(sz, 23, 19, 16));

        } else if (bw == ButtonWidget.BUTTON_ROUND || bw == ButtonWidget.BUTTON_ROUND_TOOLBAR) {
            return BasicLayoutInfo.createFixed(size(sz, 20, 17, 14), size(sz, 21, 18, 15));

        } else if (bw == ButtonWidget.BUTTON_ROUND_INSET) {
            return BasicLayoutInfo.createFixed(18, 18);

        } else if (bw == ButtonWidget.BUTTON_ROUND_TEXTURED) {
            return BasicLayoutInfo.createFixed(size(sz, 21, 18, 15), size(sz, 22, 19, 16));

        } else if (bw == ButtonWidget.BUTTON_DISCLOSURE_TRIANGLE) {
            return BasicLayoutInfo.createFixed(9, 9);

        } else if (bw == ButtonWidget.BUTTON_PUSH_INSET2) {
            return BasicLayoutInfo.createFixedHeight(size(sz, 19, 17, 15));

        } else if (bw == ButtonWidget.BUTTON_COLOR_WELL) {
            return BasicLayoutInfo.createMinimum(44, 23);

        } else {
            return BasicLayoutInfo.getInstance();
        }
    }

    @Override
    public @Nullable Insetter getButtonLabelInsets(@NotNull ButtonLayoutConfiguration g)
    {
        ButtonWidget bw = g.getButtonWidget();

        Size sz = g.getSize();

        float top = 0;
        float bottom = 0;
        float left = 0;
        float right = 0;

        // these insets are minimums to avoid painting over the border

        if (bw == ButtonWidget.BUTTON_PUSH) {
            top = size(sz, 1, 2, 1);
            bottom = size(sz, 2, 2, 2);
            left = size(sz, 4, 4, 3);
            right = left;

        } else if (bw == ButtonWidget.BUTTON_BEVEL) {
            top = 1;
            bottom = 1;
            left = 1;
            right = 1;

        } else if (bw == ButtonWidget.BUTTON_BEVEL_ROUND) {
            top = size(sz, 1, 2, 1);
            bottom = size(sz, 2, 2, 2);
            left = size(sz, 4, 4, 3);
            right = left;

        } else if (bw == ButtonWidget.BUTTON_CHECK_BOX) {
            // labels are not supported
            return null;

        } else if (bw == ButtonWidget.BUTTON_RADIO) {
            // labels are not supported
            return null;

        } else if (bw == ButtonWidget.BUTTON_DISCLOSURE) {
            // labels are not supported
            return null;

        } else if (bw == ButtonWidget.BUTTON_HELP) {
            // labels are not supported
            return null;

        } else if (bw == ButtonWidget.BUTTON_GRADIENT) {
            top = 1;
            bottom = 1;
            left = 1;
            right = 1;

        } else if (bw == ButtonWidget.BUTTON_RECESSED) {
            top = bottom = 1;
            left = right = size(sz, 4, 3, 3);

        } else if (bw == ButtonWidget.BUTTON_INLINE) {
            top = 2;  // could be 1.5
            bottom = 2;
            left = right = size(sz, 6, 5, 5);

        } else if (bw == ButtonWidget.BUTTON_ROUNDED_RECT) {
            top = bottom = 1;
            left = right = 4;

        } else if (bw == ButtonWidget.BUTTON_TEXTURED || bw == ButtonWidget.BUTTON_TEXTURED_TOOLBAR) {
            top = 0.51f;
            bottom = 1.49f;
            left = right = 3;

        } else if (bw == ButtonWidget.BUTTON_ROUND || bw == ButtonWidget.BUTTON_ROUND_TOOLBAR) {
            top = left = right = size2D(sz, 4, 3.5, 3);
            bottom = top + 1;

        } else if (bw == ButtonWidget.BUTTON_ROUND_INSET) {
            left = right = 3;
            top = 3.5f;
            bottom = top;

        } else if (bw == ButtonWidget.BUTTON_ROUND_TEXTURED) {
            top = left = right = size2D(sz, 3.5, 3, 2.5);
            bottom = top + 1;

        } else if (bw == ButtonWidget.BUTTON_DISCLOSURE_TRIANGLE) {
            // labels are not supported
            return null;

        } else if (bw == ButtonWidget.BUTTON_PUSH_INSET2) {
            top = size(sz, 1, 1, 1);
            bottom = size(sz, 1, 1, 1);
            left = size(sz, 7, 6, 6);
            right = left;

        } else if (bw == ButtonWidget.BUTTON_TOOLBAR_ITEM) {
            top = 2;
            bottom = 3;
            left = right = 4;

        } else if (bw == ButtonWidget.BUTTON_COLOR_WELL) {
            // labels are not supported
        }

        LayoutInfo layoutInfo = getLayoutInfo(g);
        return Insetters.createFixed(top, left, bottom, right, layoutInfo);
    }

    @Override
    protected @NotNull LayoutInfo getSegmentedButtonLayoutInfo(@NotNull SegmentedButtonLayoutConfiguration g)
    {
        SegmentedButtonWidget bw = g.getWidget();
        Size sz = g.getSize();

        switch (bw) {
            case BUTTON_TAB:
            case BUTTON_SEGMENTED:
            case BUTTON_SEGMENTED_SEPARATED:
            case BUTTON_SEGMENTED_SLIDER:
            case BUTTON_SEGMENTED_SLIDER_TOOLBAR:
            case BUTTON_SEGMENTED_SLIDER_TOOLBAR_ICONS:
                return BasicLayoutInfo.createFixedHeight(size(sz, 22, 19, 16));

            case BUTTON_SEGMENTED_INSET:
                return BasicLayoutInfo.createFixedHeight(size(sz, 18, 16, 14));

            case BUTTON_SEGMENTED_SCURVE:
            case BUTTON_SEGMENTED_TEXTURED:
            case BUTTON_SEGMENTED_TEXTURED_TOOLBAR:
            case BUTTON_SEGMENTED_TEXTURED_TOOLBAR_ICONS:
            case BUTTON_SEGMENTED_TOOLBAR:
            case BUTTON_SEGMENTED_TEXTURED_SEPARATED:
            case BUTTON_SEGMENTED_TEXTURED_SEPARATED_TOOLBAR:
            case BUTTON_SEGMENTED_TEXTURED_SEPARATED_TOOLBAR_ICONS:
                return BasicLayoutInfo.createFixedHeight(size(sz, 23, 19, 16));

            case BUTTON_SEGMENTED_SMALL_SQUARE:
                return BasicLayoutInfo.createFixedHeight(size(sz, 21, 19, 17));

            default:
                throw new UnsupportedOperationException();
        }
    }

    @Override
    public @NotNull Insetter getSegmentedButtonLabelInsets(@NotNull SegmentedButtonLayoutConfiguration g)
    {
        SegmentedButtonWidget bw = g.getWidget();
        Position pos = g.getPosition();

        LayoutInfo layoutInfo = getLayoutInfo(g);
        float top = 1;
        float bottom = 2;
        float left = 1;
        float right = left;
        float endAdjust = 3;

        boolean isLeftEnd = pos == Position.FIRST || pos == Position.ONLY;
        boolean isRightEnd = pos == Position.LAST || pos == Position.ONLY;

        switch (bw) {
            case BUTTON_TAB:
            case BUTTON_SEGMENTED:
            case BUTTON_SEGMENTED_SLIDER:
                break;
            case BUTTON_SEGMENTED_SEPARATED:
                endAdjust = 0;
                left = right = 3;
                break;
            case BUTTON_SEGMENTED_INSET:
                bottom = 1;
                break;
            case BUTTON_SEGMENTED_SCURVE:
            case BUTTON_SEGMENTED_TEXTURED:
            case BUTTON_SEGMENTED_TEXTURED_TOOLBAR:
            case BUTTON_SEGMENTED_TOOLBAR:
                endAdjust = 2;
                break;
            case BUTTON_SEGMENTED_TEXTURED_SEPARATED:
            case BUTTON_SEGMENTED_TEXTURED_SEPARATED_TOOLBAR:
            case BUTTON_SEGMENTED_TEXTURED_SEPARATED_TOOLBAR_ICONS:
                endAdjust = 0;
                left = right = 3;
                break;
            case BUTTON_SEGMENTED_SMALL_SQUARE:
                bottom = 1;
                endAdjust = 0;
                break;
            default:
                throw new UnsupportedOperationException();
        }

        if (isLeftEnd) {
            left += endAdjust;
        }

        if (isRightEnd) {
            right += endAdjust;
        }

        return Insetters.createFixed(top, left, bottom, right, layoutInfo);
    }

    @Override
    protected @NotNull LayoutInfo getComboBoxLayoutInfo(@NotNull ComboBoxLayoutConfiguration g)
    {
        ComboBoxWidget bw = g.getWidget();
        Size sz = g.getSize();

        if (bw == ComboBoxWidget.BUTTON_COMBO_BOX_CELL) {
            return BasicLayoutInfo.createMinimumHeight(size(sz, 14, 11, 11));

        } else if (bw == ComboBoxWidget.BUTTON_COMBO_BOX_TEXTURED || bw == ComboBoxWidget.BUTTON_COMBO_BOX_TEXTURED_TOOLBAR) {
            float fixedHeight = size(sz, 23, 19, 16);
            float minWidth = size(sz, 27, 24, 22);
            return BasicLayoutInfo.create(false, minWidth, true, fixedHeight);

        } else {
            float fixedHeight = size(sz, 22, 19, 15);
            float minWidth = size(sz, 27, 24, 22);
            return BasicLayoutInfo.create(false, minWidth, true, fixedHeight);
        }
    }

    @Override
    public @NotNull Insetter getComboBoxIndicatorInsets(@NotNull ComboBoxLayoutConfiguration g)
    {
        int indicatorWidth;

        ComboBoxWidget w = g.getWidget();
        if (w == ComboBoxWidget.BUTTON_COMBO_BOX_TEXTURED || w == ComboBoxWidget.BUTTON_COMBO_BOX_TEXTURED_TOOLBAR) {
            indicatorWidth = 18;
        } else {
            switch (g.getSize()) {
                case SMALL:
                    indicatorWidth = 19;
                    break;
                case MINI:
                    indicatorWidth = 16;
                    break;
                default:
                    indicatorWidth = 21;
            }
        }

        return g.isLeftToRight()
                 ? Insetters.createRightAligned(indicatorWidth, 0, 0, 0)
                 : Insetters.createLeftAligned(indicatorWidth, 0, 0, 0);
    }

    @Override
    public @NotNull Insetter getComboBoxEditorInsets(@NotNull ComboBoxLayoutConfiguration g)
    {
        ComboBoxWidget bw = g.getWidget();
        Size sz = g.getSize();

        if (bw == ComboBoxWidget.BUTTON_COMBO_BOX_CELL) {
            float inset = size(sz, 18, 18, 13);
            return g.isLeftToRight() ? Insetters.createFixed(0, 0, 0, inset) : Insetters.createFixed(0, inset, 0, 0);
        }

        LayoutInfo layoutInfo = getLayoutInfo(g);
        float near;
        float far;
        float top;
        float bottom;

        if (bw == ComboBoxWidget.BUTTON_COMBO_BOX_TEXTURED || bw == ComboBoxWidget.BUTTON_COMBO_BOX_TEXTURED_TOOLBAR) {
            // The inactive rendering is not as tall as the active rendering. Need to take that into account.
            // near 19 works on Yosemite but not on El Capitan
            near = bw == ComboBoxWidget.BUTTON_COMBO_BOX_TEXTURED_TOOLBAR ? 20 : 19;
            far = 2.5f;
            top = 1;
            bottom = 2;
        } else {
            near = size2D(sz, 18.5, 16.5, 14.5);
            far = 0.5f;
            top = 1;
            bottom = size2D(sz, 2, 2, 1);
        }

        return g.isLeftToRight()
                 ? Insetters.createFixed(top, far, bottom, near, layoutInfo)
                 : Insetters.createFixed(top, near, bottom, far, layoutInfo);
    }

    @Override
    protected @NotNull LayoutInfo getPopUpButtonLayoutInfo(@NotNull PopupButtonLayoutConfiguration g)
    {
        // On Yosemite and El Capitan, the square style bombs if the mini size is selected.
        // See rendering code, which must be consistent.

        PopupButtonWidget bw = g.getPopupButtonWidget();
        Size sz = g.getSize();
        boolean isSquare = bw == PopupButtonWidget.BUTTON_POP_UP_SQUARE || bw == PopupButtonWidget.BUTTON_POP_DOWN_SQUARE;
        boolean isArrowsOnly = bw == PopupButtonWidget.BUTTON_POP_UP_CELL || bw == PopupButtonWidget.BUTTON_POP_DOWN_CELL;

        if ((isSquare || isArrowsOnly) && sz == Size.MINI) {
            sz = Size.SMALL;
        }

        switch (bw) {
            case BUTTON_POP_UP:
            case BUTTON_POP_DOWN:
            {
                float fixedHeight = size(sz, 22, 19, 16);
                float minWidth = size(sz, 25, 24, 20);
                return BasicLayoutInfo.create(false, minWidth, true, fixedHeight);
            }

            case BUTTON_POP_UP_CELL:
            case BUTTON_POP_DOWN_CELL:
                return BasicLayoutInfo.createMinimumHeight(size(sz, 12, 10, 10));

            case BUTTON_POP_UP_SQUARE:
            case BUTTON_POP_DOWN_SQUARE:
                return BasicLayoutInfo.createFixedHeight(size(sz, 23, 20, 17));

            case BUTTON_POP_DOWN_BEVEL:
            case BUTTON_POP_UP_BEVEL:
                return BasicLayoutInfo.createFixedHeight(22);

            case BUTTON_POP_DOWN_ROUND_RECT:
            case BUTTON_POP_UP_ROUND_RECT:
                return BasicLayoutInfo.createFixedHeight(size(sz, 18, 16, 14));

            case BUTTON_POP_DOWN_RECESSED:
            case BUTTON_POP_UP_RECESSED:
                return BasicLayoutInfo.createFixedHeight(size(sz, 18, 16, 14));

            case BUTTON_POP_DOWN_TEXTURED:
            case BUTTON_POP_UP_TEXTURED:
            case BUTTON_POP_DOWN_TEXTURED_TOOLBAR:
            case BUTTON_POP_UP_TEXTURED_TOOLBAR:
                float fixedHeight = size(sz, 23, 19, 16);
                float minWidth = size(sz, 25, 24, 20);
                return BasicLayoutInfo.create(false, minWidth, true, fixedHeight);

            case BUTTON_POP_DOWN_GRADIENT:
            case BUTTON_POP_UP_GRADIENT:
                return BasicLayoutInfo.createFixedHeight(21);

            default:
                throw new UnsupportedOperationException();
        }
    }

    @Override
    public @NotNull Insetter getPopUpArrowInsets(@NotNull PopupButtonConfiguration g)
    {
        // used when using Core UI to paint the arrows
        // only regular and small sizes are used
        PopupButtonWidget w = g.getPopupButtonWidget();
        boolean isCell = g.isCell();
        Size sz = g.getSize();
        float buttonHeight = isCell ? size(sz, 12, 10, 10) : size(sz, 23, 20, 17);
        float width = size(sz, 7, 7, 7);    // small width is 6 at 2x
        float height;
        float top;
        float bottom;
        float right;
        if (g.isPopUp()) {
            height = size(sz, 12, 10, 10);    // mini height is 9 at 2x
            top = isCell ? size2D(sz, 0, 0, 0) : size2D(sz, 7, 5.5, 5.5);
            if (w == PopupButtonWidget.BUTTON_POP_UP_GRADIENT) {
                top -= 1;
            } else if (w == PopupButtonWidget.BUTTON_POP_UP_BEVEL) {
                top -= 2;
            } else if (w == PopupButtonWidget.BUTTON_POP_UP_RECESSED) {
                top -= size2D(sz, 1.5, 0, 2);
            } else if (w == PopupButtonWidget.BUTTON_POP_UP_TEXTURED || w == PopupButtonWidget.BUTTON_POP_UP_TEXTURED_TOOLBAR) {
                top -= size2D(sz, 2, 1, 2);
            }
            bottom = buttonHeight - top - height;
            right = isCell ? 2 : 5;
        } else {
            height = 5;
            top = isCell ? size2D(sz, 3.5, 2.5, 2.5) : size2D(sz, 9, 7.5, 7.5);
            bottom = buttonHeight - top - height;
            right = isCell ? 3 : 6;
        }

        Insetter1 horizontal = g.isLeftToRight()
                                 ? FloatingInsetter1.createRightBottomAligned(width, right)
                                 : FloatingInsetter1.createLeftTopAligned(width, right);
        Insetter1 vertical = FloatingInsetter1.createCentered(height, top, bottom);
        return new CombinedInsetter(horizontal, vertical);
    }

    @Override
    public @NotNull Insetter getPopupButtonContentInsets(@NotNull PopupButtonLayoutConfiguration g)
    {
        // On Yosemite, the square style bombs if the mini size is selected.
        // See rendering code, which must be consistent.

        PopupButtonWidget bw = g.getPopupButtonWidget();
        Size sz = g.getSize();

        boolean isSquare = bw == PopupButtonWidget.BUTTON_POP_UP_SQUARE || bw == PopupButtonWidget.BUTTON_POP_DOWN_SQUARE;
        boolean isArrowsOnly = bw == PopupButtonWidget.BUTTON_POP_UP_CELL || bw == PopupButtonWidget.BUTTON_POP_DOWN_CELL;

        if ((isSquare || isArrowsOnly) && sz == Size.MINI) {
            sz = Size.SMALL;
        }

        float top = 1;
        float bottom = 1;
        float far = 1;
        float near;

        switch (bw)
        {
            case BUTTON_POP_UP:
            default:
                far = 3;
                near = size2D(sz, 17, 15, 13);
                bottom = size2D(sz, 2.5, 2.5, 2);
                top = size2D(sz, 0.5, 0.5, 1);
                break;

            case BUTTON_POP_UP_SQUARE:
                near = size2D(sz, 16, 15, 15);
                bottom = 1;
                break;

            case BUTTON_POP_UP_CELL:
                near = size2D(sz, 10.5, 9.5, 9.5);
                return Insetters.createFixed(0, 0, 0, near);

            case BUTTON_POP_UP_ROUND_RECT:
            case BUTTON_POP_DOWN_ROUND_RECT:
                far = 3;
                near = size2D(sz, 17, 15, 13);
                break;

            case BUTTON_POP_UP_RECESSED:
            case BUTTON_POP_DOWN_RECESSED:
                near = size2D(sz, 17, 15, 13);
                break;

            case BUTTON_POP_UP_GRADIENT:
            case BUTTON_POP_DOWN_GRADIENT:
                near = 16;
                break;

            case BUTTON_POP_UP_BEVEL:
            case BUTTON_POP_DOWN_BEVEL:
                far = 3;
                bottom = 2;
                near = 15;
                break;

            case BUTTON_POP_UP_TEXTURED:
            case BUTTON_POP_DOWN_TEXTURED:
            case BUTTON_POP_UP_TEXTURED_TOOLBAR:
            case BUTTON_POP_DOWN_TEXTURED_TOOLBAR:
                // The inactive rendering is not as tall as the active rendering. Need to take that into account.
                top = 1;
                bottom = 2;
                far = 2;
                near = size2D(sz, 17, 15, 13);
                break;

            case BUTTON_POP_DOWN:
                far = 3;
                near = size2D(sz, 17, 15, 13);
                bottom = 2;
                top = 1;
                break;

            case BUTTON_POP_DOWN_SQUARE:
                near = size2D(sz, 18, 18, 16);
                break;

            case BUTTON_POP_DOWN_CELL:
                near = size2D(sz, 13, 12.5, 12);
                return Insetters.createFixed(0, 0, 0, near);
        }

        LayoutInfo layoutInfo = getLayoutInfo(g);
        return g.isLeftToRight()
                 ? Insetters.createFixed(top, far, bottom, near, layoutInfo)
                 : Insetters.createFixed(top, near, bottom, far, layoutInfo);
    }

    @Override
    protected @NotNull LayoutInfo getToolBarItemWellLayoutInfo(@NotNull ToolBarItemWellLayoutConfiguration g)
    {
        return BasicLayoutInfo.getInstance();    // TBD
    }

    @Override
    protected @NotNull LayoutInfo getTitleBarLayoutInfo(@NotNull TitleBarLayoutConfiguration g)
    {
        switch (g.getWidget())
        {
            case DOCUMENT_WINDOW:
                return BasicLayoutInfo.createFixedHeight(22);
            case UTILITY_WINDOW:
                return BasicLayoutInfo.createFixedHeight(16);
            default:
                throw new UnsupportedOperationException();
        }
    }

    @Override
    public @NotNull Insetter getTitleBarButtonInsets(@NotNull TitleBarLayoutConfiguration g,
                                                     @NotNull TitleBarButtonWidget bw)
    {
        TitleBarLayoutInfo layoutInfo = AquaNativePainter.getTitleBarLayoutInfo();
        return layoutInfo.getButtonInsets(g, bw);
    }

    @Override
    public @NotNull Shape getTitleBarButtonShape(@NotNull Rectangle2D bounds,
                                                 @NotNull TitleBarLayoutConfiguration g,
                                                 @NotNull TitleBarButtonWidget bw)
    {
        TitleBarLayoutInfo layoutInfo = AquaNativePainter.getTitleBarLayoutInfo();
        return layoutInfo.getButtonShape(bounds, g, bw);
    }

    @Override
    public @Nullable Insetter getTitleBarLabelInsets(@NotNull TitleBarLayoutConfiguration g)
    {
        TitleBarLayoutInfo layoutInfo = AquaNativePainter.getTitleBarLayoutInfo();
        return layoutInfo.getLabelInsets(g);
    }

    @Override
    public @Nullable TitleBarButtonWidget identifyTitleBarButton(@NotNull Rectangle2D bounds,
                                                                 @NotNull TitleBarLayoutConfiguration g, int x, int y)
    {
        TitleBarLayoutInfo layoutInfo = AquaNativePainter.getTitleBarLayoutInfo();
        return layoutInfo.identifyButton(bounds, g, x, y);
    }

    @Override
    protected @NotNull LayoutInfo getSliderLayoutInfo(@NotNull SliderLayoutConfiguration g)
    {
        Size sz = g.getSize();
        boolean hasTickMarks = g.hasTickMarks();

        // There is some extra space at the ends, but it is not needed. The knob does not clip even at the extremes.

        switch (g.getWidget())
        {
            case SLIDER_HORIZONTAL:
            case SLIDER_HORIZONTAL_RIGHT_TO_LEFT:
                return BasicLayoutInfo.createFixedHeight(hasTickMarks ? size(sz, 24, 18, 16) : size(sz, 18, 14, 12));

            case SLIDER_VERTICAL:
            case SLIDER_UPSIDE_DOWN:
                return BasicLayoutInfo.createFixedWidth(hasTickMarks ? size(sz, 24, 18, 16) : size(sz, 18, 14, 12));

            case SLIDER_CIRCULAR:
                int width = hasTickMarks ? size(sz, 36, 25, 25) : size(sz, 28, 21, 21);
                int height = hasTickMarks ? size(sz, 36, 25, 25) : size(sz, 28, 21, 21);
                return BasicLayoutInfo.createFixed(width, height);

            default:
                throw new UnsupportedOperationException();
        }
    }

    @Override
    public @NotNull Rectangle2D getSliderThumbBounds(@NotNull Rectangle2D bounds,
                                                     @NotNull SliderLayoutConfiguration g,
                                                     double thumbPosition)
    {
        if (true) {
            Insetter insets = getSliderThumbInsets(g, thumbPosition);
            return insets.applyToBounds2D(bounds);
        }

        double x = bounds.getX();
        double y = bounds.getY();
        double w = bounds.getWidth();
        double h = bounds.getHeight();

        if (w <= 0 || h <= 0) {
            return new Rectangle(0, 0, 0, 0);
        }

        SliderWidget sw = g.getWidget();
        if (sw == SliderWidget.SLIDER_HORIZONTAL || sw == SliderWidget.SLIDER_HORIZONTAL_RIGHT_TO_LEFT) {
            if (h >= w) {
                h = Math.max(0, w - 1);
            }
        } else if (sw == SliderWidget.SLIDER_VERTICAL || sw == SliderWidget.SLIDER_UPSIDE_DOWN) {
            if (w >= h) {
                w = Math.max(0, h - 1);
            }
        }

        if (sw != SliderWidget.SLIDER_CIRCULAR) {
            Size sz = g.getSize();
            boolean hasTickMarks = g.hasTickMarks();
            int diameter = hasTickMarks ? size(sz, 17, 12, 12) : size(sz, 16, 12, 12);
            if (sw == SliderWidget.SLIDER_HORIZONTAL || sw == SliderWidget.SLIDER_HORIZONTAL_RIGHT_TO_LEFT) {
                double cx = getSliderThumbCenter(bounds, g, thumbPosition);
                double tx = cx - diameter / 2;
                if (!hasTickMarks) {
                    double ty = y + h / 2 - diameter / 2;
                    return new Rectangle2D.Double(tx, ty, diameter, diameter);
                }
                return new Rectangle2D.Double(tx, y, diameter, h);
            } else {
                double cy = getSliderThumbCenter(bounds, g, thumbPosition);
                double ty = cy - diameter / 2;
                if (!hasTickMarks) {
                    double tx = x + w / 2 - diameter / 2;
                    return new Rectangle2D.Double(tx, ty, diameter, diameter);
                }
                return new Rectangle2D.Double(x, ty, w, diameter);
            }
        }

        // TBD: circular slider only supported by NSView at this time
        return new Rectangle(0, 0, 0, 0);
    }

    @Override
    public @NotNull Insetter getSliderTrackPaintingInsets(@NotNull SliderLayoutConfiguration g)
    {
        float trackWidth = 3;

        Insetter1 horizontal;
        Insetter1 vertical;
        if (g.isHorizontal()) {
            float majorInset = JNRUtils.size(g.getSize(), 3, 1, 1);
            float minorOffset = getTrackOffset(g);
            horizontal = new FixedInsetter1(majorInset, majorInset);
            vertical = CenteredInsetter1.createCentered(trackWidth, minorOffset);
        } else if (g.isVertical()) {
            float majorInset = JNRUtils.size(g.getSize(), 3, 1, 1);
            float minorOffset = getTrackOffset(g);
            vertical = new FixedInsetter1(majorInset, majorInset);
            horizontal = CenteredInsetter1.createCentered(trackWidth, minorOffset);
        } else {
            // not meaningful
            horizontal = new FixedInsetter1(0, 0);
            vertical = new FixedInsetter1(0, 0);
        }
        return new CombinedInsetter(horizontal, vertical);
    }

    protected float getTrackOffset(@NotNull SliderLayoutConfiguration g)
    {
        if (g.hasTickMarks()) {
            TickMarkPosition position = g.getTickMarkPosition();
            Size sz = g.getSize();
            switch (position)
            {
                case ABOVE:
                case LEFT:
                    return JNRUtils.size(sz, 5, 3, 3);
                case BELOW:
                case RIGHT:
                    return JNRUtils.size(sz, -5, -3, -3);
            }
        }

        return 0;
    }

    @Override
    public @NotNull Insetter getSliderThumbInsets(@NotNull SliderLayoutConfiguration g, double thumbPosition)
    {
        return getSliderThumbInsets(g, thumbPosition, false);
    }

    @Override
    public @NotNull Insetter getSliderThumbPaintingInsets(@NotNull SliderLayoutConfiguration g, double thumbPosition)
    {
        return getSliderThumbInsets(g, thumbPosition, true);
    }

    protected @NotNull Insetter getSliderThumbInsets(@NotNull SliderLayoutConfiguration g,
                                                     double thumbPosition,
                                                     boolean isForPainting)
    {
        SliderWidget sw = g.getWidget();

        if (sw != SliderWidget.SLIDER_CIRCULAR) {
            LayoutInfo layoutInfo = getSliderThumbLayoutInfo(g, isForPainting);
            LayoutInfo visualLayoutInfo = isForPainting ? getSliderThumbLayoutInfo(g, false) : layoutInfo;

            // For alignment purposes, we want the layout that corresponds to the outline, not the one that
            // describes the full rendering including shadows.

            if (g.isHorizontal()) {
                float offset = getPointerOffset(g);
                Insetter1 vertical = CenteredInsetter1.createCentered(layoutInfo.getFixedVisualHeight(), visualLayoutInfo.getFixedVisualHeight(), offset);
                Insetter1 horizontal = new DynamicInsetter1(layoutInfo.getFixedVisualWidth())
                {
                    @Override
                    protected float getCenterPosition(float componentSize)
                    {
                        Rectangle2D bounds = new Rectangle2D.Float(0, 0, componentSize, 30);
                        return (float) getSliderThumbCenter(bounds, g, thumbPosition);
                    }
                };
                return new CombinedInsetter(horizontal, vertical);
            } else {
                float offset = getPointerOffset(g);
                Insetter1 horizontal = CenteredInsetter1.createCentered(layoutInfo.getFixedVisualWidth(), visualLayoutInfo.getFixedVisualWidth(), offset);
                Insetter1 vertical = new DynamicInsetter1(layoutInfo.getFixedVisualHeight())
                {
                    @Override
                    protected float getCenterPosition(float componentSize)
                    {
                        Rectangle2D bounds = new Rectangle2D.Float(0, 0, 30, componentSize);
                        return (float) getSliderThumbCenter(bounds, g, thumbPosition);
                    }
                };
                return new CombinedInsetter(horizontal, vertical);
            }
        }

        // TBD: circular slider only supported by NSView at this time
        return Insetters.createFixed(0, 0, 0, 0);
    }

    /**
      Determine a position adjustment along the minor axis for painting linear slider tick marks.
    */

    protected float getPointerOffset(@NotNull SliderLayoutConfiguration g)
    {
        // TBD: there is some kind of round off that I am not capturing systematically

        if (!g.hasTickMarks()) {
            if (g.isHorizontal()) {
                Size sz = g.getSize();
                return size2D(sz, 0, -0.5, 0);
            } else {
                return 0;
            }
        }

        Size sz = g.getSize();
        switch (g.getTickMarkPosition())
        {
            case LEFT:
                return size2D(sz, 2.5, 1.5, 1.5);
            case RIGHT:
                return size2D(sz, -2, -1.5, -1.5);
            case ABOVE:
                return size2D(sz, 1.5, 2, 2);
            case BELOW:
                return size2D(sz, -2, -1.5, -1.5);
            default:
                throw new UnsupportedOperationException();
        }
    }

    // supports evaluation
    @Override
    public @NotNull LayoutInfo getSliderThumbLayoutInfo(@NotNull SliderLayoutConfiguration g)
    {
        return getSliderThumbLayoutInfo(g, true);
    }

    /**
      Return information about the fixed height and width of a slider thumb.
      @param g The slider configuration.
      @param isForPainting True to return the size for painting. False to return the size for computing an outline.
      @return the layout information.
    */
    protected @NotNull LayoutInfo getSliderThumbLayoutInfo(@NotNull SliderLayoutConfiguration g, boolean isForPainting)
    {
        SliderWidget sw = g.getWidget();
        if (sw == SliderWidget.SLIDER_CIRCULAR) {
            return BasicLayoutInfo.createFixed(0, 0);
        }

        Size sz = g.getSize();
        if (g.hasTickMarks()) {
            if (g.isHorizontal()) {
                float width = isForPainting ? size(sz, 17, 14, 14) : size(sz, 17, 13, 13);
                float height = isForPainting ? size(sz, 20, 14, 14) : size2D(sz, 19, 14, 14);
                return BasicLayoutInfo.createFixed(width, height);
            } else {
                float width = isForPainting ? size(sz, 19, 14, 14) : size2D(sz, 19, 14, 14);
                float height = isForPainting ? size(sz, 18, 14, 14) : size(sz, 17, 13, 13);
                return BasicLayoutInfo.createFixed(width, height);
            }
        } else {
            // On a 2x display, the visual diameter is 16 points (regular) and 12 points (small). However, to take
            // advantage of that more accurate visual diameter would require a custom 2x renderer description to
            // top-left align the circle.
            float width = size(sz, 17, 13, 13);
            float height = isForPainting ? size(sz, 18, 14, 14) : width;
            return BasicLayoutInfo.createFixed(width, height);
        }
    }

    @Override
    public @NotNull Insetter getSliderTickMarkPaintingInsets(@NotNull SliderLayoutConfiguration g)
    {
        return Insetter.trivial();
    }

    // supports evaluation
    @Override
    public @NotNull LayoutInfo getSliderTickLayoutInfo(@NotNull SliderLayoutConfiguration g)
    {
        if (g.isLinear() && g.hasTickMarks()) {
            Size sz = g.getSize();
            float thickness = JNRUtils.size(sz, 2, 2, 1);
            float length = JNRUtils.size(sz, 8, 8, 7);
            if (g.isHorizontal()) {
                return BasicLayoutInfo.createFixed(thickness, length);
            } else {
                return BasicLayoutInfo.createFixed(length, thickness);
            }
        }
        return BasicLayoutInfo.createFixed(0, 0);
    }

    @Override
    public @NotNull Rectangle2D getSliderLabelBounds(@NotNull Rectangle2D bounds,
                                                     @NotNull SliderLayoutConfiguration g,
                                                     double thumbPosition,
                                                     @NotNull Dimension size)
    {
        SliderWidget sw = g.getWidget();
        Size sz = g.getSize();
        TickMarkPosition position = g.getTickMarkPosition();

        double left = bounds.getX();
        double top = bounds.getY();
        double w = bounds.getWidth();
        double h = bounds.getHeight();

        // In a linear slider, the track extends beyond the extreme positions of the knob.
        // We must determine the track positions that correspond to the minimum and maximum value.

        double offset;
        switch (sz) {
            case SMALL:
                offset = 6.5;
                break;
            case MINI:
                offset = 5;
                break;
            default:
                offset = 9;
        }

        int separation = 2;

        if (sw == SliderWidget.SLIDER_HORIZONTAL_RIGHT_TO_LEFT) {
            thumbPosition = 1 - thumbPosition;
            sw = SliderWidget.SLIDER_HORIZONTAL;
        } else if (sw == SliderWidget.SLIDER_UPSIDE_DOWN) {
            thumbPosition = 1 - thumbPosition;
            sw = SliderWidget.SLIDER_VERTICAL;
        }

        if (sw == SliderWidget.SLIDER_HORIZONTAL) {
            double width = w - 2 * offset;
            double centerX = offset + thumbPosition * width;
            double topY = position == TickMarkPosition.BELOW ? h + separation : - (separation + size.height);
            double leftX = centerX - (size.width / 2);
            return new Rectangle2D.Double(left + leftX, top + topY, size.width, size.height);

        } else if (sw == SliderWidget.SLIDER_VERTICAL) {
            double height = h - 2 * offset;
            double centerY = offset + (1 - thumbPosition) * height;
            double leftX = position == TickMarkPosition.RIGHT ? w + separation : - (separation + size.width);
            double topY = centerY - (size.height / 2);
            return new Rectangle2D.Double(left + leftX, top + topY, size.width, size.height);

        } else if (sw == SliderWidget.SLIDER_CIRCULAR) {
            // TBD
            return new Rectangle2D.Double(left, top, size.width, size.height);

        } else {
            return new Rectangle2D.Double(left, top, 0, 0);
        }
    }

    @Override
    public double getSliderThumbCenter(@NotNull Rectangle2D bounds,
                                       @NotNull SliderLayoutConfiguration g,
                                       double thumbPosition)
    {
        SliderWidget sw = g.getWidget();
        Size sz = g.getSize();

        double left = bounds.getX();
        double top = bounds.getY();
        double w = bounds.getWidth();
        double h = bounds.getHeight();

        // In a linear slider, the track extends beyond the extreme positions of the knob.
        // We must compensate for these extensions.

        double extension = getSliderExtension(sz);

        if (sw == SliderWidget.SLIDER_HORIZONTAL_RIGHT_TO_LEFT) {
            thumbPosition = 1 - thumbPosition;
            sw = SliderWidget.SLIDER_HORIZONTAL;
        } else if (sw == SliderWidget.SLIDER_VERTICAL) {
            thumbPosition = 1 - thumbPosition;
            sw = SliderWidget.SLIDER_UPSIDE_DOWN;
        }

        if (sw == SliderWidget.SLIDER_HORIZONTAL) {
            double width = w - 2 * extension;
            return left + extension + thumbPosition * width;

        } else if (sw == SliderWidget.SLIDER_UPSIDE_DOWN) {
            double height = h - 2 * extension;
            return top + extension + thumbPosition * height;

        } else {
            return 0;
        }
    }

    @Override
    public double getSliderThumbPosition(@NotNull Rectangle2D bounds, @NotNull SliderLayoutConfiguration g, int x, int y)
    {
        SliderWidget sw = g.getWidget();
        Size sz = g.getSize();

        double left = bounds.getX();
        double top = bounds.getY();
        double w = bounds.getWidth();
        double h = bounds.getHeight();

        // In a linear slider, the track extends beyond the extreme positions of the knob.
        // We must determine the track positions that correspond to the minimum and maximum value.

        double extension = getSliderExtension(sz);

        if (sw == SliderWidget.SLIDER_HORIZONTAL) {
            left += extension;
            if (x < left) {
                return -10;
            }
            double right = left + w - 2 * extension;
            if (x > right) {
                return 10;
            }
            double range = right - left;
            if (range == 0) {
                return 0;
            }
            return (x - left) / range;

        } else if (sw == SliderWidget.SLIDER_HORIZONTAL_RIGHT_TO_LEFT) {
            left += extension;
            if (x < left) {
                return 10;
            }
            double right = left + w - 2 * extension;
            if (x > right) {
                return -10;
            }
            double range = right - left;
            if (range == 0) {
                return 0;
            }
            return 1 - (x - left) / range;

        } else if (sw == SliderWidget.SLIDER_VERTICAL) {
            top += extension;
            if (y < top) {
                return 10;
            }
            double bottom = top + h - 2 * extension;
            if (y > bottom) {
                return -10;
            }
            double range = bottom - top;
            if (range == 0) {
                return 0;
            }
            return 1 - (y - top) / range;

        } else if (sw == SliderWidget.SLIDER_UPSIDE_DOWN) {

            top += extension;
            if (y < top) {
                return -10;
            }
            double bottom = top + h - 2 * extension;
            if (y > bottom) {
                return 10;
            }
            double range = bottom - top;
            if (range == 0) {
                return 0;
            }
            return (y - top) / range;

        } else if (sw == SliderWidget.SLIDER_CIRCULAR) {
            double xcenter = left + w / 2.0;
            double ycenter = top + h / 2.0;
            double angle = Math.atan2(y - ycenter, x - xcenter);
            // Because Y increases downward, we get the desired clockwise increase.
            // The remaining conversion ensures that the midpoint of the range is at the bottom of the dial.
            angle += Math.PI / 2;
            if (angle < 0) {
                angle += 2 * Math.PI;
            }
            return angle / (2 * Math.PI);

        } else {
            return 0;
        }
    }

    protected double getSliderExtension(@NotNull Size sz)
    {
        switch (sz)
        {
            case MINI:
                return 5.5;

            case SMALL:
                return 6.5;

            default:
                return 10;
        }
    }

    @Override
    protected @NotNull LayoutInfo getSpinnerArrowsLayoutInfo(@NotNull SpinnerArrowsLayoutConfiguration g)
    {
        Size sz = g.getSize();
        int width = size(sz, 13, 11, 9);
        int height = size(sz, 23, 20, 16);
        return BasicLayoutInfo.createFixed(width, height);
    }

    @Override
    protected @NotNull LayoutInfo getSplitPaneDividerLayoutInfo(@NotNull SplitPaneDividerLayoutConfiguration g)
    {
        // Automatic layout calculation does not work well for these
        Orientation o = g.getOrientation();

        int d;

        switch (g.getWidget())
        {
            case THIN_DIVIDER:
                d = 1;
                break;
            case THICK_DIVIDER:
                d = 9;
                break;
            case PANE_SPLITTER:
                d = 10;
                break;
            default:
                throw new UnsupportedOperationException();
        }

        return o == Orientation.HORIZONTAL ? BasicLayoutInfo.createFixedHeight(d) : BasicLayoutInfo.createFixedWidth(d);
    }

    @Override
    protected @NotNull LayoutInfo getGroupBoxLayoutInfo(@NotNull GroupBoxLayoutConfiguration g)
    {
        return BasicLayoutInfo.getInstance();
    }

    @Override
    protected @NotNull LayoutInfo getListBoxLayoutInfo(@NotNull ListBoxLayoutConfiguration g)
    {
        return BasicLayoutInfo.getInstance();
    }

    @Override
    protected @NotNull LayoutInfo getTextFieldLayoutInfo(@NotNull TextFieldLayoutConfiguration g)
    {
        AquaUIPainter.TextFieldWidget w = g.getWidget();
        if (w.isRound() || w.isSearch()) {
            if (w.isToolbar()) {
                // The actual sizes for small and mini are mostly bogus. We do not simulate this bug.
                return BasicLayoutInfo.createFixedHeight(size(g.getSize(), 23, 19, 16));
            } else {
                return BasicLayoutInfo.createFixedHeight(size(g.getSize(), 22, 19, 17));
            }
        }

        return BasicLayoutInfo.getInstance();
    }

    @Override
    public @NotNull Insetter getTextFieldTextInsets(@NotNull TextFieldLayoutConfiguration g)
    {
        TextFieldWidget tw = g.getWidget();
        Size sz = g.getSize();

        float top;
        float left;
        float right;
        float bottom;

        int d = 0;
        float gap = 6;

        if (tw.isSearch()) {
            if (tw.hasMenu()) {
                d = 4;
                gap = 3.5f;
            }
            if (g.isLeftToRight()) {
                Insetter insets = getSearchButtonPaintingInsets(g);
                if (insets != null) {
                    Rectangle2D bounds = insets.apply2D(100, 100);
                    left = (float) (bounds.getX() + bounds.getWidth() + gap);
                } else {
                    left = size(sz, 27+d, 26+d, 22+d);
                }
                right = size(sz, 22, 19, 16);
            } else {
                Insetter insets = getSearchButtonPaintingInsets(g);
                if (insets != null) {
                    Rectangle2D bounds = insets.apply2D(100, 100);
                    right = (float) (100 - bounds.getX() + gap);
                } else {
                    right = size(sz, 27+d, 26+d, 22+d);
                }
                left = size(sz, 22, 19, 16);
            }

            if (tw.isToolbar()) {
                top = size2D(sz, 3, 1.5, 1.5);
                bottom = size2D(sz, 3, 2, 1.5);
            } else {
                top = size2D(sz, 3, 1.5, 1.5);
                bottom = size2D(sz, 3, 1.5, 1.5);
            }

        } else switch (tw) {
            case TEXT_FIELD:
                top = bottom = left = right = 1;
                break;

            case TEXT_FIELD_ROUND:
                top = 1;
                bottom = 1;
                left = right = 2.5f;
                break;

            case TEXT_FIELD_ROUND_TOOLBAR:
                top = 1;
                bottom = 2;
                left = right = 2.5f;
                break;

            default:
                throw new UnsupportedOperationException();
        }

        LayoutInfo layoutInfo = getLayoutInfo(g);
        return Insetters.createFixed(top, left, bottom, right, layoutInfo);
    }

    @Override
    public @Nullable Insetter getSearchButtonInsets(@NotNull TextFieldLayoutConfiguration g)
    {
        float d = 0;
        float h;

        TextFieldWidget w = g.getWidget();
        if (w.isSearch()) {
            if (w.hasMenu()) {
                d = 4;
            }
            h = size(g.getSize(), 27, 26, 22);
            d += h;
            return g.isLeftToRight() ? Insetters.createLeftAligned(d, 0, 0, 0) : Insetters.createRightAligned(d, 0, 0, 0);
        }

        return null;
    }

    @Override
    public @Nullable Insetter getSearchButtonPaintingInsets(@NotNull TextFieldLayoutConfiguration g)
    {
        Size sz = g.getSize();

        float buttonHeight = size(g.getSize(), 22, 19, 17);

        LayoutInfo layoutInfo = getSearchButtonLayoutInfo(g);
        if (layoutInfo != null) {
            float width = layoutInfo.getFixedVisualWidth();
            float height = layoutInfo.getFixedVisualHeight();
            if (width > 0 && height > 0) {
                float top = size2D(sz, 5, 4, 3.5+1);  // +1 because we make the search field +2 taller
                float left = size2D(sz, 6.5, 6, 5.5);
                float bottom = buttonHeight - top - height;
                Insetter1 horizontal = g.isLeftToRight() ? FloatingInsetter1.createLeftTopAligned(width, left)
                                         : FloatingInsetter1.createRightBottomAligned(width, left);
                Insetter1 vertical = FloatingInsetter1.createCentered(height, top, bottom);
                return new CombinedInsetter(horizontal, vertical);
            }
        }

        return null;
    }

    @Override
    public @Nullable LayoutInfo getSearchButtonLayoutInfo(@NotNull TextFieldLayoutConfiguration g)
    {
        TextFieldWidget w = g.getWidget();

        if (!w.isSearch()) {
            return null;
        }

        boolean hasMenu = w.hasMenu();
        Size sz = g.getSize();

        // The visual size is smaller when rendered in 2x
        float height = size2D(sz, 13, 12, 10);
        float width = hasMenu ? size2D(sz, 17, 16, 14) : height;
        return BasicLayoutInfo.createFixed(width, height);
    }

    @Override
    public @Nullable Insetter getCancelButtonInsets(@NotNull TextFieldLayoutConfiguration g)
    {
        TextFieldWidget w = g.getWidget();
        if (w.hasCancel()) {
            Size sz = g.getSize();
            float d = size(sz, 14, 11, 9);
            float right = size(sz, 4, 4, 3);
            return g.isLeftToRight() ? Insetters.createRightAligned(d, right, 0, 0) : Insetters.createLeftAligned(d, right, 0, 0);
        }
        return null;
    }

    @Override
    public @Nullable Insetter getCancelButtonPaintingInsets(@NotNull TextFieldLayoutConfiguration g)
    {
        Size sz = g.getSize();

        float buttonHeight = size(g.getSize(), 22, 19, 17);

        LayoutInfo layoutInfo = getCancelButtonLayoutInfo(g);
        if (layoutInfo != null) {
            float width = layoutInfo.getFixedVisualWidth();
            float height = layoutInfo.getFixedVisualHeight();
            if (width > 0 && height > 0) {
                float top = size(sz, 4, 4, 3+1);  // +1 because we make the search field +2 taller
                float right = size(sz, 4, 4, 3);
                float bottom = buttonHeight - top - height;
                Insetter1 horizontal = g.isLeftToRight() ? FloatingInsetter1.createRightBottomAligned(width, right)
                                         : FloatingInsetter1.createLeftTopAligned(width, right);
                Insetter1 vertical = FloatingInsetter1.createCentered(height, top, bottom);
                return new CombinedInsetter(horizontal, vertical);
            }
        }

        return null;
    }

    @Override
    public @Nullable LayoutInfo getCancelButtonLayoutInfo(@NotNull TextFieldLayoutConfiguration g)
    {
        TextFieldWidget w = g.getWidget();
        if (w.hasCancel()) {
            Size sz = g.getSize();
            float d = size(sz, 14, 11, 9);
            return BasicLayoutInfo.createFixed(d, d);
        }
        return null;
    }

    @Override
    protected @NotNull LayoutInfo getScrollBarLayoutInfo(@NotNull ScrollBarLayoutConfiguration g)
    {
        ScrollBarWidget bw = g.getWidget();
        Size sz = g.getSize();
        Orientation o = g.getOrientation();

        int d = 0;

        switch (bw)
        {
            case LEGACY:
                switch (sz)
                {
                    case REGULAR:
                    case LARGE:
                        d = 15;
                        break;
                    case SMALL:
                    case MINI:
                        d = 11;
                        break;
                }
                break;

            case OVERLAY:
                switch (sz)
                {
                    case REGULAR:
                    case LARGE:
                        d = 12;
                        break;
                    case SMALL:
                    case MINI:
                        d = 10;
                        break;
                }
                break;

            case OVERLAY_ROLLOVER:
                switch (sz)
                {
                    case REGULAR:
                    case LARGE:
                        d = 16;
                        break;
                    case SMALL:
                    case MINI:
                        d = 14;
                        break;
                }
        }

        return o == Orientation.VERTICAL ? BasicLayoutInfo.createFixedWidth(d) : BasicLayoutInfo.createFixedHeight(d);
    }

    @Override
    public float getScrollBarThumbPosition(@NotNull Rectangle2D bounds,
                                           @NotNull ScrollBarThumbLayoutConfiguration g,
                                           boolean useExtent)
    {
        double e = getScrollTrackEndInset(g);

        boolean isVertical = g.getOrientation() == Orientation.VERTICAL;
        double top = isVertical ? bounds.getY() + e : bounds.getX() + e;
        double height = isVertical ? bounds.getHeight() - 2 * e : bounds.getWidth() - 2 * e;
        double fullRange = useExtent  ? height - getVisualThumbExtent(bounds, g) : height;
        double result = (g.getValue() - top) / fullRange;
        if (result >= 0) {
            if (result <= 1) {
                return (float) result;
            } else {
                return 10;    // nothing special about this value, just want to be clear it is not a thumb coordinate
            }
        } else {
            return -10;    // nothing special about this value, just want to be clear it is not a thumb coordinate
        }
    }

    @Override
    public @NotNull Rectangle2D getScrollBarThumbBounds(@NotNull Rectangle2D bounds, @NotNull ScrollBarConfiguration g)
    {
        LinearBounds tb = getThumbBounds(bounds, g);

        if (g.getOrientation() == Orientation.VERTICAL) {
            return new Rectangle2D.Double(bounds.getX(), tb.getOrigin(), bounds.getWidth(), tb.getLength());
        } else {
            return new Rectangle2D.Double(tb.getOrigin(), bounds.getY(), tb.getLength(), bounds.getHeight());
        }
    }

    protected @NotNull LinearBounds getThumbBounds(@NotNull Rectangle2D bounds, @NotNull ScrollBarConfiguration g)
    {
        double e = getScrollTrackEndInset(g);
        int minimumThumbLength = getMinimumThumbLength(g);

        boolean isVertical = g.getOrientation() == Orientation.VERTICAL;
        double trackOrigin = isVertical ? bounds.getY() + e : bounds.getX() + e;
        double trackLength = isVertical ? bounds.getHeight() - 2 * e : bounds.getWidth() - 2 * e;
        double actualExtent = Math.max(minimumThumbLength, g.getThumbExtent() * trackLength);
        double scrollingRange = Math.max(0, trackLength - actualExtent);
        double thumbOrigin = trackOrigin + g.getThumbPosition() * scrollingRange;
        return new LinearBounds(thumbOrigin, actualExtent);
    }

    protected double getVisualThumbExtent(@NotNull Rectangle2D bounds, @NotNull ScrollBarThumbLayoutConfiguration g)
    {
        double e = getScrollTrackEndInset(g);
        boolean isVertical = g.getOrientation() == Orientation.VERTICAL;
        double trackLength = isVertical ? bounds.getHeight() - 2 * e : bounds.getWidth() - 2 * e;
        int minimumThumbLength = getMinimumThumbLength(g);
        return Math.max(minimumThumbLength, g.getThumbExtent() * trackLength);
    }

    protected int getMinimumThumbLength(@NotNull ScrollBarLayoutConfiguration g)
    {
        switch (g.getSize()) {
            case SMALL:
            case MINI:
                return 14;
            default:
                return 18;
        }
    }

    protected double getScrollTrackEndInset(@NotNull ScrollBarLayoutConfiguration g)
    {
        switch (g.getWidget()) {
            case LEGACY:
                return 3;
            default:
                return 2;
        }
    }

    @Override
    public int getScrollBarThumbHit(@NotNull Rectangle2D bounds, @NotNull ScrollBarThumbConfiguration g)
    {
        int c = g.getValue();

        boolean isVertical = g.getOrientation() == Orientation.VERTICAL;
        if (isVertical) {
            if (c < bounds.getY() || c > bounds.getY() + bounds.getHeight()) {
                return -1000;
            }
        } else {
            if (c < bounds.getX() || c > bounds.getX() + bounds.getWidth()) {
                return -1000;
            }
        }

        LinearBounds tb = getThumbBounds(bounds, g);
        if (c < tb.getOrigin()) {
            return -1;
        } else if (c > tb.getOrigin() + tb.getLength()) {
            return 1;
        } else {
            return 0;
        }
    }

    @Override
    protected @NotNull LayoutInfo getScrollColumnSizerLayoutInfo(@NotNull ScrollColumnSizerLayoutConfiguration g)
    {
        return BasicLayoutInfo.getInstance();    // obsolete
    }

    @Override
    protected @NotNull LayoutInfo getProgressIndicatorLayoutInfo(@NotNull ProgressIndicatorLayoutConfiguration g)
    {
        ProgressWidget pw = g.getWidget();
        Orientation o = g.getOrientation();
        Size sz = g.getSize();

        if (pw == ProgressWidget.BAR || pw == ProgressWidget.INDETERMINATE_BAR) {
            if (o == Orientation.HORIZONTAL) {
                return BasicLayoutInfo.createFixedHeight(6);
            } else {
                return BasicLayoutInfo.createFixedWidth(6);
            }
        } else if (pw == ProgressWidget.SPINNER || pw == ProgressWidget.INDETERMINATE_SPINNER) {
            int d = size(sz, 32, 16, 12);
            return BasicLayoutInfo.createFixed(d, d);
        } else {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    protected @NotNull LayoutInfo getTableColumnHeaderLayoutInfo(@NotNull TableColumnHeaderLayoutConfiguration g)
    {
        // Our painter extension supports arbitrary heights. Therefore we specify a minimum height here.
        // The native painters would want a fixed height of 15.
        // A minimum width is needed to show a sort indicator.
        int minWidth = g.isSortable() ? 31 : 0;
        return BasicLayoutInfo.createMinimum(minWidth, 15);
    }

    public @Nullable Insetter getTableColumnHeaderSortArrowInsets(@NotNull TableColumnHeaderLayoutConfiguration g)
    {
        int width = getTableHeaderSortIndicatorWidth(g);
        int top = 1;
        int arrowSide = 7;
        return g.isLeftToRight() ? Insetters.createRightAligned(width, arrowSide, top, top)
                 : Insetters.createLeftAligned(width, arrowSide, top, top);
    }

    @Override
    public @NotNull Insetter getTableColumnHeaderLabelInsets(@NotNull TableColumnHeaderLayoutConfiguration g)
    {
        int arrowSide = 7 + getTableHeaderSortIndicatorWidth(g);
        int top = 1;
        int side = 3;
        return g.isLeftToRight() ? Insetters.createFixed(top, side, top, arrowSide)
                 : Insetters.createFixed(top, arrowSide, top, side);
    }

    protected int getTableHeaderSortIndicatorWidth(@NotNull TableColumnHeaderLayoutConfiguration g)
    {
        return g.isSortable() ? 11 : 0;
    }
}
