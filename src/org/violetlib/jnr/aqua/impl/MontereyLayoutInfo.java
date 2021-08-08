/*
 * Copyright (c) 2021 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.aqua.impl;

import org.violetlib.jnr.Insetter;
import org.violetlib.jnr.LayoutInfo;
import org.violetlib.jnr.aqua.ButtonLayoutConfiguration;
import org.violetlib.jnr.impl.BasicLayoutInfo;
import org.violetlib.jnr.impl.Insetters;

import org.jetbrains.annotations.*;

import static org.violetlib.jnr.aqua.AquaUIPainter.*;
import static org.violetlib.jnr.aqua.AquaUIPainter.ButtonWidget.*;
import static org.violetlib.jnr.impl.JNRUtils.*;

/**

*/

public class MontereyLayoutInfo
  extends BigSurLayoutInfo
{
    @Override
    protected @NotNull LayoutInfo getButtonLayoutInfo(@NotNull ButtonLayoutConfiguration g)
    {
        ButtonWidget bw = g.getButtonWidget();
        Size sz = g.getSize();

        if (bw == BUTTON_BEVEL_ROUND) {
            switch (sz) {
                case LARGE: return BasicLayoutInfo.createMinimum(18, 30);
                case REGULAR: return BasicLayoutInfo.createMinimum(18, 22);
                case SMALL: return BasicLayoutInfo.createMinimum(14, 18);
                case MINI: return BasicLayoutInfo.createMinimum(14, 16);
                default: throw new UnsupportedOperationException("Unsupported size");
            }
        }

        return super.getButtonLayoutInfo(g);
    }

    @Override
    public @Nullable Insetter getButtonLabelInsets(@NotNull ButtonLayoutConfiguration g)
    {
        ButtonWidget bw = g.getButtonWidget();
        if (bw == BUTTON_BEVEL_ROUND) {
            // These insets match push buttons
            Size sz = g.getSize();
            float top = size(sz, 2, 2, 1, 1);
            float bottom = 2;
            float side = size(sz, 4, 4, 4, 3);
            LayoutInfo layoutInfo = getLayoutInfo(g);
            return Insetters.createFixed(top, side, bottom, side, layoutInfo);
        }

        return super.getButtonLabelInsets(g);
    }
}
