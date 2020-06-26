/*
 * Copyright (c) 2015 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.impl;

import org.violetlib.jnr.InsetterNotInvertibleException;

import org.jetbrains.annotations.*;

/**
  A floating insetter defines a region of fixed size, with either one or both insets being variable.
*/

public class FloatingInsetter1
  implements Insetter1
{
    private final float regionSize;
    private final @NotNull Alignment alignment;
    private final float d1;
    private final float d2;

    private enum Alignment
    {
        LEFT, RIGHT, CENTER
    }

    /**
      Create a single dimension insets for a fixed size region that is positioned at a fixed offset from the left or top
      edge of the component.

      @param regionSize The fixed region size.
      @param d1 The offset of the region from the left or top edge of the component.
      @throws IllegalArgumentException if {@code regionSize} or {@code d1} is negative.
    */

    public static @NotNull FloatingInsetter1 createLeftTopAligned(float regionSize, float d1)
    {
        if (regionSize < 0) {
            throw new IllegalArgumentException("Invalid negative region size");
        }
        if (d1 < 0) {
            throw new IllegalArgumentException("Invalid negative offset");
        }

        return new FloatingInsetter1(regionSize, Alignment.LEFT, d1, 0);
    }

    /**
      Create a single dimension insets for a fixed size region that is positioned at a fixed offset from the right or
      bottom edge of the component.

      @param regionSize The fixed region size.
      @param d2 The offset of the region from the right or bottom edge of the component.
      @throws IllegalArgumentException if {@code regionSize} or {@code d2} is negative.
    */

    public static @NotNull FloatingInsetter1 createRightBottomAligned(float regionSize, float d2)
    {
        if (regionSize < 0) {
            throw new IllegalArgumentException("Invalid negative region size");
        }
        if (d2 < 0) {
            throw new IllegalArgumentException("Invalid negative offset");
        }

        return new FloatingInsetter1(regionSize, Alignment.RIGHT, 0, d2);
    }

    /**
      Create a single dimension insets for a fixed size region that is centered between two minimum insets. Any extra
      space is divided equally and added to the two insets.

      @param regionSize The fixed region size.
      @param d1 The minimum offset of the region from the left or bottom edge of the component.
      @param d2 The minimum offset of the region from the right or bottom edge of the component.
      @throws IllegalArgumentException if {@code regionSize}, {@code d1}, or {@code d2} is negative.
    */

    public static @NotNull FloatingInsetter1 createCentered(float regionSize, float d1, float d2)
    {
        if (regionSize < 0) {
            throw new IllegalArgumentException("Invalid negative region size");
        }
        if (d1 < 0 || d2 < 0) {
            throw new IllegalArgumentException("Invalid negative offset");
        }

        return new FloatingInsetter1(regionSize, Alignment.CENTER, d1, d2);
    }

    private FloatingInsetter1(float regionSize, @NotNull Alignment alignment, float d1, float d2)
    {
        this.regionSize = regionSize;
        this.alignment = alignment;
        this.d1 = d1;
        this.d2 = d2;
    }

    @Override
    public float getRegionOrigin(float componentSize)
    {
        switch (alignment)
        {
            case LEFT:
                return d1;
            case RIGHT:
                return componentSize - (regionSize + d2);
            case CENTER:
                float extra = componentSize - (regionSize + d1 + d2);
                return d1 + extra/2;
            default:
                throw new UnsupportedOperationException();
        }
    }

    @Override
    public float getRegionSize(float componentSize)
    {
        return regionSize;
    }

    @Override
    public boolean isInvertible()
    {
        return true;
    }

    @Override
    public float getComponentSize(float regionSize)
      throws InsetterNotInvertibleException
    {
        return regionSize + d1 + d2;
    }

    @Override
    public float getFixedInset1()
    {
        switch (alignment)
        {
            case LEFT:
            case CENTER:
                return d1;
            default:
                return -1;
        }
    }

    @Override
    public float getFixedInset2()
    {
        switch (alignment)
        {
            case RIGHT:
            case CENTER:
                return d2;
            default:
                return -1;
        }
    }

    @Override
    public float getFixedRegionSize()
    {
        return regionSize;
    }
}
