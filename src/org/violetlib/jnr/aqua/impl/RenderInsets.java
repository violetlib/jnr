/*
 * Copyright (c) 2020 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.aqua.impl;

import java.text.DecimalFormat;
import java.util.Objects;

import org.jetbrains.annotations.*;

/**
  This immutable object describes the relationship between the layout size of a widget (which generally matches the
  bounds of the visual image) and the raster that is needed to render it. The width and height adjustments (presumably
  zero or positive) indicate the need for the raster to be larger than the intended image size. The offset parameters
  (left and top) indicate the location of the visual representation in the raster. All parameters are in points
  (independent of scale).

  <p>
  This design was chosen over insets because the width and height adjustments are generally scale independent, but the
  left and top offsets may be scale dependent.
*/

public class RenderInsets
{
    protected final static @NotNull DecimalFormat df;
    static {
        df = new DecimalFormat("0.#");
        df.setDecimalSeparatorAlwaysShown(false);
    }

    public final float left;
    public final float top;
    public final float widthAdjust;
    public final float heightAdjust;

    public RenderInsets(float left, float top, float widthAdjust, float heightAdjust)
    {
        if (left < 0) {
            throw new IllegalArgumentException("Invalid left inset");
        }

        if (top < 0) {
            throw new IllegalArgumentException("Invalid top inset");
        }

        this.left = left;
        this.top = top;
        this.widthAdjust = widthAdjust;
        this.heightAdjust = heightAdjust;
    }

    public RenderInsets(double left, double top, double widthAdjust, double heightAdjust)
    {
        this((float) left, (float) top, (float) widthAdjust, (float) heightAdjust);
    }

    public @NotNull RenderInsets scale(float scale)
    {
        if (scale == 1) {
            return this;
        }

        float left = Math.round(this.left * scale) / scale;
        float top = Math.round(this.top * scale) / scale;
        return new RenderInsets(left, top, widthAdjust, heightAdjust);
    }

    @Override
    public boolean equals(@Nullable Object o)
    {
        if (this == o) return true;
        if (!(o instanceof RenderInsets)) return false;
        RenderInsets that = (RenderInsets) o;
        return Float.compare(that.left, left) == 0 &&
                 Float.compare(that.top, top) == 0 &&
                 Float.compare(that.widthAdjust, widthAdjust) == 0 &&
                 Float.compare(that.heightAdjust, heightAdjust) == 0;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(left, top, widthAdjust, heightAdjust);
    }

    @Override
    public @NotNull String toString()
    {
        StringBuilder sb = new StringBuilder();
        if (left != 0) {
            sb.append(" Left=");
            sb.append(df.format(left));
        }
        if (top != 0) {
            sb.append(" Top=");
            sb.append(df.format(top));
        }
        if (widthAdjust != 0) {
            sb.append(" W=");
            if (widthAdjust > 0) {
                sb.append("+");
            }
            sb.append(df.format(widthAdjust));
        }
        if (heightAdjust != 0) {
            sb.append(" H=");
            if (heightAdjust > 0) {
                sb.append("+");
            }
            sb.append(df.format(heightAdjust));
        }
        String s = sb.toString().trim();
        return s.isEmpty() ? "NULL" : s;
    }
}
