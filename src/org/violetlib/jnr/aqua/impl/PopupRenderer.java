/*
 * Copyright (c) 2015-2021 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.aqua.impl;

import java.awt.geom.Rectangle2D;

import org.violetlib.jnr.Insetter;
import org.violetlib.jnr.aqua.PopupButtonConfiguration;
import org.violetlib.jnr.impl.Renderer;
import org.violetlib.jnr.impl.ReusableCompositor;

import org.jetbrains.annotations.*;

/**
  A popup renderer where the button is rendered separately from the arrows.
*/

public class PopupRenderer
  extends Renderer
{
    protected final @NotNull PopupButtonConfiguration g;
    protected final @Nullable Renderer buttonRenderer;
    protected final @NotNull Renderer arrowsRenderer;
    protected final @NotNull Insetter arrowsInsets;

    public PopupRenderer(@NotNull PopupButtonConfiguration g,
                         @Nullable Renderer buttonRenderer,
                         @NotNull Renderer arrowsRenderer,
                         @NotNull Insetter arrowsInsets)
    {
        this.g = g;
        this.buttonRenderer = buttonRenderer;
        this.arrowsRenderer = arrowsRenderer;
        this.arrowsInsets = arrowsInsets;
    }

    @Override
    public void composeTo(@NotNull ReusableCompositor compositor)
    {
        float w = compositor.getWidth();
        float h = compositor.getHeight();

        if (buttonRenderer != null) {
            buttonRenderer.composeTo(compositor);
        }

        Rectangle2D bounds = arrowsInsets.apply2D(w, h);
        Renderer r = Renderer.createOffsetRenderer(arrowsRenderer, bounds);
        r.composeTo(compositor);
    }
}
