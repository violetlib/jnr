/*
 * Copyright (c) 2015-2026 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.aqua.impl;

import org.jetbrains.annotations.NotNull;
import org.violetlib.jnr.aqua.*;
import org.violetlib.jnr.aqua.AquaUIPainter.*;
import org.violetlib.jnr.impl.BasicRendererDescription;
import org.violetlib.jnr.impl.JNRUtils;
import org.violetlib.jnr.impl.MultiResolutionRendererDescription;
import org.violetlib.jnr.impl.RendererDescription;

import static org.violetlib.jnr.aqua.AquaUIPainter.ButtonWidget.*;
import static org.violetlib.jnr.aqua.AquaUIPainter.PopupButtonWidget.*;
import static org.violetlib.jnr.aqua.AquaUIPainter.SegmentedButtonWidget.BUTTON_SEGMENTED_SEPARATED;
import static org.violetlib.jnr.aqua.AquaUIPainter.SegmentedButtonWidget.BUTTON_SEGMENTED_TEXTURED_SEPARATED;
import static org.violetlib.jnr.aqua.AquaUIPainter.macOS11;
import static org.violetlib.jnr.aqua.AquaUIPainter.macOS26;
import static org.violetlib.jnr.impl.JNRUtils.size;
import static org.violetlib.jnr.impl.JNRUtils.size2D;

/**

*/

public abstract class RendererDescriptionsImpl
{
    // TBD: should have the class with override available for nested requests.
    // Cyclic, would need separate configure method.

    protected RendererDescriptionsImpl()
    {
    }

    public @NotNull RendererDescription getRendererDescription(@NotNull ButtonConfiguration g)
    {
        ButtonWidget bw = toCanonicalButtonStyle(g.getButtonWidget());

        if (bw == BUTTON_TOOLBAR_ITEM) {
            ToolBarItemWellConfiguration tg = new ToolBarItemWellConfiguration(g.getState(), true);
            return getToolBarItemWellRendererDescription(tg);
        }

        Size sz = g.getSize();

        int version = AquaNativeRendering.getSystemRenderingVersion();
        if (bw == BUTTON_ROUND_TEXTURED_TOOLBAR && version < 101100) {
            bw = BUTTON_ROUND;
        }

        if (bw == BUTTON_PUSH) {
            switch (sz) {
                case EXTRA_LARGE:
                    return new BasicRendererDescription(0, 0, 0, 0);
                case LARGE:
                    if (version >= 101500 && version < macOS11) {
                        return BasicRendererDescription.create(6, 12, 0, 2);
                    }
                    return new BasicRendererDescription(-5, -1, 10, 1);
                case REGULAR:
                    return new BasicRendererDescription(-6, 0, 12, 2);
                case SMALL:
                    if (version >= macOS11) {
                        return new BasicRendererDescription(-5, 0, 10, 2);
                    }
                    if (version >= 101500) {
                        return BasicRendererDescription.create(5, 10, 0, 2);
                    }
                    return new BasicRendererDescription(-5, -1, 10, 3);
                case MINI:
                    if (version >= macOS11) {
                        return new BasicRendererDescription(-1, -1, 2, 2);
                    }
                    return new BasicRendererDescription(-1, 0, 2, 0);
                default:
                    throw new UnsupportedOperationException();
            }

        } else if (bw == BUTTON_BEVEL) {
            return new BasicRendererDescription(0, 0, 0, 0);

        } else if (bw == BUTTON_BEVEL_ROUND || bw == BUTTON_GLASS) {
            if (sz == Size.EXTRA_LARGE) {
                return new BasicRendererDescription(0, 0, 0, 0);
            }
            return new BasicRendererDescription(-2, -2, 4, 5);

        } else if (bw == BUTTON_CHECK_BOX) {
            switch (sz) {
                case EXTRA_LARGE:
                case LARGE:
                case REGULAR:
                    return new BasicRendererDescription(0, 0, 0, 0);
                case SMALL:
                    return new BasicRendererDescription(0, 0, 0, 0);
                case MINI:
                    return new BasicRendererDescription(0, -1, 0, 1);
                default:
                    throw new UnsupportedOperationException();
            }

        } else if (bw == BUTTON_RADIO) {
            switch (sz) {
                case EXTRA_LARGE:
                case LARGE:
                case REGULAR:
                    return new BasicRendererDescription(0, 0, 0, 0);
                case SMALL:
                    return new BasicRendererDescription(0, 0, 0, 1);
                case MINI:
                    return new BasicRendererDescription(0, 0, 0, 1);
                default:
                    throw new UnsupportedOperationException();
            }

        } else if (bw == BUTTON_DISCLOSURE) {
            switch (sz) {
                case EXTRA_LARGE:
                case LARGE:
                case REGULAR:
                    return new BasicRendererDescription(0, 0, 0, 0);
                case SMALL:
                    return new BasicRendererDescription(0, 0, 0, 0);
                case MINI:
                    if (version >= macOS11) {
                        return BasicRendererDescription.create(1, 2, 0, 0);
                    }
                    return new BasicRendererDescription(0, 0, 2, 0);
                default:
                    throw new UnsupportedOperationException();
            }

        } else if (bw == BUTTON_HELP) {
            if (version >= 101500) {
                switch (sz) {
                    case EXTRA_LARGE:
                    case LARGE:
                    case REGULAR:
                        return BasicRendererDescription.create(0, 0, 0, 4);
                    case SMALL:
                        return BasicRendererDescription.create(0, 0, 0, 0);
                    case MINI:
                        return BasicRendererDescription.create(0, 1, 0, 0);
                    default:
                        throw new UnsupportedOperationException();
                }
            } else {
                switch (sz) {
                    case EXTRA_LARGE:
                    case LARGE:
                    case REGULAR:
                        return new BasicRendererDescription(0, 0, 0, version < 101200 ? 3 : 0);
                    case SMALL:
                        return new BasicRendererDescription(0, 0, 0, 0);
                    case MINI:
                        return version < 101200
                          ? new BasicRendererDescription(0, -0.5f, 1, 0)
                          : new BasicRendererDescription(-0.49f, 0, 1, 0);
                    default:
                        throw new UnsupportedOperationException();
                }
            }

        } else if (bw == BUTTON_GRADIENT) {
            return new BasicRendererDescription(0, -1, 0, 2);

        } else if (bw == BUTTON_RECESSED) {
            int ha = version >= macOS26 ? 0 : 1;
            return new BasicRendererDescription(0, 0, 0, ha);

        } else if (bw == BUTTON_INLINE) {
            return new BasicRendererDescription(0, 0, 0, 0);

        } else if (bw == BUTTON_ROUNDED_RECT) {
            return new BasicRendererDescription(0, 0, 0, 0);

        } else if (bw == BUTTON_TEXTURED) {
            if (version >= 101100 && version < macOS11) {
                return BasicRendererDescription.create(0, 0, 0, 1);
            }
            return new BasicRendererDescription(0, 0, 0, 0);

        } else if (bw == BUTTON_TOOLBAR || bw == BUTTON_TEXTURED_TOOLBAR) {
            if (version >= 101100 && version < macOS11) {
                return BasicRendererDescription.create(0, 0, 0, 1);
            }
            return BasicRendererDescription.create(0, 0, 0, 0);

        } else if (bw == BUTTON_ROUND) {
            switch (sz) {
                case EXTRA_LARGE:
                case LARGE:
                case REGULAR:
                    return new BasicRendererDescription(0, -4, 0, 10);  // tall raster needed to show a regular size button
                case SMALL:
                    return new BasicRendererDescription(0, -2, 0, 8);   // tall raster needed to show a small size button
                case MINI:
                    RendererDescription rd1 = new BasicRendererDescription(0, -1, 0, 3);
                    RendererDescription rd2 = new BasicRendererDescription(0, -0.5f, 0, 3);
                    return new MultiResolutionRendererDescription(rd1, rd2);
                default:
                    throw new UnsupportedOperationException();
            }

        } else if (bw == BUTTON_DISCLOSURE_TRIANGLE) {
            return new BasicRendererDescription(0, 0, 0, 0);

        } else if (bw == BUTTON_PUSH_INSET2) {
            return new BasicRendererDescription(0, 0, 0, 0);

        } else if (bw == BUTTON_ROUND_INSET) {
            return new BasicRendererDescription(0, 0, 0, 0);

        } else if (bw == BUTTON_ROUND_TEXTURED) {
            if (version >= 101500 && version < macOS11) {
                switch (sz) {
                    case EXTRA_LARGE:
                    case LARGE:
                    case REGULAR:
                        return BasicRendererDescription.create(0, 0, 0, 4);
                    case SMALL:
                        return BasicRendererDescription.create(0, 0, 0, 5);
                    case MINI:
                        return BasicRendererDescription.create(0, 0, 0, 3);
                    default:
                        throw new UnsupportedOperationException();
                }
            }
            return new BasicRendererDescription(0, 0, 0, 0);

        } else if (bw == BUTTON_ROUND_TEXTURED_TOOLBAR) {
            if (version >= 101500 && version < macOS11) {
                return BasicRendererDescription.create(0, 0, 0, 1);
            }
            return new BasicRendererDescription(0, 0, 0, 0);

        } else if (bw == BUTTON_COLOR_WELL) {
            return new BasicRendererDescription(0, 0, 0, 0);

        } else {
            throw new UnsupportedOperationException();
        }
    }

    public @NotNull RendererDescription getRendererDescription(@NotNull SegmentedButtonConfiguration g)
    {
        // The native view renderer renders an entire segmented control but arranges that only one button is rendered
        // into our buffer. It does not make sense to change the raster width, because the raster width is the only way
        // that the native renderer knows how wide the button should be. If any horizontal adjustment is needed, it
        // should be made by the native renderer.

        SegmentedButtonWidget bw = g.getWidget();
        Size sz = g.getSize();
        boolean isExclusive = g.getTracking() == SwitchTracking.SELECT_ONE;
        int version = AquaNativeRendering.getSystemRenderingVersion();

        if (bw == BUTTON_SEGMENTED_SEPARATED && version >= 150000 && version < macOS26) {
            // TBD: not sure when this became an issue
            if (sz == Size.LARGE || sz == Size.EXTRA_LARGE) {
                return new BasicRendererDescription(-5, -5, 10, 6);
            }
        }

        switch (bw) {
            case BUTTON_TAB:
            case BUTTON_SEGMENTED:
            case BUTTON_SEGMENTED_SEPARATED:
            case BUTTON_SEGMENTED_SLIDER:
            case BUTTON_SEGMENTED_SLIDER_TOOLBAR:
                if (version >= 150000 && version < macOS26 && !isExclusive) {
                    // TBD: not sure when this became an issue
                    switch (sz) {
                        case EXTRA_LARGE:
                        case LARGE:
                            return new BasicRendererDescription(-5, -5, 9, 6);
                        case REGULAR:
                        case SMALL:
                            return new BasicRendererDescription(-2, -1, 5, 1);
                        case MINI:
                            return new BasicRendererDescription(-1, 0, 4, 0);
                        default:
                            throw new UnsupportedOperationException();
                    }
                }

                if (version >= macOS11) {
                    switch (sz) {
                        case EXTRA_LARGE:
                        case LARGE:
                        case REGULAR:
                            return createVertical(-1, 1);
                        case SMALL:
                            return createVertical(-1, 2);
                        case MINI:
                            return createVertical(0, 1);
                        default:
                            throw new UnsupportedOperationException();
                    }
                } else {
                    switch (sz) {
                        case EXTRA_LARGE:
                        case LARGE:
                        case REGULAR:
                            return createVertical(0, 1);
                        case SMALL:
                            return createVertical(0.49f, 2);
                        case MINI:
                            return createVertical(-0.51f, 5);
                        default:
                            throw new UnsupportedOperationException();
                    }
                }

            case BUTTON_SEGMENTED_INSET:
                switch (sz) {
                    case EXTRA_LARGE:
                    case LARGE:
                    case REGULAR:
                        return createVertical(-1, 4);
                    case SMALL:
                        return createVertical(-1, 5);
                    case MINI:
                        return createVertical(-1, 6);
                    default:
                        throw new UnsupportedOperationException();
                }

            case BUTTON_SEGMENTED_TEXTURED_TOOLBAR:
            case BUTTON_SEGMENTED_TOOLBAR:

                if (version >= 101100 && version < 101200) {
                    if (g.getPosition() == Position.LAST) {
                        return BasicRendererDescription.create(0, 1, 0, 1);
                    }
                    return BasicRendererDescription.create(1, 1, 0, 1);
                }

                // fall through

            case BUTTON_SEGMENTED_SCURVE:
            case BUTTON_SEGMENTED_TEXTURED:
            case BUTTON_SEGMENTED_TEXTURED_SEPARATED:
            case BUTTON_SEGMENTED_TEXTURED_SEPARATED_TOOLBAR:

                if (version >= 101100 && version < 101200) {
                    return BasicRendererDescription.create(0, 0, 0, 1);
                }

                switch (sz) {
                    case EXTRA_LARGE:
                    case LARGE:
                    case REGULAR:
                        return version >= 101100 ? createVertical(-1.49f, 3) : createVertical(-1, 2);
                    case SMALL:
                    {
                        float y = -0.49f;
                        if (bw.isToolbar()) {
                            y = -1.49f;
                        } else if (bw == BUTTON_SEGMENTED_TEXTURED_SEPARATED) {
                            y = -0.1f;
                        }
                        return version >= 101100
                          ? createVertical(y, 4)
                          : createVertical(-1, 4);
                    }
                    case MINI:
                        return new MultiResolutionRendererDescription(
                          createVertical(version >= 101100 ? 0 : -1, 5),
                          createVertical(0, version >= 101100 ? 5 : 4.5f));
                    default:
                        throw new UnsupportedOperationException();
                }

            case BUTTON_SEGMENTED_SMALL_SQUARE:
                switch (sz) {
                    case EXTRA_LARGE:
                    case LARGE:
                        if (version >= macOS11) {
                            return createVertical(0, 2);
                        } else {
                            return createVertical(0, 8);
                        }
                    case REGULAR:
                        return createVertical(0, 2);
                    case SMALL:
                        return createVertical(0, 3);
                    case MINI:
                        return createVertical(0, 4);
                    default:
                        throw new UnsupportedOperationException();
                }

            default:
                throw new UnsupportedOperationException();
        }
    }

    protected @NotNull RendererDescription createVertical(float yOffset, float heightAdjustment)
    {
        return new BasicRendererDescription(0, yOffset, 0, heightAdjustment);
    }

    protected @NotNull RendererDescription createVertical(float yOffset1, float yOffset2, float heightAdjustment)
    {
        RendererDescription rd1 = new BasicRendererDescription(0, yOffset1, 0, heightAdjustment);
        RendererDescription rd2 = new BasicRendererDescription(0, yOffset2, 0, heightAdjustment);
        return new MultiResolutionRendererDescription(rd1, rd2);
    }

    protected @NotNull RendererDescription adjustSegmentedRendererDescription(
      @NotNull SegmentedButtonConfiguration g,
      @NotNull RendererDescription rd,
      float extraWidth,
      float xOffset,
      float yOffset,
      float leftOffset,
      float leftExtraWidth,
      float rightExtraWidth,
      float extraHeight
    )
    {
        AquaUIPainter.SegmentedButtonWidget bw = g.getWidget();
        boolean isSeparated = bw.isSeparated();

        Position pos = g.getPosition();

        boolean atLeftEdge = pos == Position.FIRST || pos == Position.ONLY;
        boolean atRightEdge = pos == Position.LAST || pos == Position.ONLY;

        boolean isLeftDividerPossible = !isSeparated
          && (pos == Position.MIDDLE || pos == Position.LAST);
        boolean isRightDividerPossible = !isSeparated
          && (pos == Position.FIRST || pos == Position.MIDDLE);

        if (atLeftEdge) {
            xOffset += leftOffset;
            extraWidth += leftExtraWidth;
        }

        if (atRightEdge) {
            extraWidth += rightExtraWidth;
        }

        // If a left divider is possible and not requested, widen the rendering and shift it left by one point so that
        // no divider is painted in our raster. This is necessary because the divider space is allocated even if it is
        // not painted. Similar for a right divider.

        if (isLeftDividerPossible && g.getLeftDividerState() == SegmentedButtonConfiguration.DividerState.NONE) {
            xOffset -= 1;
            extraWidth += 1;
        }

        if (isRightDividerPossible && g.getRightDividerState() == SegmentedButtonConfiguration.DividerState.NONE) {
            extraWidth += 1;
        }

        try {
            return JNRUtils.adjustRendererDescription(rd, xOffset, yOffset, extraWidth, extraHeight);
        } catch (UnsupportedOperationException ex) {
            NativeSupport.log("Unable to adjust segmented button renderer description for " + g);
            return rd;
        }
    }

    public @NotNull RendererDescription getRendererDescription(@NotNull PopupButtonConfiguration g)
    {
        PopupButtonWidget bw = g.getPopupButtonWidget();
        Size sz = g.getSize();
        int platformVersion = AquaNativeRendering.getSystemRenderingVersion();

        boolean isSquare = bw == BUTTON_POP_UP_SQUARE || bw == BUTTON_POP_DOWN_SQUARE;
        boolean isArrowsOnly = bw == BUTTON_POP_UP_CELL || bw == BUTTON_POP_DOWN_CELL;

        if ((isSquare || isArrowsOnly) && sz == Size.MINI) {
            sz = Size.SMALL;
        }

        switch (bw) {
            case BUTTON_POP_UP:
                if (platformVersion >= macOS11) {
                    switch (sz) {
                        case EXTRA_LARGE:
                        case LARGE:
                            return new BasicRendererDescription(-4, -2, 8, 3);
                        case REGULAR:
                            return new BasicRendererDescription(-2, 0, 5, 2);
                        case SMALL:
                            return new BasicRendererDescription(-3, 0, 6, 2);
                        case MINI:
                            return new BasicRendererDescription(-1, -0.51f, 3, 1);
                        default:
                            throw new UnsupportedOperationException();
                    }
                } else if (platformVersion >= 101400) {
                    switch (sz) {
                        case EXTRA_LARGE:
                        case LARGE:
                        case REGULAR:
                            return BasicRendererDescription.create(2, 5, 0, 1);
                        case SMALL:
                            return BasicRendererDescription.create(3, 6, 0, 1);
                        case MINI:
                            return BasicRendererDescription.create(1, 3, 0, 0);
                        default:
                            throw new UnsupportedOperationException();
                    }
                } else {
                    switch (sz) {
                        case EXTRA_LARGE:
                        case LARGE:
                        case REGULAR:
                            return new BasicRendererDescription(-2, 0, 5, 0);
                        case SMALL:
                            return new BasicRendererDescription(-3, 0, 6, 0);
                        case MINI:
                            return new BasicRendererDescription(-1, 0, 3, 0);
                        default:
                            throw new UnsupportedOperationException();
                    }
                }
            case BUTTON_POP_UP_CELL:
                // extra height not needed for Core UI renderer (see subclass)
                switch (sz) {
                    case EXTRA_LARGE:
                    case LARGE:
                    case REGULAR:
                        return new BasicRendererDescription(0, -3, 0, 3);
                    case SMALL:
                        return new BasicRendererDescription(0, -1, 0, 1);
                    case MINI:
                        return new BasicRendererDescription(-1, -1, 3, 1);
                    default:
                        throw new UnsupportedOperationException();
                }
            case BUTTON_POP_UP_SQUARE:
                switch (sz) {
                    case EXTRA_LARGE:
                    case LARGE:
                    case REGULAR:
                        return new BasicRendererDescription(0, 0, 0, 0);
                    case SMALL:
                        return new BasicRendererDescription(0, 0, 0, 0);
                    case MINI:
                        return new BasicRendererDescription(-1, -1, 3, 1);
                    default:
                        throw new UnsupportedOperationException();
                }
            case BUTTON_POP_DOWN:
                if (platformVersion >= macOS11) {
                    switch (sz) {
                        case EXTRA_LARGE:
                        case LARGE:
                            return new BasicRendererDescription(-4, -1, 8, 2);
                        case REGULAR:
                            return new BasicRendererDescription(-2, 0, 5, 2);
                        case SMALL:
                            return new BasicRendererDescription(-3, -0.51f, 6, 2);
                        case MINI:
                            return new BasicRendererDescription(-1, -1, 3, 2);
                        default:
                            throw new UnsupportedOperationException();
                    }
                } else {
                    switch (sz) {
                        case EXTRA_LARGE:
                        case LARGE:
                        case REGULAR:
                            return new BasicRendererDescription(-3, 0, 6, 1);
                        case SMALL:
                            return new BasicRendererDescription(-3, 0, 6, 1);
                        case MINI:
                            return new BasicRendererDescription(0, 0, 1, 0);
                        default:
                            throw new UnsupportedOperationException();
                    }
                }
            case BUTTON_POP_DOWN_CELL:
                switch (sz) {
                    case EXTRA_LARGE:
                    case LARGE:
                    case REGULAR:
                        return new BasicRendererDescription(0, 0, 0, 0);
                    case SMALL:
                        return new BasicRendererDescription(0, 0, 0, 0);
                    case MINI:
                        return new BasicRendererDescription(0, 0, 1, 0);
                    default:
                        throw new UnsupportedOperationException();
                }
            case BUTTON_POP_DOWN_SQUARE:
                switch (sz) {
                    case EXTRA_LARGE:
                    case LARGE:
                    case REGULAR:
                        return new BasicRendererDescription(0, 0, 0, 0);
                    case SMALL:
                        return new BasicRendererDescription(0, 0, 0, 0);
                    case MINI:
                        return new BasicRendererDescription(0, -1, 1, 1);
                    default:
                        throw new UnsupportedOperationException();
                }

            case BUTTON_POP_UP_BEVEL:
            case BUTTON_POP_DOWN_BEVEL:
                return new BasicRendererDescription(-2, -2, 4, 4);

            case BUTTON_POP_UP_ROUND_RECT:
            case BUTTON_POP_DOWN_ROUND_RECT:
                return new BasicRendererDescription(0, 0, 0, 1);

            case BUTTON_POP_UP_RECESSED:
            case BUTTON_POP_DOWN_RECESSED:
                return new BasicRendererDescription(0, 0, 0, 1);

            case BUTTON_POP_DOWN_TEXTURED:
            case BUTTON_POP_UP_TEXTURED:
            case BUTTON_POP_DOWN_TEXTURED_TOOLBAR:
            case BUTTON_POP_UP_TEXTURED_TOOLBAR:
                if (platformVersion >= macOS11) {
                    switch (sz) {
                        case EXTRA_LARGE:
                        case LARGE:
                        case REGULAR:
                            return new BasicRendererDescription(-2, 0, 5, 2);
                        case SMALL:
                            return new BasicRendererDescription(-2.51f, 0, 5.51f, 2);
                        case MINI:
                            return new BasicRendererDescription(-1, -1, 3, 2);
                        default:
                            throw new UnsupportedOperationException();
                    }
                } else if (platformVersion >= 101100) {
                    return BasicRendererDescription.create(0, 0, 0, 1);
                } else {
                    return new BasicRendererDescription(0, 0, 0, 0);
                }

            case BUTTON_POP_UP_GRADIENT:
            case BUTTON_POP_DOWN_GRADIENT:
                return new BasicRendererDescription(0, -1, 0, 2);
            default:
                throw new UnsupportedOperationException();
        }
    }

    protected abstract @NotNull RendererDescription getRendererDescription(@NotNull ComboBoxConfiguration g);

    protected abstract @NotNull RendererDescription getRendererDescription(@NotNull SplitPaneDividerConfiguration g);

    public @NotNull RendererDescription getToolBarItemWellRendererDescription(@NotNull ToolBarItemWellConfiguration g)
    {
        return new BasicRendererDescription(0, 0, 0, 0);
    }

    public @NotNull RendererDescription getTitleBarRendererDescription(@NotNull TitleBarConfiguration g)
    {
        int version = AquaNativeRendering.getSystemRenderingVersion();
        int ha = version >= macOS26 && g.getWidget() == TitleBarWidget.UTILITY_WINDOW ? 4 : 0;
        return new BasicRendererDescription(0, 0, 0, ha);
    }

    public @NotNull RendererDescription getSliderRendererDescription(@NotNull SliderConfiguration g)
    {
        return new BasicRendererDescription(0, 0, 0, 0);
    }

    public @NotNull RendererDescription getSliderTrackRendererDescription(@NotNull SliderConfiguration g)
    {
        return new BasicRendererDescription(0, 0, 0, 0);
    }

    public @NotNull RendererDescription getCustomSliderTickMarkRendererDescription(@NotNull SliderConfiguration g)
    {
        return new BasicRendererDescription(0, 0, 0, 0);
    }

    public @NotNull RendererDescription getCustomSliderThumbRendererDescription(@NotNull SliderConfiguration g)
    {
        Size sz = g.getSize();

        if (g.isHorizontal() || g.isVertical()) {
            if (!g.hasTickMarks()) {
                float xh = 0;
                if (AquaNativePainter.getSliderRenderingVersion() == AquaUIPainterBase.SLIDER_11_0) {
                    if (sz == Size.REGULAR) {
                        xh = 4;
                    }
                }
                float yOffset1 = -1;
                float yOffset2 = g.isHorizontal() ? size2D(sz, -0.5f, 0, 0) : size2D(sz, -0.5f, -0.5f, -0.5f);
                RendererDescription rd1 = new BasicRendererDescription(0, yOffset1, 0, xh);
                RendererDescription rd2 = new BasicRendererDescription(0, yOffset2, 0, xh);
                return new MultiResolutionRendererDescription(rd1, rd2);
            }
            if (g.isHorizontal()) {
                float xh = 0;
                if (AquaNativePainter.getSliderRenderingVersion() == AquaUIPainterBase.SLIDER_11_0) {
                    if (sz == Size.REGULAR) {
                        xh = 3;
                    }
                }
                // The goal is to visually center the pointer horizontally in the layout width
                float xOffset1 = 0;
                float xOffset2 = size2D(sz, 0, 0, 0);
                if (g.getTickMarkPosition() == AquaUIPainter.TickMarkPosition.ABOVE) {
                    float yOffset1 = 0;
                    float yOffset2 = size2D(sz, 0, 0, 0);
                    xh += size2D(sz, 1, 0, 0);
                    RendererDescription rd1 = new BasicRendererDescription(xOffset1, yOffset1, 0, xh);
                    RendererDescription rd2 = new BasicRendererDescription(xOffset2, yOffset2, 0, xh);
                    return new MultiResolutionRendererDescription(rd1, rd2);
                } else {
                    float yOffset1 = size2D(sz, -1, 0, 0);
                    float yOffset2 = size2D(sz, 0, 0, 0);
                    xh += 1;
                    RendererDescription rd1 = new BasicRendererDescription(xOffset1, yOffset1, 0, xh);
                    RendererDescription rd2 = new BasicRendererDescription(xOffset2, yOffset2, 0, xh);
                    return new MultiResolutionRendererDescription(rd1, rd2);
                }
            } else {
                // Vertical sliders
                float xh = 0;
                if (AquaNativePainter.getSliderRenderingVersion() == AquaUIPainterBase.SLIDER_11_0) {
                    if (sz == Size.REGULAR) {
                        xh = 3;
                    }
                }
                // The goal is to visually center the pointer vertically in the layout height, ignoring the shadow
                float yOffset1 = 0;
                float yOffset2 = size2D(sz, 0, 0.5f, 0.5f);
                if (g.getTickMarkPosition() == AquaUIPainter.TickMarkPosition.LEFT) {
                    float wa = size2D(sz, 1, 2, 2);
                    xh += size(sz, 1, 0, 0);
                    RendererDescription rd1 = new BasicRendererDescription(0, yOffset1, wa, xh);
                    RendererDescription rd2 = new BasicRendererDescription(0, yOffset2, wa, xh);
                    return new MultiResolutionRendererDescription(rd1, rd2);
                } else {
                    xh += size(sz, 1, 0, 0);
                    RendererDescription rd1 = new BasicRendererDescription(0, yOffset1, 0, xh);
                    RendererDescription rd2 = new BasicRendererDescription(0, yOffset2, 0, xh);
                    return new MultiResolutionRendererDescription(rd1, rd2);
                }
            }
        } else {
            return new BasicRendererDescription(0, 0, 0, 0);
        }
    }

    public @NotNull RendererDescription getSpinnerArrowsRendererDescription(@NotNull SpinnerArrowsConfiguration g)
    {
        int version = AquaNativeRendering.getSystemRenderingVersion();
        if (version >= macOS26) {
            return new BasicRendererDescription(0, 0, 0, 0);
        }
        Size sz = g.getSize();
        float t = size2D(sz, 0, 0, 0.5f, 0.5f);
        int h = size(sz, 0, 0, 1, 0);
        return new BasicRendererDescription(0, t, 0, h);
    }

    public @NotNull RendererDescription getGroupBoxRendererDescription(@NotNull GroupBoxConfiguration g)
    {
        return new BasicRendererDescription(-3, -2, 6, 6);
    }

    public @NotNull RendererDescription getListBoxRendererDescription(@NotNull ListBoxConfiguration g)
    {
        return new BasicRendererDescription(0, 0, 0, 0);
    }

    public @NotNull RendererDescription getTextFieldRendererDescription(@NotNull TextFieldConfiguration g)
    {
        return new BasicRendererDescription(0, 0, 0, 0);
    }

    public @NotNull RendererDescription getScrollBarRendererDescription(@NotNull ScrollBarConfiguration g)
    {
        int version = AquaNativeRendering.getSystemRenderingVersion();
        if (version >= macOS26) {
            if (g.getOrientation() == Orientation.HORIZONTAL) {
                return BasicRendererDescription.create(3, 6, 1, 4);
            } else {
                return BasicRendererDescription.create(1, 4, 3, 6);
            }
        }
        return new BasicRendererDescription(0, 0, 0, 0);
    }

    public @NotNull RendererDescription getRendererDescription(@NotNull ProgressIndicatorConfiguration g)
    {
        ProgressWidget pw = g.getWidget();
        Orientation o = g.getOrientation();

        int version = AquaNativeRendering.getSystemRenderingVersion();
        if (pw == ProgressWidget.BAR) {
            if (o == Orientation.HORIZONTAL) {
                return version >= macOS11 ? new BasicRendererDescription(0, -0.51f, 0, 1) : new BasicRendererDescription(-1, 0, 2, 1);
            } else {
                return version >= macOS11 ? new BasicRendererDescription(-1, 0, 2, 0) : new BasicRendererDescription(0, -1, 1, 2);
            }
        } else if (pw == ProgressWidget.SPINNER) {
            return new BasicRendererDescription(0, 0, 0, 0);
        } else {
            throw new UnsupportedOperationException();
        }
    }

    public @NotNull RendererDescription getRendererDescription(@NotNull IndeterminateProgressIndicatorConfiguration g)
    {
        ProgressWidget pw = g.getWidget();
        Orientation o = g.getOrientation();
        Size sz = g.getSize();
        int version = AquaNativeRendering.getSystemRenderingVersion();

        if (pw == ProgressWidget.INDETERMINATE_BAR) {
            if (o == Orientation.HORIZONTAL) {
                return version >= macOS11
                  ? sz == Size.SMALL ? new BasicRendererDescription(0, 0, 0, 1) : new BasicRendererDescription(0, 0, 0, 0)
                  : new BasicRendererDescription(-1, 0, 2, 1);
            } else {
                return version >= macOS11 ? new BasicRendererDescription(0, 0, 0, 0) : new BasicRendererDescription(0, -1, 1, 2);
            }
        } else if (pw == ProgressWidget.INDETERMINATE_SPINNER) {
            return new BasicRendererDescription(0, 0, 0, 0);
        } else {
            throw new UnsupportedOperationException();
        }
    }

    public @NotNull RendererDescription getTableColumnHeaderRendererDescription(@NotNull TableColumnHeaderConfiguration g)
    {
        return new BasicRendererDescription(0, 0, 0, 0);
    }

    /**
      Map a button widget to a canonical equivalent. This mapping addresses the fact that certain styles have become
      obsolete and are best supported by using a similar style.
    */

    protected @NotNull ButtonWidget toCanonicalButtonStyle(ButtonWidget bw)
    {
//        switch (bw) {
//            case BUTTON_ROUND_INSET:
//                return BUTTON_ROUND;
//            case BUTTON_ROUND_TEXTURED:
//                return BUTTON_ROUND;
//        }
        return bw;
    }

    protected int pos(@NotNull Position pos, int first, int last, int only)
    {
        switch (pos)
        {
            case FIRST:
                return first;
            case LAST:
                return last;
            case ONLY:
                return only;
            default:
                return 0;
        }
    }

    protected int pos(@NotNull Position pos, int first, int middle, int last, int only)
    {
        switch (pos)
        {
            case FIRST:
                return first;
            case MIDDLE:
                return middle;
            case LAST:
                return last;
            case ONLY:
                return only;
            default:
                return 0;
        }
    }

    protected float pos(@NotNull Position pos, float first, float last, float only)
    {
        switch (pos)
        {
            case FIRST:
                return first;
            case LAST:
                return last;
            case ONLY:
                return only;
            default:
                return 0;
        }
    }

    protected float pos(@NotNull Position pos, float first, float middle, float last, float only)
    {
        switch (pos)
        {
            case FIRST:
                return first;
            case MIDDLE:
                return middle;
            case LAST:
                return last;
            case ONLY:
                return only;
            default:
                return 0;
        }
    }
}
