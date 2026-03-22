/*
 * Copyright (c) 2015-2026 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.aqua.impl;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.violetlib.jnr.Painter;
import org.violetlib.jnr.aqua.*;
import org.violetlib.jnr.impl.Colors;
import org.violetlib.jnr.impl.RendererDescription;
import org.violetlib.vappearances.VAppearance;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.Map;

import static org.violetlib.jnr.aqua.AquaUIPainter.SegmentedButtonWidget.*;

/**
  A hybrid painter that uses the best available implementation for each given configuration.
*/

public class HybridAquaUIPainter
  implements AquaUIPainter
{
    protected final @NotNull AquaUIPainter viewPainter;
    protected final @NotNull AquaUIPainter coreUIPainter;
    protected final @Nullable AquaUIPainter jrsPainter;

    private final @NotNull AquaUILayoutInfo layout;
    private int w;
    private int h;
    private @Nullable VAppearance appearance;

    public HybridAquaUIPainter(@NotNull AquaUIPainter viewPainter,
                               @NotNull AquaUIPainter coreUIPainter,
                               @Nullable AquaUIPainter jrsPainter)
    {
        this.viewPainter = viewPainter;
        this.coreUIPainter = coreUIPainter;
        this.jrsPainter = jrsPainter;

        layout = viewPainter.getLayoutInfo();  // all implementations share the same layout
    }

    @Override
    public @NotNull HybridAquaUIPainter copy()
    {
        return new HybridAquaUIPainter(viewPainter, coreUIPainter, jrsPainter);
    }

    @Override
    public @NotNull Map<String,Color> getColors(@NotNull VAppearance appearance)
    {
        Colors colors = Colors.getColors(appearance);
        return colors.getColors();
    }

    @Override
    public void configureAppearance(@NotNull VAppearance appearance)
    {
        this.appearance = appearance;
    }

    @Override
    public void configure(int w, int h)
    {
        this.w = w;
        this.h = h;
    }

    @Override
    public @NotNull Painter getPainter(@NotNull Configuration g)
      throws UnsupportedOperationException
    {
        return getPainter(g, null);
    }

    @Override
    public @NotNull Painter getPainter(@NotNull Configuration g, @Nullable RendererDescription rd)
      throws UnsupportedOperationException
    {
        AquaUIPainter p = select(g);
        if (appearance != null) {
            p.configureAppearance(appearance);
        }
        p.configure(w, h);
        return p.getPainter(g, rd);
    }

    protected @NotNull AquaUIPainter select(@NotNull Configuration g)
    {
        // Prefer the JSR painter if defined because it is faster, except where it is not accurate.
        // Otherwise, use the core UI painter except where it falls down and the view painter is better.

        int version = AquaNativeRendering.getSystemRenderingVersion();
        if (g instanceof ButtonConfiguration) {
            ButtonConfiguration bg = (ButtonConfiguration) g;
            ButtonWidget bw = bg.getButtonWidget();

            if (version >= macOS11 && (bw == ButtonWidget.BUTTON_ROUND || bw == ButtonWidget.BUTTON_ROUND_INSET)) {
                // the painters are identical
                return viewPainter;
            }

            // These styles were not supported by the NSView renderer in old releases.
            if (bw == ButtonWidget.BUTTON_ROUND_INSET && version >= 101500 && version < macOS26) {
                return coreUIPainter;
            }

            if (version >= macOS11) {
                // These styles use an augmented painter
                switch (bw) {
                    case BUTTON_ROUND_TEXTURED:
                    case BUTTON_ROUND_TEXTURED_TOOLBAR:
                    case BUTTON_TOOLBAR:
                    case BUTTON_TEXTURED_TOOLBAR:
                        return viewPainter;
                }
            }

            if (bw == ButtonWidget.BUTTON_PUSH_INSET2 && version >= macOS11) {
                return coreUIPainter;
            }

            Size sz = bg.getSize();
            if (bw == ButtonWidget.BUTTON_INLINE
              || bw == ButtonWidget.BUTTON_GLASS
              || bw == ButtonWidget.BUTTON_DISCLOSURE
              || bw == ButtonWidget.BUTTON_HELP
              || bw == ButtonWidget.BUTTON_COLOR_WELL
              || sz == Size.EXTRA_LARGE
              || version >= macOS26) {
                return viewPainter;
            } else if (bw == ButtonWidget.BUTTON_TOOLBAR && sz == Size.LARGE) {
                return viewPainter;
            } else {
                return coreUIPainter;
            }
        } else if (g instanceof SegmentedButtonConfiguration) {
            SegmentedButtonConfiguration sg = (SegmentedButtonConfiguration) g;
            SegmentedButtonWidget w = sg.getWidget();
            SwitchTracking tracking = sg.getTracking();
            if (version >= macOS26) {
                // The NSView painter on macOS 26 paints only one style, the modern one.
                // For older, obsolete styles, it is better to use the CoreUI painter.
                if (w.isTextured()
                  || w == BUTTON_SEGMENTED_SEPARATED
                  || w == BUTTON_SEGMENTED_SMALL_SQUARE
                  || w == BUTTON_SEGMENTED_INSET
                  || w == BUTTON_SEGMENTED_TOOLBAR
                  || w == BUTTON_SEGMENTED_SCURVE) {
                    return coreUIPainter;
                }
            }

            if (version >= 150000) {
                // NSView does not support pressed or rollover.
                // CoreUI does not handle slider styles or some separated styles.
                // Toolbar and icon styles should already be converted.
                if (w.isTextured()) {
                    return viewPainter;
                }
                if (w.isSlider() || w == BUTTON_TAB || w == BUTTON_SEGMENTED || w.isSeparated()) {
                    return viewPainter;
                }
                return coreUIPainter;
            }

            if (w == BUTTON_SEGMENTED && tracking == SwitchTracking.SELECT_ONE) {
                // At least on macOS 14, the NSView painter does this right and CoreUI does not.
                if (version >= 140000) {
                    return viewPainter;
                }
            }

            // The NSView painter produces more accurate backgrounds for gradient buttons
            if (w == BUTTON_SEGMENTED_SMALL_SQUARE && version < macOS11) {
                return viewPainter;
            }

            if (version < 101300) {
                if (w == SegmentedButtonWidget.BUTTON_SEGMENTED_SEPARATED
                  || w == SegmentedButtonWidget.BUTTON_SEGMENTED_TEXTURED
                  || w == SegmentedButtonWidget.BUTTON_SEGMENTED_SCURVE) {
                    return viewPainter;
                }

            } else if (version < 101400) {
                if (w == SegmentedButtonWidget.BUTTON_SEGMENTED_SEPARATED) {
                    return viewPainter;
                }

            } else if (version >= macOS11) {
                // On macOS 13, both NSView and CoreUI can do large.
                // NSView does something odd for segmented select one.
                // CoreUI does something odd for slider on toolbar.
                // TBD: assuming the same on 11 and 12
                // On 11+ (tested on 15), NSView renders textured separated incorrectly

                // The direct CoreUI painter creates blurry images.
                // The JRS CoreUI painter is good except for the slider style, but it does not support
                // components using an appearance other than the application effective appearance.
                // Therefore, use the NSView painter except where it does not work.

                if (w == BUTTON_SEGMENTED_TEXTURED_TOOLBAR) {
                    if (version >= 130000) {
                        return viewPainter;
                    }
                }

                if (w == BUTTON_SEGMENTED
                  || w == BUTTON_SEGMENTED_TEXTURED_SEPARATED
                  || w == BUTTON_SEGMENTED_TEXTURED_TOOLBAR
                  || sg.getState() == State.PRESSED
                  || sg.getState() == State.PRESSED_DEFAULT) {
                    return coreUIPainter;
                }

//                if (w == SegmentedButtonWidget.BUTTON_SEGMENTED_INSET && sg.getSize() == Size.LARGE) {
//                    return viewPainter;
//                }
//                if (w == SegmentedButtonWidget.BUTTON_SEGMENTED_SLIDER) {
//                    return viewPainter;
//                }
            }
            return viewPainter;
        } else if (g instanceof GradientConfiguration) {
            return coreUIPainter;
        } else if (g instanceof ComboBoxConfiguration) {
            ComboBoxConfiguration bg = (ComboBoxConfiguration) g;
            ComboBoxWidget w = bg.getWidget();

            if (version >= 150000 && !w.isTextured()) {
                return viewPainter;
            }
            if (version >= macOS11 && w == ComboBoxWidget.BUTTON_COMBO_BOX) {
                return viewPainter;
            }

            State st = bg.getState();
            Size sz = bg.getSize();

            // On 10.11 and earlier, all renderers paint proper cell style arrows, except JDK is unable to paint the
            // mini size. On 10.12 and later, the cell style arrows have changed to a "V" shape, but JDK still uses the
            // triangle version.

            if (w == ComboBoxWidget.BUTTON_COMBO_BOX_CELL) {
                if (version >= 101200 || sz == Size.MINI) {
                    return coreUIPainter;
                }
            } else if (st == State.DISABLED
              || st == State.DISABLED_INACTIVE
              || bg.getLayoutDirection() == UILayoutDirection.RIGHT_TO_LEFT
              || w == ComboBoxWidget.BUTTON_COMBO_BOX_TEXTURED
              || w == ComboBoxWidget.BUTTON_COMBO_BOX_TEXTURED_TOOLBAR
            ) {
                return coreUIPainter;
            }
        } else if (g instanceof PopupButtonConfiguration) {
            if (version >= macOS11) {
                return viewPainter;
            }

            PopupButtonConfiguration bg = (PopupButtonConfiguration) g;
            if (bg.getLayoutDirection() == UILayoutDirection.RIGHT_TO_LEFT) {
                return coreUIPainter;
            }
            PopupButtonWidget widget = bg.getPopupButtonWidget();
            if (widget == PopupButtonWidget.BUTTON_POP_UP_TEXTURED
              || widget == PopupButtonWidget.BUTTON_POP_DOWN_TEXTURED
              || widget == PopupButtonWidget.BUTTON_POP_UP_TEXTURED_TOOLBAR
              || widget == PopupButtonWidget.BUTTON_POP_DOWN_TEXTURED_TOOLBAR) {
                return coreUIPainter;
            }
        } else if (g instanceof ProgressIndicatorConfiguration) {
            ProgressIndicatorConfiguration bg = (ProgressIndicatorConfiguration) g;
            return coreUIPainter;
        } else if (g instanceof IndeterminateProgressIndicatorConfiguration) {
            IndeterminateProgressIndicatorConfiguration bg = (IndeterminateProgressIndicatorConfiguration) g;
            if (version >= macOS11 && bg.getWidget() == ProgressWidget.INDETERMINATE_BAR) {
                return viewPainter;
            }
            return coreUIPainter;
        } else if (g instanceof TextFieldConfiguration) {
            TextFieldConfiguration bg = (TextFieldConfiguration) g;
            TextFieldWidget w = bg.getWidget();
            if (version >= macOS26) {
                return viewPainter;
            }
            if (version >= 140000 && w == TextFieldWidget.TEXT_FIELD) {
                // The NSView renderer fails on macOS 15.
                // Not sure about 14.
                return coreUIPainter;
            }
            if (version >= macOS11 && version < 120000 && w == TextFieldWidget.TEXT_FIELD) {
                if (bg.getSize() == Size.MINI) {
                    // A unique rendering problem?
                    return coreUIPainter;
                }
            }
            if (version >= macOS11 && !w.isToolbar()) {
                return viewPainter;
            }
            if (w != TextFieldWidget.TEXT_FIELD && w != TextFieldWidget.TEXT_FIELD_ROUND) {
                return coreUIPainter;
            }
        } else if (g instanceof SliderConfiguration) {
            SliderConfiguration bg = (SliderConfiguration) g;
            if (version >= macOS26) {
                // The sliders introduced in macOS 26 are not supported by CoreUI.
                return viewPainter;
            }
            if (bg.getSize() == Size.MINI) {
                if (version < 101500) {
                    return viewPainter;
                }
            }
            return coreUIPainter;
        } else if (g instanceof TitleBarConfiguration) {
            return viewPainter;
        } else if (g instanceof ScrollBarConfiguration) {
            ScrollBarConfiguration sg = (ScrollBarConfiguration) g;
            ScrollBarWidget w = sg.getWidget();
            if (w == ScrollBarWidget.OVERLAY || w == ScrollBarWidget.OVERLAY_ROLLOVER) {
                return coreUIPainter;
            }
            ScrollBarKnobWidget kw = sg.getKnobWidget();
            if (kw == ScrollBarKnobWidget.NONE) {
                return coreUIPainter;
            }
            return viewPainter;
        } else if (g instanceof SpinnerArrowsConfiguration) {
            if (version >= macOS26) {
                return viewPainter;
            }
        } else if (g instanceof GroupBoxConfiguration) {
            GroupBoxConfiguration bg = (GroupBoxConfiguration) g;
            if (bg.isFrameOnly()) {
                return viewPainter;
            }
        }

        // The JRS painter does not support the dark appearance.
        // TBD: check to see if the dark appearance is being used, if possible

        if (version >= 101400) {
            return coreUIPainter;
        }

        return jrsPainter != null ? jrsPainter : coreUIPainter;
    }

    @Override
    public @NotNull AquaUILayoutInfo getLayoutInfo()
    {
        return layout;
    }

    @Override
    public @Nullable Shape getOutline(@NotNull LayoutConfiguration g)
    {
        viewPainter.configure(w, h);
        return viewPainter.getOutline(g);
    }

    @Override
    public @NotNull Rectangle2D getComboBoxEditorBounds(@NotNull ComboBoxLayoutConfiguration g)
    {
        viewPainter.configure(w, h);
        return viewPainter.getComboBoxEditorBounds(g);
    }

    @Override
    public @NotNull Rectangle2D getComboBoxIndicatorBounds(@NotNull ComboBoxLayoutConfiguration g)
    {
        viewPainter.configure(w, h);
        return viewPainter.getComboBoxIndicatorBounds(g);
    }

    @Override
    public @NotNull Rectangle2D getPopupButtonContentBounds(@NotNull PopupButtonLayoutConfiguration g)
    {
        viewPainter.configure(w, h);
        return viewPainter.getPopupButtonContentBounds(g);
    }

    @Override
    public @NotNull Rectangle2D getSliderThumbBounds(@NotNull SliderLayoutConfiguration g, double thumbPosition)
    {
        viewPainter.configure(w, h);
        return viewPainter.getSliderThumbBounds(g, thumbPosition);
    }

    @Override
    public double getSliderThumbPosition(@NotNull SliderLayoutConfiguration g, int x, int y)
    {
        viewPainter.configure(w, h);
        return viewPainter.getSliderThumbPosition(g, x, y);
    }

    @Override
    public float getScrollBarThumbPosition(@NotNull ScrollBarThumbLayoutConfiguration g, boolean useExtent)
    {
        viewPainter.configure(w, h);
        return viewPainter.getScrollBarThumbPosition(g, useExtent);
    }

    @Override
    public int getScrollBarThumbHit(@NotNull ScrollBarThumbConfiguration g)
    {
        viewPainter.configure(w, h);
        return viewPainter.getScrollBarThumbHit(g);
    }

    @Override
    public @NotNull Rectangle2D getSliderLabelBounds(@NotNull SliderLayoutConfiguration g,
                                                     double thumbPosition,
                                                     @NotNull Dimension size)
    {
        viewPainter.configure(w, h);
        return viewPainter.getSliderLabelBounds(g, thumbPosition, size);
    }

    @Override
    public @NotNull String toString()
    {
        String s = "Hybrid " + viewPainter + "+" + coreUIPainter;
        if (jrsPainter != null) {
            s = s + "+" + jrsPainter;
        }
        return s;
    }
}
