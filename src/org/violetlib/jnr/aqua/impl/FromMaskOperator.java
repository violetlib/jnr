/*
 * Copyright (c) 2020 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.aqua.impl;

import org.violetlib.jnr.impl.ImageUtils;
import org.violetlib.jnr.impl.JNRUtils;
import org.violetlib.jnr.impl.ReusableCompositor;
import org.violetlib.vappearances.VAppearance;

import org.jetbrains.annotations.*;

import static org.violetlib.jnr.impl.ImageUtils.*;

/**

*/

public class FromMaskOperator
  implements ReusableCompositor.PixelOperator
{
    private final @Nullable VAppearance appearance;

    public FromMaskOperator(@Nullable VAppearance appearance)
    {
        this.appearance = appearance;
    }

    @Override
    public int combine(int destinationPixel, int sourcePixel)
    {
        boolean isDark = appearance != null && appearance.isDark();

        int alpha = alpha(sourcePixel);
        if (alpha == 0) {
            return destinationPixel;
        }
        int newAlpha = Math.min(255, alpha * 5);
        int target = isDark ? 73 : 186;
        int value = target * newAlpha / 255;

        sourcePixel = ImageUtils.createPixel(newAlpha, value, value, value);
        return JNRUtils.combine(destinationPixel, sourcePixel);
    }
}
