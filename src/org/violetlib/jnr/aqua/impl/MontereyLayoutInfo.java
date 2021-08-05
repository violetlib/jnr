/*
 * Copyright (c) 2021 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.aqua.impl;

import org.violetlib.jnr.LayoutInfo;
import org.violetlib.jnr.aqua.ButtonLayoutConfiguration;
import org.violetlib.jnr.impl.BasicLayoutInfo;

import org.jetbrains.annotations.*;

import static org.violetlib.jnr.aqua.AquaUIPainter.*;
import static org.violetlib.jnr.aqua.AquaUIPainter.ButtonWidget.*;

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
}
