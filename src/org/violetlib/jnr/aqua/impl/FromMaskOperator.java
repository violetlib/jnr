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

import static org.violetlib.jnr.impl.ImageUtils.*;

/**

*/

public class FromMaskOperator
  implements ReusableCompositor.PixelOperator
{
    @Override
    public int combine(int destinationPixel, int sourcePixel)
    {
        int alpha = alpha(sourcePixel);
        if (alpha == 0) {
            return destinationPixel;
        }
        int newAlpha = Math.min(255, alpha * 5);
        int value = 73 * newAlpha / 255;

        sourcePixel = ImageUtils.createPixel(newAlpha, value, value, value);
        return JNRUtils.combine(destinationPixel, sourcePixel);
    }
}
