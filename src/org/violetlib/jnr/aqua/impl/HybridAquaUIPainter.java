/*
 * Copyright (c) 2015-2020 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.aqua.impl;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.util.Map;

import org.violetlib.jnr.Painter;
import org.violetlib.jnr.aqua.*;
import org.violetlib.jnr.impl.Colors;
import org.violetlib.jnr.impl.JNRPlatformUtils;
import org.violetlib.vappearances.VAppearance;

import org.jetbrains.annotations.*;

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
        AquaUIPainter p = select(g);
        if (appearance != null) {
            p.configureAppearance(appearance);
        }
        p.configure(w, h);
        return p.getPainter(g);
    }

    protected @NotNull AquaUIPainter select(@NotNull Configuration g)
    {
        // Prefer the JSR painter if defined because it is faster, except where it is not accurate.
        // Otherwise the core UI painter except where it falls down and the view painter is better.

        if (g instanceof ButtonConfiguration) {
            ButtonConfiguration bg = (ButtonConfiguration) g;
            ButtonWidget bw = bg.getButtonWidget();
            if (bw == ButtonWidget.BUTTON_INLINE) {
                return viewPainter;
            } else {
                return coreUIPainter;
            }
        } else if (g instanceof SegmentedButtonConfiguration) {
            SegmentedButtonConfiguration sg = (SegmentedButtonConfiguration) g;
            // The NSView painter produces more accurate backgrounds for gradient buttons
            if (sg.getWidget() == SegmentedButtonWidget.BUTTON_SEGMENTED_SMALL_SQUARE) {
                return viewPainter;
            }
            return coreUIPainter;
        } else if (g instanceof GradientConfiguration) {
            return coreUIPainter;
        } else if (g instanceof ComboBoxConfiguration) {
            ComboBoxConfiguration bg = (ComboBoxConfiguration) g;
            ComboBoxWidget w = bg.getWidget();
            State st = bg.getState();
            Size sz = bg.getSize();

            // On 10.11 and earlier, all renderers paint proper cell style arrows, except JDK is unable to paint the
            // mini size. On 10.12 and later, the cell style arrows have changed to a "V" shape, but JDK still uses the
            // triangle version.

            if (w == ComboBoxWidget.BUTTON_COMBO_BOX_CELL) {
                int platformVersion = JNRPlatformUtils.getPlatformVersion();
                if (platformVersion >= 101200 || sz == Size.MINI) {
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
            return coreUIPainter;
        } else if (g instanceof TextFieldConfiguration) {
            TextFieldConfiguration bg = (TextFieldConfiguration) g;
            TextFieldWidget w = bg.getWidget();
            if (w != TextFieldWidget.TEXT_FIELD && w != TextFieldWidget.TEXT_FIELD_ROUND) {
                return coreUIPainter;
            }
        } else if (g instanceof SliderConfiguration) {
            SliderConfiguration bg = (SliderConfiguration) g;
            if (bg.getSize() == Size.MINI) {
                int platformVersion = JNRPlatformUtils.getPlatformVersion();
                if (platformVersion < 101500) {
                    return viewPainter;
                }
            }
            return coreUIPainter;
        } else if (g instanceof TitleBarConfiguration) {
            return coreUIPainter;
        } else if (g instanceof ScrollBarConfiguration) {
            return coreUIPainter;
        }

        // The JRS painter does not support the dark appearance.
        // TBD: check to see if the dark appearance is being used, if possible

        int platformVersion = JNRPlatformUtils.getPlatformVersion();
        if (platformVersion >= 101400) {
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
