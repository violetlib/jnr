/*
 * Copyright (c) 2015-2020 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.aqua.impl;

import org.violetlib.jnr.Insetter;
import org.violetlib.jnr.aqua.IndeterminateProgressIndicatorConfiguration;
import org.violetlib.jnr.aqua.ScrollBarConfiguration;
import org.violetlib.jnr.aqua.TableColumnHeaderConfiguration;
import org.violetlib.jnr.aqua.TextFieldConfiguration;
import org.violetlib.jnr.aqua.TitleBarConfiguration;
import org.violetlib.jnr.impl.JNRPlatformUtils;
import org.violetlib.jnr.impl.PainterExtension;
import org.violetlib.jnr.impl.Renderer;

import org.jetbrains.annotations.*;

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

        if (sw == ScrollBarWidget.LEGACY) {
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
        TextFieldWidget w = g.getWidget();
        if (w.hasMenu()) {
            Insetter insets = uiLayout.getSearchButtonPaintingInsets(g);
            if (insets != null) {
                PainterExtension px = new SearchFieldMenuIconPainter(g, insets, appearance);
                Renderer pr = Renderer.create(px);
                return Renderer.createCompositeRenderer(r, pr);
            }
        }
        return r;
    }

    @Override
    protected @NotNull Renderer getIndeterminateProgressIndicatorRenderer(@NotNull IndeterminateProgressIndicatorConfiguration g)
    {
        int platformVersion = JNRPlatformUtils.getPlatformVersion();
        if (platformVersion >= 101600 && g.getWidget() == ProgressWidget.INDETERMINATE_BAR) {
            PainterExtension px = new IndeterminateProgressBarPainterExtension(uiLayout, g, appearance);
            return Renderer.create(px);
        }
        return super.getIndeterminateProgressIndicatorRenderer(g);
    }

    @Override
    public @NotNull String toString()
    {
        return "Augmented " + super.toString();
    }
}
