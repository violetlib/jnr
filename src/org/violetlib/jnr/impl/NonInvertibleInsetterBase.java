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
  A convenient base class for a non-invertible insetter.
*/

public abstract class NonInvertibleInsetterBase
  implements Insetter1
{
    @Override
    public boolean isInvertible()
    {
        return false;
    }

    @Override
    public float getComponentSize(float regionSize)
      throws InsetterNotInvertibleException
    {
        throw new InsetterNotInvertibleException();
    }

    @Override
    public float getFixedInset1()
    {
        return -1;
    }

    @Override
    public float getFixedInset2()
    {
        return -1;
    }
}
