/*
 * Copyright (c) 2020 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.aqua.impl;

import java.text.DecimalFormat;

/**

*/

public abstract class SegmentedControlLayoutInfo
{
    protected static final DecimalFormat df = new DecimalFormat("0.#");

    static {
        df.setDecimalSeparatorAlwaysShown(false);
    }
}
