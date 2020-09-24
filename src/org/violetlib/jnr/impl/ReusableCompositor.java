/*
 * Copyright (c) 2015-2020 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.impl;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.util.Arrays;

import org.jetbrains.annotations.*;

import static org.violetlib.jnr.impl.ImageUtils.*;

/**
  A compositor that renders into an INT_ARGB_PRE raster from various sources. Multiple renderings can be composed into
  the same raster. The raster can be drawn to a graphics context or returned as an image.

  <p>
  The compositor has an integer scale factor, used to support high resolution displays. The scale factor is available to
  the sources so that they can produce a rendering at the appropriate scale; it is not used for drawing the raster or
  returning the raster as an image.

  <p>
  The compositor can be reused without reallocating the raster. Reusing a compositor is permitted except when the raster
  image is in use. The raster size is configured when the compositor is first used or reused.

  <p>
  This class is not thread safe.
*/

public class ReusableCompositor
{
    // TBD: would it be faster to turn everything into an Image and use graphics operations?

    // Note: the data is intended to be private, which is why ReusableCompositor does not support the PixelRaster interface.

    private @Nullable int[] data;  // the actual raster buffer, reallocated as needed to contain at least the required number of pixels.
    // May be null if the raster has zero size.

    private final @NotNull PixelRaster dataAccess = new MyPixelRaster();  // for internal access to the data

    private @Nullable BufferedImage b;  // an image using the raster buffer, created on demand and released when the raster buffer is replaced.
    // May be null if the raster has zero size.

    private boolean isConfigured;  // true if the raster dimensions have been changed but the raster has not been updated
    private boolean isEmpty;       // true if the raster is known to have no content (allows compose to be faster)

    private int rasterWidth;       // the current raster width (may be inconsistent with the raster buffer until it has been configured)
    private int rasterHeight;      // the current raster height (may be inconsistent with the raster buffer until it has been configured)
    private int scaleFactor;       // the scale factor

    /**
      This interface permits additional image sources to be supported. They must be implemented using already supported
      sources.
    */

    public interface PixelSource
    {
        /**
          Render pixels into the specified compositor.

          @param compositor The compositor.
        */

        void composeTo(@NotNull ReusableCompositor compositor);
    }

    public interface PixelOperator
    {
        int combine(int destinationPixel, int sourcePixel);
    }

    /**
      Create a reusable compositor. The raster width and height are zero.
    */

    public ReusableCompositor()
    {
    }

    /**
      Create a reusable compositor using the specified buffer.
    */

    public ReusableCompositor(@NotNull int[] data, int rw, int rh, int scaleFactor)
    {
        if (rw < 0 || rh < 0) {
            throw new IllegalArgumentException("Invalid negative raster width and/or height");
        }

        if (scaleFactor < 1 || scaleFactor > 8) {
            throw new IllegalArgumentException("Invalid or unsupported scale factor");
        }

        this.data = data;
        this.rasterWidth = rw;
        this.rasterHeight = rh;
        this.scaleFactor = scaleFactor;
        this.isConfigured = true;
        this.isEmpty = true;
    }

    private class MyPixelRaster
      implements PixelRaster
    {
        private final @NotNull int[] emptyRaster = new int[0];

        @Override
        public void provide(@NotNull Accessor a)
        {
            if (data == null) {
                a.access(emptyRaster, 0, 0);
            } else {
                a.access(data, rasterWidth, rasterHeight);
            }
        }
    }

    /**
      Return the INT_ARGB_PRE color model.
    */

    public static @NotNull ColorModel getColorModel()
    {
        return BasicImageSupport.getColorModel();
    }

    /**
      Return the width of the raster, in pixels.
    */

    public int getRasterWidth()
    {
        return rasterWidth;
    }

    /**
      Return the height of the raster, in pixels.
    */

    public int getRasterHeight()
    {
        return rasterHeight;
    }

    /**
      Return the scale factor, which relates raster pixels to device independent pixels.
    */

    public int getScaleFactor()
    {
        return scaleFactor;
    }

    /**
      Return the width of the raster, in device independent pixels (according to the scale factor).
    */

    public float getWidth()
    {
        return ((float) rasterWidth) / scaleFactor;
    }

    /**
      Return the height of the raster, in device independent pixels (according to the scale factor).
    */

    public float getHeight()
    {
        return ((float) rasterHeight) / scaleFactor;
    }


    /**
      Create a compositor that is configured to the same raster size and scale factor as this one.
    */

    public @NotNull ReusableCompositor createSimilar()
    {
        ReusableCompositor c = new ReusableCompositor();
        c.reset(rasterWidth, rasterHeight, scaleFactor);
        return c;
    }

    /**
      Create a compositor that is configured to the same scale factor as this one.
      @param width The compositor will be configured to this width, specified in device independent pixels.
      @param height The compositor will be configured to this height, specified in device independent pixels.
    */

    public @NotNull ReusableCompositor createSimilar(float width, float height)
    {
        ReusableCompositor c = new ReusableCompositor();
        int rw = (int) Math.ceil(width * scaleFactor);
        int rh = (int) Math.ceil(height * scaleFactor);
        c.reset(rw, rh, scaleFactor);
        return c;
    }

    /**
      Create a compositor containing a horizontally flipped copy of this one.
    */

    public @NotNull ReusableCompositor createHorizontallyFlippedCopy()
    {
        ReusableCompositor output = createSimilar();
        output.copyHorizontallyFlippedFrom(this);
        return output;
    }

    /**
      Create a compositor containing a vertically flipped copy of this one.
    */

    public @NotNull ReusableCompositor createVerticallyFlippedCopy()
    {
        ReusableCompositor output = createSimilar();
        output.copyVerticallyFlippedFrom(this);
        return output;
    }

    /**
      Resize and clear the raster in preparation for rendering. The actual clearing or reallocation of the raster may be
      deferred until the raster is needed.

      @param rasterWidth The new raster width.
      @param rasterHeight The new raster height.
      @param scaleFactor The scale factor that relates raster pixels to device independent pixels.
    */

    public void reset(int rasterWidth, int rasterHeight, int scaleFactor)
    {
        if (rasterWidth < 0 || rasterHeight < 0) {
            throw new IllegalArgumentException("Invalid negative raster width and/or height");
        }

        if (scaleFactor < 1 || scaleFactor > 8) {
            throw new IllegalArgumentException("Invalid or unsupported scale factor");
        }

        this.rasterWidth = rasterWidth;
        this.rasterHeight = rasterHeight;
        this.scaleFactor = scaleFactor;
        isEmpty = true;
    }

    /**
      Ensure that the raster buffer has been configured to the proper size and cleared if needed. This method supports
      lazy configuration of the raster buffer.
    */

    protected void ensureConfigured()
    {
        if (!isConfigured) {
            isConfigured = true;
            int requiredSize = rasterWidth * rasterHeight;
            if (requiredSize > 0) {
                if (data == null || data.length < requiredSize) {
                    data = new int[requiredSize];
                    b = null;
                } else {
                    Arrays.fill(data, 0);
                }
            }
        }
    }

    /**
      Render into the raster after resizing it and clearing it. This is a convenience method.

      @param r The renderer.
      @param rasterWidth The new raster width.
      @param rasterHeight The new raster height.
      @param scaleFactor The scale factor that relates raster pixels to device independent pixels.
    */

    public void render(@NotNull BasicRenderer r, int rasterWidth, int rasterHeight, int scaleFactor)
    {
        reset(rasterWidth, rasterHeight, scaleFactor);
        ensureConfigured();
        if (data != null) {
            float rw = ((float) rasterWidth) / scaleFactor;
            float rh = ((float) rasterHeight) / scaleFactor;
            r.render(data, rasterWidth, rasterHeight, rw, rh);
            isEmpty = false;
        }
    }

    /**
      Render into the raster, composing with existing contents.

      @param o The source of the pixels to compose with the existing contents. This object may be any of the standard
      sources ({@link BasicRenderer}, {@link PainterExtension}, or another {@link ReusableCompositor}), or an object
      that supports the {@link PixelSource} or {@link PixelRaster} interface.
    */

    public void compose(@NotNull Object o)
    {
        if (o instanceof BasicRenderer) {
            BasicRenderer br = (BasicRenderer) o;
            composeRenderer(br);
        } else if (o instanceof PainterExtension) {
            PainterExtension px = (PainterExtension) o;
            composePainter(px, 0, 0);
        } else if (o instanceof ReusableCompositor) {
            ReusableCompositor rc = (ReusableCompositor) o;
            composeFrom(rc, 0, 0, rasterWidth, rasterHeight);
        } else if (o instanceof PixelRaster) {
            PixelRaster r = (PixelRaster) o;
            composeFrom(r, 0, 0, rasterWidth, rasterHeight);
        } else if (o instanceof PixelSource) {
            PixelSource sr = (PixelSource) o;
            sr.composeTo(this);
        } else {
            throw new UnsupportedOperationException("Unsupported pixel source");
        }
    }

    /**
      Render into the raster, composing with existing contents.

      @param r The renderer that provides the pixels.
    */

    public void composeRenderer(@NotNull BasicRenderer r)
    {
        ensureConfigured();
        if (data != null) {
            if (isEmpty) {
                float rw = ((float) rasterWidth) / scaleFactor;
                float rh = ((float) rasterHeight) / scaleFactor;
                r.render(data, rasterWidth, rasterHeight, rw, rh);
                isEmpty = false;
            } else {
                composeRenderer(r, 0, 0, rasterWidth, rasterHeight);
            }
        }
    }

    /**
      Render into a region of the raster, composing with existing contents. The region defines translation and clipping,
      the source data is not scaled.

      @param r The renderer.
      @param dx The X origin of the raster region.
      @param dy The Y origin of the raster region.
      @param dw The width of the raster region.
      @param dh The height of the raster region.
    */

    public void composeRenderer(@NotNull BasicRenderer r, int dx, int dy, int dw, int dh)
    {
        if (dw > 0 && dh > 0) {
            ReusableCompositor temp = new ReusableCompositor();
            temp.render(r, dw, dh, scaleFactor);
            composeFrom(temp, dx, dy, dw, dh);
        }
    }

    /**
      Render a painter extension into a region of the raster, composing with existing contents. The region defines
      translation and clipping, the source data is not scaled.

      @param px The painter.
      @param dx The X origin of the raster region.
      @param dy The Y origin of the raster region.
      @param dw The width of the raster region.
      @param dh The height of the raster region.
    */

    public void composePainter(@NotNull PainterExtension px, int dx, int dy, int dw, int dh)
    {
        if (dw > 0 && dh > 0) {
            ReusableCompositor temp = new ReusableCompositor();
            temp.reset(dw, dh, scaleFactor);
            temp.composePainter(px, 0, 0);
            composeFrom(temp, dx, dy, dw, dh);
        }
    }

    /**
      Copy pixels from a compositor, flipping horizontally.

      @param source The compositor that is the source of the pixels.
    */

    private void copyHorizontallyFlippedFrom(@NotNull ReusableCompositor source)
    {
        ensureConfigured();

        if (data != null) {
            copyHorizontallyFlippedFrom(source.dataAccess);
        }
    }

    /**
      Copy pixels from a source raster, flipping horizontally.

      @param source The source of the pixels.
    */

    private void copyHorizontallyFlippedFrom(@NotNull PixelRaster source)
    {
        ensureConfigured();

        if (data != null) {
            source.provide((sourceData, sourceWidth, sourceHeight) -> {
                if (sourceWidth > 0 && sourceHeight > 0) {
                    isEmpty = true;
                    for (int row = 0; row < rasterHeight; row++) {
                        for (int col = 0; col < rasterWidth; col++) {
                            int sourceCol = rasterWidth - col - 1;
                            int pixel = sourceData[row * sourceWidth + sourceCol];
                            int alpha = alpha(pixel);
                            if (alpha != 0) {
                                isEmpty = false;
                                data[row * rasterWidth + col] = pixel;
                            }
                        }
                    }
                }
            });
        }
    }

    /**
      Copy pixels from a compositor, flipping vertically.

      @param source The compositor that is the source of the pixels.
    */

    private void copyVerticallyFlippedFrom(@NotNull ReusableCompositor source)
    {
        ensureConfigured();

        if (data != null) {
            copyVerticallyFlippedFrom(source.dataAccess);
        }
    }

    /**
      Copy pixels from a source raster, flipping vertically.

      @param source The source of the pixels.
    */

    private void copyVerticallyFlippedFrom(@NotNull PixelRaster source)
    {
        ensureConfigured();

        if (data != null) {
            source.provide((sourceData, sourceWidth, sourceHeight) -> {
                if (sourceWidth > 0 && sourceHeight > 0) {
                    isEmpty = true;
                    for (int row = 0; row < rasterHeight; row++) {
                        for (int col = 0; col < rasterWidth; col++) {
                            int sourceRow = rasterHeight - row - 1;
                            int pixel = sourceData[sourceRow * sourceWidth + col];
                            int alpha = alpha(pixel);
                            if (alpha != 0) {
                                isEmpty = false;
                                data[row * rasterWidth + col] = pixel;
                            }
                        }
                    }
                }
            });
        }
    }

    /**
      Render from a compositor into a region of the raster, composing with existing contents. The region defines
      translation and clipping, the source data is not scaled.

      @param source The compositor that is the source of the pixels.
      @param dx The X origin of the raster region.
      @param dy The Y origin of the raster region.
      @param dw The width of the raster region.
      @param dh The height of the raster region.
    */

    public void composeFrom(@NotNull ReusableCompositor source, int dx, int dy, int dw, int dh)
    {
        ensureConfigured();

        if (data != null) {
            composeFrom(source.dataAccess, dx, dy, dw, dh);
        }
    }

    /**
      Render from source pixels into a region of the raster, composing with existing contents. The region defines
      translation and clipping, the source data is not scaled.

      @param source The source of the pixels.
      @param dx The X origin of the raster region.
      @param dy The Y origin of the raster region.
      @param dw The width of the raster region.
      @param dh The height of the raster region.
    */

    public void composeFrom(@NotNull PixelRaster source, int dx, int dy, int dw, int dh)
    {
        ensureConfigured();

        if (data != null) {
            source.provide((sourceData, sourceWidth, sourceHeight) -> {
                if (sourceWidth > 0 && sourceHeight > 0) {
                    isEmpty = false;
                    int columnCount = Math.min(dw, sourceWidth);
                    int rowCount = Math.min(dh, sourceHeight);
                    for (int rowOffset = 0; rowOffset < rowCount; rowOffset++) {
                        int row = dy + rowOffset;
                        if (row >= 0 && row < rasterHeight) {
                            for (int colOffset = 0; colOffset < columnCount; colOffset++) {
                                int col = dx + colOffset;
                                if (col >= 0 && col < rasterWidth) {
                                    int pixel = sourceData[rowOffset * sourceWidth + colOffset];
                                    int alpha = alpha(pixel);
                                    if (alpha != 0) {
                                        if (alpha != 0xff) {
                                            pixel = JNRUtils.combine(data[row * rasterWidth + col], pixel);
                                        }
                                        data[row * rasterWidth + col] = pixel;
                                    }
                                }
                            }
                        }
                    }
                }
            });
        }
    }

    /**
      Render from a designated region of a source compositor into a designated region of the raster, composing with
      existing contents. The regions define translation and clipping, the source data is not scaled.

      @param source The compositor that is the source of the pixels.
      @param sx The X origin of the source region.
      @param sy The Y origin of the source region.
      @param dx The X origin of the raster region.
      @param dy The Y origin of the raster region.
      @param dw The width of the region.
      @param dh The height of the region.
    */

    public void composeFrom(@NotNull ReusableCompositor source, int sx, int sy, int dx, int dy, int dw, int dh)
    {
        ensureConfigured();

        if (data != null) {
            composeFrom(source.dataAccess, sx, sy, dx, dy, dw, dh);
        }
    }

    /**
      Render from a designated region of a source raster into a designated region of the raster, composing with existing
      contents. The regions define translation and clipping, the source data is not scaled.

      @param source The source of the pixels.
      @param sx The X origin of the source region.
      @param sy The Y origin of the source region.
      @param dx The X origin of the raster region.
      @param dy The Y origin of the raster region.
      @param dw The width of the region.
      @param dh The height of the region.
    */

    public void composeFrom(@NotNull PixelRaster source, int sx, int sy, int dx, int dy, int dw, int dh)
    {
        ensureConfigured();

        if (data != null) {
            source.provide((sourceData, sourceWidth, sourceHeight) -> {
                if (sourceWidth > 0 && sourceHeight > 0) {
                    isEmpty = false;
                    for (int rowOffset = 0; rowOffset < dh; rowOffset++) {
                        int sourceRow = sy + rowOffset;
                        int row = dy + rowOffset;
                        if (row >= 0 && row < rasterHeight && sourceRow >= 0 && sourceRow < sourceHeight) {
                            for (int colOffset = 0; colOffset < dw; colOffset++) {
                                int sourceColumn = sx + colOffset;
                                int col = dx + colOffset;
                                if (col >= 0 && col < rasterWidth && sourceColumn >= 0 && sourceColumn < sourceWidth) {
                                    int pixel = sourceData[sourceRow * sourceWidth + sourceColumn];
                                    int alpha = alpha(pixel);
                                    if (alpha != 0) {
                                        if (alpha != 0xff) {
                                            pixel = JNRUtils.combine(data[row * rasterWidth + col], pixel);
                                        }
                                        data[row * rasterWidth + col] = pixel;
                                    }
                                }
                            }
                        }
                    }
                }
            });
        }
    }

    /**
      Render a painter extension into the raster, composing with existing contents.
    */

    public void composePainter(@NotNull PainterExtension px, float x, float y)
    {
        BufferedImage im = getImage();  // this method configures the raster buffer and the buffered image

        if (im != null) {
            isEmpty = false;
            Graphics2D g = im.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.scale(scaleFactor, scaleFactor);
            g.translate(x, y);
            float rw = ((float) rasterWidth) / scaleFactor;
            float rh = ((float) rasterHeight) / scaleFactor;
            px.paint(g, rw, rh);
        }
    }

    /**
      A very special case. Allows direct manipulation of the pixels, not just composing.
    */

    public void renderFrom(@NotNull BasicRenderer r)
    {
        ensureConfigured();

        if (data != null) {
            isEmpty = false;
            float w = ((float) rasterWidth) / scaleFactor;
            float h = ((float) rasterHeight) / scaleFactor;
            r.render(data, rasterWidth, rasterHeight, w, h);
        }
    }

    /**
      Blend pixels from a source compostior into the raster.

      @param source The compositor that is the source of the pixels.
      @param op The blending operator.
    */

    public void blendFrom(@NotNull ReusableCompositor source, @NotNull PixelOperator op)
    {
        blendFrom(source, op, 0, 0, rasterWidth, rasterHeight);
    }

    /**
      Blend pixels from a source compositor into a region of the raster. The region defines translation and clipping,
      the source data is not scaled.

      @param source The compositor that is the source of the pixels.
      @param op The blending operator.
      @param dx The X origin of the raster region.
      @param dy The Y origin of the raster region.
      @param dw The width of the raster region.
      @param dh The height of the raster region.
    */

    public void blendFrom(@NotNull ReusableCompositor source, @NotNull PixelOperator op, int dx, int dy, int dw, int dh)
    {
        ensureConfigured();

        if (data != null) {
            blendFrom(source.dataAccess, op, dx, dy, dw, dh);
        }
    }

    /**
      Blend pixels from a specified source into a region of the raster. The region defines translation and clipping, the
      source data is not scaled.

      @param source The source of the pixels.
      @param op The blending operator.
      @param dx The X origin of the raster region.
      @param dy The Y origin of the raster region.
      @param dw The width of the raster region.
      @param dh The height of the raster region.
    */

    public void blendFrom(@NotNull PixelRaster source, @NotNull PixelOperator op, int dx, int dy, int dw, int dh)
    {
        ensureConfigured();

        if (data != null) {
            source.provide((sourceData, sourceWidth, sourceHeight) -> {
                if (sourceWidth > 0 && sourceHeight > 0) {
                    for (int rowOffset = 0; rowOffset < dh; rowOffset++) {
                        int row = dy + rowOffset;
                        if (row >= 0 && row < rasterHeight) {
                            for (int colOffset = 0; colOffset < dw; colOffset++) {
                                int col = dx + colOffset;
                                if (col >= 0 && col < rasterWidth) {
                                    int destinationIndex = row * rasterWidth + col;
                                    int sourcePixel = sourceData[rowOffset * sourceWidth + colOffset];
                                    int destinationPixel = data[destinationIndex];
                                    int pixel = op.combine(destinationPixel, sourcePixel);
                                    int alpha = alpha(pixel);
                                    if (alpha != 0) {
                                        data[destinationIndex] = pixel;
                                        isEmpty = false;
                                    }
                                }
                            }
                        }
                    }
                }
            });
        }
    }

    /**
      Erase pixels in the existing contents.
    */

    public void erase(int dx, int dy, int dw, int dh)
    {
        if (dw > 0 && dh > 0) {
            ensureConfigured();

            if (data != null) {
                for (int rowOffset = 0; rowOffset < dh; rowOffset++) {
                    int row = dy + rowOffset;
                    if (row >= 0 && row < rasterHeight) {
                        for (int colOffset = 0; colOffset < dw; colOffset++) {
                            int col = dx + colOffset;
                            if (col >= 0 && col < rasterWidth) {
                                data[row * rasterWidth + col] = 0;
                            }
                        }
                    }
                }
            }
        }
    }

    /**
      Return the raster as an image. The image shares the raster buffer with this compositor. The compositor should not
      be reused until the image is no longer in use.

      @return the image, or null if the raster has zero size.
    */

    public @Nullable BufferedImage getImage()
    {
        ensureConfigured();

        if (b == null && data != null) {
            b = BasicImageSupport.createImage(data, rasterWidth, rasterHeight);
        }

        return b;
    }

    /**
      Draw the raster to the specified graphics context.
      @param g The graphics context.
    */

    public void paint(@NotNull Graphics2D g)
    {
        BufferedImage im = getImage();

        if (im != null) {
            g.drawImage(im, null, null);
        }
    }
}
