/*
 * Copyright (c) 2015 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr;

/**
  This exception is thrown when an operation that requires an invertible insetter is performed on an insetter that is
  not invertible.
*/

public class InsetterNotInvertibleException
  extends IllegalStateException
{
    public InsetterNotInvertibleException()
    {
        super("Insetter is not invertible. Cannot determine a component size from the size of the region.");
    }
}
