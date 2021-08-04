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

        if (bw == BUTTON_BEVEL) {
            return BasicLayoutInfo.createMinimumHeight(size(sz, 0, 0, 0, 0));
        }

        return super.getButtonLayoutInfo(g);
    }
}
