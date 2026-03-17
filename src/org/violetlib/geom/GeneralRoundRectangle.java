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
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;

import org.jetbrains.annotations.*;

/**
  * A generalized rounded rectangle where each corner has its own arc width and arc height.
  * Based on RoundRectangle2D.
*/
public class GeneralRoundRectangle extends RectangularShape implements ExpandableOutline.ExpandableShape
{

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
        if (isEmpty()) {
            return false;
        }

        double x0 = this.x;
        double y0 = this.y;
        double x1 = x0 + width;
        double y1 = y0 + height;

        // Check for trivial rejection - point is outside bounding rectangle
        if (x < x0 || y < y0 || x >= x1 || y >= y1) {
            return false;
        }

        // Check each corner. If the point is in a corner's arc zone, test against that corner's ellipse.
        // Arc widths and heights are clamped so they don't exceed the rectangle dimensions.

        // Top-left corner
        double aw = Math.min(width, Math.abs(tlaw)) / 2.0;
        double ah = Math.min(height, Math.abs(tlah)) / 2.0;
        if (aw > 0 && ah > 0 && x < x0 + aw && y < y0 + ah) {
            double nx = (x - (x0 + aw)) / aw;
            double ny = (y - (y0 + ah)) / ah;
            return nx * nx + ny * ny <= 1.0;
        }

        // Top-right corner
        aw = Math.min(width, Math.abs(traw)) / 2.0;
        ah = Math.min(height, Math.abs(trah)) / 2.0;
        if (aw > 0 && ah > 0 && x >= x1 - aw && y < y0 + ah) {
            double nx = (x - (x1 - aw)) / aw;
            double ny = (y - (y0 + ah)) / ah;
            return nx * nx + ny * ny <= 1.0;
        }

        // Bottom-right corner
        aw = Math.min(width, Math.abs(braw)) / 2.0;
        ah = Math.min(height, Math.abs(brah)) / 2.0;
        if (aw > 0 && ah > 0 && x >= x1 - aw && y >= y1 - ah) {
            double nx = (x - (x1 - aw)) / aw;
            double ny = (y - (y1 - ah)) / ah;
            return nx * nx + ny * ny <= 1.0;
        }

        // Bottom-left corner
        aw = Math.min(width, Math.abs(blaw)) / 2.0;
        ah = Math.min(height, Math.abs(blah)) / 2.0;
        if (aw > 0 && ah > 0 && x < x0 + aw && y >= y1 - ah) {
            double nx = (x - (x0 + aw)) / aw;
            double ny = (y - (y1 - ah)) / ah;
            return nx * nx + ny * ny <= 1.0;
        }

        // Not in any corner zone — must be in the body of the rectangle
        return true;
    }

    /**
      Test whether a corner's quarter-ellipse intersects with a rectangle. The corner point is at (cx, cy), the arc
      zone extends inward by aw horizontally and ah vertically, and the rectangle to test is given by its edges.

      @return true if the quarter-ellipse region intersects the given rectangle.
    */
    private static boolean cornerIntersects(double cx, double cy, double aw, double ah,
                                            double rx0, double ry0, double rx1, double ry1,
                                            double signX, double signY)
    {
        // Find the point in the rectangle nearest to the ellipse center
        double ecx = cx + signX * aw;
        double ecy = cy + signY * ah;
        double nearestX = Math.max(rx0, Math.min(rx1, ecx));
        double nearestY = Math.max(ry0, Math.min(ry1, ecy));
        double nx = (nearestX - ecx) / aw;
        double ny = (nearestY - ecy) / ah;
        return nx * nx + ny * ny <= 1.0;
    }

    public boolean intersects(double x, double y, double w, double h) {
        if (isEmpty() || w <= 0 || h <= 0) {
            return false;
        }

        double x0 = this.x;
        double y0 = this.y;
        double x1 = x0 + width;
        double y1 = y0 + height;

        // Check for trivial rejection - bounding rectangles do not intersect
        if (x + w <= x0 || x >= x1 || y + h <= y0 || y >= y1) {
            return false;
        }

        // Clamp arc radii
        double tlawH = Math.min(width, Math.abs(tlaw)) / 2.0;
        double tlahH = Math.min(height, Math.abs(tlah)) / 2.0;
        double trawH = Math.min(width, Math.abs(traw)) / 2.0;
        double trahH = Math.min(height, Math.abs(trah)) / 2.0;
        double brawH = Math.min(width, Math.abs(braw)) / 2.0;
        double brahH = Math.min(height, Math.abs(brah)) / 2.0;
        double blawH = Math.min(width, Math.abs(blaw)) / 2.0;
        double blahH = Math.min(height, Math.abs(blah)) / 2.0;

        // If the query rect extends into the horizontal or vertical middle band, it definitely intersects
        double leftMax = Math.max(tlawH, blawH);
        double rightMax = Math.max(trawH, brawH);
        double topMax = Math.max(tlahH, trahH);
        double bottomMax = Math.max(blahH, brahH);

        if (x + w > x0 + leftMax && x < x1 - rightMax) {
            return true;
        }
        if (y + h > y0 + topMax && y < y1 - bottomMax) {
            return true;
        }

        // The query rect is confined to corner zones. Check each relevant corner.
        double rx0 = x;
        double ry0 = y;
        double rx1 = x + w;
        double ry1 = y + h;

        // Top-left corner
        if (rx0 < x0 + tlawH && ry0 < y0 + tlahH && tlawH > 0 && tlahH > 0) {
            if (cornerIntersects(x0, y0, tlawH, tlahH, rx0, ry0, rx1, ry1, 1, 1)) {
                return true;
            }
        }

        // Top-right corner
        if (rx1 > x1 - trawH && ry0 < y0 + trahH && trawH > 0 && trahH > 0) {
            if (cornerIntersects(x1, y0, trawH, trahH, rx0, ry0, rx1, ry1, -1, 1)) {
                return true;
            }
        }

        // Bottom-right corner
        if (rx1 > x1 - brawH && ry1 > y1 - brahH && brawH > 0 && brahH > 0) {
            if (cornerIntersects(x1, y1, brawH, brahH, rx0, ry0, rx1, ry1, -1, -1)) {
                return true;
            }
        }

        // Bottom-left corner
        if (rx0 < x0 + blawH && ry1 > y1 - blahH && blawH > 0 && blahH > 0) {
            if (cornerIntersects(x0, y1, blawH, blahH, rx0, ry0, rx1, ry1, 1, -1)) {
                return true;
            }
        }

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
