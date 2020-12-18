/*
 * Copyright (c) 2020 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.aqua.impl;

import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

import org.violetlib.geom.GeneralRoundRectangle;
import org.violetlib.jnr.Insetter;
import org.violetlib.jnr.aqua.AquaUIPainter;
import org.violetlib.jnr.aqua.AquaUIPainter.ButtonWidget;
import org.violetlib.jnr.aqua.AquaUIPainter.ComboBoxWidget;
import org.violetlib.jnr.aqua.AquaUIPainter.Position;
import org.violetlib.jnr.aqua.AquaUIPainter.SegmentedButtonWidget;
import org.violetlib.jnr.aqua.AquaUIPainter.Size;
import org.violetlib.jnr.aqua.ButtonConfiguration;
import org.violetlib.jnr.aqua.ButtonLayoutConfiguration;
import org.violetlib.jnr.aqua.ComboBoxLayoutConfiguration;
import org.violetlib.jnr.aqua.PopupButtonLayoutConfiguration;
import org.violetlib.jnr.aqua.SegmentedButtonLayoutConfiguration;
import org.violetlib.jnr.aqua.SliderThumbLayoutConfiguration;

import org.jetbrains.annotations.*;

import static org.violetlib.jnr.aqua.AquaUIPainter.ButtonWidget.*;
import static org.violetlib.jnr.aqua.AquaUIPainter.PopupButtonWidget.*;
import static org.violetlib.jnr.aqua.AquaUIPainter.SegmentedButtonWidget.*;

/**

*/
public class BigSurOutliner
  extends YosemiteOutliner
{
    public BigSurOutliner(@NotNull BigSurLayoutInfo uiLayout)
    {
        super(uiLayout);
    }

    @Override
    protected @Nullable Shape getButtonOutline(@NotNull Rectangle2D bounds, @NotNull ButtonLayoutConfiguration g)
    {
        ButtonWidget bw = g.getButtonWidget();
        Size sz = g.getSize();
        AquaUIPainter.ButtonState bs = getButtonState(g);

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
                    width += size2D(sz, -7, -4, 0, -2);
                    height += size2D(sz, -6.5, -3.5, -1, -2);
                    x += size2D(sz, 3, 2, -0.5, 0.5);
                    y += size2D(sz, 0, -0.5, 1);
                    break;
                case BUTTON_ROUND:
                    width += -5;
                    height += size2D(sz, -4, -5, -5, -5);
                    x += size2D(sz, 2.5, 2, 2.5);
                    y += size2D(sz, 3, 4, 5.5, 3.5);
                    break;
                case BUTTON_ROUND_TEXTURED:
                    width -= 1;
                    height -= 2;
                    break;
                case BUTTON_ROUND_TEXTURED_TOOLBAR:
                    width += size2D(sz, -4, -4.5, -3.5, -3.5);
                    height += size2D(sz, -4, -5, -3.5, -3.5);
                    x += size2D(sz, 2, 2, 1.5, 1.5);
                    y += size2D(sz, 2, 2, 1, 1);
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

        // all others are rounded rectangles

        double corner = 8;

        if (bw == BUTTON_PUSH) {
            //corner = 6;
            x += size2D(sz, 0, 0.5, 0.5, 0.5);
            y += size2D(sz, 2, 0.5, 0.5, 0.5);
            height += size2D(sz, -2, -2, -2, -2);
            width += size2D(sz, 0, -1, -1, -1);

            if (sz == Size.REGULAR
                  ? bs == AquaUIPainter.ButtonState.ON
                  : bs != AquaUIPainter.ButtonState.STATELESS && bs != AquaUIPainter.ButtonState.OFF) {
                x += size2D(sz, 0.5, 0, 0);
                y += size2D(sz, 1, -0.5, -0.5);
                width += size2D(sz, -1, 0, 0);
                height += size2D(sz, -1, 0.5, 1);
            } else if (sz == Size.LARGE) {
                // LARGE size, OFF or STATELESS
                height += -0.5;
            } else if (sz == Size.SMALL) {
                // SMALL size, OFF or STATELESS
                y += -0.5;
            } else if (sz == Size.MINI) {
                // MINI size, OFF or STATELESS
                y += -0.5;
                height += 1;
            }

        } else if (bw == BUTTON_BEVEL_ROUND) {
            corner = 10;
            x += size2D(sz, 0.5, 0.5, 0.5);
            y += size2D(sz, 0.5, 0.5, 0.5);
            height += size2D(sz, -2, -2, -2);
            width += size2D(sz, -1, -1, -1);
        } else if (bw == BUTTON_CHECK_BOX) {
            corner = 5;
        } else if (bw == BUTTON_DISCLOSURE) {
            x += size2D(sz, 2.5, 3.5, 5.5, 3.5);
            y += size2D(sz, 2.5, 2.5, 4, 0.5);
            width += size2D(sz, -5, -7, -11, -5);
            height += size2D(sz, -5, -5, -9, -2);
        } else if (bw == BUTTON_INLINE) {
            height -= 1;
            corner = 16;
        } else if (bw == BUTTON_RECESSED) {
            corner = 6;
            y += size2D(sz, 0.5, 0, 0, 0);
            height += size2D(sz, -1, 0, 0, 0);
        } else if (bw == BUTTON_TEXTURED || bw == BUTTON_TEXTURED_TOOLBAR || bw == BUTTON_TEXTURED_TOOLBAR_ICONS) {
            y += 1;
            height += -3;
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
        } else if (bw == BUTTON_TOOLBAR_ITEM) {
            corner = 10;
            x += 1.5;
            y += 1.5;
            width += -3.5;
            height += -5.5;
        }

        return new RoundRectangle2D.Double(x, y, width, height, corner, corner);
    }

    private static @NotNull AquaUIPainter.ButtonState getButtonState(@NotNull ButtonLayoutConfiguration g) {
        if (g instanceof ButtonConfiguration) {
            ButtonConfiguration bg = (ButtonConfiguration) g;
            return bg.getButtonState();
        }
        return AquaUIPainter.ButtonState.STATELESS;
    }

    @Override
    protected @Nullable Shape getSegmentedButtonOutline(@NotNull Rectangle2D bounds,
                                                        @NotNull SegmentedButtonLayoutConfiguration g)
    {
        SegmentedButtonWidget bw = g.getWidget();
        Size sz = g.getSize();
        Position pos = g.getPosition();

        double x = bounds.getX();
        double y = bounds.getY();
        double width = bounds.getWidth();
        double height = bounds.getHeight();

        boolean isLeft = pos == Position.FIRST;
        boolean useOnly = pos == Position.ONLY;

        double corner = 8;

        switch (bw)
        {
            case BUTTON_TAB:
            case BUTTON_SEGMENTED:
            case BUTTON_SEGMENTED_SEPARATED:
            case BUTTON_SEGMENTED_SLIDER:
            case BUTTON_SEGMENTED_SLIDER_TOOLBAR:
            case BUTTON_SEGMENTED_SLIDER_TOOLBAR_ICONS:

                // The outline is used for focus rings. Full Keyboard Access causes one segment to be focusable via the
                // keyboard. For select one controls, including tabs, it is the selected segment. The selected segment
                // is always painted using the "only" style. Therefore, the outline should be "only" style regardless of
                // the position. For select any controls, the first segment is focusable. No special case.

                corner = 6;
                if (bw == BUTTON_TAB || bw == BUTTON_SEGMENTED_SLIDER || bw == SegmentedButtonWidget.BUTTON_SEGMENTED_SEPARATED) {
                    useOnly = true;
                }

                float extraTopForRegular = 0.5f;

                x += size2D(sz, isLeft ? 0.5f : 0, 0, 0);
                y += size2D(sz, 0.5f + extraTopForRegular, 0.5, 0.5);
                height += size2D(sz, -2, -2, -2);
                width += size2D(sz, -0.5, 0, 0);

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

                if (bw == BUTTON_TAB || bw.isSlider()) {

                    double widthDelta = -1.5;
                    if (sz == Size.REGULAR) {
                        if (pos == Position.FIRST || pos == Position.ONLY) {
                            // This adjustment is needed only when the button to the right is not selected, but that
                            // information is beyond what is available for layout. Therefore, choose the larger outline
                            // and use it for all states.
                            widthDelta = -0.5;
                        }
                    } else if (sz == Size.SMALL) {
                        if (pos == Position.ONLY) {
                            widthDelta = -0.5;
                        }
                    } else if (sz == Size.MINI) {
                        // This adjustment is needed only in certain selection states, but that information is not
                        // available for layout. Therefore, choose the larger outline and use it for all states.
                        widthDelta = -0.5;
                    }

                    width += (float) widthDelta;
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
                height -= size2D(sz, 1, 0.5, 1);
                break;

            case BUTTON_SEGMENTED_SMALL_SQUARE:
                corner = 0;
                break;
        }

        if (useOnly) {
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
    protected @Nullable Shape getPopUpButtonOutline(@NotNull Rectangle2D bounds, @NotNull PopupButtonLayoutConfiguration g)
    {
        AquaUIPainter.PopupButtonWidget bw = g.getPopupButtonWidget();
        Size sz = g.getSize();

        if (bw == BUTTON_POP_UP || bw == BUTTON_POP_DOWN) {
            double x = bounds.getX();
            double y = bounds.getY();
            double width = bounds.getWidth();
            double height = bounds.getHeight();

            double corner = size2D(sz, 12, 10, 8, 8);

            if (bw == BUTTON_POP_DOWN) {
                x += size2D(sz, 1.5, -0.5, .5, 1.5);
                width += size2D(sz, -3, 0, -1, -3);
                y += size2D(sz, 1.5, -0.5, 0.5, 0.5);
                height += size2D(sz, -2, -1, -2, -2);
            } else {
                x += size2D(sz, 2.5, .5, .5, .5);
                width += size2D(sz, -4, -1, -1, -1);
                y += size2D(sz, 1, -0.5, 0, 0.5);
                height += size2D(sz, -2, -1.5, -2, -2);
            }

            return new RoundRectangle2D.Double(x, y, width, height, corner, corner);
        }

        return super.getPopUpButtonOutline(bounds, g);
    }

    @Override
    protected @Nullable Shape getComboBoxOutline(@NotNull Rectangle2D bounds, @NotNull ComboBoxLayoutConfiguration g)
    {
        double x = bounds.getX();
        double y = bounds.getY();
        double width = bounds.getWidth();
        double height = bounds.getHeight();

        ComboBoxWidget widget = g.getWidget();
        if (widget == ComboBoxWidget.BUTTON_COMBO_BOX) {
            Size sz = g.getSize();
            y += size2D(sz, 0.5f, 0.5f, 0.5f);
            height += size2D(sz, -1, -2, -1);
            x += size2D(sz, 0, 0, 0);
            width += size2D(sz, -1, -1, -1);
            double corner = size2D(sz, 8, 6, 4);

            // TBD: support right to left

            return new GeneralRoundRectangle(x, y, width, height, corner, corner, corner, corner, corner, corner, corner, corner);

        } else if (widget == ComboBoxWidget.BUTTON_COMBO_BOX_CELL) {
            Insetter insets = uiLayout.getComboBoxEditorInsets(g);
            return insets.applyToBounds2D(bounds);

        } else if (widget == ComboBoxWidget.BUTTON_COMBO_BOX_TEXTURED || widget == ComboBoxWidget.BUTTON_COMBO_BOX_TEXTURED_TOOLBAR) {
            x += 0.5f;
            width -= 1;
            height -= 1;
            double corner = 8;
            return new GeneralRoundRectangle(x, y, width, height, corner, corner, corner, corner, corner, corner, corner, corner);

        } else {
            return null;
        }
    }

    @Override
    protected @Nullable Shape getSliderThumbOutline(@NotNull Rectangle2D bounds,
                                                    @NotNull SliderThumbLayoutConfiguration g)
    {
        // macOS 11 introduced new linear slider styles with different layout properties. However, the NSView renderer
        // may or may not use the new style, based on runtime determined linkage information.

        if (!g.isLinear() || AquaUIPainterBase.internalGetSliderRenderingVersion() == AquaUIPainterBase.SLIDER_10_10) {
            return super.getSliderThumbOutline(bounds, g);
        }

        Insetter trackInsets = uiLayout.getSliderTrackPaintingInsets(g);
        Insetter insets = uiLayout.getSliderThumbInsets(g, g.getThumbPosition());
        Rectangle2D tb = insets.applyToBounds2D(trackInsets.applyToBounds2D(bounds));

        boolean isCircle = !g.hasTickMarks();

        Size sz = g.getSize();
        float length = size2D(sz, 20, 17, 13.5);
        float width = isCircle ? length : size2D(sz, 8, 9, 8);

        double x = g.isVertical() ? size2D(sz, 2, 1.5, 3) : size2D(sz, 2, 1.5, 3);
        double y = g.isHorizontal() ? size2D(sz, 1.5, 1.5, 3) : size2D(sz, 1.5, 1, 2.5);
        double w = g.isHorizontal() ? width : length;
        double h = g.isHorizontal() ? length + size2D(sz, 0.5, 0, 0) : width;

        if (!isCircle) {
            x += g.isVertical() ? size2D(sz, 0, 0, -1.5) : size2D(sz, 0.5, 0, -1.5);
            y += g.isVertical() ? size2D(sz, 1, 1, -1) : size2D(sz, 0.5, 0.5, -1);
            h += size2D(sz, 0, -1, 0);
        }

        x += tb.getX();
        y += tb.getY();
        if (isCircle) {
            return new Ellipse2D.Double(x, y, w, h);
        } else {
            float corner = 6;
            return new RoundRectangle2D.Double(x, y, w, h, corner, corner);
        }
    }
}
