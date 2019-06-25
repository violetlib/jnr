/*
 * Copyright (c) 2015 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.impl;

import org.violetlib.jnr.InsetterNotInvertibleException;

/**
  A single dimension insetter where the region size and the component size are both fixed.
*/

public class RigidInsetter1
  implements Insetter1
{
    private final float regionSize;
    private final float componentSize;
    private final float d1;

    /**
      Create insets for a fixed size region at a fixed offset in a fixed size component.
      @param regionSize The fixed region size.
      @param componentSize The fixed component size.
      @param d1 The fixed offset of the region from the left or top edge of the component.
    */

    public RigidInsetter1(float regionSize, float componentSize, float d1)
    {
        if (regionSize < 0 || componentSize < 0) {
            throw new IllegalArgumentException("Invalid negative region or component size");
        }

        if (regionSize > componentSize) {
            throw new IllegalArgumentException("Invalid region size exceeds component size");
        }

        if (d1 < 0 || d1 > componentSize - regionSize) {
            throw new IllegalArgumentException("Invalid offset");
        }

        this.regionSize = regionSize;
        this.componentSize = componentSize;
        this.d1 = d1;
    }

    @Override
    public float getRegionOrigin(float componentSize)
    {
        return d1;
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
        return Math.max(componentSize, regionSize + getFixedInset1() + getFixedInset2());
    }

    @Override
    public float getFixedInset1()
    {
        return d1;
    }

    @Override
    public float getFixedInset2()
    {
        return componentSize - regionSize - d1;
    }

    @Override
    public float getFixedRegionSize()
    {
        return regionSize;
    }
}
