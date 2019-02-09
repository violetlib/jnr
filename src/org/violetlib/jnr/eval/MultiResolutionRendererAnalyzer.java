/*
 * Copyright (c) 2015 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.eval;

import org.violetlib.jnr.impl.BasicRendererDescription;
import org.violetlib.jnr.impl.MultiResolutionRendererDescription;
import org.violetlib.jnr.impl.Renderer;
import org.violetlib.jnr.impl.RendererDescription;

import org.jetbrains.annotations.*;

/**
  Examine the output of a render at multiple scale factors and combine the results.
*/

public class MultiResolutionRendererAnalyzer
{
    private final @Nullable RendererAnalysisResults results1;
    private final @Nullable RendererAnalysisResults results2;
    private float fixedWidth;
    private float fixedHeight;
    private @Nullable RendererDescription rd;

    public MultiResolutionRendererAnalyzer(@NotNull Renderer r, boolean forceVertical)
    {
        this(r, forceVertical ? BasicRendererAnalyzer.DEFAULT_HEIGHT : BasicRendererAnalyzer.DEFAULT_WIDTH,
          forceVertical ? BasicRendererAnalyzer.DEFAULT_WIDTH : BasicRendererAnalyzer.DEFAULT_HEIGHT);
    }

    public MultiResolutionRendererAnalyzer(@NotNull Renderer r, int testWidth, int testHeight)
    {
        results1 = new BasicRendererAnalyzer(r, 1, testWidth, testHeight).getResults();
        results2 = new BasicRendererAnalyzer(r, 2, testWidth, testHeight).getResults();
        combineResults(results1, results2);
    }

    public @Nullable RendererAnalysisResults getResults1()
    {
        return results1;
    }

    public @Nullable RendererAnalysisResults getResults2()
    {
        return results2;
    }

    public float getFixedWidth()
    {
        return fixedWidth;
    }

    public float getFixedHeight()
    {
        return fixedHeight;
    }

    public @Nullable RendererDescription getRendererDescription()
    {
        return rd;
    }

    protected void combineResults(@Nullable RendererAnalysisResults r1, @Nullable RendererAnalysisResults r2)
    {
        if (r1 == null || r2 == null) {
            return;
        }

        fixedWidth = 0;
        fixedHeight = 0;

        if (r1.getFixedWidth() > 0 && r2.getFixedWidth() > 0) {
            fixedWidth = (int) Math.ceil(Math.max(r1.getFixedWidth(), r2.getFixedWidth()));
        }

        if (r1.getFixedHeight() > 0 && r2.getFixedHeight() > 0) {
            fixedHeight = (int) Math.ceil(Math.max(r1.getFixedHeight(), r2.getFixedHeight()));
        }

        BasicRendererDescription rd1 = createRendererDescription(r1);
        BasicRendererDescription rd2 = createRendererDescription(r2);
        if (rd1.equals(rd2)) {
            rd = rd1;
        } else {
            rd = new MultiResolutionRendererDescription(rd1, rd2);
        }
    }

    protected @NotNull BasicRendererDescription createRendererDescription(@NotNull RendererAnalysisResults r)
    {
        int wa = (int) Math.ceil(r.getWidthAdjustment());
        int ha = (int) Math.ceil(r.getHeightAdjustment());
        return new BasicRendererDescription(r.getXOffset(), r.getYOffset(), wa, ha);
    }
}
