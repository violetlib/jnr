/*
 * Copyright (c) 2015-2020 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.aqua.coreui;

import org.violetlib.jnr.aqua.ButtonConfiguration;
import org.violetlib.jnr.aqua.ComboBoxConfiguration;
import org.violetlib.jnr.aqua.GroupBoxConfiguration;
import org.violetlib.jnr.aqua.PopupButtonConfiguration;
import org.violetlib.jnr.aqua.ScrollBarConfiguration;
import org.violetlib.jnr.aqua.SegmentedButtonConfiguration;
import org.violetlib.jnr.aqua.SliderConfiguration;
import org.violetlib.jnr.aqua.SplitPaneDividerConfiguration;
import org.violetlib.jnr.aqua.TableColumnHeaderConfiguration;
import org.violetlib.jnr.aqua.impl.CircularSliderPainterExtension;
import org.violetlib.jnr.aqua.impl.LegacyScrollBarPainterExtension;
import org.violetlib.jnr.aqua.impl.LinearSliderPainterExtension;
import org.violetlib.jnr.aqua.impl.OverlayScrollBarPainterExtension;
import org.violetlib.jnr.aqua.impl.PopUpArrowPainter;
import org.violetlib.jnr.aqua.impl.PullDownArrowPainter;
import org.violetlib.jnr.aqua.impl.RoundToolbarButtonPainterExtension;
import org.violetlib.jnr.aqua.impl.TableColumnHeaderCellPainterExtension;
import org.violetlib.jnr.aqua.impl.ThinSplitPaneDividerPainterExtension;
import org.violetlib.jnr.impl.AdjustDarkToolbarButtonRenderer;
import org.violetlib.jnr.impl.BasicRenderer;
import org.violetlib.jnr.impl.DarkGroupBoxRenderer;
import org.violetlib.jnr.impl.JNRPlatformUtils;
import org.violetlib.jnr.impl.PainterExtension;
import org.violetlib.jnr.impl.Renderer;
import org.violetlib.jnr.impl.ReusableCompositor;

import org.jetbrains.annotations.*;

import static org.violetlib.jnr.aqua.AquaUIPainter.PopupButtonWidget.*;
import static org.violetlib.jnr.aqua.AquaUIPainter.SegmentedButtonWidget.*;

/**
  This class augments the Core UI native painting code to work around its deficiencies.
*/

public class AugmentedCoreUIPainter
  extends CoreUIPainter
{
    /**
      Create a painter that uses Core UI rendering by way of the Java Runtime Support framework, supplemented with Java
      rendering.
    */

    public AugmentedCoreUIPainter()
    {
    }

    /**
      Create a painter that uses Core UI rendering, supplemented with Java rendering.

      @param useJRS If true, the Java Runtime Support framework is used to access Core UI rendering. If false, a private
      method is used to access Core UI rendering.
    */

    public AugmentedCoreUIPainter(boolean useJRS)
    {
        super(useJRS);
    }

    @Override
    public @NotNull AugmentedCoreUIPainter copy()
    {
        return new AugmentedCoreUIPainter(useJRS);
    }

    @Override
    protected @NotNull Renderer getGroupBoxRenderer(@NotNull GroupBoxConfiguration g)
    {
        if (appearance != null && appearance.isDark()) {
            // workaround for CoreUI painting light mode group box even in dark mode
            // CoreUI does not produce a mask
            // the light mode rendering works as a mask — all of the non-transparent pixels are white
            Renderer r = super.getGroupBoxRenderer(g);
            BasicRenderer br = r.getBasicRenderer();
            assert br != null;
            DarkGroupBoxRenderer rr = new DarkGroupBoxRenderer(br, appearance);
            return Renderer.create(rr, r.getRendererDescription());
        }

        return super.getGroupBoxRenderer(g);
    }

    @Override
    protected @NotNull Renderer getButtonRenderer(@NotNull ButtonConfiguration g)
    {
        Renderer r = super.getButtonRenderer(g);
        if (g.getButtonWidget() == ButtonWidget.BUTTON_COLOR_WELL) {
            return new ColorWellRenderer(g, r);
        }
        if (g.getButtonWidget() == ButtonWidget.BUTTON_ROUND_TOOLBAR) {
            PainterExtension px = new RoundToolbarButtonPainterExtension(g, appearance);
            return Renderer.create(px);
        }
        return r;
    }

    @Override
    protected @NotNull Renderer getComboBoxButtonRenderer(@NotNull ComboBoxConfiguration g)
    {
        Renderer r = super.getComboBoxButtonRenderer(g);

        // Workaround for incorrect colors in dark mode for buttons on the toolbar. Not sure when this was needed,
        // but it is not needed in latest release of 10.14+.

        if (false) {
            int version = JNRPlatformUtils.getPlatformVersion();
            if (version < 1015) {
                if (appearance != null && appearance.isDark()) {
                    ComboBoxWidget w = g.getWidget();
                    if (w == ComboBoxWidget.BUTTON_COMBO_BOX_TEXTURED_TOOLBAR) {
                        BasicRenderer br = r.getBasicRenderer();
                        assert br != null;
                        AdjustDarkToolbarButtonRenderer rr = new AdjustDarkToolbarButtonRenderer(br);
                        r = Renderer.create(rr, r.getRendererDescription());
                    }
                }
            }
        }

        return r;
    }

    @Override
    public @Nullable Renderer getBasicPopupButtonRenderer(@NotNull PopupButtonConfiguration g)
    {
        Renderer r = super.getBasicPopupButtonRenderer(g);

        // Workaround for incorrect colors in dark mode for buttons on the toolbar. Not sure when this was needed,
        // but it is not needed in latest release of 10.14+.

        if (false) {
            int version = JNRPlatformUtils.getPlatformVersion();
            if (version < 1015) {
                if (r != null && appearance != null && appearance.isDark()) {
                    PopupButtonWidget w = g.getPopupButtonWidget();
                    if (w == BUTTON_POP_DOWN_TEXTURED_TOOLBAR || w == BUTTON_POP_UP_TEXTURED_TOOLBAR) {
                        BasicRenderer br = r.getBasicRenderer();
                        assert br != null;
                        AdjustDarkToolbarButtonRenderer rr = new AdjustDarkToolbarButtonRenderer(br);
                        r = Renderer.create(rr, r.getRendererDescription());
                    }
                }
            }
        }

        return r;
    }

    @Override
    protected @NotNull Renderer getSegmentedButtonRenderer(@NotNull SegmentedButtonConfiguration g)
    {
        Renderer r = super.getSegmentedButtonRenderer(g);

        // Workaround for incorrect colors in dark mode for buttons on the toolbar. Not sure when this was needed,
        // but it is not needed in latest release of 10.14+.

        if (false) {
            int version = JNRPlatformUtils.getPlatformVersion();
            if (version < 1015) {
                if (appearance != null && appearance.isDark()) {
                    SegmentedButtonWidget w = g.getWidget();
                    if (w.isTextured() && w.isToolbar()) {
                        BasicRenderer br = r.getBasicRenderer();
                        assert br != null;
                        AdjustDarkToolbarButtonRenderer rr = new AdjustDarkToolbarButtonRenderer(br);
                        r = Renderer.create(rr, r.getRendererDescription());
                    }
                }
            }
        }

        if (isCustomSegmentedButtonRendererNeeded(g)) {
            return createCustomSegmentedButtonRenderer(g, r);
        } else {
            return r;
        }
    }

    /**
      Identify the possible need for adjustments to paint proper dividers in a segmented button. The response is
      conservative. It implies an adjustment may be needed, but in actual practice it might not. The issue is that the
      display scale factor is not known at this time.
    */

    protected boolean isCustomSegmentedButtonRendererNeeded(@NotNull SegmentedButtonConfiguration g)
    {
        if (g.getPosition() == Position.ONLY) {
            return false;
        }

        return g.getLeftDividerState() != SegmentedButtonConfiguration.DividerState.NONE
                 || g.getRightDividerState() != SegmentedButtonConfiguration.DividerState.NONE;
    }

    /**
      This class defines the possible adjustments needed for a segmented button. Adjustments are needed only when
      dividers are requested. The adjustments involve painting a missing divider or extending a divider that is not wide
      enough.
    */

    protected static class SegmentedButtonAdjustment
    {
        // These values are in pixels, not points.
        public final int leftDividerActual;
        public final int leftDividerRequested;
        public final int rightDividerActual;
        public final int rightDividerRequested;

        public SegmentedButtonAdjustment(int leftDividerActual,
                                         int leftDividerRequested,
                                         int rightDividerActual,
                                         int rightDividerRequested)
        {
            this.leftDividerActual = leftDividerActual;
            this.leftDividerRequested = leftDividerRequested;
            this.rightDividerActual = rightDividerActual;
            this.rightDividerRequested = rightDividerRequested;
        }

        public SegmentedButtonAdjustment(int left, int right)
        {
            this.leftDividerActual = left;
            this.rightDividerActual = right;
            this.leftDividerRequested = 0;
            this.rightDividerRequested = 0;
        }
    }

    /**
      Identify any adjustments needed to paint the proper dividers.
      @param g
      @return a description of the needed adjustments, or null if none.
    */

    protected @Nullable SegmentedButtonAdjustment getSegmentedButtonAdjustment(
      @NotNull SegmentedButtonConfiguration g, int scaleFactor)
    {
        // The CoreUI rendering of segmented button cells assumes a style of use that is different than Java.
        // Here we identify the actual rendering of dividers and compare with the desired rendering.

        if (g.getPosition() == Position.ONLY) {
            return null;
        }

        SegmentedButtonWidget w = g.getWidget();
        if (w.isSeparated() && scaleFactor == 2) {
            return null;
        }

        if (scaleFactor > 2) {
            // unsupported
            return null;
        }

        int leftDividerRequested = 0;
        int rightDividerRequested = 0;

        if (g.getLeftDividerState() != SegmentedButtonConfiguration.DividerState.NONE) {
            leftDividerRequested = scaleFactor;
        }

        if (g.getRightDividerState() != SegmentedButtonConfiguration.DividerState.NONE) {
            rightDividerRequested = scaleFactor;
        }

        SegmentedButtonAdjustment basic = getSegmentedDividerAvailable(g, scaleFactor);
        int leftDividerActual = leftDividerRequested > 0 ? basic.leftDividerActual : 0;
        int rightDividerActual = rightDividerRequested > 0 ? basic.rightDividerActual : 0;

        return new SegmentedButtonAdjustment(leftDividerActual, leftDividerRequested,
          rightDividerActual, rightDividerRequested);
    }

    private @NotNull SegmentedButtonAdjustment getSegmentedDividerAvailable(
      @NotNull SegmentedButtonConfiguration g, int scaleFactor)
    {
        // Supported only for configurations that might need adjustment, used internally.

        SegmentedButtonWidget w = g.getWidget();

        int leftDividerActual;
        int rightDividerActual;

        int version = getSegmentedButtonRenderingVersion();

        if (scaleFactor == 1) {
            if (version == SEGMENTED_10_10 || version == SEGMENTED_10_11 || version == SEGMENTED_10_13_OLD) {
                if (w == BUTTON_SEGMENTED_SMALL_SQUARE) {
                    leftDividerActual = 1;
                    rightDividerActual = 1;
                } else {
                    leftDividerActual = 0;
                    rightDividerActual = 1;
                }
            } else {
                leftDividerActual = 0;
                rightDividerActual = 1;
            }
        } else {
            if (version == SEGMENTED_10_13_OLD || version == SEGMENTED_10_13) {
                if (w == BUTTON_SEGMENTED_SMALL_SQUARE) {
                    leftDividerActual = 2;
                    rightDividerActual = 2;
                } else if (w == BUTTON_SEGMENTED_TEXTURED || w == BUTTON_SEGMENTED_SCURVE) {
                    leftDividerActual = 2;
                    rightDividerActual = 0;
                } else {
                    leftDividerActual = 1;
                    rightDividerActual = 1;
                }
            } else {
                leftDividerActual = 1;
                rightDividerActual = 1;
            }
        }

        return new SegmentedButtonAdjustment(leftDividerActual, 0, rightDividerActual, 0);
    }

    protected @NotNull Renderer createCustomSegmentedButtonRenderer(@NotNull SegmentedButtonConfiguration g,
                                                                    @NotNull Renderer r)
    {
        return new MySegmentedButtonRenderer(g, r);
    }

    protected class MySegmentedButtonRenderer
      extends Renderer
    {
        private final @NotNull SegmentedButtonConfiguration g;
        private final @NotNull Renderer r;

        public MySegmentedButtonRenderer(@NotNull SegmentedButtonConfiguration g, @NotNull Renderer r)
        {
            this.g = g;
            this.r = r;
        }

        @Override
        public void composeTo(@NotNull ReusableCompositor compositor)
        {
            int scaleFactor = compositor.getScaleFactor();
            SegmentedButtonAdjustment adjustment = getSegmentedButtonAdjustment(g, scaleFactor);
            if (adjustment == null) {
                r.composeTo(compositor);
            } else {
                int w = compositor.getRasterWidth();
                int h = compositor.getRasterHeight();

                // The left and right insets define the region that can be painted directly using the basic renderer
                int leftInset = 0;
                int rightInset = 0;

                // Capture the basic rendering in a reusable compositor so that it can be used as a source
                ReusableCompositor basicSource = compositor.createSimilar();
                r.composeTo(basicSource);

                if (adjustment.leftDividerRequested > adjustment.leftDividerActual) {
                    leftInset = adjustment.leftDividerRequested;
                }

                if (adjustment.rightDividerRequested > adjustment.rightDividerActual) {
                    rightInset = adjustment.rightDividerRequested;
                }

                // Paint the parts of the basic rendering that are completely valid, the interior and the dividers that
                // are already of the requested width.
                compositor.composeFrom(basicSource, leftInset, 0, leftInset, 0, w-leftInset-rightInset, h);

                SegmentedRendering flipped = null;

                if (leftInset > 0) {
                    int columnsNeeded = adjustment.leftDividerRequested;
                    ReusableCompositor dividerSource = basicSource;
                    int sx = 0;
                    int availableColumns = adjustment.leftDividerActual;

                    if (availableColumns == 0) {
                        // Created a flipped rendering and copy from it
                        flipped = createFlippedRendering(g, h, scaleFactor);
                        dividerSource = flipped.rendering;
                        availableColumns = flipped.dividers.leftDividerActual;
                    }

                    if (availableColumns > 0) {
                        int x = 0;
                        while (columnsNeeded > 0) {
                            int columns = Math.min(columnsNeeded, availableColumns);
                            compositor.composeFrom(dividerSource, sx, 0, x, 0, columns, h);
                            columnsNeeded -= columns;
                            x += columns;
                        }
                    } else {
                        System.err.println("Unsupported left divider from right:" + g);
                    }
                }

                if (rightInset > 0) {
                    int columnsNeeded = adjustment.rightDividerRequested;
                    ReusableCompositor dividerSource = basicSource;
                    int sx = w - adjustment.rightDividerActual;
                    int availableColumns = adjustment.rightDividerActual;

                    if (availableColumns == 0) {
                        // Created a flipped rendering and copy from it
                        if (flipped == null) {
                            flipped = createFlippedRendering(g, h, scaleFactor);
                        }
                        dividerSource = flipped.rendering;
                        availableColumns = flipped.dividers.rightDividerActual;
                        sx = dividerSource.getRasterWidth() - availableColumns;
                    }

                    if (availableColumns > 0) {
                        int x = w - adjustment.rightDividerRequested;
                        while (columnsNeeded > 0) {
                            int columns = Math.min(columnsNeeded, availableColumns);
                            compositor.composeFrom(dividerSource, sx, 0, x, 0, columns, h);
                            columnsNeeded -= columns;
                            x += columns;
                        }
                    } else {
                        System.err.println("Unsupported right divider from left: " + g);
                    }
                }
            }
        }
    }

    protected static class SegmentedRendering
    {
        public final @NotNull ReusableCompositor rendering;
        public final @NotNull SegmentedButtonAdjustment dividers;

        public SegmentedRendering(@NotNull ReusableCompositor rendering, @NotNull SegmentedButtonAdjustment dividers)
        {
            this.rendering = rendering;
            this.dividers = dividers;
        }
    }

    /**
      Create a horizontally flipped rendering of a segmented button as a source of unavailable dividers.
      Flipping handles the 1x separated case, where the rendering is not symmetric.
    */

    protected @NotNull SegmentedRendering createFlippedRendering(@NotNull SegmentedButtonConfiguration sg,
                                                                 int height,
                                                                 int scaleFactor)
    {
        SegmentedButtonConfiguration g = new SegmentedButtonConfiguration(sg.getWidget(),
          sg.getSize(),
          sg.getState(),
          sg.isSelected(),
          sg.isFocused(),
          sg.getDirection(),
          Position.MIDDLE,
          sg.getRightDividerState(),
          sg.getLeftDividerState(),
          sg.getTracking());

        int sourceWidth = 100;
        Renderer unconfiguredSource = AugmentedCoreUIPainter.super.getSegmentedButtonRenderer(g);
        ReusableCompositor source = new ReusableCompositor();
        source.reset(sourceWidth, height, scaleFactor);
        unconfiguredSource.composeTo(source);
        ReusableCompositor output = source.createHorizontallyFlippedCopy();
        SegmentedButtonAdjustment sourceDividers = getSegmentedDividerAvailable(g, scaleFactor);
        SegmentedButtonAdjustment dividers = new SegmentedButtonAdjustment(sourceDividers.rightDividerActual, sourceDividers.leftDividerActual);
        return new SegmentedRendering(output, dividers);
    }

    @Override
    protected @NotNull Renderer getSplitPaneDividerRenderer(@NotNull SplitPaneDividerConfiguration g)
    {
        if (g.getWidget() == DividerWidget.THIN_DIVIDER) {
            PainterExtension px = new ThinSplitPaneDividerPainterExtension(g, appearance);
            return Renderer.create(px);
        } else {
            return super.getSplitPaneDividerRenderer(g);
        }
    }

    @Override
    public @NotNull Renderer getTableColumnHeaderRenderer(@NotNull TableColumnHeaderConfiguration g)
    {
        // Do not use the native renderer. Use our simulation instead.

        PainterExtension px = new TableColumnHeaderCellPainterExtension(g, appearance);
        return Renderer.create(px);
    }

    @Override
    protected @NotNull Renderer getSliderRenderer(@NotNull SliderConfiguration g)
    {
        Renderer r = super.getSliderRenderer(g);
        if (g.getWidget() == SliderWidget.SLIDER_CIRCULAR) {
            Renderer pr = Renderer.create(new CircularSliderPainterExtension(g, appearance));
            return Renderer.createCompositeRenderer(r, pr);
        }
        return r;
    }

    @Override
    protected @Nullable Renderer getSliderTickMarkRenderer(@NotNull SliderConfiguration g)
    {
        int style = getSliderRenderingVersion();
        if (style == SLIDER_10_10 && g.isLinear() && g.hasTickMarks()) {
            return Renderer.create(new LinearSliderPainterExtension(uiLayout, g, appearance));
        } else {
            return super.getSliderTickMarkRenderer(g);
        }
    }

    @Override
    protected @NotNull Renderer getScrollBarRenderer(@NotNull ScrollBarConfiguration g)
    {
        int platformVersion = JNRPlatformUtils.getPlatformVersion();
        ScrollBarWidget sw = g.getWidget();

        if (platformVersion < 101400) {
            return super.getScrollBarRenderer(g);
        }

        if (sw == ScrollBarWidget.LEGACY) {
            return Renderer.create(new LegacyScrollBarPainterExtension(uiLayout, g, appearance));
        } else {
            return Renderer.create(new OverlayScrollBarPainterExtension(uiLayout, g, appearance));
        }
    }

    @Override
    public @Nullable Renderer getPopupArrowRenderer(@NotNull PopupButtonConfiguration g)
    {
        Renderer r = super.getPopupArrowRenderer(g);
        if (isArrowNeeded(g)) {
            if (g.isPopUp()) {
                return Renderer.create(new PopUpArrowPainter(g, appearance));
            } else {
                return Renderer.create(new PullDownArrowPainter(g, appearance));
            }
        }
        return r;
    }

    private boolean isArrowNeeded(@NotNull PopupButtonConfiguration g)
    {
        PopupButtonWidget w = g.getPopupButtonWidget();
        // Correct arrow color for recessed style
        return w == BUTTON_POP_UP_RECESSED || w == BUTTON_POP_DOWN_RECESSED;
    }

    @Override
    public @NotNull String toString()
    {
        return "Augmented " + super.toString();
    }
}
