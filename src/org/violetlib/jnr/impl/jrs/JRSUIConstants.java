/*
 * Copyright (c) 2011, 2014, Oracle and/or its affiliates. All rights reserved.
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

import java.lang.annotation.Native;
import java.nio.ByteBuffer;

public final class JRSUIConstants {

    /**
     * There is no way to get width of focus border, so it is hardcoded here.
     * All components, which can be focused should take care about it.
     */
    public static final int FOCUS_SIZE = 4;

    private static native long getPtrForConstant(final int constant);

    static class Key {
        @Native protected static final int _value = 20;
        public static final JRSUIConstants.Key VALUE = new JRSUIConstants.Key(_value);

        @Native protected static final int _thumbProportion = 24;
        public static final JRSUIConstants.Key THUMB_PROPORTION = new JRSUIConstants.Key(_thumbProportion);

        @Native protected static final int _thumbStart = 25;
        public static final JRSUIConstants.Key THUMB_START = new JRSUIConstants.Key(_thumbStart);

        @Native protected static final int _windowTitleBarHeight = 28;
        public static final JRSUIConstants.Key WINDOW_TITLE_BAR_HEIGHT = new JRSUIConstants.Key(_windowTitleBarHeight);

        @Native protected static final int _animationFrame = 23;
        public static final JRSUIConstants.Key ANIMATION_FRAME = new JRSUIConstants.Key(_animationFrame);

        final int constant;
        private long ptr;

        private Key(final int constant) {
            this.constant = constant;
        }

        long getConstantPtr() {
            if (ptr != 0) return ptr;
            ptr = getPtrForConstant(constant);
            if (ptr != 0) return ptr;
            throw new RuntimeException("Constant not implemented in native: " + this);
        }

        private String getConstantName(JRSUIConstants.Key hit) {
            if (hit == VALUE) {
                return "VALUE";
            } else if (hit == THUMB_PROPORTION) {
                return "THUMB_PROPORTION";
            } else if (hit == THUMB_START) {
                return "THUMB_START";
            } else if (hit == WINDOW_TITLE_BAR_HEIGHT) {
                return "WINDOW_TITLE_BAR_HEIGHT";
            } else if (hit == THUMB_START) {
                return "ANIMATION_FRAME";
            }
            return getClass().getSimpleName();
        }

        public String toString() {
            return getConstantName(this) + (ptr == 0 ? "(unlinked)" : "");
        }
    }

    static class DoubleValue {
        @Native protected static final byte TYPE_CODE = 1;

        final double doubleValue;

        DoubleValue(final double doubleValue) {
            this.doubleValue = doubleValue;
        }

        public byte getTypeCode() {
            return TYPE_CODE;
        }

        public void putValueInBuffer(final ByteBuffer buffer) {
            buffer.putDouble(doubleValue);
        }

        public boolean equals(final Object obj) {
            return (obj instanceof JRSUIConstants.DoubleValue) && (((JRSUIConstants.DoubleValue)obj).doubleValue == doubleValue);
        }

        public int hashCode() {
            final long bits = Double.doubleToLongBits(doubleValue);
            return (int)(bits ^ (bits >>> 32));
        }

        public String toString() {
            return Double.toString(doubleValue);
        }
    }


    static class PropertyEncoding {
        final long mask;
        final byte shift;

        PropertyEncoding(final long mask, final byte shift) {
            this.mask = mask;
            this.shift = shift;
        }
    }

    static class Property {
        final JRSUIConstants.PropertyEncoding encoding;
        final long value;
        final byte ordinal;

        Property(final JRSUIConstants.PropertyEncoding encoding, final byte ordinal) {
            this.encoding = encoding;
            this.value = ((long)ordinal) << encoding.shift;
            this.ordinal = ordinal;
        }

        /**
         * Applies this property value to the provided state
         * @param encodedState the incoming JRSUI encoded state
         * @return the composite of the provided JRSUI encoded state and this value
         */
        public long apply(final long encodedState) {
            return (encodedState & ~encoding.mask) | value;
        }

        public String toString() {
            return getClass().getSimpleName();
        }
    }

    public static class Size extends JRSUIConstants.Property
		{
        @Native private static final byte SHIFT = 0;
        @Native private static final byte SIZE = 3;
        @Native private static final long MASK = (long)0x7 << SHIFT;
        private static final JRSUIConstants.PropertyEncoding size = new JRSUIConstants.PropertyEncoding(MASK, SHIFT);

        Size(final byte value) {
            super(size, value);
        }

        @Native private static final byte _mini = 1;
        public static final JRSUIConstants.Size MINI = new JRSUIConstants.Size(_mini);
        @Native private static final byte _small = 2;
        public static final JRSUIConstants.Size SMALL = new JRSUIConstants.Size(_small);
        @Native private static final byte _regular = 3;
        public static final JRSUIConstants.Size REGULAR = new JRSUIConstants.Size(_regular);
        @Native private static final byte _large = 4;
        public static final JRSUIConstants.Size LARGE = new JRSUIConstants.Size(_large);
    }

    public static class State extends JRSUIConstants.Property
		{
        @Native private static final byte SHIFT = JRSUIConstants.Size.SHIFT + JRSUIConstants.Size.SIZE;
        @Native private static final byte SIZE = 4;
        @Native private static final long MASK = (long)0xF << SHIFT;
        private static final JRSUIConstants.PropertyEncoding state = new JRSUIConstants.PropertyEncoding(MASK, SHIFT);

        State(final byte value) {
            super(state, value);
        }

        @Native private static final byte _active = 1;
        public static final JRSUIConstants.State ACTIVE = new JRSUIConstants.State(_active);
        @Native private static final byte _inactive = 2;
        public static final JRSUIConstants.State INACTIVE = new JRSUIConstants.State(_inactive);
        @Native private static final byte _disabled = 3;
        public static final JRSUIConstants.State DISABLED = new JRSUIConstants.State(_disabled);
        @Native private static final byte _pressed = 4;
        public static final JRSUIConstants.State PRESSED = new JRSUIConstants.State(_pressed);
        @Native private static final byte _pulsed = 5;
        public static final JRSUIConstants.State PULSED = new JRSUIConstants.State(_pulsed);
        @Native private static final byte _rollover = 6;
        public static final JRSUIConstants.State ROLLOVER = new JRSUIConstants.State(_rollover);
        @Native private static final byte _drag = 7;
        public static final JRSUIConstants.State DRAG = new JRSUIConstants.State(_drag);
    }

    public static class Direction extends JRSUIConstants.Property
		{
        @Native private static final byte SHIFT = JRSUIConstants.State.SHIFT + JRSUIConstants.State.SIZE;
        @Native private static final byte SIZE = 4;
        @Native private static final long MASK = (long)0xF << SHIFT;
        private static final JRSUIConstants.PropertyEncoding direction = new JRSUIConstants.PropertyEncoding(MASK, SHIFT);

        Direction(final byte value) {
            super(direction, value);
        }

        @Native private static final byte _none = 1;
        public static final JRSUIConstants.Direction NONE = new JRSUIConstants.Direction(_none);
        @Native private static final byte _up = 2;
        public static final JRSUIConstants.Direction UP = new JRSUIConstants.Direction(_up);
        @Native private static final byte _down = 3;
        public static final JRSUIConstants.Direction DOWN = new JRSUIConstants.Direction(_down);
        @Native private static final byte _left = 4;
        public static final JRSUIConstants.Direction LEFT = new JRSUIConstants.Direction(_left);
        @Native private static final byte _right = 5;
        public static final JRSUIConstants.Direction RIGHT = new JRSUIConstants.Direction(_right);
        @Native private static final byte _north = 6;
        public static final JRSUIConstants.Direction NORTH = new JRSUIConstants.Direction(_north);
        @Native private static final byte _south = 7;
        public static final JRSUIConstants.Direction SOUTH = new JRSUIConstants.Direction(_south);
        @Native private static final byte _east = 8;
        public static final JRSUIConstants.Direction EAST = new JRSUIConstants.Direction(_east);
        @Native private static final byte _west = 9;
        public static final JRSUIConstants.Direction WEST = new JRSUIConstants.Direction(_west);
    }

    public static class Orientation extends JRSUIConstants.Property
		{
        @Native private static final byte SHIFT = JRSUIConstants.Direction.SHIFT + JRSUIConstants.Direction.SIZE;
        @Native private static final byte SIZE = 2;
        @Native private static final long MASK = (long)0x3 << SHIFT;
        private static final JRSUIConstants.PropertyEncoding orientation = new JRSUIConstants.PropertyEncoding(MASK, SHIFT);

        Orientation(final byte value) {
            super(orientation, value);
        }

        @Native private static final byte _horizontal = 1;
        public static final JRSUIConstants.Orientation HORIZONTAL = new JRSUIConstants.Orientation(_horizontal);
        @Native private static final byte _vertical = 2;
        public static final JRSUIConstants.Orientation VERTICAL = new JRSUIConstants.Orientation(_vertical);
    }

    public static class AlignmentVertical extends JRSUIConstants.Property
		{
        @Native private static final byte SHIFT = JRSUIConstants.Orientation.SHIFT + JRSUIConstants.Orientation.SIZE;
        @Native private static final byte SIZE = 2;
        @Native private static final long MASK = (long)0x3 << SHIFT;
        private static final JRSUIConstants.PropertyEncoding alignmentVertical = new JRSUIConstants.PropertyEncoding(MASK, SHIFT);

        AlignmentVertical(final byte value){
            super(alignmentVertical, value);
        }

        @Native private static final byte _top = 1;
        public static final JRSUIConstants.AlignmentVertical TOP = new JRSUIConstants.AlignmentVertical(_top);
        @Native private static final byte _center = 2;
        public static final JRSUIConstants.AlignmentVertical CENTER = new JRSUIConstants.AlignmentVertical(_center);
        @Native private static final byte _bottom = 3;
        public static final JRSUIConstants.AlignmentVertical BOTTOM = new JRSUIConstants.AlignmentVertical(_bottom);
    }

    public static class AlignmentHorizontal extends JRSUIConstants.Property
		{
        @Native private static final byte SHIFT = JRSUIConstants.AlignmentVertical.SHIFT + JRSUIConstants.AlignmentVertical.SIZE;
        @Native private static final byte SIZE = 2;
        @Native private static final long MASK = (long)0x3 << SHIFT;
        private static final JRSUIConstants.PropertyEncoding alignmentHorizontal = new JRSUIConstants.PropertyEncoding(MASK, SHIFT);

        AlignmentHorizontal(final byte value){
            super(alignmentHorizontal, value);
        }

        @Native private static final byte _left = 1;
        public static final JRSUIConstants.AlignmentHorizontal LEFT = new JRSUIConstants.AlignmentHorizontal(_left);
        @Native private static final byte _center =  2;
        public static final JRSUIConstants.AlignmentHorizontal CENTER = new JRSUIConstants.AlignmentHorizontal(_center);
        @Native private static final byte _right = 3;
        public static final JRSUIConstants.AlignmentHorizontal RIGHT = new JRSUIConstants.AlignmentHorizontal(_right);
    }

    public static class SegmentPosition extends JRSUIConstants.Property
		{
        @Native private static final byte SHIFT = JRSUIConstants.AlignmentHorizontal.SHIFT + JRSUIConstants.AlignmentHorizontal.SIZE;
        @Native private static final byte SIZE = 3;
        @Native private static final long MASK = (long)0x7 << SHIFT;
        private static final JRSUIConstants.PropertyEncoding segmentPosition = new JRSUIConstants.PropertyEncoding(MASK, SHIFT);

        SegmentPosition(final byte value) {
            super(segmentPosition, value);
        }

        @Native private static final byte _first = 1;
        public static final JRSUIConstants.SegmentPosition FIRST = new JRSUIConstants.SegmentPosition(_first);
        @Native private static final byte _middle = 2;
        public static final JRSUIConstants.SegmentPosition MIDDLE = new JRSUIConstants.SegmentPosition(_middle);
        @Native private static final byte _last = 3;
        public static final JRSUIConstants.SegmentPosition LAST = new JRSUIConstants.SegmentPosition(_last);
        @Native private static final byte _only = 4;
        public static final JRSUIConstants.SegmentPosition ONLY = new JRSUIConstants.SegmentPosition(_only);
    }

    public static class ScrollBarPart extends JRSUIConstants.Property
		{
        @Native private static final byte SHIFT = JRSUIConstants.SegmentPosition.SHIFT + JRSUIConstants.SegmentPosition.SIZE;
        @Native private static final byte SIZE = 4;
        @Native private static final long MASK = (long)0xF << SHIFT;
        private static final JRSUIConstants.PropertyEncoding scrollBarPart = new JRSUIConstants.PropertyEncoding(MASK, SHIFT);

        ScrollBarPart(final byte value) {
            super(scrollBarPart, value);
        }

        @Native private static final byte _none = 1;
        public static final JRSUIConstants.ScrollBarPart NONE = new JRSUIConstants.ScrollBarPart(_none);
        @Native private static final byte _thumb = 2;
        public static final JRSUIConstants.ScrollBarPart THUMB = new JRSUIConstants.ScrollBarPart(_thumb);
        @Native private static final byte _arrowMin = 3;
        public static final JRSUIConstants.ScrollBarPart ARROW_MIN = new JRSUIConstants.ScrollBarPart(_arrowMin);
        @Native private static final byte _arrowMax = 4;
        public static final JRSUIConstants.ScrollBarPart ARROW_MAX = new JRSUIConstants.ScrollBarPart(_arrowMax);
        @Native private static final byte _arrowMaxInside = 5;
        public static final JRSUIConstants.ScrollBarPart ARROW_MAX_INSIDE = new JRSUIConstants.ScrollBarPart(_arrowMaxInside);
        @Native private static final byte _arrowMinInside = 6;
        public static final JRSUIConstants.ScrollBarPart ARROW_MIN_INSIDE = new JRSUIConstants.ScrollBarPart(_arrowMinInside);
        @Native private static final byte _trackMin = 7;
        public static final JRSUIConstants.ScrollBarPart TRACK_MIN = new JRSUIConstants.ScrollBarPart(_trackMin);
        @Native private static final byte _trackMax = 8;
        public static final JRSUIConstants.ScrollBarPart TRACK_MAX = new JRSUIConstants.ScrollBarPart(_trackMax);
    }

    public static class Variant extends JRSUIConstants.Property
		{
        @Native private static final byte SHIFT = JRSUIConstants.ScrollBarPart.SHIFT + JRSUIConstants.ScrollBarPart.SIZE;
        @Native private static final byte SIZE = 4;
        @Native private static final long MASK = (long)0xF << SHIFT;
        private static final JRSUIConstants.PropertyEncoding variant = new JRSUIConstants.PropertyEncoding(MASK, SHIFT);

        Variant(final byte value) {
            super(variant, value);
        }

        @Native private static final byte _menuGlyph = 1;
        public static final JRSUIConstants.Variant MENU_GLYPH = new JRSUIConstants.Variant(_menuGlyph);
        @Native private static final byte _menuPopup = JRSUIConstants.Variant._menuGlyph + 1;
        public static final JRSUIConstants.Variant MENU_POPUP = new JRSUIConstants.Variant(_menuPopup);
        @Native private static final byte _menuPulldown = JRSUIConstants.Variant._menuPopup + 1;
        public static final JRSUIConstants.Variant MENU_PULLDOWN = new JRSUIConstants.Variant(_menuPulldown);
        @Native private static final byte _menuHierarchical = JRSUIConstants.Variant._menuPulldown + 1;
        public static final JRSUIConstants.Variant MENU_HIERARCHICAL = new JRSUIConstants.Variant(_menuHierarchical);

        @Native private static final byte _gradientListBackgroundEven = JRSUIConstants.Variant._menuHierarchical + 1;
        public static final JRSUIConstants.Variant GRADIENT_LIST_BACKGROUND_EVEN = new JRSUIConstants.Variant(_gradientListBackgroundEven);
        @Native private static final byte _gradientListBackgroundOdd = JRSUIConstants.Variant._gradientListBackgroundEven + 1;
        public static final JRSUIConstants.Variant GRADIENT_LIST_BACKGROUND_ODD = new JRSUIConstants.Variant(_gradientListBackgroundOdd);
        @Native private static final byte _gradientSideBar = JRSUIConstants.Variant._gradientListBackgroundOdd + 1;
        public static final JRSUIConstants.Variant GRADIENT_SIDE_BAR = new JRSUIConstants.Variant(_gradientSideBar);
        @Native private static final byte _gradientSideBarSelection = JRSUIConstants.Variant._gradientSideBar + 1;
        public static final JRSUIConstants.Variant GRADIENT_SIDE_BAR_SELECTION = new JRSUIConstants.Variant(_gradientSideBarSelection);
        @Native private static final byte _gradientSideBarFocusedSelection = JRSUIConstants.Variant._gradientSideBarSelection + 1;
        public static final JRSUIConstants.Variant GRADIENT_SIDE_BAR_FOCUSED_SELECTION = new JRSUIConstants.Variant(_gradientSideBarFocusedSelection);
    }

    public static class WindowType extends JRSUIConstants.Property
		{
        @Native private static final byte SHIFT = JRSUIConstants.Variant.SHIFT + JRSUIConstants.Variant.SIZE;
        @Native private static final byte SIZE = 2;
        @Native private static final long MASK = (long)0x3 << SHIFT;
        private static final JRSUIConstants.PropertyEncoding windowType = new JRSUIConstants.PropertyEncoding(MASK, SHIFT);

        WindowType(final byte value){
            super(windowType, value);
        }

        @Native private static final byte _document = 1;
        public static final JRSUIConstants.WindowType DOCUMENT = new JRSUIConstants.WindowType(_document);
        @Native private static final byte _utility = 2;
        public static final JRSUIConstants.WindowType UTILITY = new JRSUIConstants.WindowType(_utility);
        @Native private static final byte _titlelessUtility = 3;
        public static final JRSUIConstants.WindowType TITLELESS_UTILITY = new JRSUIConstants.WindowType(_titlelessUtility);
    }

    public static class Focused extends JRSUIConstants.Property
		{
        @Native private static final byte SHIFT = JRSUIConstants.WindowType.SHIFT + JRSUIConstants.WindowType.SIZE;
        @Native private static final byte SIZE = 1;
        @Native private static final long MASK = (long)0x1 << SHIFT;
        private static final JRSUIConstants.PropertyEncoding focused = new JRSUIConstants.PropertyEncoding(MASK, SHIFT);

        Focused(final byte value) {
            super(focused, value);
        }

        @Native private static final byte _no = 0;
        public static final JRSUIConstants.Focused NO = new JRSUIConstants.Focused(_no);
        @Native private static final byte _yes = 1;
        public static final JRSUIConstants.Focused YES = new JRSUIConstants.Focused(_yes);
    }

    public static class IndicatorOnly extends JRSUIConstants.Property
		{
        @Native private static final byte SHIFT = JRSUIConstants.Focused.SHIFT + JRSUIConstants.Focused.SIZE;
        @Native private static final byte SIZE = 1;
        @Native private static final long MASK = (long)0x1 << SHIFT;
        private static final JRSUIConstants.PropertyEncoding indicatorOnly = new JRSUIConstants.PropertyEncoding(MASK, SHIFT);

        IndicatorOnly(final byte value) {
            super(indicatorOnly, value);
        }

        @Native private static final byte _no = 0;
        public static final JRSUIConstants.IndicatorOnly NO = new JRSUIConstants.IndicatorOnly(_no);
        @Native private static final byte _yes = 1;
        public static final JRSUIConstants.IndicatorOnly YES = new JRSUIConstants.IndicatorOnly(_yes);
    }

    public static class NoIndicator extends JRSUIConstants.Property
		{
        @Native private static final byte SHIFT = JRSUIConstants.IndicatorOnly.SHIFT + JRSUIConstants.IndicatorOnly.SIZE;
        @Native private static final byte SIZE = 1;
        @Native private static final long MASK = (long)0x1 << SHIFT;
        private static final JRSUIConstants.PropertyEncoding noIndicator = new JRSUIConstants.PropertyEncoding(MASK, SHIFT);

        NoIndicator(final byte value) {
            super(noIndicator, value);
        }

        @Native private static final byte _no = 0;
        public static final JRSUIConstants.NoIndicator NO = new JRSUIConstants.NoIndicator(_no);
        @Native private static final byte _yes = 1;
        public static final JRSUIConstants.NoIndicator YES = new JRSUIConstants.NoIndicator(_yes);
    }

    public static class ArrowsOnly extends JRSUIConstants.Property
		{
        @Native private static final byte SHIFT = JRSUIConstants.NoIndicator.SHIFT + JRSUIConstants.NoIndicator.SIZE;
        @Native private static final byte SIZE = 1;
        @Native private static final long MASK = (long)0x1 << SHIFT;
        private static final JRSUIConstants.PropertyEncoding focused = new JRSUIConstants.PropertyEncoding(MASK, SHIFT);

        ArrowsOnly(final byte value) {
            super(focused, value);
        }

        @Native private static final byte _no = 0;
        public static final JRSUIConstants.ArrowsOnly NO = new JRSUIConstants.ArrowsOnly(_no);
        @Native private static final byte _yes = 1;
        public static final JRSUIConstants.ArrowsOnly YES = new JRSUIConstants.ArrowsOnly(_yes);
    }

    public static class FrameOnly extends JRSUIConstants.Property
		{
        @Native private static final byte SHIFT = JRSUIConstants.ArrowsOnly.SHIFT + JRSUIConstants.ArrowsOnly.SIZE;
        @Native private static final byte SIZE = 1;
        @Native private static final long MASK = (long)0x1 << SHIFT;
        private static final JRSUIConstants.PropertyEncoding focused = new JRSUIConstants.PropertyEncoding(MASK, SHIFT);

        FrameOnly(final byte value) {
            super(focused, value);
        }

        @Native private static final byte _no = 0;
        public static final JRSUIConstants.FrameOnly NO = new JRSUIConstants.FrameOnly(_no);
        @Native private static final byte _yes = 1;
        public static final JRSUIConstants.FrameOnly YES = new JRSUIConstants.FrameOnly(_yes);
    }

    public static class SegmentTrailingSeparator extends JRSUIConstants.Property
		{
        @Native private static final byte SHIFT = JRSUIConstants.FrameOnly.SHIFT + JRSUIConstants.FrameOnly.SIZE;
        @Native private static final byte SIZE = 1;
        @Native private static final long MASK = (long)0x1 << SHIFT;
        private static final JRSUIConstants.PropertyEncoding focused = new JRSUIConstants.PropertyEncoding(MASK, SHIFT);

        SegmentTrailingSeparator(final byte value) {
            super(focused, value);
        }

        @Native private static final byte _no = 0;
        public static final JRSUIConstants.SegmentTrailingSeparator NO = new JRSUIConstants.SegmentTrailingSeparator(_no);
        @Native private static final byte _yes = 1;
        public static final JRSUIConstants.SegmentTrailingSeparator YES = new JRSUIConstants.SegmentTrailingSeparator(_yes);
    }

    public static class SegmentLeadingSeparator extends JRSUIConstants.Property
		{
        @Native private static final byte SHIFT = JRSUIConstants.SegmentTrailingSeparator.SHIFT + JRSUIConstants.SegmentTrailingSeparator.SIZE;
        @Native private static final byte SIZE = 1;
        @Native private static final long MASK = (long)0x1 << SHIFT;
        private static final JRSUIConstants.PropertyEncoding leadingSeparator = new JRSUIConstants.PropertyEncoding(MASK, SHIFT);

        SegmentLeadingSeparator(final byte value) {
            super(leadingSeparator, value);
        }

        @Native private static final byte _no = 0;
        public static final JRSUIConstants.SegmentLeadingSeparator NO = new JRSUIConstants.SegmentLeadingSeparator(_no);
        @Native private static final byte _yes = 1;
        public static final JRSUIConstants.SegmentLeadingSeparator YES = new JRSUIConstants.SegmentLeadingSeparator(_yes);
    }

    public static class NothingToScroll extends JRSUIConstants.Property
		{
        @Native private static final byte SHIFT = JRSUIConstants.SegmentLeadingSeparator.SHIFT + JRSUIConstants.SegmentLeadingSeparator.SIZE;
        @Native private static final byte SIZE = 1;
        @Native private static final long MASK = (long)0x1 << SHIFT;
        private static final JRSUIConstants.PropertyEncoding focused = new JRSUIConstants.PropertyEncoding(MASK, SHIFT);

        NothingToScroll(final byte value) {
            super(focused, value);
        }

        @Native private static final byte _no = 0;
        public static final JRSUIConstants.NothingToScroll NO = new JRSUIConstants.NothingToScroll(_no);
        @Native private static final byte _yes = 1;
        public static final JRSUIConstants.NothingToScroll YES = new JRSUIConstants.NothingToScroll(_yes);
    }

    public static class WindowTitleBarSeparator extends JRSUIConstants.Property
		{
        @Native private static final byte SHIFT = JRSUIConstants.NothingToScroll.SHIFT + JRSUIConstants.NothingToScroll.SIZE;
        @Native private static final byte SIZE = 1;
        @Native private static final long MASK = (long)0x1 << SHIFT;
        private static final JRSUIConstants.PropertyEncoding focused = new JRSUIConstants.PropertyEncoding(MASK, SHIFT);

        WindowTitleBarSeparator(final byte value) {
            super(focused, value);
        }

        @Native private static final byte _no = 0;
        public static final JRSUIConstants.WindowTitleBarSeparator NO = new JRSUIConstants.WindowTitleBarSeparator(_no);
        @Native private static final byte _yes = 1;
        public static final JRSUIConstants.WindowTitleBarSeparator YES = new JRSUIConstants.WindowTitleBarSeparator(_yes);
    }

    public static class WindowClipCorners extends JRSUIConstants.Property
		{
        @Native private static final byte SHIFT = JRSUIConstants.WindowTitleBarSeparator.SHIFT + JRSUIConstants.WindowTitleBarSeparator.SIZE;
        @Native private static final byte SIZE = 1;
        @Native private static final long MASK = (long)0x1 << SHIFT;
        private static final JRSUIConstants.PropertyEncoding focused = new JRSUIConstants.PropertyEncoding(MASK, SHIFT);

        WindowClipCorners(final byte value) {
            super(focused, value);
        }

        @Native private static final byte _no = 0;
        public static final JRSUIConstants.WindowClipCorners NO = new JRSUIConstants.WindowClipCorners(_no);
        @Native private static final byte _yes = 1;
        public static final JRSUIConstants.WindowClipCorners YES = new JRSUIConstants.WindowClipCorners(_yes);
    }

    public static class ShowArrows extends JRSUIConstants.Property
		{
        @Native private static final byte SHIFT = JRSUIConstants.WindowClipCorners.SHIFT + JRSUIConstants.WindowClipCorners.SIZE;
        @Native private static final byte SIZE = 1;
        @Native private static final long MASK = (long)0x1 << SHIFT;
        private static final JRSUIConstants.PropertyEncoding showArrows = new JRSUIConstants.PropertyEncoding(MASK, SHIFT);

        ShowArrows(final byte value) {
            super(showArrows, value);
        }

        @Native private static final byte _no = 0;
        public static final JRSUIConstants.ShowArrows NO = new JRSUIConstants.ShowArrows(_no);
        @Native private static final byte _yes = 1;
        public static final JRSUIConstants.ShowArrows YES = new JRSUIConstants.ShowArrows(_yes);
    }

    public static class BooleanValue extends JRSUIConstants.Property
		{
        @Native private static final byte SHIFT = JRSUIConstants.ShowArrows.SHIFT + JRSUIConstants.ShowArrows.SIZE;
        @Native private static final byte SIZE = 1;
        @Native private static final long MASK = (long)0x1 << SHIFT;
        private static final JRSUIConstants.PropertyEncoding booleanValue = new JRSUIConstants.PropertyEncoding(MASK, SHIFT);

        BooleanValue(final byte value) {
            super(booleanValue, value);
        }

        @Native private static final byte _no = 0;
        public static final JRSUIConstants.BooleanValue NO = new JRSUIConstants.BooleanValue(_no);
        @Native private static final byte _yes = 1;
        public static final JRSUIConstants.BooleanValue YES = new JRSUIConstants.BooleanValue(_yes);
    }

    public static class Animating extends JRSUIConstants.Property
		{
        @Native private static final byte SHIFT = JRSUIConstants.BooleanValue.SHIFT + JRSUIConstants.BooleanValue.SIZE;
        @Native private static final byte SIZE = 1;
        @Native private static final long MASK = (long)0x1 << SHIFT;
        private static final JRSUIConstants.PropertyEncoding animating = new JRSUIConstants.PropertyEncoding(MASK, SHIFT);

        Animating(final byte value) {
            super(animating, value);
        }

        @Native private static final byte _no = 0;
        public static final JRSUIConstants.Animating NO = new JRSUIConstants.Animating(_no);
        @Native private static final byte _yes = 1;
        public static final JRSUIConstants.Animating YES = new JRSUIConstants.Animating(_yes);
    }

    public static class Widget extends JRSUIConstants.Property
		{
        @Native private static final byte SHIFT = JRSUIConstants.Animating.SHIFT + JRSUIConstants.Animating.SIZE;
        @Native private static final byte SIZE = 7;
        @Native private static final long MASK = (long)0x7F << SHIFT;
        private static final JRSUIConstants.PropertyEncoding widget = new JRSUIConstants.PropertyEncoding(MASK, SHIFT);

        Widget(final byte constant) {
            super(widget, constant);
        }

        @Native private static final byte _background = 1;
        public static final JRSUIConstants.Widget BACKGROUND = new JRSUIConstants.Widget(_background);

        @Native private static final byte _buttonBevel = _background + 1;
        public static final JRSUIConstants.Widget BUTTON_BEVEL = new JRSUIConstants.Widget(_buttonBevel);
        @Native private static final byte _buttonBevelInset = _buttonBevel + 1;
        public static final JRSUIConstants.Widget BUTTON_BEVEL_INSET = new JRSUIConstants.Widget(_buttonBevelInset);
        @Native private static final byte _buttonBevelRound = _buttonBevelInset + 1;
        public static final JRSUIConstants.Widget BUTTON_BEVEL_ROUND = new JRSUIConstants.Widget(_buttonBevelRound);

        @Native private static final byte _buttonCheckBox = _buttonBevelRound + 1;
        public static final JRSUIConstants.Widget BUTTON_CHECK_BOX = new JRSUIConstants.Widget(_buttonCheckBox);

        @Native private static final byte _buttonComboBox = _buttonCheckBox + 1;
        public static final JRSUIConstants.Widget BUTTON_COMBO_BOX = new JRSUIConstants.Widget(_buttonComboBox);
        @Native private static final byte _buttonComboBoxInset = _buttonComboBox + 1;
        public static final JRSUIConstants.Widget BUTTON_COMBO_BOX_INSET = new JRSUIConstants.Widget(_buttonComboBoxInset); // not hooked up in JRSUIConstants.m

        @Native private static final byte _buttonDisclosure = _buttonComboBoxInset + 1;
        public static final JRSUIConstants.Widget BUTTON_DISCLOSURE = new JRSUIConstants.Widget(_buttonDisclosure);

        @Native private static final byte _buttonListHeader = _buttonDisclosure + 1;
        public static final JRSUIConstants.Widget BUTTON_LIST_HEADER = new JRSUIConstants.Widget(_buttonListHeader);

        @Native private static final byte _buttonLittleArrows = _buttonListHeader + 1;
        public static final JRSUIConstants.Widget BUTTON_LITTLE_ARROWS = new JRSUIConstants.Widget(_buttonLittleArrows);

        @Native private static final byte _buttonPopDown = _buttonLittleArrows + 1;
        public static final JRSUIConstants.Widget BUTTON_POP_DOWN = new JRSUIConstants.Widget(_buttonPopDown);
        @Native private static final byte _buttonPopDownInset = _buttonPopDown + 1;
        public static final JRSUIConstants.Widget BUTTON_POP_DOWN_INSET = new JRSUIConstants.Widget(_buttonPopDownInset);
        @Native private static final byte _buttonPopDownSquare = _buttonPopDownInset + 1;
        public static final JRSUIConstants.Widget BUTTON_POP_DOWN_SQUARE = new JRSUIConstants.Widget(_buttonPopDownSquare);

        @Native private static final byte _buttonPopUp = _buttonPopDownSquare + 1;
        public static final JRSUIConstants.Widget BUTTON_POP_UP = new JRSUIConstants.Widget(_buttonPopUp);
        @Native private static final byte _buttonPopUpInset = _buttonPopUp + 1;
        public static final JRSUIConstants.Widget BUTTON_POP_UP_INSET = new JRSUIConstants.Widget(_buttonPopUpInset);
        @Native private static final byte _buttonPopUpSquare = _buttonPopUpInset + 1;
        public static final JRSUIConstants.Widget BUTTON_POP_UP_SQUARE = new JRSUIConstants.Widget(_buttonPopUpSquare);

        @Native private static final byte _buttonPush = _buttonPopUpSquare + 1;
        public static final JRSUIConstants.Widget BUTTON_PUSH = new JRSUIConstants.Widget(_buttonPush);
        @Native private static final byte _buttonPushScope = _buttonPush + 1;
        public static final JRSUIConstants.Widget BUTTON_PUSH_SCOPE = new JRSUIConstants.Widget(_buttonPushScope);
        @Native private static final byte _buttonPushScope2 = _buttonPushScope + 1;
        public static final JRSUIConstants.Widget BUTTON_PUSH_SCOPE2 = new JRSUIConstants.Widget(_buttonPushScope2);
        @Native private static final byte _buttonPushTextured = _buttonPushScope2 + 1;
        public static final JRSUIConstants.Widget BUTTON_PUSH_TEXTURED = new JRSUIConstants.Widget(_buttonPushTextured);
        @Native private static final byte _buttonPushInset = _buttonPushTextured + 1;
        public static final JRSUIConstants.Widget BUTTON_PUSH_INSET = new JRSUIConstants.Widget(_buttonPushInset);
        @Native private static final byte _buttonPushInset2 = _buttonPushInset + 1;
        public static final JRSUIConstants.Widget BUTTON_PUSH_INSET2 = new JRSUIConstants.Widget(_buttonPushInset2);

        @Native private static final byte _buttonRadio = _buttonPushInset2 + 1;
        public static final JRSUIConstants.Widget BUTTON_RADIO = new JRSUIConstants.Widget(_buttonRadio);

        @Native private static final byte _buttonRound = _buttonRadio + 1;
        public static final JRSUIConstants.Widget BUTTON_ROUND = new JRSUIConstants.Widget(_buttonRound);
        @Native private static final byte _buttonRoundHelp = _buttonRound + 1;
        public static final JRSUIConstants.Widget BUTTON_ROUND_HELP = new JRSUIConstants.Widget(_buttonRoundHelp);
        @Native private static final byte _buttonRoundInset = _buttonRoundHelp + 1;
        public static final JRSUIConstants.Widget BUTTON_ROUND_INSET = new JRSUIConstants.Widget(_buttonRoundInset);
        @Native private static final byte _buttonRoundInset2 =_buttonRoundInset + 1;
        public static final JRSUIConstants.Widget BUTTON_ROUND_INSET2 = new JRSUIConstants.Widget(_buttonRoundInset2);

        @Native private static final byte _buttonSearchFieldCancel = _buttonRoundInset2 + 1;
        public static final JRSUIConstants.Widget BUTTON_SEARCH_FIELD_CANCEL = new JRSUIConstants.Widget(_buttonSearchFieldCancel);
        @Native private static final byte _buttonSearchFieldFind = _buttonSearchFieldCancel + 1;
        public static final JRSUIConstants.Widget BUTTON_SEARCH_FIELD_FIND = new JRSUIConstants.Widget(_buttonSearchFieldFind);

        @Native private static final byte _buttonSegmented = _buttonSearchFieldFind + 1;
        public static final JRSUIConstants.Widget BUTTON_SEGMENTED = new JRSUIConstants.Widget(_buttonSegmented);
        @Native private static final byte _buttonSegmentedInset = _buttonSegmented + 1;
        public static final JRSUIConstants.Widget BUTTON_SEGMENTED_INSET = new JRSUIConstants.Widget(_buttonSegmentedInset);
        @Native private static final byte _buttonSegmentedInset2 = _buttonSegmentedInset + 1;
        public static final JRSUIConstants.Widget BUTTON_SEGMENTED_INSET2 = new JRSUIConstants.Widget(_buttonSegmentedInset2);
        @Native private static final byte _buttonSegmentedSCurve = _buttonSegmentedInset2 + 1;
        public static final JRSUIConstants.Widget BUTTON_SEGMENTED_SCURVE = new JRSUIConstants.Widget(_buttonSegmentedSCurve);
        @Native private static final byte _buttonSegmentedTextured = _buttonSegmentedSCurve + 1;
        public static final JRSUIConstants.Widget BUTTON_SEGMENTED_TEXTURED = new JRSUIConstants.Widget(_buttonSegmentedTextured);
        @Native private static final byte _buttonSegmentedToolbar = _buttonSegmentedTextured + 1;
        public static final JRSUIConstants.Widget BUTTON_SEGMENTED_TOOLBAR = new JRSUIConstants.Widget(_buttonSegmentedToolbar);

        @Native private static final byte _dial = _buttonSegmentedToolbar + 1;
        public static final JRSUIConstants.Widget DIAL = new JRSUIConstants.Widget(_dial);

        @Native private static final byte _disclosureTriangle = _dial + 1;
        public static final JRSUIConstants.Widget DISCLOSURE_TRIANGLE = new JRSUIConstants.Widget(_disclosureTriangle);

        @Native private static final byte _dividerGrabber = _disclosureTriangle + 1;
        public static final JRSUIConstants.Widget DIVIDER_GRABBER = new JRSUIConstants.Widget(_dividerGrabber);
        @Native private static final byte _dividerSeparatorBar = _dividerGrabber + 1;
        public static final JRSUIConstants.Widget DIVIDER_SEPARATOR_BAR = new JRSUIConstants.Widget(_dividerSeparatorBar);
        @Native private static final byte _dividerSplitter = _dividerSeparatorBar + 1;
        public static final JRSUIConstants.Widget DIVIDER_SPLITTER = new JRSUIConstants.Widget(_dividerSplitter);

        @Native private static final byte _focus = _dividerSplitter + 1;
        public static final JRSUIConstants.Widget FOCUS = new JRSUIConstants.Widget(_focus);

        @Native private static final byte _frameGroupBox = _focus + 1;
        public static final JRSUIConstants.Widget FRAME_GROUP_BOX = new JRSUIConstants.Widget(_frameGroupBox);
        @Native private static final byte _frameGroupBoxSecondary = _frameGroupBox + 1;
        public static final JRSUIConstants.Widget FRAME_GROUP_BOX_SECONDARY = new JRSUIConstants.Widget(_frameGroupBoxSecondary);

        @Native private static final byte _frameListBox = _frameGroupBoxSecondary + 1;
        public static final JRSUIConstants.Widget FRAME_LIST_BOX = new JRSUIConstants.Widget(_frameListBox);

        @Native private static final byte _framePlacard = _frameListBox + 1;
        public static final JRSUIConstants.Widget FRAME_PLACARD = new JRSUIConstants.Widget(_framePlacard);

        @Native private static final byte _frameTextField = _framePlacard + 1;
        public static final JRSUIConstants.Widget FRAME_TEXT_FIELD = new JRSUIConstants.Widget(_frameTextField);
        @Native private static final byte _frameTextFieldRound = _frameTextField + 1;
        public static final JRSUIConstants.Widget FRAME_TEXT_FIELD_ROUND = new JRSUIConstants.Widget(_frameTextFieldRound);

        @Native private static final byte _frameWell = _frameTextFieldRound + 1;
        public static final JRSUIConstants.Widget FRAME_WELL = new JRSUIConstants.Widget(_frameWell);

        @Native private static final byte _growBox = _frameWell + 1;
        public static final JRSUIConstants.Widget GROW_BOX = new JRSUIConstants.Widget(_growBox);
        @Native private static final byte _growBoxTextured = _growBox + 1;
        public static final JRSUIConstants.Widget GROW_BOX_TEXTURED = new JRSUIConstants.Widget(_growBoxTextured);

        @Native private static final byte _gradient = _growBoxTextured + 1;
        public static final JRSUIConstants.Widget GRADIENT = new JRSUIConstants.Widget(_gradient);

        @Native private static final byte _menu = _gradient + 1;
        public static final JRSUIConstants.Widget MENU = new JRSUIConstants.Widget(_menu);
        @Native private static final byte _menuItem = _menu + 1;
        public static final JRSUIConstants.Widget MENU_ITEM = new JRSUIConstants.Widget(_menuItem);
        @Native private static final byte _menuBar = _menuItem + 1;
        public static final JRSUIConstants.Widget MENU_BAR = new JRSUIConstants.Widget(_menuBar);
        @Native private static final byte _menuTitle = _menuBar + 1;
        public static final JRSUIConstants.Widget MENU_TITLE = new JRSUIConstants.Widget(_menuTitle);

        @Native private static final byte _progressBar = _menuTitle + 1;
        public static final JRSUIConstants.Widget PROGRESS_BAR = new JRSUIConstants.Widget(_progressBar);
        @Native private static final byte _progressIndeterminateBar = _progressBar + 1;
        public static final JRSUIConstants.Widget PROGRESS_INDETERMINATE_BAR = new JRSUIConstants.Widget(_progressIndeterminateBar);
        @Native private static final byte _progressRelevance = _progressIndeterminateBar + 1;
        public static final JRSUIConstants.Widget PROGRESS_RELEVANCE = new JRSUIConstants.Widget(_progressRelevance);
        @Native private static final byte _progressSpinner = _progressRelevance + 1;
        public static final JRSUIConstants.Widget PROGRESS_SPINNER = new JRSUIConstants.Widget(_progressSpinner);

        @Native private static final byte _scrollBar = _progressSpinner + 1;
        public static final JRSUIConstants.Widget SCROLL_BAR = new JRSUIConstants.Widget(_scrollBar);

        @Native private static final byte _scrollColumnSizer = _scrollBar + 1;
        public static final JRSUIConstants.Widget SCROLL_COLUMN_SIZER = new JRSUIConstants.Widget(_scrollColumnSizer);

        @Native private static final byte _slider = _scrollColumnSizer + 1;
        public static final JRSUIConstants.Widget SLIDER = new JRSUIConstants.Widget(_slider);
        @Native private static final byte _sliderThumb = _slider + 1;
        public static final JRSUIConstants.Widget SLIDER_THUMB = new JRSUIConstants.Widget(_sliderThumb);

        @Native private static final byte _synchronization = _sliderThumb + 1;
        public static final JRSUIConstants.Widget SYNCHRONIZATION = new JRSUIConstants.Widget(_synchronization);

        @Native private static final byte _tab = _synchronization + 1;
        public static final JRSUIConstants.Widget TAB = new JRSUIConstants.Widget(_tab);

        @Native private static final byte _titleBarCloseBox = _tab + 1;
        public static final JRSUIConstants.Widget TITLE_BAR_CLOSE_BOX = new JRSUIConstants.Widget(_titleBarCloseBox);
        @Native private static final byte _titleBarCollapseBox = _titleBarCloseBox + 1;
        public static final JRSUIConstants.Widget TITLE_BAR_COLLAPSE_BOX = new JRSUIConstants.Widget(_titleBarCollapseBox);
        @Native private static final byte _titleBarZoomBox = _titleBarCollapseBox + 1;
        public static final JRSUIConstants.Widget TITLE_BAR_ZOOM_BOX = new JRSUIConstants.Widget(_titleBarZoomBox);

        @Native private static final byte _titleBarToolbarButton = _titleBarZoomBox + 1;
        public static final JRSUIConstants.Widget TITLE_BAR_TOOLBAR_BUTTON = new JRSUIConstants.Widget(_titleBarToolbarButton);

        @Native private static final byte _toolbarItemWell = _titleBarToolbarButton + 1;
        public static final JRSUIConstants.Widget TOOLBAR_ITEM_WELL = new JRSUIConstants.Widget(_toolbarItemWell);

        @Native private static final byte _windowFrame = _toolbarItemWell + 1;
        public static final JRSUIConstants.Widget WINDOW_FRAME = new JRSUIConstants.Widget(_windowFrame);
    }

    public static class Hit {
        @Native private static final int _unknown = -1;
        public static final JRSUIConstants.Hit UNKNOWN = new JRSUIConstants.Hit(_unknown);
        @Native private static final int _none = 0;
        public static final JRSUIConstants.Hit NONE = new JRSUIConstants.Hit(_none);
        @Native private static final int _hit = 1;
        public static final JRSUIConstants.Hit HIT = new JRSUIConstants.Hit(_hit);

        final int hit;
        Hit(final int hit) { this.hit = hit; }

        public boolean isHit() {
            return hit > 0;
        }

        private String getConstantName(JRSUIConstants.Hit hit) {
            if (hit == UNKNOWN) {
                return "UNKNOWN";
            } else if (hit == NONE) {
                return "NONE";
            } else if (hit == HIT) {
                return "HIT";
            }
            return getClass().getSimpleName();
        }

        public String toString() {
            return getConstantName(this);
        }
    }

    public static class ScrollBarHit extends JRSUIConstants.Hit
		{
        @Native private static final int _thumb = 2;
        public static final JRSUIConstants.ScrollBarHit THUMB = new JRSUIConstants.ScrollBarHit(_thumb);

        @Native private static final int _trackMin = 3;
        public static final JRSUIConstants.ScrollBarHit TRACK_MIN = new JRSUIConstants.ScrollBarHit(_trackMin);
        @Native private static final int _trackMax = 4;
        public static final JRSUIConstants.ScrollBarHit TRACK_MAX = new JRSUIConstants.ScrollBarHit(_trackMax);

        @Native private static final int _arrowMin = 5;
        public static final JRSUIConstants.ScrollBarHit ARROW_MIN = new JRSUIConstants.ScrollBarHit(_arrowMin);
        @Native private static final int _arrowMax = 6;
        public static final JRSUIConstants.ScrollBarHit ARROW_MAX = new JRSUIConstants.ScrollBarHit(_arrowMax);
        @Native private static final int _arrowMaxInside = 7;
        public static final JRSUIConstants.ScrollBarHit ARROW_MAX_INSIDE = new JRSUIConstants.ScrollBarHit(_arrowMaxInside);
        @Native private static final int _arrowMinInside = 8;
        public static final JRSUIConstants.ScrollBarHit ARROW_MIN_INSIDE = new JRSUIConstants.ScrollBarHit(_arrowMinInside);

        ScrollBarHit(final int hit) { super(hit); }
    }

    static JRSUIConstants.Hit getHit(final int hit) {
        switch (hit) {
            case JRSUIConstants.Hit._none:
                return JRSUIConstants.Hit.NONE;
            case JRSUIConstants.Hit._hit:
                return JRSUIConstants.Hit.HIT;

            case JRSUIConstants.ScrollBarHit._thumb:
                return JRSUIConstants.ScrollBarHit.THUMB;
            case JRSUIConstants.ScrollBarHit._trackMin:
                return JRSUIConstants.ScrollBarHit.TRACK_MIN;
            case JRSUIConstants.ScrollBarHit._trackMax:
                return JRSUIConstants.ScrollBarHit.TRACK_MAX;
            case JRSUIConstants.ScrollBarHit._arrowMin:
                return JRSUIConstants.ScrollBarHit.ARROW_MIN;
            case JRSUIConstants.ScrollBarHit._arrowMax:
                return JRSUIConstants.ScrollBarHit.ARROW_MAX;
            case JRSUIConstants.ScrollBarHit._arrowMaxInside:
                return JRSUIConstants.ScrollBarHit.ARROW_MAX_INSIDE;
            case JRSUIConstants.ScrollBarHit._arrowMinInside:
                return JRSUIConstants.ScrollBarHit.ARROW_MIN_INSIDE;
        }
        return JRSUIConstants.Hit.UNKNOWN;
    }
}
