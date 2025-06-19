/*
 * Copyright (c) 2025 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.aqua.impl;

import org.jetbrains.annotations.NotNull;
import org.violetlib.jnr.LayoutInfo;
import org.violetlib.jnr.aqua.AquaUIPainter;
import org.violetlib.jnr.aqua.SegmentedButtonLayoutConfiguration;
import org.violetlib.jnr.impl.BasicLayoutInfo;

import static org.violetlib.jnr.impl.JNRUtils.size;

/**
  Layout info for macOS 15 widgets.
*/

public class LayoutInfo_15
  extends LayoutInfo_12
{
    @Override
    protected @NotNull LayoutInfo getSegmentedButtonLayoutInfo(@NotNull SegmentedButtonLayoutConfiguration g)
    {
        AquaUIPainter.SegmentedButtonWidget bw = g.getWidget();
        AquaUIPainter.Size sz = g.getSize();

        switch (bw) {
            case BUTTON_TAB:
            case BUTTON_SEGMENTED:
            case BUTTON_SEGMENTED_SLIDER:
            case BUTTON_SEGMENTED_SEPARATED:
            case BUTTON_SEGMENTED_SLIDER_TOOLBAR:
            case BUTTON_SEGMENTED_SLIDER_TOOLBAR_ICONS:
                return BasicLayoutInfo.createFixedHeight(size(sz, 30, 22, 18, 15));

            case BUTTON_SEGMENTED_INSET:
                return BasicLayoutInfo.createFixedHeight(size(sz, 18, 16, 14));

            case BUTTON_SEGMENTED_SMALL_SQUARE:
                return BasicLayoutInfo.createFixedHeight(size(sz, 21, 21, 19, 17));

            case BUTTON_SEGMENTED_TEXTURED:
            case BUTTON_SEGMENTED_TEXTURED_SEPARATED:
            case BUTTON_SEGMENTED_TEXTURED_TOOLBAR:
            case BUTTON_SEGMENTED_TEXTURED_TOOLBAR_ICONS:
            case BUTTON_SEGMENTED_TEXTURED_SEPARATED_TOOLBAR:
            case BUTTON_SEGMENTED_TEXTURED_SEPARATED_TOOLBAR_ICONS:
            case BUTTON_SEGMENTED_SCURVE:
                return BasicLayoutInfo.createFixedHeight(size(sz, 20, 20, 16, 13));

            case BUTTON_SEGMENTED_TOOLBAR:
                if (g.getPosition() == AquaUIPainter.Position.ONLY) {
                    // This is probably a bug in AppKit
                    return BasicLayoutInfo.createFixedHeight(size(sz, 20, 20, 16, 13));
                }
                return BasicLayoutInfo.createFixedHeight(size(sz, 30, 22, 18, 15));

            default:
                throw new UnsupportedOperationException();
        }
    }
}
