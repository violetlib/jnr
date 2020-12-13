/*
 * Copyright (c) 2015-2020 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.aqua.coreui;

import org.violetlib.jnr.aqua.AquaUIPainter.*;
import org.violetlib.jnr.aqua.ButtonConfiguration;
import org.violetlib.jnr.aqua.ComboBoxConfiguration;
import org.violetlib.jnr.aqua.PopupButtonConfiguration;
import org.violetlib.jnr.aqua.ScrollBarConfiguration;
import org.violetlib.jnr.aqua.SegmentedButtonConfiguration;
import org.violetlib.jnr.aqua.SliderConfiguration;
import org.violetlib.jnr.aqua.SplitPaneDividerConfiguration;
import org.violetlib.jnr.aqua.impl.AquaUIPainterBase;
import org.violetlib.jnr.aqua.impl.NativeSupport;
import org.violetlib.jnr.aqua.impl.RendererDescriptionsBase;
import org.violetlib.jnr.impl.BasicRendererDescription;
import org.violetlib.jnr.impl.JNRPlatformUtils;
import org.violetlib.jnr.impl.JNRUtils;
import org.violetlib.jnr.impl.MultiResolutionRendererDescription;
import org.violetlib.jnr.impl.RendererDescription;

import org.jetbrains.annotations.*;

import static org.violetlib.jnr.aqua.AquaUIPainter.ButtonWidget.*;
import static org.violetlib.jnr.aqua.AquaUIPainter.SegmentedButtonWidget.*;
import static org.violetlib.jnr.impl.JNRUtils.*;

/**
  Renderer descriptions for Core UI based rendering on OS X 10.10 and later. This mostly includes rendering via the Java
  Runtime Support framework.
*/

public class CoreUIRendererDescriptions
  extends RendererDescriptionsBase
{
    @Override
    public @NotNull RendererDescription getSplitPaneDividerRendererDescription(@NotNull SplitPaneDividerConfiguration g)
    {
        DividerWidget dw = g.getWidget();
        Orientation o = g.getOrientation();

        switch (g.getWidget())
        {
            case THIN_DIVIDER:
            case THICK_DIVIDER:
                return new BasicRendererDescription(0, 0, 0, 0);
            case PANE_SPLITTER:
                return o == Orientation.HORIZONTAL
                         ? new BasicRendererDescription(0, -1, 0, 2)
                         : new BasicRendererDescription(-1, 0, 2, 0);
            default:
                return null;
        }
    }

    @Override
    public @NotNull RendererDescription getButtonRendererDescription(@NotNull ButtonConfiguration g)
    {
        int platformVersion = JNRPlatformUtils.getPlatformVersion();
        ButtonWidget bw = toCanonicalButtonStyle(g.getButtonWidget());
        Size sz = g.getSize();

        if (bw == BUTTON_CHECK_BOX) {
            switch (sz) {
                case LARGE:
                case REGULAR:
                    return new BasicRendererDescription(0, 0, 0, 0);
                case SMALL:
                case MINI:
                    return new BasicRendererDescription(0, -1, 0, 1);
                default:
                    throw new UnsupportedOperationException();
            }

        } else if (bw == BUTTON_RADIO) {
            switch (sz) {
                case LARGE:
                case REGULAR:
                    return new BasicRendererDescription(0, 0, 0, 0);
                case SMALL:
                    return new BasicRendererDescription(0, 0, 0, 1);
                case MINI:
                    return new BasicRendererDescription(0, -0.49f, 0, 1);
                default:
                    throw new UnsupportedOperationException();
            }

        } else if (bw == BUTTON_ROUND) {
            switch (sz) {
                case LARGE:
                case REGULAR:
                    return new BasicRendererDescription(0, 0, 0, 2);
                case SMALL:
                    return new BasicRendererDescription(0, 0, 0, 4);
                case MINI:
                    return new BasicRendererDescription(0, 0, 0, 2);
                default:
                    throw new UnsupportedOperationException();
            }
        } else if (bw == BUTTON_TEXTURED || bw == BUTTON_TEXTURED_TOOLBAR || bw == BUTTON_TEXTURED_TOOLBAR_ICONS) {
            if ((bw == BUTTON_TEXTURED_TOOLBAR || bw == BUTTON_TEXTURED_TOOLBAR_ICONS)
                  && sz == Size.LARGE
                  && platformVersion >= 101600) {
                return new BasicRendererDescription(-5, -1, 10, 2);
            }
            if (platformVersion >= 101100) {
                BasicRendererDescription x1 = new BasicRendererDescription(0, -1, 0, 2);
                BasicRendererDescription x2 = new BasicRendererDescription(-0.5f, -1, 1, 2);
                return new MultiResolutionRendererDescription(x1, x2);
            } else {
                return new BasicRendererDescription(0, 0, 0, 0);
            }
        } else {
            return super.getButtonRendererDescription(g);
        }
    }

    @Override
    public @NotNull RendererDescription getSegmentedButtonRendererDescription(@NotNull SegmentedButtonConfiguration g)
    {
        int platformVersion = JNRPlatformUtils.getPlatformVersion();

        SegmentedButtonWidget bw = g.getWidget();
        Size sz = g.getSize();

        RendererDescription rd = super.getSegmentedButtonRendererDescription(g);

        float extraWidth = 0;
        float xOffset = 0;
        float yOffset = 0;
        float leftOffset = 0;
        float leftExtraWidth = 0;
        float rightExtraWidth = 0;
        float extraHeight = 0;

        switch (bw) {
            case BUTTON_TAB:
            case BUTTON_SEGMENTED:
            case BUTTON_SEGMENTED_SEPARATED:
            case BUTTON_SEGMENTED_SLIDER:

                if (platformVersion >= 101600 && bw == BUTTON_SEGMENTED_SEPARATED) {
                    leftOffset = size(sz, -4, -1, -1, -1);
                    leftExtraWidth = size(sz, 4, 1, 1, 1);
                    rightExtraWidth = size(sz, 4, 1, 1, 1);
                    break;
                } else if (platformVersion >= 101500 && bw == BUTTON_SEGMENTED_SEPARATED) {
                    Position pos = g.getPosition();
                    xOffset = pos == Position.MIDDLE ? 0 : size(sz, -2, -2, -1);
                    leftExtraWidth = size(sz, 2, 2, 1);
                    rightExtraWidth = size(sz, 2, 2, 1);
                    yOffset = size2D(sz, 0, -1, 0.51);
                    extraHeight = size(sz, 0, 0, -5);
                    break;
                } else if (platformVersion >= 101400 && bw == BUTTON_SEGMENTED_SEPARATED) {
                    Position pos = g.getPosition();
                    xOffset = pos == Position.MIDDLE ? 0 : size(sz, -2, -2, -1);
                    leftExtraWidth = size(sz, 2, 2, 1);
                    rightExtraWidth = size(sz, 2, 2, 1);
                    yOffset = size2D(sz, 0, -0.49, 0.51);
                    extraHeight = size(sz, 0, -2, -5);
                    break;
                } else if (platformVersion >= 101300 && bw == BUTTON_SEGMENTED_SEPARATED) {
                    xOffset = -0.49f;
                    leftOffset = size2D(sz, -2, -2, -1);
                    extraWidth = 1;
                    leftExtraWidth = size(sz, 2, 2, 1);
                    rightExtraWidth = size2D(sz, 1.49, 1.49, 1);
                    yOffset = size2D(sz, 0, -0.98, 0.51);
                    extraHeight = size(sz, -1, -2, -5);
                    break;
                } else if (platformVersion >= 101200 && bw == BUTTON_SEGMENTED_SEPARATED) {
                    xOffset = -0.49f;
                    leftOffset = size2D(sz, -2, -2, -1);
                    extraWidth = 1;
                    leftExtraWidth = size(sz, 2, 2, 1);
                    rightExtraWidth = size2D(sz, 1.49, 1.49, 0.49);
                    yOffset = size2D(sz, 0, -1.98, 0.51);
                    extraHeight = size(sz, 0, 0, -5);
                    break;
                }

                yOffset = size2D(sz, -0.51f, -1.49f, -2);  // regular size should be -1 at 1x
                leftOffset = size(sz, -5, -2, -2, -1);
                leftExtraWidth = size(sz, 5, 2, 2, 1);
                rightExtraWidth = size(sz, 4, 2, 2, 1);

                if (shouldUseSpecialSeparatedDescription(g)) {
                    // completely different rules
                    rd = getSegmentedSeparatedRendererDescription(g, rd, yOffset, leftOffset);
                    yOffset = 0;
                    leftOffset = 0;
                }

                break;

            case BUTTON_SEGMENTED_SLIDER_TOOLBAR:
            case BUTTON_SEGMENTED_SLIDER_TOOLBAR_ICONS:

                yOffset = size2D(sz, -1, 0, -0.49f, -1.49f);
                leftOffset = size(sz, -6, -1, -1, -1);
                leftExtraWidth = size(sz, 6, 1, 1, 1);
                rightExtraWidth = size(sz, 5, 1, 1, 1);
                extraHeight = size(sz, -1, 0, 0, 0);

                if (shouldUseSpecialSeparatedDescription(g)) {
                    // completely different rules
                    rd = getSegmentedSeparatedRendererDescription(g, rd, yOffset, leftOffset);
                    yOffset = 0;
                    leftOffset = 0;
                }

                break;

            case BUTTON_SEGMENTED_INSET:
                yOffset = size2D(sz, -1, -1.51f, -2);  // small size should be -2 at 1x
                leftOffset = -1;
                leftExtraWidth = 1;
                rightExtraWidth = 1;
                break;

            case BUTTON_SEGMENTED_SCURVE:
            case BUTTON_SEGMENTED_TEXTURED:
            case BUTTON_SEGMENTED_TOOLBAR:
            case BUTTON_SEGMENTED_TEXTURED_TOOLBAR:
            case BUTTON_SEGMENTED_TEXTURED_TOOLBAR_ICONS:
            case BUTTON_SEGMENTED_TEXTURED_SEPARATED:
            case BUTTON_SEGMENTED_TEXTURED_SEPARATED_TOOLBAR:
            case BUTTON_SEGMENTED_TEXTURED_SEPARATED_TOOLBAR_ICONS:

                if (platformVersion >= 101600) {
                    if (!bw.isToolbar()) {
                        int x = g.getPosition() == Position.FIRST ? -1 : 0;
                        if (!bw.isSeparated()) {
                            return new BasicRendererDescription(x, 0, 1, 0);
                        } else {
                            return new BasicRendererDescription(x, 0, 2, 1);
                        }
                    } else if (bw.isSeparated()) {
                        if (sz == Size.LARGE) {
                            Position pos = g.getPosition();
                            int x = pos == Position.FIRST || pos == Position.ONLY? -6 : 0;
                            // This state dependence is unbelievable. Someday it will probably change.
                            State st = g.getState();
                            int y = st == State.PRESSED
                                      || st == State.ROLLOVER
                                      || g.isSelected()
                                      ? -6 : 0;
                            RendererDescription srd = new BasicRendererDescription(x, y, 0, 11);
                            return adjustSegmentedRendererDescription(g, srd, 0, 0, 0, 0, 6, 5, 0);
                        }
                        // This state dependence is unbelievable. Someday it will probably change.
                        State st = g.getState();
                        int y = g.isSelected() && st != State.PRESSED && st != State.ROLLOVER ? -2 : -1;
                        return new BasicRendererDescription(-1, y, 1, 3);
                    } else {
                        if (sz == Size.LARGE) {
                            return new BasicRendererDescription(-6, 0, 11, 0);
                        }
                        int x = g.getPosition() == Position.FIRST ? -1 : 0;
                        int w = g.getPosition() == Position.FIRST ? 1 : 0;
                        return new BasicRendererDescription(x, 0, w, 0);
                    }
                }

                if (sz == Size.MINI) {
                    rd = createVertical(0, 4);
                }
                float smallYOffset = platformVersion >= 101100
                                       ? bw.isToolbar() ? -0.49f : -1.49f
                                       : -1;
                yOffset = size2D(sz, 0, smallYOffset, -2);
                if (bw.isSeparated()) {
                    return getTexturedSeparatedRendererDescription(g, rd, yOffset);
                }
                break;

            case BUTTON_SEGMENTED_SMALL_SQUARE:
                yOffset = -1;
                extraHeight = size(sz, 0, -1, -2);
                break;

            default:
                throw new UnsupportedOperationException();
        }

        return adjustSegmentedRendererDescription(g, rd, extraWidth, xOffset, yOffset, leftOffset, leftExtraWidth,
          rightExtraWidth, extraHeight);
    }

    protected boolean shouldUseSpecialSeparatedDescription(@NotNull SegmentedButtonConfiguration g)
    {
        if (g.getPosition() == Position.ONLY) {
            return false;
        }
        SegmentedButtonWidget bw = g.getWidget();
        if (bw == BUTTON_SEGMENTED_SEPARATED) {
            return true;
        }
        if (g.isSelected() && bw.isSlider()) {
            return true;
        }
        return false;
    }

    protected @NotNull RendererDescription getTexturedSeparatedRendererDescription(
      @NotNull SegmentedButtonConfiguration g,
      @NotNull RendererDescription rd,
      float yOffset)
    {
        float extraWidth1 = 0;
        float extraWidth2 = -0.5f;
        float xOffset1 = 0;
        float xOffset2 = 0;

        boolean hasLeft = g.getLeftDividerState() != SegmentedButtonConfiguration.DividerState.NONE;
        boolean hasRight = g.getRightDividerState() != SegmentedButtonConfiguration.DividerState.NONE;

        Position pos = g.getPosition();
        if (pos == Position.FIRST) {
            if (!hasRight) {
                extraWidth1 = 1;
                extraWidth2 = 0.5f;

            } else {
                extraWidth2 = 0;
            }
        } else if (pos == Position.MIDDLE) {
            if (hasRight && !hasLeft) {
                extraWidth2 = 0.5f;
                xOffset2 = -0.5f;
            } else if (hasLeft && !hasRight) {
                extraWidth1 = 1;
                extraWidth2 = 0.5f;
            } else if (!hasLeft && !hasRight) {
                extraWidth1 = 1;
                extraWidth2 = 1;
                xOffset2 = -0.5f;
            } else {
                extraWidth2 = 0;
            }
        } else if (pos == Position.LAST) {
            if (!hasLeft) {
                extraWidth2 = 0.5f;
                xOffset2 = -0.5f;
            } else {
                extraWidth2 = 0;
            }
        }

        RendererDescription d1 = adjustSegmentedRendererDescription(g, rd, extraWidth1, xOffset1, yOffset, 0, 0, 0, 0);
        RendererDescription d2 = adjustSegmentedRendererDescription(g, rd, extraWidth2, xOffset2, yOffset, 0, 0, 0, 0);
        return new MultiResolutionRendererDescription(d1, d2);
    }

    protected @NotNull RendererDescription getSegmentedSeparatedRendererDescription(
      @NotNull SegmentedButtonConfiguration g,
      @NotNull RendererDescription rd,
      float yOffset,
      float xOffset
    )
    {
        boolean hasLeft = g.getLeftDividerState() != SegmentedButtonConfiguration.DividerState.NONE;
        boolean hasRight = g.getRightDividerState() != SegmentedButtonConfiguration.DividerState.NONE;

        float extraWidth = hasRight ? 2 : 2.5f;

        Position pos = g.getPosition();
        if (pos == Position.FIRST) {
        } else if (pos == Position.MIDDLE) {
            xOffset = 0;
            extraWidth = 0.51f;
            if (hasRight && !hasLeft) {
                xOffset = -0.49f;
                extraWidth = 0.49f;
            } else if (hasLeft && hasRight) {
                extraWidth = 0;
            } else if (!hasLeft && !hasRight) {
                extraWidth = 1;
                xOffset = -0.49f;
            }
        } else if (pos == Position.LAST) {
            extraWidth = 2;
            xOffset = hasLeft ? 0 : -0.49f;
        }

        try {
            return JNRUtils.adjustRendererDescription(rd, xOffset, yOffset, extraWidth, 0);
        } catch (UnsupportedOperationException ex) {
            NativeSupport.log("Unable to adjust segmented button renderer description for " + g);
            return rd;
        }
    }

    @Override
    public @NotNull RendererDescription getComboBoxRendererDescription(@NotNull ComboBoxConfiguration g)
    {
        ComboBoxWidget bw = g.getWidget();
        Size sz = g.getSize();

        if (bw == ComboBoxWidget.BUTTON_COMBO_BOX_CELL) {
            switch (sz) {
                case LARGE:
                case REGULAR:
                    return new BasicRendererDescription(0, -3, 2, 3);
                case SMALL:
                    return new BasicRendererDescription(0, -3, 2, 3);
                case MINI:
                    return new BasicRendererDescription(0, -1.5f, 2, 2);
                default:
                    throw new UnsupportedOperationException();
            }
        } else if (bw == ComboBoxWidget.BUTTON_COMBO_BOX_TEXTURED
                     || bw == ComboBoxWidget.BUTTON_COMBO_BOX_TEXTURED_TOOLBAR){
            switch (sz) {
                case LARGE:
                case REGULAR:
                    return new BasicRendererDescription(0, 0, 0, 0);
                case SMALL:
                    return new BasicRendererDescription(0, 0, 0, 0);
                case MINI:
                    return new BasicRendererDescription(0, 0, 0, 0);
                default:
                    throw new UnsupportedOperationException();
            }
        } else {
            switch (sz) {
                case LARGE:
                case REGULAR:
                    return new BasicRendererDescription(-0.5f, 0, 2, 1);
                case SMALL:
                    return new BasicRendererDescription(-0.5f, 0, 1, 0);
                case MINI:
                    return new BasicRendererDescription(-0.5f, -0.51f, 2, 1);
                default:
                    throw new UnsupportedOperationException();
            }
        }
    }

    @Override
    public @NotNull RendererDescription getPopupButtonRendererDescription(@NotNull PopupButtonConfiguration g)
    {
        PopupButtonWidget bw = g.getPopupButtonWidget();
        Size sz = g.getSize();

        if (bw == PopupButtonWidget.BUTTON_POP_UP_CELL) {
            // extra height not needed for Core UI renderer
            switch (sz) {
                case LARGE:
                case REGULAR:
                    return new BasicRendererDescription(0, 0, 0, 0);
                case SMALL:
                    return new BasicRendererDescription(0, 0, 0, 0);
                case MINI:
                    return new BasicRendererDescription(0, 0, 0, 0);
                default:
                    throw new UnsupportedOperationException();
            }
        }

        return super.getPopupButtonRendererDescription(g);
    }

    public @Nullable RendererDescription getPopUpArrowRendererDescription(@NotNull PopupButtonConfiguration g,
                                                                          @NotNull Size arrowSize)
    {
        float left = JNRUtils.size2D(arrowSize, 1, 0, 2);
        float top = JNRUtils.size2D(arrowSize, 3, 1, 1);
        int w = JNRUtils.size(arrowSize, 1, 0, 1);
        int h = JNRUtils.size(arrowSize, 3, 1, 0);
        return new BasicRendererDescription(-left, -top, w, h);
    }

    public @NotNull RendererDescription getPullDownArrowRendererDescription(@NotNull PopupButtonConfiguration g)
    {
        return new BasicRendererDescription(0, 0, 0, 0);
    }

    public @NotNull RendererDescription getScrollBarThumbRendererDescription(@NotNull ScrollBarConfiguration g)
    {
        // NSScroller leaves a 3 point gap at the ends for legacy, apparently 2 points for overlay.
        // CoreUI leaves a 1 point gap at the ends.
        // Here we compensate.

        int extra = g.getWidget() == ScrollBarWidget.LEGACY ? 2 : 1;

        if (g.getOrientation() == Orientation.VERTICAL) {
            return new BasicRendererDescription(0, extra, 0, -2*extra);
        } else {
            return new BasicRendererDescription(extra, 0, -2*extra, 0);
        }
    }

    @Override
    public @NotNull RendererDescription getSliderThumbRendererDescription(@NotNull SliderConfiguration g)
    {
        // macOS 11 introduced new linear slider styles with different layout properties. However, the NSView renderer
        // may or may not use the new style, based on runtime determined linkage information.

        if (!g.isLinear() || AquaUIPainterBase.internalGetSliderRenderingVersion() == AquaUIPainterBase.SLIDER_10_10) {
            return super.getSliderThumbRendererDescription(g);
        }

        return getSlider11ThumbRendererDescription(g);
    }

    private @NotNull RendererDescription getSlider11ThumbRendererDescription(@NotNull SliderConfiguration g)
    {
        Size sz = g.getSize();
        float h = g.hasTickMarks() ? size2D(sz, 3.5, 0, 0) : size2D(sz, 4, 0, 0);
        return new BasicRendererDescription(0, 0, 0, h);
    }

    @Override
    public @NotNull RendererDescription getSliderTickMarkRendererDescription(@NotNull SliderConfiguration g)
    {
        // macOS 11 introduced new linear slider styles with different layout properties. However, the NSView renderer
        // may or may not use the new style, based on runtime determined linkage information.

        if (!g.isLinear() || AquaUIPainterBase.internalGetSliderRenderingVersion() == AquaUIPainterBase.SLIDER_10_10) {
            return super.getSliderTickMarkRendererDescription(g);
        }

        Size sz = g.getSize();
        float h = g.hasTickMarks() ? size2D(sz, 4, 0, 0) : 0;
        return new BasicRendererDescription(0, 0, 0, h);
    }
}
