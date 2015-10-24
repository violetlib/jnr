/*
 * Changes copyright (c) 2015 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.geom;

// Based on RoundRectIterator, see copyright notice below.

import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.util.NoSuchElementException;

/**
 * A path iterator for a general rounded rectangle.
 */
class GeneralRoundRectIterator implements PathIterator {
    double x, y, w, h, tlaw, tlah, traw, trah, braw, brah, blaw, blah;
    AffineTransform affine;
    int index;
    int cornerIndex;
    double arcWidths[];
    double arcHeights[];

    GeneralRoundRectIterator(GeneralRoundRectangle rr, AffineTransform at) {
        this.x = rr.getX();
        this.y = rr.getY();
        this.w = rr.getWidth();
        this.h = rr.getHeight();
        this.tlaw = Math.min(w, Math.abs(rr.getTopLeftArcWidth()));
        this.tlah = Math.min(h, Math.abs(rr.getTopLeftArcHeight()));
        this.traw = Math.min(w, Math.abs(rr.getTopRightArcWidth()));
        this.trah = Math.min(h, Math.abs(rr.getTopRightArcHeight()));
        this.braw = Math.min(w, Math.abs(rr.getBottomRightArcWidth()));
        this.brah = Math.min(h, Math.abs(rr.getBottomRightArcHeight()));
        this.blaw = Math.min(w, Math.abs(rr.getBottomLeftArcWidth()));
        this.blah = Math.min(h, Math.abs(rr.getBottomLeftArcHeight()));
        this.affine = at;
        if (tlaw < 0 || tlah < 0 || traw < 0 || trah < 0 || braw < 0 || brah < 0 || blaw < 0 || blah < 0) {
            // Don't draw anything...
            index = ctrlpts.length;
        }
        arcWidths = new double[] { tlaw, blaw, braw, traw };
        arcHeights = new double[] { tlah, blah, brah, trah };
    }

    /**
     * Return the winding rule for determining the insideness of the
     * path.
     * @see #WIND_EVEN_ODD
     * @see #WIND_NON_ZERO
     */
    public int getWindingRule() {
        return WIND_NON_ZERO;
    }

    /**
     * Tests if there are more points to read.
     * @return true if there are more points to read
     */
    public boolean isDone() {
        return index >= ctrlpts.length;
    }

    /**
     * Moves the iterator to the next segment of the path forwards
     * along the primary direction of traversal as long as there are
     * more points in that direction.
     */
    public void next() {
        index++;
        if (index % 2 == 1) {
            cornerIndex++;
            if (cornerIndex >= 4) {
                cornerIndex = 0;
            }
        }
    }

    private static final double angle = Math.PI / 4.0;
    private static final double a = 1.0 - Math.cos(angle);
    private static final double b = Math.tan(angle);
    private static final double c = Math.sqrt(1.0 + b * b) - 1 + a;
    private static final double cv = 4.0 / 3.0 * a * b / c;
    private static final double acv = (1.0 - cv) / 2.0;

    // For each array:
    //     4 values for each point {v0, v1, v2, v3}:
    //         point = (x + v0 * w + v1 * arcWidth,
    //                  y + v2 * h + v3 * arcHeight);
    private static double ctrlpts[][] = {
        {  0.0,  0.0,  0.0,  0.5 },
        {  0.0,  0.0,  1.0, -0.5 },
        {  0.0,  0.0,  1.0, -acv,
           0.0,  acv,  1.0,  0.0,
           0.0,  0.5,  1.0,  0.0 },
        {  1.0, -0.5,  1.0,  0.0 },
        {  1.0, -acv,  1.0,  0.0,
           1.0,  0.0,  1.0, -acv,
           1.0,  0.0,  1.0, -0.5 },
        {  1.0,  0.0,  0.0,  0.5 },
        {  1.0,  0.0,  0.0,  acv,
           1.0, -acv,  0.0,  0.0,
           1.0, -0.5,  0.0,  0.0 },
        {  0.0,  0.5,  0.0,  0.0 },
        {  0.0,  acv,  0.0,  0.0,
           0.0,  0.0,  0.0,  acv,
           0.0,  0.0,  0.0,  0.5 },
        {},
    };
    private static int types[] = {
        SEG_MOVETO,
        SEG_LINETO, SEG_CUBICTO,
        SEG_LINETO, SEG_CUBICTO,
        SEG_LINETO, SEG_CUBICTO,
        SEG_LINETO, SEG_CUBICTO,
        SEG_CLOSE,
    };

    public int currentSegment(float[] coords) {
        if (isDone()) {
            throw new NoSuchElementException("GeneralRoundRect iterator out of bounds");
        }
        double ctrls[] = ctrlpts[index];
        int nc = 0;
        for (int i = 0; i < ctrls.length; i += 4) {
            coords[nc++] = (float) (x + ctrls[i + 0] * w + ctrls[i + 1] * arcWidths[cornerIndex]);
            coords[nc++] = (float) (y + ctrls[i + 2] * h + ctrls[i + 3] * arcHeights[cornerIndex]);
        }
        if (affine != null) {
            affine.transform(coords, 0, coords, 0, nc / 2);
        }
        return types[index];
    }

    public int currentSegment(double[] coords) {
        if (isDone()) {
            throw new NoSuchElementException("GeneralRoundRect iterator out of bounds");
        }
        double ctrls[] = ctrlpts[index];
        int nc = 0;
        for (int i = 0; i < ctrls.length; i += 4) {
            coords[nc++] = (x + ctrls[i + 0] * w + ctrls[i + 1] * arcWidths[cornerIndex]);
            coords[nc++] = (y + ctrls[i + 2] * h + ctrls[i + 3] * arcHeights[cornerIndex]);
        }
        if (affine != null) {
            affine.transform(coords, 0, coords, 0, nc / 2);
        }
        return types[index];
    }
}

/*
 * Copyright (c) 1997, 2006, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
