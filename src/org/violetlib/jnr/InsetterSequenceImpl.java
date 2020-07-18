/*
 * Copyright (c) 2020 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr;

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.geom.Dimension2D;
import java.awt.geom.Rectangle2D;

import org.jetbrains.annotations.*;

/**

*/

/* package private */ class InsetterSequenceImpl
  implements Insetter
{
    private final @NotNull Insetter i1;
    private final @NotNull Insetter i2;

    public InsetterSequenceImpl(@NotNull Insetter i1, @NotNull Insetter i2)
    {
        this.i1 = i1;
        this.i2 = i2;
    }

    @Override
    public @NotNull Rectangle2D applyToBounds2D(@NotNull Rectangle2D bounds)
    {
        return i2.applyToBounds2D(i1.applyToBounds2D(bounds));
    }

    @Override
    public @NotNull Rectangle applyToBounds(@NotNull Rectangle bounds)
    {
        return i2.applyToBounds(i1.applyToBounds(bounds));
    }

    @Override
    public @NotNull Rectangle2D apply2D(double width, double height)
    {
        return i2.applyToBounds2D(i1.apply2D(width, height));
    }

    @Override
    public @NotNull Rectangle apply(int width, int height)
    {
        return i2.applyToBounds(i1.apply(width, height));
    }

    @Override
    public boolean isInvertible()
    {
        return i1.isInvertible() && i2.isInvertible();
    }

    @Override
    public @NotNull Dimension2D expand2D(@NotNull Dimension2D regionSize)
      throws InsetterNotInvertibleException
    {
        return i2.expand2D(i1.expand2D(regionSize));
    }

    @Override
    public @NotNull Dimension expand(@NotNull Dimension regionSize)
      throws InsetterNotInvertibleException
    {
        return i2.expand(i1.expand(regionSize));
    }

    @Override
    public @Nullable Insets2D asInsets2D()
    {
        return null;
    }

    @Override
    public @Nullable Insets asInsets()
    {
        return null;
    }
}
