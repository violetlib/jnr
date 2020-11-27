/*
 * Copyright (c) 2015-2020 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.aqua;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.util.Map;

import org.violetlib.jnr.Painter;
import org.violetlib.vappearances.VAppearance;

import org.jetbrains.annotations.*;

/**
  An interface that supports native rendering for the Aqua look and feel. The goals of this interface are to isolate the
  specific set of features needed by the Aqua look and feel when using native rendering and to allow experimentation
  with different native rendering implementations. Although designed to support the Aqua look and feel, additional
  options are provided that could be supported by an extension to the Aqua look and feel.

  <p>
  This interface supports the creation of renderings for user interface components. In most cases, the renderings are
  incomplete. For example, the rendering of a button does not include the button label or icon. For lack of a better
  word, the different kinds of renderings are identified as <b>widgets</b>.

  <p>
  A rendering is represented by a {@link org.violetlib.jnr.Painter painter} object. To render a specific component, a
  painter is created for that component and then used to draw the rendering into a graphics context. The painter can be
  cached for future use (assuming that the rendering parameters have not changed); however, caching a painter is
  unlikely to provide a significant performance benefit. Implementations of this interface may support internal caching
  of images.

  <p>
  This interface also makes available some layout related information associated with native renderings, such as fixed
  layout widths and/or heights, information about component outlines for painting focus rings, and information about
  active areas within rendered components, such as the label within a button or the portion of a search field that
  corresponds to the cancel button.

  <p>
  The appropriate use of this interface is to call configure() immediately before each call to obtain a painter or to
  obtain a shape or bounds.

  <p>
  All sizes and positions are specified in device independent pixels, sometimes called points.

  <p>
  All combinations of parameters are not necessarily supported. Some widgets support only the regular and small size
  variants. Some widgets support only two display states, active and disabled. Other size variants and states are
  mapped to the most appropriate supported value.

  <p>
  Some possible rendering options may not be supported by a specific implementation of this interface. In general, when
  a requested rendering cannot be performed, an UnsupportedOperationException is thrown.

  <p>
  Because of the limitations of the Swing architecture, no current implementation of this interface paints the overlay
  focus rings used in recent releases of OS X. The isFocused parameters should always be false. They are provided for
  possible use for other UIs.
*/

public interface AquaUIPainter
{
    /*
        Issues:

        Should the label insets for a button be the smallest needed to avoid overwriting the border, or should they
        reflect good practice? Currently using the former approach.

        AquaSliderUI suppresses the painting of the track or the ticks based on the clip region, an option we do not
        support.

        JSlider has options to suppress the painting of the track or the ticks.

        JSlider has a tick color option.

        Do the North etc directions relate to L/R UI direction or can Left etc be used? See AquaTabbedPaneUI.
    */

    /**
      States of a toggle button.
    */

    enum ButtonState
    {
        STATELESS,        // a button that does not display state, such as push button
        ON,
        OFF,
        MIXED
    }

    /**
      Size options.
    */

    enum Size
    {
        MINI,
        SMALL,
        REGULAR,
        LARGE
    }

    /**
      States of a component.
    */

    enum State
    {
        ACTIVE,
        INACTIVE,
        DISABLED,
        DISABLED_INACTIVE,
        PRESSED,
        ACTIVE_DEFAULT,  // for the default button when it is active
        ROLLOVER;

        public boolean isInactive()
        {
            return this == INACTIVE || this == DISABLED_INACTIVE;
        }

        public State toActive()
        {
            if (this == INACTIVE) {
                return ACTIVE;
            }
            if (this == DISABLED_INACTIVE) {
                return DISABLED;
            }
            return this;
        }
    }

    /**
      Selection mode for a segmented control.
    */

    enum SwitchTracking
    {
        SELECT_ONE,
        SELECT_ANY
    }

    /**
      Horizontal alignment options.
    */

    enum HorizontalAlignment
    {
        LEFT,
        CENTER,
        RIGHT
    }

    /**
      Vertical alignment options.
    */

    enum VerticalAlignment
    {
        TOP,
        CENTER,
        BOTTOM
    }

    /**
      Direction options for a column sort indicator in a table header.
    */

    enum ColumnSortArrowDirection
    {
        NONE,
        UP,
        DOWN
    }

    /**
      Direction options.
    */

    enum Direction
    {
        NONE,
        UP,
        DOWN,
        LEFT,
        RIGHT
    }

    /**
      Orientation options.
    */

    enum Orientation
    {
        HORIZONTAL,
        VERTICAL
    }

    /**
      Options for the position of a button in a segmented control.
    */

    enum Position
    {
        FIRST,
        MIDDLE,
        LAST,
        ONLY
    }

    /**
      Options for the position of slider tick marks.
    */

    enum TickMarkPosition
    {
        BELOW,
        ABOVE,
        LEFT,
        RIGHT
    }

    /**
      User interface layout direction options.
    */

    enum UILayoutDirection
    {
        LEFT_TO_RIGHT,
        RIGHT_TO_LEFT
    }

    /**
      Generic button widgets.
    */

    interface GenericButtonWidget
    {
        boolean isTextured();
        boolean isToolbar();
    }

    /**
      Widgets for a button.
    */

    enum ButtonWidget implements GenericButtonWidget
    {
        BUTTON_PUSH,                  // the default style, fixed height, not suitable for a toggle button
        BUTTON_CHECK_BOX,
        BUTTON_RADIO,
        BUTTON_DISCLOSURE,
        BUTTON_DISCLOSURE_TRIANGLE,
        BUTTON_HELP,                    // not suitable for a toggle button
        BUTTON_GRADIENT,                // a square button with no border - recommended for icon buttons - push, toggle, or menu (small square)
        BUTTON_RECESSED,                // a recessed scope (toggle) button; fixed height; displayed without border when not selected
        BUTTON_INLINE,                  // a short, fixed height button with rounded ends, used as a push button or indicator inside lists
        BUTTON_ROUNDED_RECT,            // fixed height, displays as an rectangle outline with rounded corners and no background, darkens when pressed, in IB
        BUTTON_TEXTURED,                // fixed height, recommended for use in window frame, previously called scurve, now called textured rounded
        BUTTON_TEXTURED_TOOLBAR,        // introduced in 10.11 for textured buttons on the tool bar (taller)
        BUTTON_TOOLBAR_ITEM,            // a tool bar item
        BUTTON_COLOR_WELL,              // a color well

        // The following styles are no longer recommended

        BUTTON_BEVEL,                   // Bevel button with square corners (I call this Square)
        BUTTON_BEVEL_ROUND,             // Bevel button with rounded corners (I call this Bevel)
        BUTTON_ROUND,                   // a round white button with a border
        BUTTON_ROUND_INSET,             // a round transparent button with an outline, probably obsolete
        BUTTON_ROUND_TEXTURED,          // a round white borderless button with a shadow
        BUTTON_ROUND_TEXTURED_TOOLBAR,  // introduced in 10.11 for round textured buttons on the toolbar (taller)
        BUTTON_PUSH_INSET2;             // an obsolete style supported by Core UI

        @Override
        public boolean isTextured()
        {
            return this == BUTTON_TEXTURED
                     || this == BUTTON_TEXTURED_TOOLBAR
                     || this == BUTTON_ROUND_TEXTURED
                     || this == BUTTON_ROUND_TEXTURED_TOOLBAR;
        }

        @Override
        public boolean isToolbar()
        {
            return this == BUTTON_TEXTURED_TOOLBAR
                     || this == BUTTON_ROUND_TEXTURED_TOOLBAR;
        }
    }

    /**
      Widgets for a button in a segmented control.
    */

    enum SegmentedButtonWidget implements GenericButtonWidget
    {
        BUTTON_TAB,                                         // the segmented control on a Tab View, prior to macOS 11
        BUTTON_SEGMENTED,                                   // the default button for the content area, known as Rounded (before macOS 11, looks like Tab)
        BUTTON_SEGMENTED_SEPARATED,                         // separated buttons that look like Rounded buttons
        BUTTON_SEGMENTED_INSET,                             // also known as Round Rect, a transparent button whose outline has rounded corners
        BUTTON_SEGMENTED_SMALL_SQUARE,                      // a square button similar to a gradient button
        BUTTON_SEGMENTED_TEXTURED,                          // also known as Textured Rounded, for use in window frames
        BUTTON_SEGMENTED_TEXTURED_TOOLBAR,                  // introduced in 10.11 for textured segmented controls on the tool bar (taller)
        BUTTON_SEGMENTED_TEXTURED_TOOLBAR_ICONS,            // added in release 10 for macOS 11; textured segmented controls on the tool bar, icons only
        BUTTON_SEGMENTED_TEXTURED_SEPARATED,                // separated buttons that look like Textured buttons, for use in window frames
        BUTTON_SEGMENTED_TEXTURED_SEPARATED_TOOLBAR,        // introduced in 10.11 for textured segmented controls on the tool bar (taller)
        BUTTON_SEGMENTED_TEXTURED_SEPARATED_TOOLBAR_ICONS,  // introduced in 10.11 for textured segmented controls on the tool bar (taller), icons only
        BUTTON_SEGMENTED_SLIDER,                            // added in release 10 for macOS 11; select one rounded style, also used for tab
        BUTTON_SEGMENTED_SLIDER_TOOLBAR,                    // added in release 10 for macOS 11; select one textured style on the toolbar, has text labels
        BUTTON_SEGMENTED_SLIDER_TOOLBAR_ICONS,              // added in release 10 for macOS 11; select one textured style on the toolbar, icons only

        // The following styles are obsolete and are replaced by other styles in Yosemite

        BUTTON_SEGMENTED_TOOLBAR,       // also known as Capsule or Textured Square
        BUTTON_SEGMENTED_SCURVE;        // also known as Capsule or Textured Square

        public boolean isSeparated()
        {
            return this == BUTTON_SEGMENTED_SEPARATED
                     || this == BUTTON_SEGMENTED_TEXTURED_SEPARATED
                     || this == BUTTON_SEGMENTED_TEXTURED_SEPARATED_TOOLBAR
                     || this == BUTTON_SEGMENTED_TEXTURED_SEPARATED_TOOLBAR_ICONS;
        }

        public boolean isSlider()
        {
            return this == BUTTON_SEGMENTED_SLIDER
                     || this == BUTTON_SEGMENTED_SLIDER_TOOLBAR
                     || this == BUTTON_SEGMENTED_SLIDER_TOOLBAR_ICONS;
        }

        @Override
        public boolean isToolbar()
        {
            return this == BUTTON_SEGMENTED_TEXTURED_TOOLBAR
                     || this == BUTTON_SEGMENTED_TEXTURED_TOOLBAR_ICONS
                     || this == BUTTON_SEGMENTED_TEXTURED_SEPARATED_TOOLBAR
                     || this == BUTTON_SEGMENTED_SLIDER_TOOLBAR
                     || this == BUTTON_SEGMENTED_SLIDER_TOOLBAR_ICONS
                     || this == BUTTON_SEGMENTED_TEXTURED_SEPARATED_TOOLBAR_ICONS;
        }

        @Override
        public boolean isTextured()
        {
            return this == BUTTON_SEGMENTED_TEXTURED
                     || this == BUTTON_SEGMENTED_TEXTURED_TOOLBAR
                     || this == BUTTON_SEGMENTED_TEXTURED_SEPARATED
                     || this == BUTTON_SEGMENTED_TEXTURED_SEPARATED_TOOLBAR
                     || this == BUTTON_SEGMENTED_TEXTURED_TOOLBAR_ICONS
                     || this == BUTTON_SEGMENTED_TEXTURED_SEPARATED_TOOLBAR_ICONS;
        }

        public boolean isIconsOnly()
        {
            return this == BUTTON_SEGMENTED_TEXTURED_TOOLBAR_ICONS
                     || this == BUTTON_SEGMENTED_SLIDER_TOOLBAR_ICONS
                     || this == BUTTON_SEGMENTED_TEXTURED_SEPARATED_TOOLBAR_ICONS;
        }

        public @NotNull SegmentedButtonWidget toToolbarWidget()
        {
            switch (this) {
                case BUTTON_SEGMENTED_TEXTURED:
                    return BUTTON_SEGMENTED_TEXTURED_TOOLBAR;
                case BUTTON_SEGMENTED_TEXTURED_SEPARATED:
                    return BUTTON_SEGMENTED_TEXTURED_SEPARATED_TOOLBAR;
            }
            return this;
        }

        public @NotNull SegmentedButtonWidget toBasicWidget()
        {
            switch (this) {
                case BUTTON_SEGMENTED_TEXTURED_TOOLBAR:
                    return BUTTON_SEGMENTED_TEXTURED;
                case BUTTON_SEGMENTED_TEXTURED_SEPARATED_TOOLBAR:
                    return BUTTON_SEGMENTED_TEXTURED_SEPARATED;
            }
            return this;
        }
    }

    /**
      Widgets for a text field.
    */

    enum TextFieldWidget
    {
        TEXT_FIELD(false, false, false, false),                 // square corners, no fixed height
        TEXT_FIELD_ROUND(false, false, false, false),           // rounded corners, fixed height
        TEXT_FIELD_ROUND_TOOLBAR(false, false, false, true),    // for text fields on the tool bar (taller)
        TEXT_FIELD_SEARCH(true, false, false, false),
        TEXT_FIELD_SEARCH_WITH_CANCEL(true, false, true, false),
        TEXT_FIELD_SEARCH_WITH_MENU(true, true, false, false),
        TEXT_FIELD_SEARCH_WITH_MENU_AND_CANCEL(true, true, true, false),
        TEXT_FIELD_SEARCH_TOOLBAR(true, false, false, true),
        TEXT_FIELD_SEARCH_WITH_CANCEL_TOOLBAR(true, false, true, true),
        TEXT_FIELD_SEARCH_WITH_MENU_TOOLBAR(true, true, false, true),
        TEXT_FIELD_SEARCH_WITH_MENU_AND_CANCEL_TOOLBAR(true, true, true, true);

        private final boolean isSearch;
        private final boolean hasMenu;
        private final boolean hasCancel;
        private final boolean isToolbar;

        TextFieldWidget(boolean isSearch, boolean hasMenu, boolean hasCancel, boolean isToolbar)
        {
            this.isSearch = isSearch;
            this.hasMenu = hasMenu;
            this.hasCancel = hasCancel;
            this.isToolbar = isToolbar;
        }

        public boolean isSearch()
        {
            return isSearch;
        }

        public boolean hasMenu()
        {
            return hasMenu;
        }

        public boolean hasCancel()
        {
            return hasCancel;
        }

        public boolean isToolbar()
        {
            return isToolbar;
        }

        public boolean isRound()
        {
            return this == TEXT_FIELD_ROUND || this == TEXT_FIELD_ROUND_TOOLBAR;
        }
    }

    /**
      Widgets for an editable combo box.
    */

    enum ComboBoxWidget implements GenericButtonWidget
    {
        BUTTON_COMBO_BOX,
        BUTTON_COMBO_BOX_CELL,
        BUTTON_COMBO_BOX_TEXTURED,
        BUTTON_COMBO_BOX_TEXTURED_TOOLBAR;

        @Override
        public boolean isTextured()
        {
            return this == BUTTON_COMBO_BOX_TEXTURED || this == BUTTON_COMBO_BOX_TEXTURED_TOOLBAR;
        }

        @Override
        public boolean isToolbar()
        {
            return this == BUTTON_COMBO_BOX_TEXTURED_TOOLBAR;
        }
    }

    /**
      Widgets for a non-editable combo box.
    */

    enum PopupButtonWidget implements GenericButtonWidget
    {
        BUTTON_POP_DOWN,
        BUTTON_POP_DOWN_CELL,                // for use as cell editors
        BUTTON_POP_DOWN_BEVEL,
        BUTTON_POP_DOWN_ROUND_RECT,
        BUTTON_POP_DOWN_RECESSED,
        BUTTON_POP_DOWN_TEXTURED,
        BUTTON_POP_DOWN_TEXTURED_TOOLBAR,
        BUTTON_POP_DOWN_GRADIENT,            // the preferred square style
        BUTTON_POP_DOWN_SQUARE,              // replaced by Gradient in Yosemite

        BUTTON_POP_UP,
        BUTTON_POP_UP_CELL,                  // for use as cell editors
        BUTTON_POP_UP_BEVEL,
        BUTTON_POP_UP_ROUND_RECT,
        BUTTON_POP_UP_RECESSED,
        BUTTON_POP_UP_TEXTURED,
        BUTTON_POP_UP_TEXTURED_TOOLBAR,
        BUTTON_POP_UP_GRADIENT,              // the preferred square style
        BUTTON_POP_UP_SQUARE;                // replaced by Gradient in Yosemite

        public boolean isDefault()
        {
            return this == BUTTON_POP_DOWN || this == BUTTON_POP_UP;
        }

        @Override
        public boolean isTextured()
        {
            return this == BUTTON_POP_DOWN_TEXTURED
                     || this == BUTTON_POP_DOWN_TEXTURED_TOOLBAR
                     || this == BUTTON_POP_UP_TEXTURED
                     || this == BUTTON_POP_UP_TEXTURED_TOOLBAR;
        }

        @Override
        public boolean isToolbar()
        {
            return this == BUTTON_POP_DOWN_TEXTURED_TOOLBAR || this == BUTTON_POP_UP_TEXTURED_TOOLBAR;
        }
    }

    /**
      Widgets for window title bar.
    */

    enum TitleBarWidget
    {
        DOCUMENT_WINDOW,
        UTILITY_WINDOW
    }

    /**
      Widgets for a button on a window title bar.
    */

    enum TitleBarButtonWidget
    {
        CLOSE_BOX,
        MINIMIZE_BOX,
        RESIZE_BOX
    }

    /**
      Widgets for a progress indicator.
    */

    enum ProgressWidget
    {
        SPINNER,
        BAR,
        INDETERMINATE_SPINNER,
        INDETERMINATE_BAR
    }

    /**
      Widgets for a scroll bar.
    */
    enum ScrollBarWidget
    {
        LEGACY,
        OVERLAY,
        OVERLAY_ROLLOVER
    }

    /**
      Widgets for a scroll bar knob.
    */

    enum ScrollBarKnobWidget
    {
        NONE,
        DEFAULT,
        DARK,
        LIGHT
    }

    /**
      Widgets for a slider.
    */

    enum SliderWidget
    {
        SLIDER_HORIZONTAL,
        SLIDER_HORIZONTAL_RIGHT_TO_LEFT,
        SLIDER_VERTICAL,
        SLIDER_CIRCULAR,
        SLIDER_UPSIDE_DOWN
    }

    /**
      Widgets for a split pane divider.
    */

    enum DividerWidget
    {
        PANE_SPLITTER,
        THIN_DIVIDER,
        THICK_DIVIDER
    }

    /**
      Widgets for a gradient background.
    */

    enum GradientWidget
    {
        GRADIENT_FIND_BAR,
        GRADIENT_FINDER_INFO,
        GRADIENT_FINDER_SIDE_BAR,
        GRADIENT_FREEFORM,
        GRADIENT_LIST_BACKGROUND_EVEN,
        GRADIENT_LIST_BACKGROUND_ODD,
        GRADIENT_SCOPE_BACKGROUND_BAR,
        GRADIENT_SCOPE_BACKGROUND_EVEN,
        GRADIENT_SCOPE_BACKGROUND_ODD,
        GRADIENT_SIDE_BAR,
        GRADIENT_SIDE_BAR_SELECTION,
        GRADIENT_SIDE_BAR_SELECTION_MULTIPLE
    }

//    /**
//        Enable or disable alignment.
//
//        <p>
//        When a client specifies a bounds that exceeds the limits of a rendering, the normal behavior is to center the
//        rendering within the specified bounds. This method allows that alignment to be suppressed for testing purposes,
//        specifically to allow the rendering to be examined to determine the appropriate insets.
//
//        <p>
//        This method has no effect if the painter does not normally perform alignment.
//
//        @param b True to enable alignment, false to suppress alignment.
//    */
//
//    void setAlignmentEnabled(boolean b);

    /**
      Create a new instance with the same implementation.
    */

    @NotNull AquaUIPainter copy();

    /**
      Return the custom colors for the specified appearance.
    */

    @NotNull Map<String,Color> getColors(@NotNull VAppearance appearance);

    /**
      Configure the system appearance to be used by the painter.

      @param appearance The appearance to use.
    */

    void configureAppearance(@NotNull VAppearance appearance);

    /**
      Configure the generic parameters for the next request.

      @param w The width of the widget.
      @param h The height of the widget.
    */

    void configure(int w, int h);

    /**
      Return a widget painter based on the specified configuration and the previously configured widget size.
      @param g The widget configuration.
      @return the painter.
      @throws UnsupportedOperationException if the configuration is not supported.
    */

    @NotNull Painter getPainter(@NotNull Configuration g)
      throws UnsupportedOperationException;

    /**
      Return a provider of layout information.
    */

    @NotNull AquaUILayoutInfo getLayoutInfo();

    /**
      Return the visible outline of a widget based on the configured width and height. The returned shape can be used to
      create a focus ring. The returned shape need not correspond exactly to the painted area. For example, a shadow
      might be painted outside this shape.

      @param g This configuration describes the widget.
      @return the outline.
    */

    @Nullable Shape getOutline(@NotNull LayoutConfiguration g);

    /**
      Return the bounds of the editor area of a combo box based on the configured width and height. The bounds of the
      editor area may differ from that predicted using the editor insets if the rendering is limited in width or height.

      @param g This parameter specifies the layout configuration of the combo box.
    */

    @NotNull Rectangle2D getComboBoxEditorBounds(@NotNull ComboBoxLayoutConfiguration g);

    /**
      Return the bounds of the indicator within the combo box based on the configured width and height.

      @param g This parameter specifies the layout configuration of the segmented button.
    */

    @NotNull Rectangle2D getComboBoxIndicatorBounds(@NotNull ComboBoxLayoutConfiguration g);

    /**
      Return the bounds of the content area of a pop up button based on the configured width and height. The bounds of
      the content area may differ from that predicted using the content insets if the rendering is limited in width or
      height.

      @param g This parameter specifies the layout configuration of the pop up button.
    */

    @NotNull Rectangle2D getPopupButtonContentBounds(@NotNull PopupButtonLayoutConfiguration g);

    /**
      Return the bounds of the thumb area based on the configured width and height.

      @param g This parameter specifies the layout configuration of the slider.
      @param thumbPosition The position where the thumb would be be painted, expressed as a fraction of the slider
      range, in the range 0 to 1 (inclusive). Thumb position 0 corresponds to the lowest slider value.
    */

    @NotNull Rectangle2D getSliderThumbBounds(@NotNull SliderLayoutConfiguration g, double thumbPosition);

    /**
      Map a mouse coordinate to a slider thumb position. This method relies on the previously configured widget size.

      @param g This parameter specifies the layout configuration of the slider.
      @param x The x coordinate relative to the configured bounds.
      @param y The y coordinate relative to the configured bounds.

      @return the thumb position as a fraction of the slider range, if in the range 0 to 1 (inclusive), or a value less
      than 0 if the coordinate is outside the slider range in the area corresponding to low values, or a value greater
      than 1 if the coordinate is outside the slider range in the area corresponding to high values.
    */

    double getSliderThumbPosition(@NotNull SliderLayoutConfiguration g, int x, int y);

    /**
      Map a major axis coordinate of a scroll bar to a thumb position along the scroll bar track. This method relies on
      the previously configured widget size.

      @param g This parameter describes the scroll bar and the major axis coordinate.
      @param useExtent If true, the coordinate is interpreted as the location of the leading edge of the thumb, for the
      purpose of repositioning the thumb. If false, the coordinate is interpreted as a fraction of the full track, for
      the purpose of scroll-to-here.

      @return the thumb position as a fraction of the scroll bar track, if in the range 0 to 1 (inclusive), or a value
      less than 0 if the coordinate is outside the track in the area corresponding to low values, or a value greater
      than 1 if the coordinate is outside the track in the area corresponding to high values.

      The scroll bar track is the portion of the widget that the thumb can occupy.
    */

    float getScrollBarThumbPosition(@NotNull ScrollBarThumbLayoutConfiguration g, boolean useExtent);

    /**
      Determine whether a major axis coordinate of a scroll bar corresponds to the visible thumb. This method relies on
      the previously configured widget size.

      @param g This parameter describes the scroll bar and the major axis coordinate.

      @return zero if the coordinate corresponds to the visible thumb, -1 if it is in the track at a lower position,
      1 if it is in the track at a higher position, a large negative number otherwise.

      The scroll bar track is the portion of the widget that the thumb can occupy.
    */

    int getScrollBarThumbHit(@NotNull ScrollBarThumbConfiguration g);

    /**
      Return the recommended bounds for a label corresponding to a given thumb position. This method relies on the
      previously configured widget size.

      @param g This parameter specifies the layout configuration of the slider.
      @param thumbPosition The thumb position represented by the label, expressed as a fraction of the slider range, in
      the range 0 to 1 (inclusive). Thumb position 0 corresponds to the lowest slider value.
      @param size The intended size of the label.
      @return the recommended bounds for the label.
    */

    @NotNull Rectangle2D getSliderLabelBounds(@NotNull SliderLayoutConfiguration g,
                                              double thumbPosition,
                                              @NotNull Dimension size);
}
