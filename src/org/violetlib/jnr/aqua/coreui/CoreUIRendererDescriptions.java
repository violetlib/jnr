/*
 * Copyright (c) 2015-2020 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.aqua.coreui;

import org.violetlib.jnr.aqua.AquaUIPainter;
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
        AquaUIPainter.DividerWidget dw = g.getWidget();
        AquaUIPainter.Orientation o = g.getOrientation();

        switch (g.getWidget())
        {
            case THIN_DIVIDER:
            case THICK_DIVIDER:
                return new BasicRendererDescription(0, 0, 0, 0);
            case PANE_SPLITTER:
                return o == AquaUIPainter.Orientation.HORIZONTAL
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
        AquaUIPainter.ButtonWidget bw = toCanonicalButtonStyle(g.getButtonWidget());
        AquaUIPainter.Size sz = g.getSize();

        if (bw == AquaUIPainter.ButtonWidget.BUTTON_CHECK_BOX) {
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

        } else if (bw == AquaUIPainter.ButtonWidget.BUTTON_RADIO) {
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

        } else if (bw == AquaUIPainter.ButtonWidget.BUTTON_ROUND) {
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
        } else if (bw == AquaUIPainter.ButtonWidget.BUTTON_TEXTURED
                     || bw == AquaUIPainter.ButtonWidget.BUTTON_TEXTURED_TOOLBAR) {
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

        AquaUIPainter.SegmentedButtonWidget bw = g.getWidget();
        AquaUIPainter.Size sz = g.getSize();

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
                yOffset = size2D(sz, -0.51f, -1.49f, -2);  // regular size should be -1 at 1x
                leftOffset = size(sz, -2, -2, -1);

                if (shouldUseSpecialSeparatedDescription(g)) {
                    // completely different rules
                    return getSegmentedSeparatedRendererDescription(g, rd, yOffset, leftOffset);
                }

                leftExtraWidth = rightExtraWidth = size(sz, 2, 2, 1);
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
            case BUTTON_SEGMENTED_TEXTURED_SEPARATED:
            case BUTTON_SEGMENTED_TEXTURED_SEPARATED_TOOLBAR:

                if (platformVersion >= 101600) {
                    if (!bw.isToolbar()) {
                        return new BasicRendererDescription(-1, 0, 0, 1);
                    }
                }

                if (sz == AquaUIPainter.Size.MINI) {
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
        if (g.getPosition() == AquaUIPainter.Position.ONLY) {
            return false;
        }
        AquaUIPainter.SegmentedButtonWidget bw = g.getWidget();
        if (bw == AquaUIPainter.SegmentedButtonWidget.BUTTON_SEGMENTED_SEPARATED) {
            return true;
        }
        if (bw == AquaUIPainter.SegmentedButtonWidget.BUTTON_TAB && g.isSelected()) {
            int platformVersion = JNRPlatformUtils.getPlatformVersion();
            return platformVersion >= 101600;
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

        AquaUIPainter.Position pos = g.getPosition();
        if (pos == AquaUIPainter.Position.FIRST) {
            if (!hasRight) {
                extraWidth1 = 1;
                extraWidth2 = 0.5f;

            } else {
                extraWidth2 = 0;
            }
        } else if (pos == AquaUIPainter.Position.MIDDLE) {
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
        } else if (pos == AquaUIPainter.Position.LAST) {
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

        AquaUIPainter.Position pos = g.getPosition();
        if (pos == AquaUIPainter.Position.FIRST) {
        } else if (pos == AquaUIPainter.Position.MIDDLE) {
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
        } else if (pos == AquaUIPainter.Position.LAST) {
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
        AquaUIPainter.ComboBoxWidget bw = g.getWidget();
        AquaUIPainter.Size sz = g.getSize();

        if (bw == AquaUIPainter.ComboBoxWidget.BUTTON_COMBO_BOX_CELL) {
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
        } else if (bw == AquaUIPainter.ComboBoxWidget.BUTTON_COMBO_BOX_TEXTURED
                     || bw == AquaUIPainter.ComboBoxWidget.BUTTON_COMBO_BOX_TEXTURED_TOOLBAR){
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
        AquaUIPainter.PopupButtonWidget bw = g.getPopupButtonWidget();
        AquaUIPainter.Size sz = g.getSize();

        if (bw == AquaUIPainter.PopupButtonWidget.BUTTON_POP_UP_CELL) {
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
                                                                          @NotNull AquaUIPainter.Size arrowSize)
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

        int extra = g.getWidget() == AquaUIPainter.ScrollBarWidget.LEGACY ? 2 : 1;

        if (g.getOrientation() == AquaUIPainter.Orientation.VERTICAL) {
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
        AquaUIPainter.Size sz = g.getSize();
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

        AquaUIPainter.Size sz = g.getSize();
        float h = g.hasTickMarks() ? size2D(sz, 4, 0, 0) : 0;
        return new BasicRendererDescription(0, 0, 0, h);
    }
}
