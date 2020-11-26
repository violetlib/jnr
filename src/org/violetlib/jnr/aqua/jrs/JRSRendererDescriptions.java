/*
 * Copyright (c) 2015-2020 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.aqua.jrs;

import org.violetlib.jnr.aqua.AquaUIPainter;
import org.violetlib.jnr.aqua.SegmentedButtonConfiguration;
import org.violetlib.jnr.aqua.coreui.CoreUIRendererDescriptions;
import org.violetlib.jnr.aqua.impl.NativeSupport;
import org.violetlib.jnr.impl.BasicRendererDescription;
import org.violetlib.jnr.impl.JNRPlatformUtils;
import org.violetlib.jnr.impl.RendererDescription;

import org.jetbrains.annotations.*;

import static org.violetlib.jnr.impl.JNRUtils.*;

/**
  Renderer descriptions for rendering on OS X 10.10 and later using the JDK JRS classes.
*/

public class JRSRendererDescriptions
  extends CoreUIRendererDescriptions
{
    @Override
    public @NotNull RendererDescription getSegmentedButtonRendererDescription(@NotNull SegmentedButtonConfiguration g)
    {
        int platformVersion = JNRPlatformUtils.getPlatformVersion();
        boolean v2 = platformVersion >= 101100;

        AquaUIPainter.SegmentedButtonWidget w = g.getWidget();
        AquaUIPainter.Size sz = g.getSize();
        AquaUIPainter.Position position = g.getPosition();

        if (w == AquaUIPainter.SegmentedButtonWidget.BUTTON_SEGMENTED_TEXTURED_SEPARATED
              || w == AquaUIPainter.SegmentedButtonWidget.BUTTON_SEGMENTED_TEXTURED_SEPARATED_TOOLBAR
              || w == AquaUIPainter.SegmentedButtonWidget.BUTTON_SEGMENTED_TEXTURED_SEPARATED_TOOLBAR_ICONS) {
            // an attempted workaround, must coordinate with renderer
            g = g.withWidget(AquaUIPainter.SegmentedButtonWidget.BUTTON_SEGMENTED_TEXTURED);
        } else if (w == AquaUIPainter.SegmentedButtonWidget.BUTTON_SEGMENTED_SEPARATED) {
            // an attempted workaround, must coordinate with renderer
            g = g.withWidget(AquaUIPainter.SegmentedButtonWidget.BUTTON_SEGMENTED);
        }

        RendererDescription rd = super.getSegmentedButtonRendererDescription(g);

        try {

            switch (w)
            {
//                case BUTTON_SEGMENTED:
//                {
//                    if (sz == AquaUIPainter.Size.MINI) {
//                        rd = new BasicRendererDescription(0, 0, position == AquaUIPainter.Position.MIDDLE ? 1 : 0, 4);
//                    }
//                    float yOffset = size2D(sz, -1, -1.49f, v2 ? -2 : 0);
//                    float xOffset = compress ? size2D(sz, -2.49f, -2.49f, -1.49f) : NO_CHANGE;
//                    float widthAdjust = compress ? size2D(sz, 5, 5, 3) : NO_CHANGE;
//                    return changeRendererDescription(rd, xOffset, yOffset, widthAdjust, NO_CHANGE);
//                }

                case BUTTON_SEGMENTED_SEPARATED:  // supported by workaround
                {
                    float x;
                    float width;
                    if (sz == AquaUIPainter.Size.REGULAR || sz == AquaUIPainter.Size.SMALL) {
                        switch (position) {
                            case FIRST: width = 5; x = -2; break;
                            case MIDDLE: width = 5.49f; x = -2.49f; break;
                            case LAST: width = 4.49f; x = -2.49f; break;
                            default: width = 4; x = -2;
                        }
                    } else {
                        switch (position) {
                            case FIRST: width = 3; x = -1; break;
                            case MIDDLE: width = 3.49f; x = -1.49f; break;
                            case LAST: width = 2.49f; x = -1.49f; break;
                            default: width = 2; x = -1;
                        }
                    }

                    return changeRendererDescription(rd, x, NO_CHANGE, width, NO_CHANGE);
                }

                case BUTTON_SEGMENTED_INSET:
                {
                    float y = size2D(sz, -2, -2.51f, -3);
                    return changeRendererDescription(rd, NO_CHANGE, y, NO_CHANGE, NO_CHANGE);
                }

                case BUTTON_SEGMENTED_SCURVE:
                case BUTTON_SEGMENTED_TEXTURED:
                {
                    float y = v2 ? size2D(sz, -1.49f, -2, -2) : size2D(sz, -1, -2, -2);
                    return fix(rd, g, y);
                }

                case BUTTON_SEGMENTED_TEXTURED_TOOLBAR:    // supported by workaround
                case BUTTON_SEGMENTED_TEXTURED_TOOLBAR_ICONS:
                {
                    float y = v2 ? size2D(sz, -2.49f, -3, -3) : size2D(sz, -1, -2, -2);
                    return fix(rd, g, y);
                }

                case BUTTON_SEGMENTED_TOOLBAR:    // not supported
                {
                    float y = v2 ? size2D(sz, 0, 0, 0) : size2D(sz, -1, -2, -2);
                    return fix(rd, g, y);
                }
            }
        } catch (UnsupportedOperationException ex) {
            NativeSupport.log("Unable to adjust segmented button renderer description for " + g);
        }

        return rd;
    }

    private @NotNull RendererDescription fix(@NotNull RendererDescription rd,
                                             @NotNull SegmentedButtonConfiguration g, float y)
    {
        AquaUIPainter.Size sz = g.getSize();
        AquaUIPainter.Position position = g.getPosition();
        if (sz == AquaUIPainter.Size.MINI) {
            rd = new BasicRendererDescription(0, 0, position == AquaUIPainter.Position.MIDDLE ? 1 : 0, 4);
        }
        return changeRendererDescription(rd, NO_CHANGE, y, NO_CHANGE, NO_CHANGE);
    }
}
