/*
 * Copyright (c) 2015-2020 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.aqua.jrs;

import org.violetlib.jnr.aqua.ComboBoxConfiguration;
import org.violetlib.jnr.aqua.PopupButtonConfiguration;
import org.violetlib.jnr.aqua.ScrollBarConfiguration;
import org.violetlib.jnr.aqua.SliderConfiguration;
import org.violetlib.jnr.aqua.SplitPaneDividerConfiguration;
import org.violetlib.jnr.aqua.TableColumnHeaderConfiguration;
import org.violetlib.jnr.aqua.impl.*;
import org.violetlib.jnr.impl.JNRPlatformUtils;
import org.violetlib.jnr.impl.PainterExtension;
import org.violetlib.jnr.impl.Renderer;

import org.jetbrains.annotations.*;

/**
  This class augments the JRS native painting code to work around its deficiencies.
*/

public class AugmentedJRSPainter
  extends JRSPainter
{
    public AugmentedJRSPainter()
    {
    }

    @Override
    public @NotNull AugmentedJRSPainter copy()
    {
        return new AugmentedJRSPainter();
    }

    @Override
    public @NotNull Renderer getTableColumnHeaderRenderer(@NotNull TableColumnHeaderConfiguration g)
    {
        // Do not use the native renderer. Use our simulation instead.

        PainterExtension px = new TableColumnHeaderCellPainterExtension(g, appearance);
        return Renderer.create(px);
    }

    @Override
    protected @NotNull Renderer getSplitPaneDividerRenderer(@NotNull SplitPaneDividerConfiguration g)
    {
        if (g.getWidget() == DividerWidget.THICK_DIVIDER) {
            PainterExtension px = new ThickSplitPaneDividerPainterExtension(g, appearance);
            return Renderer.create(px);
        } else if (g.getWidget() == DividerWidget.THIN_DIVIDER) {
            PainterExtension px = new ThinSplitPaneDividerPainterExtension(g, appearance);
            return Renderer.create(px);
        } else {
            return super.getSplitPaneDividerRenderer(g);
        }
    }

    @Override
    protected @NotNull Renderer getComboBoxButtonRenderer(@NotNull ComboBoxConfiguration g)
    {
        ComboBoxWidget bw = g.getWidget();
        if (bw == ComboBoxWidget.BUTTON_COMBO_BOX_CELL) {
            PainterExtension px = new ComboBoxButtonCellPainterExtension(g, appearance);
            return Renderer.create(px);
        } else {
            return super.getComboBoxButtonRenderer(g);
        }
    }

    @Override
    public @Nullable Renderer getPopupArrowRenderer(@NotNull PopupButtonConfiguration g)
    {
        Renderer r = super.getPopupArrowRenderer(g);
        if (isArrowNeeded(g)) {
            if (g.isPopUp()) {
                return Renderer.create(new PopUpArrowPainter(g, appearance));
            } else {
                return Renderer.create(new PullDownArrowPainter(g, appearance));
            }
        }
        return r;
    }

    private boolean isArrowNeeded(@NotNull PopupButtonConfiguration g)
    {
        PopupButtonWidget w = g.getPopupButtonWidget();

        if (g.isPopUp()) {
            return w == PopupButtonWidget.BUTTON_POP_UP_RECESSED;  // correct the color
        }

        switch (w) {
            // These button widgets paint their own arrows
            case BUTTON_POP_DOWN:
            case BUTTON_POP_UP:
            case BUTTON_POP_DOWN_ROUND_RECT:
            case BUTTON_POP_UP_ROUND_RECT:
                return false;
        }

        return true;
    }

    @Override
    protected @NotNull Renderer getScrollBarRenderer(@NotNull ScrollBarConfiguration g)
    {
        int platformVersion = JNRPlatformUtils.getPlatformVersion();
        ScrollBarWidget sw = g.getWidget();

        if (platformVersion < 101400) {
            return super.getScrollBarRenderer(g);
        }

        if (sw == ScrollBarWidget.LEGACY) {
            return Renderer.create(new LegacyScrollBarPainterExtension(uiLayout, g, appearance));
        } else {
            return Renderer.create(new OverlayScrollBarPainterExtension(uiLayout, g, appearance));
        }
    }

    @Override
    protected @NotNull Renderer getSliderRenderer(@NotNull SliderConfiguration g)
    {
        Renderer r = super.getSliderRenderer(g);
        if (g.getWidget() == SliderWidget.SLIDER_CIRCULAR) {
            Renderer pr = Renderer.create(new CircularSliderPainterExtension(g, appearance));
            return Renderer.createCompositeRenderer(r, pr);
        }
        return r;
    }

    @Override
    protected @Nullable Renderer getSliderTickMarkRenderer(@NotNull SliderConfiguration g)
    {
        int style = getSliderRenderingVersion();
        if (style == SLIDER_10_10 && g.isLinear() && g.hasTickMarks()) {
            return Renderer.create(new LinearSliderPainterExtension(uiLayout, g, appearance));
        } else {
            return null;
        }
    }

    @Override
    public @NotNull String toString()
    {
        return "Augmented " + super.toString();
    }
}
