/*
 * Copyright (c) 2011, 2012, Oracle and/or its affiliates. All rights reserved.
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

package org.violetlib.jnr.impl.jrs;

public class JRSUIStateFactory {
    public static JRSUIState getSliderTrack() {
        return new JRSUIState(JRSUIConstants.Widget.SLIDER.apply(JRSUIConstants.NoIndicator.YES.apply(0)));
    }

    public static JRSUIState getSliderThumb() {
        return new JRSUIState(JRSUIConstants.Widget.SLIDER_THUMB.apply(0));
    }

    public static JRSUIState getSpinnerArrows() {
        return new JRSUIState(JRSUIConstants.Widget.BUTTON_LITTLE_ARROWS.apply(0));
    }

    public static JRSUIState getSplitPaneDivider() {
        return new JRSUIState(JRSUIConstants.Widget.DIVIDER_SPLITTER.apply(0));
    }

    public static JRSUIState getTab() {
        return new JRSUIState(JRSUIConstants.Widget.TAB.apply(JRSUIConstants.SegmentTrailingSeparator.YES.apply(0)));
    }

    public static JRSUIState.AnimationFrameState getDisclosureTriangle() {
        return new JRSUIState.AnimationFrameState(JRSUIConstants.Widget.DISCLOSURE_TRIANGLE.apply(0), 0);
    }

    public static JRSUIState.ScrollBarState getScrollBar() {
        return new JRSUIState.ScrollBarState(JRSUIConstants.Widget.SCROLL_BAR.apply(0), 0, 0, 0);
    }

    public static JRSUIState.TitleBarHeightState getTitleBar() {
        return new JRSUIState.TitleBarHeightState(JRSUIConstants.Widget.WINDOW_FRAME.apply(0), 0);
    }

    public static JRSUIState.ValueState getProgressBar() {
        return new JRSUIState.ValueState(0, 0);
    }

    public static JRSUIState.ValueState getLabeledButton() {
        return new JRSUIState.ValueState(0, 0);
    }
}
