/*
 * Copyright (c) 2015 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.aqua;

import java.util.Objects;

import org.jetbrains.annotations.*;

import org.violetlib.jnr.aqua.AquaUIPainter.Orientation;
import org.violetlib.jnr.aqua.AquaUIPainter.ScrollBarKnobWidget;
import org.violetlib.jnr.aqua.AquaUIPainter.ScrollBarWidget;
import org.violetlib.jnr.aqua.AquaUIPainter.Size;
import org.violetlib.jnr.aqua.AquaUIPainter.State;
import org.violetlib.jnr.impl.JNRUtils;

/**
	A configuration for a scroll bar.
*/

public class ScrollBarConfiguration
	extends ScrollBarLayoutConfiguration
	implements Configuration
{
	private final @NotNull ScrollBarKnobWidget kw;
	private final @NotNull State state;
	private final float thumbPosition;
	private final float thumbExtent;
	private final boolean noTrack;

	/**
		Create a scroll bar configuration.

		@param bw The scroll bar widget.
		@param kw The scroll bar thumb (knob) widget.
		@param sz The size option.
		@param state The scroll bar state.
		@param o The scroll bar orientation.
		@param thumbPosition The position of the thumb, expressed as a fraction of the full range of scrolling.
		@param thumbExtent The extent of the thumb, expressed as the fraction of the full view that is displayed in the
			viewport.
		@param noTrack If true, the track is not painted. This option is not applicable to overlay scroll bars.
	*/

	public ScrollBarConfiguration(@NotNull ScrollBarWidget bw,
																@NotNull ScrollBarKnobWidget kw,
																@NotNull Size sz,
																@NotNull State state,
																@NotNull Orientation o,
																float thumbPosition,
																float thumbExtent,
																boolean noTrack)
	{
		super(bw, sz, o);

		this.kw = kw;
		this.state = state;
		this.thumbPosition = thumbPosition;
		this.thumbExtent = thumbExtent;
		this.noTrack = noTrack;
	}

	public ScrollBarConfiguration(@NotNull ScrollBarConfiguration g)
	{
		this(g.getWidget(), g.getKnobWidget(), g.getSize(), g.getState(), g.getOrientation(), g.getThumbPosition(),
			g.getThumbExtent(), g.isTrackSuppressed());
	}

	public @NotNull ScrollBarKnobWidget getKnobWidget()
	{
		return kw;
	}

	public @NotNull State getState()
	{
		return state;
	}

	public float getThumbPosition()
	{
		return thumbPosition;
	}

	public float getThumbExtent()
	{
		return thumbExtent;
	}

	public boolean isTrackSuppressed()
	{
		return noTrack;
	}

	@Override
	public boolean equals(@Nullable Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;
		ScrollBarConfiguration that = (ScrollBarConfiguration) o;
		return kw == that.kw && state == that.state && thumbPosition == that.thumbPosition
			&& thumbExtent == that.thumbExtent && noTrack == that.noTrack;
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(super.hashCode(), kw, state, thumbPosition, thumbExtent, noTrack);
	}

	@Override
	public @NotNull String toString()
	{
		return super.toString() + " " + kw + " " + state
			+ " " + JNRUtils.format2(thumbPosition) + " " + JNRUtils.format2(thumbExtent)
			+ (noTrack ? " [no track]" : "");
	}
}
