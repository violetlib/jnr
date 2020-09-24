/*
 * Copyright (c) 2015-2020 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.impl;

import java.text.DecimalFormat;

import org.violetlib.jnr.aqua.AquaUIPainter;

import org.jetbrains.annotations.*;

import static org.violetlib.jnr.impl.ImageUtils.*;

/**
  Utilities
*/

public class JNRUtils
{
    static final @NotNull DecimalFormat df2 = new DecimalFormat("0.00");

    static {
        df2.setDecimalSeparatorAlwaysShown(true);
    }

    public static int size(@NotNull AquaUIPainter.Size sz, int regular, int small, int mini)
    {
        switch (sz) {
            case SMALL:
                return small;
            case MINI:
                return mini;
            default:
                return regular;
        }
    }

    public static int size(@NotNull AquaUIPainter.Size sz, int large, int regular, int small, int mini)
    {
        switch (sz) {
            case SMALL:
                return small;
            case MINI:
                return mini;
            case LARGE:
                return large;
            default:
                return regular;
        }
    }

    public static float size2D(@NotNull AquaUIPainter.Size sz, float regular, float small, float mini)
    {
        switch (sz) {
            case SMALL:
                return small;
            case MINI:
                return mini;
            default:
                return regular;
        }
    }

    public static float size2D(@NotNull AquaUIPainter.Size sz, double regular, double small, double mini)
    {
        switch (sz) {
            case SMALL:
                return (float) small;
            case MINI:
                return (float) mini;
            default:
                return (float) regular;
        }
    }

    public static float size2D(@NotNull AquaUIPainter.Size sz, double large, double regular, double small, double mini)
    {
        switch (sz) {
            case SMALL:
                return (float) small;
            case MINI:
                return (float) mini;
            case LARGE:
                return (float) large;
            default:
                return (float) regular;
        }
    }

    public static @NotNull String format2(double v)
    {
        return df2.format(v);
    }

    public static @Nullable BasicRendererDescription toBasicRendererDescription(@NotNull RendererDescription rd)
    {
        if (rd instanceof BasicRendererDescription) {
            return (BasicRendererDescription) rd;
        }
        return null;
    }

    /**
      Return the basic renderer description corresponding to a renderer description assuming a specific scale factor.
      @param rd The source renderer description.
      @param scaleFactor The scale factor.
      @return the basic renderer description corresponding to {@code rd}, or null if not determinable.
    */

    public static @Nullable BasicRendererDescription toBasicRendererDescription(@NotNull RendererDescription rd,
                                                                                int scaleFactor)
    {
        if (rd instanceof BasicRendererDescription) {
            return (BasicRendererDescription) rd;
        } else if (rd instanceof MultiResolutionRendererDescription) {
            MultiResolutionRendererDescription mrd = (MultiResolutionRendererDescription) rd;
            RendererDescription ard = scaleFactor == 1 ? mrd.getDescription1() : mrd.getDescription2();
            if (ard instanceof BasicRendererDescription) {
                return (BasicRendererDescription) ard;
            }
        }
        return null;
    }

    public static @NotNull RendererDescription adjustRendererDescription(@NotNull RendererDescription rd,
                                                                         float deltaX, float deltaY,
                                                                         float deltaWA, float deltaHA)
      throws UnsupportedOperationException
    {
        if (deltaX == 0 && deltaY == 0 && deltaWA == 0 && deltaHA == 0) {
            return rd;
        }

        BasicRendererDescription brd = toBasicRendererDescription(rd);
        if (brd != null) {
            return brd.withAdjustments(deltaX, deltaY, deltaWA, deltaHA);
        }

        if (rd instanceof MultiResolutionRendererDescription) {
            MultiResolutionRendererDescription mrd = (MultiResolutionRendererDescription) rd;
            BasicRendererDescription rd1 = toBasicRendererDescription(mrd.getDescription1());
            BasicRendererDescription rd2 = toBasicRendererDescription(mrd.getDescription2());
            if (rd1 != null && rd2 != null) {
                rd1 = rd1.withAdjustments(deltaX, deltaY, deltaWA, deltaHA);
                rd2 = rd2.withAdjustments(deltaX, deltaY, deltaWA, deltaHA);
                return new MultiResolutionRendererDescription(rd1, rd2);
            }
        }

        throw new UnsupportedOperationException("Renderer description cannot be adjusted");
    }

    public static final float NO_CHANGE = -123456;

    public static @NotNull RendererDescription changeRendererDescription(@NotNull RendererDescription rd,
                                                                         float x, float y, float wa, float ha)
      throws UnsupportedOperationException
    {
        if (x == NO_CHANGE && y == NO_CHANGE && wa == NO_CHANGE && ha == NO_CHANGE) {
            return rd;
        }

        BasicRendererDescription brd = toBasicRendererDescription(rd);
        if (brd != null) {
            return change(brd, x, y, wa, ha);
        }

        if (rd instanceof MultiResolutionRendererDescription) {
            MultiResolutionRendererDescription mrd = (MultiResolutionRendererDescription) rd;
            BasicRendererDescription rd1 = toBasicRendererDescription(mrd.getDescription1());
            BasicRendererDescription rd2 = toBasicRendererDescription(mrd.getDescription2());
            if (rd1 != null && rd2 != null) {
                rd1 = change(rd1, x, y, wa, ha);
                rd2 = change(rd2, x, y, wa, ha);
                return new MultiResolutionRendererDescription(rd1, rd2);
            }
        }

        throw new UnsupportedOperationException("Renderer description cannot be changed");
    }

    private static @NotNull BasicRendererDescription change(@NotNull BasicRendererDescription brd,
                                                            float x, float y, float wa, float ha)
    {
        float nx = x == NO_CHANGE ? brd.getXOffset() : x;
        float ny = y == NO_CHANGE ? brd.getYOffset() : y;
        float nwa = wa == NO_CHANGE ? brd.getWidthAdjustment() : wa;
        float nha = ha == NO_CHANGE ? brd.getHeightAdjustment(): ha;
        return new BasicRendererDescription(nx, ny, nwa, nha);
    }

    public static int combine(int oldPixel, int newPixel)
    {
        int newAlpha = alpha(newPixel);
        if (newAlpha == 255) {
            return newPixel;
        } else if (newAlpha == 0) {
            return oldPixel;
        }

        int oldAlpha = alpha(oldPixel);
        int oldRed = red(oldPixel);
        int oldGreen = green(oldPixel);
        int oldBlue = blue(oldPixel);
        int newRed = red(newPixel);
        int newGreen = green(newPixel);
        int newBlue = blue(newPixel);
        int f = 255 - newAlpha;
        int red = (newRed + ((oldRed * f) >> 8));
        int green = (newGreen + ((oldGreen * f) >> 8));
        int blue = (newBlue + ((oldBlue * f) >> 8));
        int alpha = ((255 * newAlpha + oldAlpha * f) / 255);
        return createPixel(alpha, red, green, blue);
    }

    public static boolean describeRenderer(@NotNull BasicRenderer r, int w, int h, int scaleFactor)
    {
        int rw = (int) Math.ceil(scaleFactor * w);
        int rh = (int) Math.ceil(scaleFactor * h);
        int[] buffer = new int[rw * rh];
        r.render(buffer, rw, rh, w, h);
        return describeRaster(buffer, rw, rh);
    }

    public static void showRenderer(@NotNull BasicRenderer r, int w, int h, int scaleFactor)
    {
        int rw = (int) Math.ceil(scaleFactor * w);
        int rh = (int) Math.ceil(scaleFactor * h);
        int[] buffer = new int[rw * rh];
        r.render(buffer, rw, rh, w, h);
        showRaster(buffer, rw, rh);
    }

    /**
      Describe the contents of a raster.
      @param buffer The raster buffer.
      @param rw The width of each raster line.
      @param rh The number of raster lines.
      @return true if the raster is not empty.
    */

    public static boolean describeRaster(@NotNull int[] buffer, int rw, int rh)
    {
        int transparentPixelCount = 0;
        int opaquePixelCount = 0;
        int maximumAlpha = 0;
        int maximumAlphaRow = 0;
        int maximumAlphaCol = 0;
        int minimumAlpha = 1000;
        int minimumAlphaRow = 0;
        int minimumAlphaCol = 0;
        boolean hasGrayPixels = false;
        boolean hasColoredPixels = false;

        for (int row = 0; row < rh; row++) {
            for (int col = 0; col < rw; col++) {
                int index = row * rw + col;
                int pixel = buffer[index];
                int alpha = alpha(pixel);
                if (alpha == 0) {
                    continue;
                }

                if (alpha > maximumAlpha) {
                    maximumAlpha = alpha;
                    maximumAlphaRow = row;
                    maximumAlphaCol = col;
                }

                if (alpha < minimumAlpha) {
                    minimumAlpha = alpha;
                    minimumAlphaRow = row;
                    minimumAlphaCol = col;
                }

                if (alpha == 255) {
                    ++opaquePixelCount;
                } else {
                    ++transparentPixelCount;
                }

                if (!hasGrayPixels || !hasColoredPixels) {
                    int red = red(pixel);
                    int green = green(pixel);
                    int blue = blue(pixel);
                    if (red > 0 || green > 0 || blue > 0) {
                        if (red == green && green == blue) {
                            hasGrayPixels = true;
                        } else {
                            hasColoredPixels = true;
                        }
                    }
                }
            }
        }

        int pixelCount = rw * rh;

        String s = "";
        if (transparentPixelCount > 0) {
            int percent = Math.round(transparentPixelCount * 100f / pixelCount);
            s += " " + transparentPixelCount + " transparent pixels (" + percent + "%)";
        }
        if (opaquePixelCount > 0) {
            int percent = Math.round(opaquePixelCount * 100f / pixelCount);
            s += " " + transparentPixelCount + " opaque pixels (" + percent + "%)";
        }
        if (maximumAlpha > 0) {
            s += " maximum alpha: " + maximumAlpha + " at " + maximumAlphaCol + ", " + maximumAlphaRow;
        }

        if (minimumAlpha < 256) {
            s += " minimum alpha: " + minimumAlpha + " at " + minimumAlphaCol + ", " + minimumAlphaRow;
        }

        if (s.isEmpty()) {
            System.out.println("Empty raster");
            return false;
        }
        System.out.println(s);
        return true;
    }

    /**
      Display the contents of a raster, converted from pre-multiplied alpha values.
      @param buffer The raster buffer.
      @param rw The width of each raster line.
      @param rh The number of raster lines.
    */

    public static void showRaster(@NotNull int[] buffer, int rw, int rh)
    {
        for (int row = 0; row < rh; row++) {
            for (int col = 0; col < rw; col++) {
                int index = row * rw + col;
                int pixel = buffer[index];
                int alpha = alpha(pixel);
                if (alpha == 0) {
                    System.out.print("                 ");
                } else {
                    int red = red(pixel);
                    int green = green(pixel);
                    int blue = blue(pixel);

                    // convert from premultiplied alpha
                    if (alpha > 0) {
                        if (red > 0) {
                            red = red * 255 / alpha;
                        }
                        if (green > 0) {
                            green = green * 255 / alpha;
                        }
                        if (blue > 0) {
                            blue = blue * 255 / alpha;
                        }
                    }
                    System.out.print(String.format("%3d %3d %3d %3d", red, green, blue, alpha));
                }
                System.out.print("  ");
            }
            System.out.println();
        }
    }
}
