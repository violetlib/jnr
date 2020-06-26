/*
 * Copyright (c) 2015 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.impl;

import org.violetlib.jnr.Insetter;
import org.violetlib.jnr.InsetterNotInvertibleException;

/**
  A definition of insets along one axis, either horizontal (left and right insets) or vertical (top and bottom insets).
  This definition relates the size of a component along the axis to the size and position of a region of that
  component along the same axis. See {@link Insetter} for more information.
*/

public interface Insetter1
{
    /**
      Map the component size to the origin of the region, relative to the left or top edge of the component.

      @param componentSize The component size along the relevant axis.

      @return The origin of the region along the axis relative to the component.
    */

    float getRegionOrigin(float componentSize);

    /**
      Map the component size to the size of the region.

      @param componentSize The component size along the relevant axis.

      @return the region size along the relevant axis.
    */

    float getRegionSize(float componentSize);

    /**
      Indicate whether this insetter is invertible. An invertible insetter can map a region size to the component size.
    */

    boolean isInvertible();

    /**
      Map the region size to the size of the component. This operation is valid only if the insetter is invertible.

      @param regionSize The region size along the relevant axis.

      @return the component size along the axis. Note that the returned size may exceed a fixed size of the component.

      @throws InsetterNotInvertibleException if this insetter is not invertible.
    */

    float getComponentSize(float regionSize)
      throws InsetterNotInvertibleException;

    /**
      Return the left or top inset, if it is fixed.
      @return the inset, or -1 if not fixed.
    */

    float getFixedInset1();

    /**
      Return the right or bottom inset, if it is fixed.
      @return the inset, or -1 if not fixed.
    */

    float getFixedInset2();

    /**
      Return the region size, if it is fixed.
      @return the fixed region size, or zero if the region size is not fixed.
    */

    float getFixedRegionSize();
}
