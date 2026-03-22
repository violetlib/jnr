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
import org.violetlib.jnr.Insetter;
import org.violetlib.jnr.aqua.*;
import org.violetlib.jnr.impl.PainterExtension;
import org.violetlib.jnr.impl.Renderer;

import static org.violetlib.jnr.aqua.AquaUIPainter.ButtonWidget.*;

/**
  This class augments the native painting code with painting to work around deficiencies in the native painting code.
*/

public class AugmentedAquaNativePainter
  extends AquaNativePainter
{
    public AugmentedAquaNativePainter()
    {
    }

    @Override
    public @NotNull AugmentedAquaNativePainter copy()
    {
        return new AugmentedAquaNativePainter();
    }

    @Override
    protected @NotNull Renderer getButtonRenderer(@NotNull ButtonConfiguration g)
    {
        ButtonWidget w = g.getButtonWidget();

        if (w == BUTTON_GLASS) {
            GlassButtonPainterExtension x = new GlassButtonPainterExtension(g, appearance, this::getOutline);
            return Renderer.create(x);
        }

        if (w == BUTTON_TOOLBAR || w == BUTTON_TEXTURED_TOOLBAR || w == ButtonWidget.BUTTON_ROUND_TEXTURED_TOOLBAR) {
            int version = AquaNativeRendering.getSystemRenderingVersion();
            if (version >= macOS11) {
                GlassButtonPainterExtension x = new GlassButtonPainterExtension(g, appearance, this::getOutline);
                return Renderer.create(x);
            }
        }

        if (w == ButtonWidget.BUTTON_ROUND_TEXTURED) {
            int version = AquaNativeRendering.getSystemRenderingVersion();
            if (version >= macOS11) {
                // The NSView renderer makes textured buttons look like ordinary buttons, with an accent color
                // background when selected.
                PainterExtension px = new TexturedButtonPainterExtension(g.getState(), g.getButtonState(), appearance,
                  (bounds) -> getOutline(bounds, g.getLayoutConfiguration()));
                return Renderer.create(px);
            }
        }

        return super.getButtonRenderer(g);
    }

    @Override
    protected @NotNull Renderer getPopupButtonRenderer(@NotNull PopupButtonConfiguration g)
    {
        int version = AquaNativeRendering.getSystemRenderingVersion();

        State state = g.getState();
        if (state == State.PRESSED || state == State.ROLLOVER) {
            // The NSView renderer does not support ROLLOVER or PRESSED states, but their effect (to lighten or
            // darken more) can be simulated.
            if (version >= macOS26) {
                if (appearance == null) {
                    throw new IllegalStateException("Appearance must be configured");
                }
                RolloverPainterExtension px = new RolloverPainterExtension(state, appearance,
                  (bounds) -> getOutline(bounds, g.getLayoutConfiguration()));
                Renderer r = super.getPopupButtonRenderer(g);
                Renderer pr = Renderer.create(px);
                return Renderer.createCompositeRenderer(pr, r);
            }
        }

        if (version >= macOS11) {
            if (g.isTextured()) {
                // The NSView renderer does not paint the outline
                TexturedButtonPainterExtension px = new TexturedButtonPainterExtension(state, null, appearance,
                  (bounds) -> getOutline(bounds, g.getLayoutConfiguration()));
                Renderer r = super.getPopupButtonRenderer(g);
                Renderer pr = Renderer.create(px);
                return Renderer.createCompositeRenderer(pr, r);
            }
        }

        return super.getPopupButtonRenderer(g);
    }

    @Override
    protected @NotNull Renderer getComboBoxButtonRenderer(@NotNull ComboBoxConfiguration g)
    {
        int version = AquaNativeRendering.getSystemRenderingVersion();
        if (version >= macOS26 && g.getWidget() == ComboBoxWidget.BUTTON_COMBO_BOX_CELL) {
            ComboBoxButtonCell26PainterExtension px = new ComboBoxButtonCell26PainterExtension(g, appearance);
            return Renderer.create(px);
        } else {
            return super.getComboBoxButtonRenderer(g);
        }
    }

    @Override
    public @NotNull Renderer getTableColumnHeaderRenderer(@NotNull TableColumnHeaderConfiguration g)
    {
        // Do not use the native renderer. Use our simulation instead.

        PainterExtension px = new TableColumnHeaderCellPainterExtension(g, appearance);
        return Renderer.create(px);
    }

    @Override
    protected @NotNull Renderer getTitleBarRenderer(@NotNull TitleBarConfiguration g)
    {
        Renderer r = super.getTitleBarRenderer(g);
        PainterExtension px = getTitleBarButtonPainter(g);
        if (px == null) {
            return r;
        }
        Renderer pr = Renderer.create(px);
        return Renderer.createCompositeRenderer(r, pr);
    }

    protected @Nullable PainterExtension getTitleBarButtonPainter(@NotNull TitleBarConfiguration g)
    {
        return new TitleBarPainterExtension(getTitleBarLayoutInfo(), g, appearance);
    }

    @Override
    protected @NotNull Renderer getScrollBarRenderer(@NotNull ScrollBarConfiguration g)
    {
        ScrollBarWidget sw = g.getWidget();

        if (sw == ScrollBarWidget.LEGACY_SIDEBAR) {
            return Renderer.create(new RoundedScrollBarPainterExtension(uiLayout, g, appearance));
        } else if (sw == ScrollBarWidget.LEGACY) {
            return super.getScrollBarRenderer(g);
        } else {
            PainterExtension px = new OverlayScrollBarPainterExtension(uiLayout, g, appearance);
            return Renderer.create(px);
        }
    }

    @Override
    protected @NotNull Renderer getTextFieldRenderer(@NotNull TextFieldConfiguration g)
    {
        Renderer r = super.getTextFieldRenderer(g);

        int version = AquaNativeRendering.getSystemRenderingVersion();
        if (version < macOS11) {  // TBD: not sure which release fixed the menu icon issue
            TextFieldWidget w = g.getWidget();
            if (w.hasMenu()) {
                Insetter insets = uiLayout.getSearchButtonPaintingInsets(g);
                if (insets != null) {
                    PainterExtension px = new SearchFieldMenuIconPainter(g, insets, appearance);
                    Renderer pr = Renderer.create(px);
                    return Renderer.createCompositeRenderer(r, pr);
                }
            }
        }

        return r;
    }

    @Override
    protected @NotNull Renderer getIndeterminateProgressIndicatorRenderer(@NotNull IndeterminateProgressIndicatorConfiguration g)
    {
        int version = AquaNativeRendering.getSystemRenderingVersion();
        if (version >= macOS11 && g.getWidget() == ProgressWidget.INDETERMINATE_BAR) {
            PainterExtension px = new IndeterminateProgressBarPainterExtension(uiLayout, g, appearance);
            return Renderer.create(px);
        }
        return super.getIndeterminateProgressIndicatorRenderer(g);
    }

    protected @NotNull Renderer getSliderRenderer(@NotNull SliderConfiguration g)
    {
        return super.getSliderRenderer(g);
    }

    @Override
    public @NotNull String toString()
    {
        return "Augmented " + super.toString();
    }
}
