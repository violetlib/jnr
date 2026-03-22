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
import org.violetlib.jnr.impl.BasicRendererDescription;
import org.violetlib.jnr.impl.MultiResolutionRendererDescription;
import org.violetlib.jnr.impl.RendererDescription;

import static org.violetlib.jnr.aqua.AquaUIPainter.ButtonWidget.*;
import static org.violetlib.jnr.aqua.AquaUIPainter.ComboBoxWidget.BUTTON_COMBO_BOX_CELL;
import static org.violetlib.jnr.aqua.AquaUIPainter.Orientation.HORIZONTAL;
import static org.violetlib.jnr.aqua.AquaUIPainter.PopupButtonWidget.BUTTON_POP_DOWN;
import static org.violetlib.jnr.aqua.AquaUIPainter.PopupButtonWidget.BUTTON_POP_UP;
import static org.violetlib.jnr.aqua.AquaUIPainter.macOS11;
import static org.violetlib.jnr.aqua.AquaUIPainter.macOS26;

/**
  Renderer descriptions for NSView based rendering on macOS 10.10 and later.
*/

public class ViewRendererDescriptions
  extends RendererDescriptionsImpl
{
    public ViewRendererDescriptions()
    {
    }

    @Override
    public @NotNull RendererDescription getRendererDescription(@NotNull ButtonConfiguration g)
    {
        int version = AquaNativeRendering.getSystemRenderingVersion();
        AquaUIPainter.ButtonWidget bw = g.getButtonWidget();
        AquaUIPainter.Size sz = g.getSize();

        if (version >= 120000 && version < macOS26) {
            if (bw == BUTTON_BEVEL_ROUND) {
                if (sz == AquaUIPainter.Size.MINI) {
                    return new BasicRendererDescription(-1, -1, 2, 1);
                } else {
                    return new BasicRendererDescription(-2, -2, 4, 4);
                }
            }
        }

        if (version >= macOS26) {
            if (bw == BUTTON_PUSH || bw == BUTTON_BEVEL_ROUND || bw == BUTTON_ROUNDED_RECT
              || bw == BUTTON_DISCLOSURE || bw == BUTTON_HELP
              || bw == BUTTON_CHECK_BOX || bw == BUTTON_RADIO
            ) {
                return new BasicRendererDescription(0, 0, 0, 0);
            }
        }

        if (version >= 150000) {
            if (bw == BUTTON_CHECK_BOX) {
                switch (sz) {
                    case EXTRA_LARGE:
                    case LARGE:
                        return new BasicRendererDescription(-1, 0, 0, 0);
                    case REGULAR:
                        return new BasicRendererDescription(0, 0, 1, 0);
                    case SMALL:
                        return new BasicRendererDescription(0, -0.49f, 0, 0);
                    case MINI:
                        return new BasicRendererDescription(0, -3, 0, 3);
                    default:
                        throw new UnsupportedOperationException();
                }
            } else if (bw == BUTTON_RADIO) {
                switch (sz) {
                    case EXTRA_LARGE:
                    case LARGE:
                        return new BasicRendererDescription(-1, 0, 1, 0);
                    case REGULAR:
                        return new BasicRendererDescription(0, 0, 0, 0);
                    case SMALL:
                        return new BasicRendererDescription(0, 0, 0, 2);
                    case MINI:
                        return new BasicRendererDescription(0, 0, 0, 3);
                    default:
                        throw new UnsupportedOperationException();
                }
            }
        }

        if (version >= macOS11) {
            if (bw == BUTTON_CHECK_BOX) {
                switch (sz) {
                    case EXTRA_LARGE:
                    case LARGE:
                        return new BasicRendererDescription(-1.49f, 0, 0, 0);
                    case REGULAR:
                        return new BasicRendererDescription(0, 0, 1, 0);
                    case SMALL:
                        return new BasicRendererDescription(0, -0.49f, 0, 0);
                    case MINI:
                        return new BasicRendererDescription(0, -1.49f, 0, 1);
                    default:
                        throw new UnsupportedOperationException();
                }
            } else if (bw == BUTTON_RADIO) {
                switch (sz) {
                    case EXTRA_LARGE:
                    case LARGE:
                        return new BasicRendererDescription(-1, 0, 1, 0);
                    case REGULAR:
                        return new BasicRendererDescription(0, 0, 0, 0);
                    case SMALL:
                        return new BasicRendererDescription(0, 0, 0, 1);
                    case MINI:
                        return new BasicRendererDescription(0, 0, 0, 1);
                    default:
                        throw new UnsupportedOperationException();
                }
            } else if (bw == BUTTON_TEXTURED) {
                return new BasicRendererDescription(-1, 0, 2, 0);
            } else if (bw == BUTTON_ROUND) {
                return new BasicRendererDescription(0, 0, 0, 0);
            } else if (bw == BUTTON_HELP) {
                switch (sz) {
                    case EXTRA_LARGE:
                    case LARGE:
                        return new BasicRendererDescription(0, 0, 0, 6);
                    case REGULAR:
                        return new BasicRendererDescription(-0.49f, 0, 0, 2);
                    case SMALL:
                        return new BasicRendererDescription(0, 0, 0, 0);
                    case MINI:
                        return new BasicRendererDescription(0.49f, 0, 0, 0);
                    default:
                        throw new UnsupportedOperationException();
                }
            } else if (bw == BUTTON_TOOLBAR) {
                return BasicRendererDescription.create(0, 0, 1, 2);
            } else if (bw == BUTTON_PUSH_INSET2) {
                switch (sz) {
                    case EXTRA_LARGE:
                    case LARGE:
                        return BasicRendererDescription.create(5, 10, 0, 0);
                    case REGULAR:
                        return BasicRendererDescription.create(6, 12, 0, 1);
                    case SMALL:
                        return BasicRendererDescription.create(5, 10, 0, 2);
                    case MINI:
                        return BasicRendererDescription.create(1, 2, 0, 0);
                    default:
                        throw new UnsupportedOperationException();
                }
            }
        } else if (version >= 101400) {
            if (bw == BUTTON_HELP) {
                switch (sz) {
                    case EXTRA_LARGE:
                    case LARGE:
                    case REGULAR:
                        return BasicRendererDescription.create(0, 0, 0, 3);
                    case SMALL:
                        return BasicRendererDescription.create(0, 0, 0, 0);
                    case MINI:
                        return BasicRendererDescription.create(0, 1, 0, 0);
                    default:
                        throw new UnsupportedOperationException();
                }
            }
            if (bw == BUTTON_ROUND_INSET && sz == AquaUIPainter.Size.SMALL) {
                return BasicRendererDescription.create(0, 0, 0, 5);
            }
        }

        if (bw == BUTTON_ROUNDED_RECT) {
            if (version < 101400) {
                return BasicRendererDescription.create(0, 0, 0, 1);
            } else if (version < macOS11) {
                if (sz == AquaUIPainter.Size.REGULAR) {
                    return BasicRendererDescription.create(0, -1, 0, 1);  // renderer is broken
                }
                return BasicRendererDescription.create(0, 0, 0, 1);
            }
        }

        if (version >= 101200 && version < 101500) {
            if (bw == BUTTON_ROUND_TEXTURED) {
                if (sz == AquaUIPainter.Size.SMALL && version >= 101400) {
                    return BasicRendererDescription.create(0, 0, 0, 4);
                }
                return BasicRendererDescription.create(0, 0, -1, 2);
            }
        }

        return super.getRendererDescription(g);
    }

    @Override
    public @NotNull RendererDescription getRendererDescription(@NotNull SegmentedButtonConfiguration g)
    {
        // This method is not used.
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull RendererDescription getRendererDescription(@NotNull PopupButtonConfiguration g)
    {
        int version = AquaNativeRendering.getSystemRenderingVersion();
        if (version >= macOS26) {
            return new BasicRendererDescription(0, 0, 0, 0);
        }

        AquaUIPainter.PopupButtonWidget w = g.getPopupButtonWidget();
        AquaUIPainter.Size sz = g.getSize();

        if (version >= macOS11) {
            if (w.isTextured()) {
                int width = 2;
                // Adjustments when using a simulated bezel (outline).
                if (version < 150000) {
                    width = -2;
                }
                switch (sz) {
                    case EXTRA_LARGE:
                    case LARGE:
                    case REGULAR:
                    case SMALL:
                    case MINI:
                        return BasicRendererDescription.create(1, width, 0, 1);
                    default:
                        throw new UnsupportedOperationException();
                }
            }
        }

        if (version >= macOS11) {
            if (w == BUTTON_POP_DOWN || w == BUTTON_POP_UP) {
                switch (sz) {
                    case EXTRA_LARGE:
                    case LARGE:
                        return new BasicRendererDescription(-4, -1, 8, 1);
                    case REGULAR:
                        return new BasicRendererDescription(-2, -1, 5, 4);
                    case SMALL:
                        return new BasicRendererDescription(-3, 0.51f, 5.51f, 1);
                    case MINI:
                        return new BasicRendererDescription(-1, 0, 3, 0);
                    default:
                        throw new UnsupportedOperationException();
                }
            } else if (w.isTextured()) {
                switch (sz) {
                    case EXTRA_LARGE:
                    case LARGE:
                        return BasicRendererDescription.create(2, 5, 0, 0);
                    case REGULAR:
                    case SMALL:
                        return BasicRendererDescription.create(1, 2, 0, 2);
                    case MINI:
                        return BasicRendererDescription.create(1, 2, 0, 1);
                    default:
                        throw new UnsupportedOperationException();
                }
            }
        }

        if (w.isTextured() && !w.isToolbar()) {
            if (version >= 101400 && version < 101500) {
                if (sz == AquaUIPainter.Size.MINI) {
                    return BasicRendererDescription.create(0, 1, 0, 1);
                }
                return BasicRendererDescription.create(0, 0, 0, 1);
            }
        }

        return super.getRendererDescription(g);
    }

    @Override
    public @NotNull RendererDescription getRendererDescription(@NotNull ComboBoxConfiguration g)
    {
        int version = AquaNativeRendering.getSystemRenderingVersion();
        if (version >= macOS26) {
            return new BasicRendererDescription(0, 0, 0, 0);
        }

        AquaUIPainter.ComboBoxWidget bw = g.getWidget();
        AquaUIPainter.Size sz = g.getSize();

        if (bw.isTextured() && version >= 150000) {
            return new BasicRendererDescription(-1, 0, 2, 1);
        }

        if (bw == BUTTON_COMBO_BOX_CELL) {
            switch (sz) {
                case EXTRA_LARGE:
                case LARGE:
                case REGULAR:
                    return new BasicRendererDescription(0, -3, 3, 3);
                case SMALL:
                    return new BasicRendererDescription(0, -1, 3, 1);
                case MINI:
                    return new BasicRendererDescription(0, 0, 2, 1);
                default:
                    throw new UnsupportedOperationException();
            }
        }

        if (bw.isTextured()) {
            if (version < 101100) {
                switch (sz) {
                    case EXTRA_LARGE:
                    case LARGE:
                    case REGULAR:
                        return BasicRendererDescription.create(0, 1, 0, 0);
                    case SMALL:
                        return BasicRendererDescription.create(0, 2, 0, 2);
                    case MINI:
                        return BasicRendererDescription.create(0, 0, 0, 2);
                    default:
                        throw new UnsupportedOperationException();
                }
            } else if (version < 101200) {
                switch (sz) {
                    case EXTRA_LARGE:
                    case LARGE:
                    case REGULAR:
                        return BasicRendererDescription.create(1, 3, 0, 1);
                    case SMALL:
                        return BasicRendererDescription.create(1, 4, 0, 3);
                    case MINI:
                        return BasicRendererDescription.create(1, 2, 0, 1);
                    default:
                        throw new UnsupportedOperationException();
                }
            } else if (version < 101300) {
                switch (sz) {
                    case EXTRA_LARGE:
                    case LARGE:
                    case REGULAR:
                        return BasicRendererDescription.create(1, 3, 0, 1);
                    case SMALL:
                        return BasicRendererDescription.create(1, 3, 0, 3);
                    case MINI:
                        return BasicRendererDescription.create(1, 2, 0, 1);
                    default:
                        throw new UnsupportedOperationException();
                }
            } else if (version < 101400) {
                switch (sz) {
                    case EXTRA_LARGE:
                    case LARGE:
                    case REGULAR:
                        return BasicRendererDescription.create(1, 3, 0, 1);
                    case SMALL:
                        return BasicRendererDescription.create(1, 3, 0, 3);
                    case MINI:
                        return BasicRendererDescription.create(1, 2, 1, 3);
                    default:
                        throw new UnsupportedOperationException();
                }
            } else if (version < 101500) {
                if (bw.isToolbar()) {
                    switch (sz) {
                        case EXTRA_LARGE:
                        case LARGE:
                        case REGULAR:
                            return BasicRendererDescription.create(0.49f, 2, 0, 0);
                        case SMALL:
                            return BasicRendererDescription.create(0.49f, 2, 0, 2);
                        case MINI:
                            return BasicRendererDescription.create(0.49f, 1, 0, 1);
                        default:
                            throw new UnsupportedOperationException();
                    }
                } else {
                    switch (sz) {
                        case EXTRA_LARGE:
                        case LARGE:
                        case REGULAR:
                            return BasicRendererDescription.create(1, 3, 0, 1);
                        case SMALL:
                            return BasicRendererDescription.create(1, 3, 0, 3);
                        case MINI:
                            return BasicRendererDescription.create(1, 2, 0, 1);
                        default:
                            throw new UnsupportedOperationException();
                    }
                }
            } else if (version < macOS11) {
                switch (sz) {
                    case EXTRA_LARGE:
                    case LARGE:
                    case REGULAR:
                        return BasicRendererDescription.create(1, 3, 0, 1);
                    case SMALL:
                        return BasicRendererDescription.create(1, 3, 0, 3);
                    case MINI:
                        return BasicRendererDescription.create(1, 2, 0, 1);
                    default:
                        throw new UnsupportedOperationException();
                }
            }

            // macOS 11+
            if (bw.isToolbar()) {
                if (version < 130000) {
                    // macOSO 11 and 12
                    switch (sz) {
                        case EXTRA_LARGE:
                        case LARGE:
                            return BasicRendererDescription.create(1, 3, 0, 1);
                    }
                }
                switch (sz) {
                    case EXTRA_LARGE:
                    case LARGE:
                        return BasicRendererDescription.create(1, 1, 0, 1);
                    case REGULAR:
                    case SMALL:
                    case MINI:
                        return BasicRendererDescription.create(1, 2, 0, 1);
                    default:
                        throw new UnsupportedOperationException();
                }
            } else {
                if (version < 130000) {
                    // macOSO 11 and 12
                    switch (sz) {
                        case EXTRA_LARGE:
                        case LARGE:
                        case REGULAR:
                            return BasicRendererDescription.create(1, 3, 0, 1);
                        case SMALL:
                            return BasicRendererDescription.create(1, 3, 0, 3);
                        case MINI:
                            return BasicRendererDescription.create(1, 2, 0, 1);
                        default:
                            throw new UnsupportedOperationException();
                    }
                } else {
                    switch (sz) {
                        case EXTRA_LARGE:
                        case LARGE:
                        case REGULAR:
                        case SMALL:
                            return BasicRendererDescription.create(1, 2, 0, 1);
                        case MINI:
                            return new BasicRendererDescription(0, 0, 0, 1);
                        default:
                            throw new UnsupportedOperationException();
                    }
                }
            }
        } else {
            if (version >= 130000) {
                switch (sz) {
                    case EXTRA_LARGE:
                    case LARGE:
                        return BasicRendererDescription.create(0, 2, 1, 3);
                    case REGULAR:
                        return BasicRendererDescription.create(0, 2, 0, 1);
                    case SMALL:
                        return BasicRendererDescription.create(0, 1, 0, 1);
                    case MINI:
                        return BasicRendererDescription.create(0, 2, 1, 2);
                    default:
                        throw new UnsupportedOperationException();
                }
            } else if (version >= macOS11) {
                switch (sz) {
                    case EXTRA_LARGE:
                    case LARGE:
                    case REGULAR:
                        return BasicRendererDescription.create(0, 3, 0, 1);
                    case SMALL:
                        return BasicRendererDescription.create(0, 2, .49f, 3);
                    case MINI:
                        return BasicRendererDescription.create(0, 2, 0, 2);
                    default:
                        throw new UnsupportedOperationException();
                }
            } else if (version >= 101400) {
                switch (sz) {
                    case EXTRA_LARGE:
                    case LARGE:
                    case REGULAR:
                        return BasicRendererDescription.create(0, 3, 0, 2);
                    case SMALL:
                    case MINI:
                        return BasicRendererDescription.create(0, 2, 0, 2);
                    default:
                        throw new UnsupportedOperationException();
                }
            } else if (version >= 101300) {
                switch (sz) {
                    case EXTRA_LARGE:
                    case LARGE:
                    case REGULAR:
                        return BasicRendererDescription.create(0, 3, 0, 1);
                    case SMALL:
                    case MINI:
                        return BasicRendererDescription.create(0, 2, 0, 2);
                    default:
                        throw new UnsupportedOperationException();
                }
            } else if (version >= 101100) {
                switch (sz) {
                    case EXTRA_LARGE:
                    case LARGE:
                    case REGULAR:
                        return BasicRendererDescription.create(0, 3, 0, 1);
                    case SMALL:
                        return BasicRendererDescription.create(0, 3, 0, 3);
                    case MINI:
                        return BasicRendererDescription.create(0, 2, 0, 2);
                    default:
                        throw new UnsupportedOperationException();
                }
            } else {
                switch (sz) {
                    case EXTRA_LARGE:
                    case LARGE:
                    case REGULAR:
                        return BasicRendererDescription.create(0, 3, 0, 2);
                    case SMALL:
                        return BasicRendererDescription.create(0, 3, 1, 4);
                    case MINI:
                        return BasicRendererDescription.create(0, 2, 0, 2);
                    default:
                        throw new UnsupportedOperationException();
                }
            }
        }
    }

    @Override
    public @NotNull RendererDescription getRendererDescription(@NotNull SplitPaneDividerConfiguration g)
    {
        AquaUIPainter.Orientation o = g.getOrientation();

        switch (g.getWidget())
        {
            case THIN_DIVIDER:
                // At 2x, the native view painter requires a "divider width" of at least 2 points.
                // At 1x, a larger size works better for both horizontal and vertical dividers.
                // We should only be given a "divider width" of one point, as that is the fixed logical divider width.
                return o == HORIZONTAL ? new BasicRendererDescription(0, 0, 0, 9) : new BasicRendererDescription(-1, 0, 2, 0);

            case THICK_DIVIDER:
                // At 2x, the native view painter requires a "divider width" of at least 10 points.
                // At 1x, a larger width works better for vertical dividers.
                // We should only be given a "divider width" of 9 points, as that is the fixed logical divider width.
                return o == HORIZONTAL ? new BasicRendererDescription(0, 0, 0, 1) :
                  new MultiResolutionRendererDescription(new BasicRendererDescription(-4, 0, 6, 0), new BasicRendererDescription(-3, 0, 6, 0));

            case PANE_SPLITTER:
                // At 2x, the native view painter requires a "divider width" of at least 11 points.
                // At 1x, a larger width works better for vertical dividers.
                // We should only be given a "divider width" of 10 points, as that is the fixed logical divider width.
                return o == HORIZONTAL ? new BasicRendererDescription(0, 0, 0, 1) : new BasicRendererDescription(-5, 0, 10, 0);

            default:
                return BasicRendererDescription.create(0, 0, 0, 0);
        }
    }

    public @NotNull RendererDescription getTextFieldRendererDescription(@NotNull TextFieldConfiguration g)
    {
        int version = AquaNativeRendering.getSystemRenderingVersion();
        if (version >= macOS26) {
            return new BasicRendererDescription(0, 0, 0, 0);
        }

//        if (version >= 150000) {
//            if (g.isSearchField()) {
//                AquaUIPainter.Size sz = g.getSize();
//                if (sz == AquaUIPainter.Size.MINI) {
//                    return BasicRendererDescription.create(0, 0, 2, 2);
//                }
//                if (sz == AquaUIPainter.Size.SMALL) {
//                    return BasicRendererDescription.create(0, 0, 0.5f, 0.5f);
//                }
//            }
//            return new BasicRendererDescription(0, 0, 0, 0);
//        }

        AquaUIPainter.TextFieldWidget w = g.getWidget();
        AquaUIPainter.Size sz = g.getSize();

        if (w == AquaUIPainter.TextFieldWidget.TEXT_FIELD && version >= macOS11) {
            switch (sz) {
                case MINI:
                    return BasicRendererDescription.create(1, 2, 3, 4);
                case SMALL:
                    return BasicRendererDescription.create(1, 2, 2, 3);
                case REGULAR:
                case LARGE:
                case EXTRA_LARGE:
                    return BasicRendererDescription.create(1, 2, 1, 2);
            }
        }

        if (version >= macOS11) {
            if (w == AquaUIPainter.TextFieldWidget.TEXT_FIELD_ROUND_TOOLBAR) {
                switch (sz) {
                    case MINI:
                        return new MultiResolutionRendererDescription(
                          BasicRendererDescription.create(1, 2, 2, 2),
                          BasicRendererDescription.create(1, 2, 2, 2.5f)
                        );
                    default:
                        return BasicRendererDescription.create(1, 2, 0, 0);
                }
            } else if (g.isSearchField()) {
                switch (sz) {
                    case EXTRA_LARGE:
                    case LARGE:
                        return BasicRendererDescription.create(1, 2, 1, 2);
                    case REGULAR:
                        return new MultiResolutionRendererDescription(
                          BasicRendererDescription.create(1, 2, 0, 1),
                          BasicRendererDescription.create(1, 2, 0, 0.5f));
                    case SMALL:
                        return new MultiResolutionRendererDescription(
                          BasicRendererDescription.create(1, 2, 1, 1),
                          BasicRendererDescription.create(1, 2, 1, 1.5f));
                    case MINI:
                        if (version >= 150000) {
                            return new MultiResolutionRendererDescription(
                              BasicRendererDescription.create(1, 2, 2, 2),
                              BasicRendererDescription.create(1, 2, 2, 2.5f)
                            );
                        } else {
                            return new MultiResolutionRendererDescription(
                              BasicRendererDescription.create(1, 2, 3, 4),
                              BasicRendererDescription.create(1, 2, 2.5f, 3.5f)
                            );
                        }
                    default:
                        throw new UnsupportedOperationException();
                }
            }
        }

        return super.getTextFieldRendererDescription(g);
    }
}
