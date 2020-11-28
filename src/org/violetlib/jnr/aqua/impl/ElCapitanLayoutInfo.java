/*
 * Copyright (c) 2016-2020 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.aqua.impl;

import org.violetlib.jnr.Insetter;
import org.violetlib.jnr.LayoutInfo;
import org.violetlib.jnr.aqua.AquaUIPainter;
import org.violetlib.jnr.aqua.ButtonLayoutConfiguration;
import org.violetlib.jnr.aqua.ComboBoxLayoutConfiguration;
import org.violetlib.jnr.aqua.PopupButtonLayoutConfiguration;
import org.violetlib.jnr.aqua.SegmentedButtonLayoutConfiguration;
import org.violetlib.jnr.aqua.TextFieldLayoutConfiguration;
import org.violetlib.jnr.aqua.ToolBarItemWellLayoutConfiguration;
import org.violetlib.jnr.impl.BasicLayoutInfo;
import org.violetlib.jnr.impl.Insetters;
import org.violetlib.jnr.impl.JNRPlatformUtils;

import org.jetbrains.annotations.*;

import static org.violetlib.jnr.impl.JNRUtils.*;

/**
  Layout information for OS 10.11 widgets.
*/

public class ElCapitanLayoutInfo
  extends YosemiteLayoutInfo
{
    public ElCapitanLayoutInfo()
    {
    }

    @Override
    protected @NotNull LayoutInfo getButtonLayoutInfo(@NotNull ButtonLayoutConfiguration g)
    {
        AquaUIPainter.ButtonWidget bw = g.getButtonWidget();

        if (bw == AquaUIPainter.ButtonWidget.BUTTON_TOOLBAR_ITEM) {
            ToolBarItemWellLayoutConfiguration tg = new ToolBarItemWellLayoutConfiguration();
            return getToolBarItemWellLayoutInfo(tg);
        }

        AquaUIPainter.Size sz = g.getSize();

        int platformVersion = JNRPlatformUtils.getPlatformVersion();

        if (bw == AquaUIPainter.ButtonWidget.BUTTON_PUSH) {
            return BasicLayoutInfo.createFixedHeight(size(sz, 22, 19, 16));

        } else if (bw == AquaUIPainter.ButtonWidget.BUTTON_BEVEL) {
            return BasicLayoutInfo.getInstance();

        } else if (bw == AquaUIPainter.ButtonWidget.BUTTON_BEVEL_ROUND) {
            return BasicLayoutInfo.getInstance();

        } else if (bw == AquaUIPainter.ButtonWidget.BUTTON_CHECK_BOX) {
            if (platformVersion >= 101400) {
                return BasicLayoutInfo.createFixed(size(sz, 16, 14, 10), size(sz, 17, 15, 11));
            }
            return BasicLayoutInfo.createFixed(size(sz, 14, 12, 10), size(sz, 14, 12, 10));

        } else if (bw == AquaUIPainter.ButtonWidget.BUTTON_RADIO) {
            if (platformVersion >= 101400) {
                return BasicLayoutInfo.createFixed(size(sz, 18, 14, 10), size(sz, 18, 15, 11));
            }
            return BasicLayoutInfo.createFixed(size(sz, 16, 14, 10), size(sz, 16, 14, 10));

        } else if (bw == AquaUIPainter.ButtonWidget.BUTTON_DISCLOSURE) {
            return BasicLayoutInfo.createFixed(size(sz, 21, 19, 15), size(sz, 22, 19, 16));

        } else if (bw == AquaUIPainter.ButtonWidget.BUTTON_HELP) {
            return BasicLayoutInfo.createFixed(size(sz, 21, 18, 15), size(sz, 22, 19, 16));

        } else if (bw == AquaUIPainter.ButtonWidget.BUTTON_GRADIENT) {
            return BasicLayoutInfo.getInstance();

        } else if (bw == AquaUIPainter.ButtonWidget.BUTTON_RECESSED) {
            return BasicLayoutInfo.createFixedHeight(size(sz, 18, 16, 14));

        } else if (bw == AquaUIPainter.ButtonWidget.BUTTON_INLINE) {
            // Note that the NSView does not limit the size, but there seems to be an intended fixed size.
            return BasicLayoutInfo.createFixedHeight(17);

        } else if (bw == AquaUIPainter.ButtonWidget.BUTTON_ROUNDED_RECT) {
            return BasicLayoutInfo.createFixedHeight(size(sz, 18, 16, 14));

        } else if (bw == AquaUIPainter.ButtonWidget.BUTTON_TEXTURED) {
            return BasicLayoutInfo.createFixedHeight(size(sz, 22, 18, 15));  // changed in El Capitan

        } else if (bw == AquaUIPainter.ButtonWidget.BUTTON_TEXTURED_TOOLBAR) {
            return BasicLayoutInfo.createFixedHeight(size(sz, 24, 20, 17));  // new in El Capitan

        } else if (bw == AquaUIPainter.ButtonWidget.BUTTON_ROUND) {
            return BasicLayoutInfo.createFixed(size(sz, 20, 17, 14), size(sz, 21, 18, 15));

        } else if (bw == AquaUIPainter.ButtonWidget.BUTTON_ROUND_INSET) {
            return BasicLayoutInfo.createFixed(18, 18);

        } else if (bw == AquaUIPainter.ButtonWidget.BUTTON_ROUND_TEXTURED) {
            return BasicLayoutInfo.createFixed(size(sz, 21, 18, 15), size(sz, 22, 19, 16));

        } else if (bw == AquaUIPainter.ButtonWidget.BUTTON_ROUND_TEXTURED_TOOLBAR) {
            return BasicLayoutInfo.createFixed(size(sz, 23, 20, 17), size(sz, 23, 20, 17));  // new in El Capitan

        } else if (bw == AquaUIPainter.ButtonWidget.BUTTON_DISCLOSURE_TRIANGLE) {
            return BasicLayoutInfo.createFixed(9, 9);

        } else if (bw == AquaUIPainter.ButtonWidget.BUTTON_PUSH_INSET2) {
            return BasicLayoutInfo.createFixedHeight(size(sz, 19, 17, 15));

        } else if (bw == AquaUIPainter.ButtonWidget.BUTTON_COLOR_WELL) {
            return BasicLayoutInfo.createMinimum(44, 23);

        } else {
            return BasicLayoutInfo.getInstance();
        }
    }

    @Override
    public @Nullable Insetter getButtonLabelInsets(@NotNull ButtonLayoutConfiguration g)
    {
        AquaUIPainter.ButtonWidget bw = g.getButtonWidget();
        AquaUIPainter.Size sz = g.getSize();

        float top = 0;
        float bottom = 0;
        float left = 0;
        float right = 0;

        // these insets are minimums to avoid painting over the border

        if (bw == AquaUIPainter.ButtonWidget.BUTTON_PUSH) {
            top = size(sz, 1, 2, 1);
            bottom = size(sz, 2, 2, 2);
            left = size(sz, 4, 4, 3);
            right = left;

        } else if (bw == AquaUIPainter.ButtonWidget.BUTTON_BEVEL) {
            top = 1;
            bottom = 1;
            left = 1;
            right = 1;

        } else if (bw == AquaUIPainter.ButtonWidget.BUTTON_BEVEL_ROUND) {
            top = size(sz, 1, 2, 1);
            bottom = size(sz, 2, 2, 2);
            left = size(sz, 4, 4, 3);
            right = left;

        } else if (bw == AquaUIPainter.ButtonWidget.BUTTON_CHECK_BOX) {
            // labels are not supported
            return null;

        } else if (bw == AquaUIPainter.ButtonWidget.BUTTON_RADIO) {
            // labels are not supported
            return null;

        } else if (bw == AquaUIPainter.ButtonWidget.BUTTON_DISCLOSURE) {
            // labels are not supported
            return null;

        } else if (bw == AquaUIPainter.ButtonWidget.BUTTON_HELP) {
            // labels are not supported
            return null;

        } else if (bw == AquaUIPainter.ButtonWidget.BUTTON_GRADIENT) {
            top = 1;
            bottom = 1;
            left = 1;
            right = 1;

        } else if (bw == AquaUIPainter.ButtonWidget.BUTTON_RECESSED) {
            top = bottom = 1;
            left = right = size(sz, 4, 3, 3);

        } else if (bw == AquaUIPainter.ButtonWidget.BUTTON_INLINE) {
            top = 2;  // could be 1.5
            bottom = 2;
            left = right = size(sz, 6, 5, 5);

        } else if (bw == AquaUIPainter.ButtonWidget.BUTTON_ROUNDED_RECT) {
            top = bottom = 1;
            left = right = 4;

        } else if (bw == AquaUIPainter.ButtonWidget.BUTTON_TEXTURED
                     || bw == AquaUIPainter.ButtonWidget.BUTTON_TEXTURED_TOOLBAR) {
            top = 0.51f;
            bottom = 1.49f;
            left = right = 3;

        } else if (bw == AquaUIPainter.ButtonWidget.BUTTON_ROUND) {
            top = left = right = size2D(sz, 4, 3.5f, 3);
            bottom = top + 1;

        } else if (bw == AquaUIPainter.ButtonWidget.BUTTON_ROUND_INSET) {
            left = right = 3;
            top = 3.5f;
            bottom = top;

        } else if (bw == AquaUIPainter.ButtonWidget.BUTTON_ROUND_TEXTURED) {
            top = left = right = size2D(sz, 3.5f, 3, 2.5f);
            bottom = top + 1;

        } else if (bw == AquaUIPainter.ButtonWidget.BUTTON_ROUND_TEXTURED_TOOLBAR) {
            // changed in El Capitan
            top = bottom = size2D(sz, 4.5f, 4, 3.5f);
            left = right = size2D(sz, 3.5f, 3, 2.5f);

        } else if (bw == AquaUIPainter.ButtonWidget.BUTTON_DISCLOSURE_TRIANGLE) {
            // labels are not supported
            return null;

        } else if (bw == AquaUIPainter.ButtonWidget.BUTTON_PUSH_INSET2) {
            top = size(sz, 1, 1, 1);
            bottom = size(sz, 1, 1, 1);
            left = size(sz, 7, 6, 6);
            right = left;

        } else if (bw == AquaUIPainter.ButtonWidget.BUTTON_TOOLBAR_ITEM) {
            top = 2;
            bottom = 3;
            left = right = 4;

        } else if (bw == AquaUIPainter.ButtonWidget.BUTTON_COLOR_WELL) {
            // labels are not supported
        }

        LayoutInfo layoutInfo = getLayoutInfo(g);
        return Insetters.createFixed(top, left, bottom, right, layoutInfo);
    }

    @Override
    protected @NotNull LayoutInfo getSegmentedButtonLayoutInfo(@NotNull SegmentedButtonLayoutConfiguration g)
    {
        int version = AquaUIPainterBase.internalGetSegmentedButtonRenderingVersion();
        AquaUIPainter.SegmentedButtonWidget bw = g.getWidget();
        AquaUIPainter.Size sz = g.getSize();

        switch (bw) {
            case BUTTON_TAB:
            case BUTTON_SEGMENTED:
            case BUTTON_SEGMENTED_SLIDER:
            case BUTTON_SEGMENTED_SLIDER_TOOLBAR:
            case BUTTON_SEGMENTED_SLIDER_TOOLBAR_ICONS:
            case BUTTON_SEGMENTED_SEPARATED:
                return BasicLayoutInfo.createFixedHeight(size(sz, 22, 19, 16));

            case BUTTON_SEGMENTED_INSET:
                return BasicLayoutInfo.createFixedHeight(size(sz, 18, 16, 14));

            case BUTTON_SEGMENTED_SCURVE:
            case BUTTON_SEGMENTED_TEXTURED:
            case BUTTON_SEGMENTED_TOOLBAR:
            case BUTTON_SEGMENTED_TEXTURED_SEPARATED:
                if (version == AquaUIPainterBase.SEGMENTED_10_14) {
                    return BasicLayoutInfo.createFixedHeight(size(sz, 23, 19, 16));
                }
                return BasicLayoutInfo.createFixedHeight(size(sz, 22, 18, 15));  // changed in El Capitan

            case BUTTON_SEGMENTED_TEXTURED_TOOLBAR:
            case BUTTON_SEGMENTED_TEXTURED_TOOLBAR_ICONS:
            case BUTTON_SEGMENTED_TEXTURED_SEPARATED_TOOLBAR:
            case BUTTON_SEGMENTED_TEXTURED_SEPARATED_TOOLBAR_ICONS:
                if (version == AquaUIPainterBase.SEGMENTED_10_14) {
                    return BasicLayoutInfo.createFixedHeight(size(sz, 22, 18, 15));
                }
                return BasicLayoutInfo.createFixedHeight(size(sz, 24, 20, 17));  // introduced in El Capitan

            case BUTTON_SEGMENTED_SMALL_SQUARE:
                return BasicLayoutInfo.createFixedHeight(size(sz, 21, 19, 17));

            default:
                throw new UnsupportedOperationException();
        }
    }

    @Override
    public @NotNull Insetter getSegmentedButtonLabelInsets(@NotNull SegmentedButtonLayoutConfiguration g)
    {
        AquaUIPainter.SegmentedButtonWidget bw = g.getWidget();
        AquaUIPainter.Position pos = g.getPosition();

        LayoutInfo layoutInfo = getLayoutInfo(g);
        float top = 1;
        float bottom = 2;
        float left = 1;
        float right = left;
        float endAdjust = 3;

        boolean isLeftEnd = pos == AquaUIPainter.Position.FIRST || pos == AquaUIPainter.Position.ONLY;
        boolean isRightEnd = pos == AquaUIPainter.Position.LAST || pos == AquaUIPainter.Position.ONLY;

        switch (bw) {
            case BUTTON_TAB:
            case BUTTON_SEGMENTED:
            case BUTTON_SEGMENTED_SLIDER:
            case BUTTON_SEGMENTED_SLIDER_TOOLBAR:
            case BUTTON_SEGMENTED_SLIDER_TOOLBAR_ICONS:
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
            case BUTTON_SEGMENTED_TEXTURED_TOOLBAR_ICONS:
            case BUTTON_SEGMENTED_TOOLBAR:
                top = 0.5f;     // changed in El Capitan
                bottom = 1.5f;  // changed in El Capitan
                endAdjust = 2;
                break;
            case BUTTON_SEGMENTED_TEXTURED_SEPARATED:
            case BUTTON_SEGMENTED_TEXTURED_SEPARATED_TOOLBAR:
            case BUTTON_SEGMENTED_TEXTURED_SEPARATED_TOOLBAR_ICONS:
                top = 0.5f;     // changed in El Capitan
                bottom = 1.5f;  // changed in El Capitan
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
        AquaUIPainter.ComboBoxWidget bw = g.getWidget();
        AquaUIPainter.Size sz = g.getSize();

        if (bw == AquaUIPainter.ComboBoxWidget.BUTTON_COMBO_BOX_CELL) {
            return BasicLayoutInfo.createMinimumHeight(size(sz, 14, 11, 11));

        } else if (bw == AquaUIPainter.ComboBoxWidget.BUTTON_COMBO_BOX_TEXTURED) {
            float fixedHeight = size(sz, 22, 18, 15);  // changed in El Capitan
            float minWidth = size(sz, 27, 24, 22);
            return BasicLayoutInfo.create(false, minWidth, true, fixedHeight);

        } else if (bw == AquaUIPainter.ComboBoxWidget.BUTTON_COMBO_BOX_TEXTURED_TOOLBAR) {
            // introduced in El Capitan
            float fixedHeight = size(sz, 24, 20, 17);
            float minWidth = size(sz, 27, 24, 22);
            return BasicLayoutInfo.create(false, minWidth, true, fixedHeight);

        } else {
            float fixedHeight = size(sz, 22, 19, 15);
            float minWidth = size(sz, 27, 24, 22);
            return BasicLayoutInfo.create(false, minWidth, true, fixedHeight);
        }
    }

    @Override
    protected @NotNull LayoutInfo getPopUpButtonLayoutInfo(@NotNull PopupButtonLayoutConfiguration g)
    {
        // On Yosemite and El Capitan, the square style bombs if the mini size is selected.
        // See rendering code, which must be consistent.

        AquaUIPainter.PopupButtonWidget bw = g.getPopupButtonWidget();
        AquaUIPainter.Size sz = g.getSize();
        boolean isSquare = bw == AquaUIPainter.PopupButtonWidget.BUTTON_POP_UP_SQUARE
                             || bw == AquaUIPainter.PopupButtonWidget.BUTTON_POP_DOWN_SQUARE;
        boolean isArrowsOnly = bw == AquaUIPainter.PopupButtonWidget.BUTTON_POP_UP_CELL
                                 || bw == AquaUIPainter.PopupButtonWidget.BUTTON_POP_DOWN_CELL;

        if ((isSquare || isArrowsOnly) && sz == AquaUIPainter.Size.MINI) {
            sz = AquaUIPainter.Size.SMALL;
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
            {
                float fixedHeight = size(sz, 22, 18, 15);  // changed in El Capitan
                float minWidth = size(sz, 25, 24, 20);
                return BasicLayoutInfo.create(false, minWidth, true, fixedHeight);
            }

            case BUTTON_POP_DOWN_TEXTURED_TOOLBAR:
            case BUTTON_POP_UP_TEXTURED_TOOLBAR:
            {
                // introduced in El Capitan
                float fixedHeight = size(sz, 24, 20, 17);
                float minWidth = size(sz, 25, 24, 20);
                return BasicLayoutInfo.create(false, minWidth, true, fixedHeight);
            }

            case BUTTON_POP_DOWN_GRADIENT:
            case BUTTON_POP_UP_GRADIENT:
                return BasicLayoutInfo.createFixedHeight(21);

            default:
                throw new UnsupportedOperationException();
        }
    }

    @Override
    protected @NotNull LayoutInfo getTextFieldLayoutInfo(@NotNull TextFieldLayoutConfiguration g)
    {
        AquaUIPainter.TextFieldWidget w = g.getWidget();
        if (w.isRound() || w.isSearch()) {
            if (w.isToolbar()) {
                // The actual sizes for small and mini are bogus. We do not simulate this bug.
                return BasicLayoutInfo.createFixedHeight(size(g.getSize(), 24, 20, 17));
            } else {
                return BasicLayoutInfo.createFixedHeight(size(g.getSize(), 22, 19, 17));
            }
        }

        return BasicLayoutInfo.getInstance();
    }
}
