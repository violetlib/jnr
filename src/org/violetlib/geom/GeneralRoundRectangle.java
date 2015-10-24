/*
 * Changes copyright (c) 2015 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.geom;

// Based on RoundRectangle2D, see copyright below.

import java.awt.Shape;
import java.awt.geom.*;

import org.jetbrains.annotations.*;

/**
 * A generalized rounded rectangle where each corner has its own arc width and arc height.
 * Based on RoundRectangle2D.
 */
public class GeneralRoundRectangle extends RectangularShape implements ExpandableOutline.ExpandableShape
{
    // TBD: contains and intersects are not implemented

    private double x;
    private double y;
    private double width;
    private double height;
    private double tlaw;    // top left arc width
    private double tlah;    // top left arc height
    private double traw;    // top right arc width
    private double trah;    // top right arc height
    private double braw;    // bottom right arc width
    private double brah;    // bottom right arc height
    private double blaw;    // bottom left arc width
    private double blah;    // bottom left arc height

    public GeneralRoundRectangle() {
    }

    public GeneralRoundRectangle(double x, double y, double w, double h,
                                 double tlaw, double tlah, double traw, double trah,
                                 double braw, double brah, double blaw, double blah)
    {
        set(x, y, w, h, tlaw, tlah, traw, trah, braw, brah, blaw, blah);
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    public double getTopLeftArcWidth() {
        return tlaw;
    }

    public double getTopLeftArcHeight() {
        return tlah;
    }

    public double getTopRightArcWidth() {
        return traw;
    }

    public double getTopRightArcHeight() {
        return trah;
    }

    public double getBottomRightArcWidth() {
        return braw;
    }

    public double getBottomRightArcHeight() {
        return brah;
    }

    public double getBottomLeftArcWidth() {
        return blaw;
    }

    public double getBottomLeftArcHeight() {
        return blah;
    }

    public boolean isEmpty() {
        return (width <= 0.0f) || (height <= 0.0f);
    }

    public void set(double x, double y, double w, double h,
                    double tlaw, double tlah, double traw, double trah,
                    double braw, double brah, double blaw, double blah)
    {
        this.x = x;
        this.y = y;
        this.width = w;
        this.height = h;
        this.tlaw = tlaw;
        this.tlah = tlah;
        this.traw = traw;
        this.trah = trah;
        this.braw = braw;
        this.brah = brah;
        this.blaw = blaw;
        this.blah = blah;
    }

    public void set(GeneralRoundRectangle rr) {
        this.x = rr.getX();

        this.y = rr.getY();
        this.width = rr.getWidth();
        this.height = rr.getHeight();
        this.tlaw = rr.getTopLeftArcWidth();
        this.tlah = rr.getTopLeftArcHeight();
        this.traw = rr.getTopRightArcWidth();
        this.trah = rr.getTopRightArcHeight();
        this.braw = rr.getBottomRightArcWidth();
        this.brah = rr.getBottomRightArcHeight();
        this.blaw = rr.getBottomLeftArcWidth();
        this.blah = rr.getBottomLeftArcHeight();
    }

    @Override
    public @NotNull Shape createExpandedShape(float offset)
    {
        return new GeneralRoundRectangle(x - offset, y - offset, width + 2 * offset, height + 2 * offset,
					tlaw > 0 ? tlaw + offset : 0,
					tlah > 0 ? tlah + offset : 0,
					traw > 0 ? traw + offset : 0,
					trah > 0 ? trah + offset : 0,
					braw > 0 ? braw + offset : 0,
					brah > 0 ? brah + offset : 0,
					blaw > 0 ? blaw + offset : 0,
					blah > 0 ? blah + offset : 0
          );
    }

    @Override
    public @NotNull Shape createTranslatedShape(double x, double y)
    {
        return new GeneralRoundRectangle(x + this.x, y + this.y, width, height,
					tlaw, tlah, traw, trah, braw, brah, blaw, blah);
    }

    public Rectangle2D getBounds2D() {
        return new Rectangle2D.Double(x, y, width, height);
    }

    public void setFrame(double x, double y, double w, double h) {
        set(x, y, w, h,
                getTopLeftArcWidth(), getTopLeftArcHeight(),
                getTopRightArcWidth(), getTopRightArcHeight(),
                getBottomRightArcWidth(), getBottomRightArcHeight(),
                getBottomLeftArcWidth(), getBottomLeftArcHeight());
    }

    public boolean contains(double x, double y) {
//        if (isEmpty()) {
//            return false;
//        }
//        double rrx0 = getX();
//        double rry0 = getY();
//        double rrx1 = rrx0 + getWidth();
//        double rry1 = rry0 + getHeight();
//        // Check for trivial rejection - point is outside bounding rectangle
//        if (x < rrx0 || y < rry0 || x >= rrx1 || y >= rry1) {
//            return false;
//        }
//        double aw = Math.min(getWidth(), Math.abs(getArcWidth())) / 2.0;
//        double ah = Math.min(getHeight(), Math.abs(getArcHeight())) / 2.0;
//        // Check which corner point is in and do circular containment
//        // test - otherwise simple acceptance
//        if (x >= (rrx0 += aw) && x < (rrx0 = rrx1 - aw)) {
//            return true;
//        }
//        if (y >= (rry0 += ah) && y < (rry0 = rry1 - ah)) {
//            return true;
//        }
//        x = (x - rrx0) / aw;
//        y = (y - rry0) / ah;
//        return (x * x + y * y <= 1.0);

        // TBD
        return false;
    }

//    private int classify(double coord, double left, double right,
//                         double arcsize)
//    {
//        if (coord < left) {
//            return 0;
//        } else if (coord < left + arcsize) {
//            return 1;
//        } else if (coord < right - arcsize) {
//            return 2;
//        } else if (coord < right) {
//            return 3;
//        } else {
//            return 4;
//        }
//    }

    public boolean intersects(double x, double y, double w, double h) {
//        if (isEmpty() || w <= 0 || h <= 0) {
//            return false;
//        }
//        double rrx0 = getX();
//        double rry0 = getY();
//        double rrx1 = rrx0 + getWidth();
//        double rry1 = rry0 + getHeight();
//        // Check for trivial rejection - bounding rectangles do not intersect
//        if (x + w <= rrx0 || x >= rrx1 || y + h <= rry0 || y >= rry1) {
//            return false;
//        }
//        double aw = Math.min(getWidth(), Math.abs(getArcWidth())) / 2.0;
//        double ah = Math.min(getHeight(), Math.abs(getArcHeight())) / 2.0;
//        int x0class = classify(x, rrx0, rrx1, aw);
//        int x1class = classify(x + w, rrx0, rrx1, aw);
//        int y0class = classify(y, rry0, rry1, ah);
//        int y1class = classify(y + h, rry0, rry1, ah);
//        // Trivially accept if any point is inside inner rectangle
//        if (x0class == 2 || x1class == 2 || y0class == 2 || y1class == 2) {
//            return true;
//        }
//        // Trivially accept if either edge spans inner rectangle
//        if ((x0class < 2 && x1class > 2) || (y0class < 2 && y1class > 2)) {
//            return true;
//        }
//        // Since neither edge spans the center, then one of the corners
//        // must be in one of the rounded edges.  We detect this case if
//        // a [xy]0class is 3 or a [xy]1class is 1.  One of those two cases
//        // must be true for each direction.
//        // We now find a "nearest point" to test for being inside a rounded
//        // corner.
//        x = (x1class == 1) ? (x = x + w - (rrx0 + aw)) : (x = x - (rrx1 - aw));
//        y = (y1class == 1) ? (y = y + h - (rry0 + ah)) : (y = y - (rry1 - ah));
//        x = x / aw;
//        y = y / ah;
//        return (x * x + y * y <= 1.0);

        // TBD
        return false;
    }

    public boolean contains(double x, double y, double w, double h) {
        if (isEmpty() || w <= 0 || h <= 0) {
            return false;
        }
        return (contains(x, y) &&
                contains(x + w, y) &&
                contains(x, y + h) &&
                contains(x + w, y + h));
    }

    public PathIterator getPathIterator(AffineTransform at) {
        return new GeneralRoundRectIterator(this, at);
    }

    public int hashCode() {
        long bits = java.lang.Double.doubleToLongBits(getX());
        bits += java.lang.Double.doubleToLongBits(getY()) * 37;
        bits += java.lang.Double.doubleToLongBits(getWidth()) * 43;
        bits += java.lang.Double.doubleToLongBits(getHeight()) * 47;
        bits += java.lang.Double.doubleToLongBits(getTopLeftArcWidth()) * 53;
        bits += java.lang.Double.doubleToLongBits(getTopLeftArcHeight()) * 59;
        bits += java.lang.Double.doubleToLongBits(getTopRightArcWidth()) * 61;
        bits += java.lang.Double.doubleToLongBits(getTopRightArcHeight()) * 67;
        bits += java.lang.Double.doubleToLongBits(getBottomRightArcWidth()) * 71;
        bits += java.lang.Double.doubleToLongBits(getBottomRightArcHeight()) * 73;
        bits += java.lang.Double.doubleToLongBits(getBottomLeftArcWidth()) * 79;
        bits += java.lang.Double.doubleToLongBits(getBottomLeftArcHeight()) * 83;
        return (((int) bits) ^ ((int) (bits >> 32)));
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof GeneralRoundRectangle) {
            GeneralRoundRectangle rr2d = (GeneralRoundRectangle) obj;
            return ((getX() == rr2d.getX()) &&
                    (getY() == rr2d.getY()) &&
                    (getWidth() == rr2d.getWidth()) &&
                    (getHeight() == rr2d.getHeight()) &&
                    (getTopLeftArcWidth() == rr2d.getTopLeftArcWidth()) &&
                    (getTopLeftArcHeight() == rr2d.getTopLeftArcHeight()) &&
                    (getTopRightArcWidth() == rr2d.getTopRightArcWidth()) &&
                    (getTopRightArcHeight() == rr2d.getTopRightArcHeight()) &&
                    (getBottomRightArcWidth() == rr2d.getBottomRightArcWidth()) &&
                    (getBottomRightArcHeight() == rr2d.getBottomRightArcHeight()) &&
                    (getBottomLeftArcWidth() == rr2d.getBottomLeftArcWidth()) &&
                    (getBottomLeftArcHeight() == rr2d.getBottomLeftArcHeight())
            );
        }
        return false;
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
