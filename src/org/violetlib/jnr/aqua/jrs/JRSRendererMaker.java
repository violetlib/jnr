/*
 * Copyright (c) 2015 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.aqua.jrs;

import java.lang.reflect.Method;

import org.jetbrains.annotations.*;

import apple.laf.JRSUIConstants;
import apple.laf.JRSUIControl;
import apple.laf.JRSUIState;
import apple.laf.JRSUIStateFactory;
import org.violetlib.jnr.impl.EmptyRenderer;
import org.violetlib.jnr.impl.BasicRenderer;

/**
	A maker of renderers that use the Java Runtime Support framework. Set the widget first to get the right kind of state.
*/

public class JRSRendererMaker
{
	protected @Nullable JRSUIConstants.Widget currentWidget;
	protected @Nullable JRSUIControl control;
	protected @Nullable JRSUIState state;

	private static final Method stateSetMethod = getStateSetMethod();

	public JRSRendererMaker()
	{
	}

	private static Method getStateSetMethod()
	{
		Class c = JRSUIState.class;
		Method[] methods = c.getMethods();
		for (Method m : methods) {
			if (m.getName().equals("set")) {
				return m;
			}
		}
		throw new UnsupportedOperationException("Set method on JRSUIState not found");
	}

	public void reset()
	{
		if (state != null) {
			state.reset();
		}
	}

	protected void setProperty(@NotNull Object p)
	{
		if (false) {  // debug
			System.err.println("  Setting property: " + p);
		}

		// Too bad the Property class is private
		try {
			stateSetMethod.invoke(state, p);
		} catch (Exception ex) {
			throw new UnsupportedOperationException("Unable to set JRSUIState property: " + ex);
		}
	}

	public void set(@NotNull JRSUIConstants.Size p)
	{
		setProperty(p);
	}

	public void set(@NotNull JRSUIConstants.State p)
	{
		setProperty(p);
	}

	public void set(@NotNull JRSUIConstants.Direction p)
	{
		setProperty(p);
	}

	public void set(@NotNull JRSUIConstants.Orientation p)
	{
		setProperty(p);
	}

	public void set(@NotNull JRSUIConstants.AlignmentVertical p)
	{
		setProperty(p);
	}

	public void set(@NotNull JRSUIConstants.AlignmentHorizontal p)
	{
		setProperty(p);
	}

	public void set(@NotNull JRSUIConstants.SegmentPosition p)
	{
		setProperty(p);
	}

	public void set(@NotNull JRSUIConstants.ScrollBarPart p)
	{
		setProperty(p);
	}

	public void set(@NotNull JRSUIConstants.Variant p)
	{
		setProperty(p);
	}

	public void set(@NotNull JRSUIConstants.WindowType p)
	{
		setProperty(p);
	}

	public void set(@NotNull JRSUIConstants.Focused p)
	{
		setProperty(p);
	}

	public void set(@NotNull JRSUIConstants.IndicatorOnly p)
	{
		setProperty(p);
	}

	public void set(@NotNull JRSUIConstants.NoIndicator p)
	{
		setProperty(p);
	}

	public void set(@NotNull JRSUIConstants.ArrowsOnly p)
	{
		setProperty(p);
	}

	public void set(@NotNull JRSUIConstants.FrameOnly p)
	{
		setProperty(p);
	}

	public void set(@NotNull JRSUIConstants.SegmentTrailingSeparator p)
	{
		setProperty(p);
	}

	public void set(@NotNull JRSUIConstants.SegmentLeadingSeparator p)
	{
		setProperty(p);
	}

	public void set(@NotNull JRSUIConstants.NothingToScroll p)
	{
		setProperty(p);
	}

	public void set(@NotNull JRSUIConstants.WindowTitleBarSeparator p)
	{
		setProperty(p);
	}

	public void set(@NotNull JRSUIConstants.WindowClipCorners p)
	{
		setProperty(p);
	}

	public void set(@NotNull JRSUIConstants.ShowArrows p)
	{
		setProperty(p);
	}

	public void set(@NotNull JRSUIConstants.BooleanValue p)
	{
		setProperty(p);
	}

	public void set(@NotNull JRSUIConstants.Animating p)
	{
		setProperty(p);
	}

	public void set(@NotNull JRSUIConstants.Widget p)
	{
		if (p == currentWidget) {
			return;
		}

		// If the widget has changed, we need a new state and a new control.

		if (p == JRSUIConstants.Widget.SCROLL_BAR) {
			state = JRSUIStateFactory.getScrollBar();
		} else if (p == JRSUIConstants.Widget.PROGRESS_BAR
			|| p == JRSUIConstants.Widget.PROGRESS_INDETERMINATE_BAR
			|| p == JRSUIConstants.Widget.PROGRESS_RELEVANCE
			|| p == JRSUIConstants.Widget.PROGRESS_SPINNER) {
			state = JRSUIStateFactory.getProgressBar();
			setProperty(p);
		} else if (p == JRSUIConstants.Widget.DISCLOSURE_TRIANGLE) {
			state = JRSUIStateFactory.getDisclosureTriangle();
		} else if (p == JRSUIConstants.Widget.BUTTON_CHECK_BOX || p == JRSUIConstants.Widget.BUTTON_RADIO) {
			state = JRSUIStateFactory.getLabeledButton();
			setProperty(p);
		} else if (p == JRSUIConstants.Widget.WINDOW_FRAME) {
			state = JRSUIStateFactory.getTitleBar();
		} else if (p == JRSUIConstants.Widget.TAB) {
			state = JRSUIStateFactory.getTab();
		} else if (p == JRSUIConstants.Widget.DIVIDER_SPLITTER) {
			state = JRSUIStateFactory.getSplitPaneDivider();
		} else if (p == JRSUIConstants.Widget.BUTTON_LITTLE_ARROWS) {
			state = JRSUIStateFactory.getSpinnerArrows();
		} else if (p == JRSUIConstants.Widget.SLIDER_THUMB) {
			state = JRSUIStateFactory.getProgressBar();	// to get a ValueState
			setProperty(p);
		} else if (p == JRSUIConstants.Widget.SLIDER
			|| p == JRSUIConstants.Widget.DIAL
			|| p == JRSUIConstants.Widget.BUTTON_BEVEL
			|| p == JRSUIConstants.Widget.BUTTON_BEVEL_ROUND
			|| p == JRSUIConstants.Widget.BUTTON_BEVEL_INSET
			|| p == JRSUIConstants.Widget.BUTTON_PUSH_TEXTURED
			|| p == JRSUIConstants.Widget.BUTTON_ROUND) {
			state = JRSUIStateFactory.getProgressBar();	// to get a ValueState
			setProperty(p);
		} else {
			state = JRSUIState.getInstance();
			setProperty(p);
		}

		control = new JRSUIControl(false);
	}

	public void set(@NotNull JRSUIConstants.Hit p)
	{
		setProperty(p);
	}

	public void set(@NotNull JRSUIConstants.ScrollBarHit p)
	{
		setProperty(p);
	}

	public void setValue(double value)
	{
		if (state instanceof JRSUIState.ValueState) {
			JRSUIState.ValueState vstate = (JRSUIState.ValueState) state;
			vstate.setValue(value);
		} else {
			throw new IllegalStateException("Renderer not configured for value attribute");
		}
	}

	public void setThumbStart(double value)
	{
		if (state instanceof JRSUIState.ScrollBarState) {
			JRSUIState.ScrollBarState sbstate = (JRSUIState.ScrollBarState) state;
			sbstate.setThumbStart(value);
		} else {
			throw new IllegalStateException("Renderer not configured for thumb start attribute");
		}
	}

	public void setThumbPercent(double value)
	{
		if (state instanceof JRSUIState.ScrollBarState) {
			JRSUIState.ScrollBarState sbstate = (JRSUIState.ScrollBarState) state;
			sbstate.setThumbPercent(value);
		} else {
			throw new IllegalStateException("Renderer not configured for thumb percent attribute");
		}
	}

	public void setAnimationFrame(int frame)
	{
		if (state instanceof JRSUIState.AnimationFrameState) {
			JRSUIState.AnimationFrameState astate = (JRSUIState.AnimationFrameState) state;
			astate.setAnimationFrame(frame);
		} else {
			throw new IllegalStateException("Renderer not configured for animation frame attribute");
		}
	}

	public @NotNull BasicRenderer getRenderer()
	{
		if (state != null) {
			assert control != null;
			state = state.derive();
			return new JRSRenderer(control, state);
		} else {
			return new EmptyRenderer();
		}
	}
}
