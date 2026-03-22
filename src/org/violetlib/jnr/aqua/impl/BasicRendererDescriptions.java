/*
 * Copyright (c) 2018-2025 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.aqua.impl;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.violetlib.jnr.aqua.*;
import org.violetlib.jnr.impl.BasicRendererDescription;
import org.violetlib.jnr.impl.RendererDescription;

/**

*/

public class BasicRendererDescriptions
  implements RendererDescriptions
{
    protected final @NotNull RendererDescriptionsImpl impl;
    private @Nullable RendererDescription override; // for debugging

    public BasicRendererDescriptions(@NotNull RendererDescriptionsImpl impl)
    {
        this.impl = impl;
    }

    @Override
    public void setOverride(@Nullable RendererDescription rd)
    {
        override = rd;
    }

    @Override
    public final @NotNull RendererDescription getButtonRendererDescription(@NotNull ButtonConfiguration g)
    {
        if (override != null) {
            return override;
        }
        return impl.getRendererDescription(g);
    }

    @Override
    public final @NotNull RendererDescription getSegmentedButtonRendererDescription(@NotNull SegmentedButtonConfiguration g)
    {
        if (override != null) {
            return override;
        }
        return impl.getRendererDescription(g);
    }

    @Override
    public final @Nullable RendererDescription getBasicPopupButtonRendererDescription(@NotNull PopupButtonConfiguration g)
    {
        return getPopupButtonRendererDescription(g);
    }

    @Override
    public final @NotNull RendererDescription getPopupButtonRendererDescription(@NotNull PopupButtonConfiguration g)
    {
        if (override != null) {
            return override;
        }
        return impl.getRendererDescription(g);
    }

    public final @NotNull RendererDescription getComboBoxRendererDescription(@NotNull ComboBoxConfiguration g)
    {
        if (override != null) {
            return override;
        }
        return impl.getRendererDescription(g);
    }

    @Override
    public @NotNull RendererDescription getSplitPaneDividerRendererDescription(@NotNull SplitPaneDividerConfiguration g)
    {
        if (override != null) {
            return override;
        }
        return impl.getRendererDescription(g);
    }

    @Override
    public final @NotNull RendererDescription getToolBarItemWellRendererDescription(@NotNull ToolBarItemWellConfiguration g)
    {
        if (override != null) {
            return override;
        }
        return impl.getToolBarItemWellRendererDescription(g);
    }

    @Override
    public final @NotNull RendererDescription getTitleBarRendererDescription(@NotNull TitleBarConfiguration g)
    {
        if (override != null) {
            return override;
        }
        return impl.getTitleBarRendererDescription(g);
    }

    @Override
    public final @NotNull RendererDescription getSliderRendererDescription(@NotNull SliderConfiguration g)
    {
        if (override != null) {
            return override;
        }
        return impl.getSliderRendererDescription(g);
    }

    @Override
    public final @NotNull RendererDescription getSliderTrackRendererDescription(@NotNull SliderConfiguration g)
    {
        if (override != null) {
            return override;
        }
        return impl.getSliderTrackRendererDescription(g);
    }

    @Override
    public final @NotNull RendererDescription getSliderTickMarkRendererDescription(@NotNull SliderConfiguration g)
    {
        if (override != null) {
            return override;
        }
        return impl.getCustomSliderTickMarkRendererDescription(g);
    }

    @Override
    public final @NotNull RendererDescription getSliderThumbRendererDescription(@NotNull SliderConfiguration g)
    {
        if (override != null) {
            return override;
        }
        return impl.getCustomSliderThumbRendererDescription(g);
    }

    @Override
    public final @NotNull RendererDescription getSpinnerArrowsRendererDescription(@NotNull SpinnerArrowsConfiguration g)
    {
        if (override != null) {
            return override;
        }
        return impl.getSpinnerArrowsRendererDescription(g);
    }

    @Override
    public final @NotNull RendererDescription getGroupBoxRendererDescription(@NotNull GroupBoxConfiguration g)
    {
        if (override != null) {
            return override;
        }
        return impl.getGroupBoxRendererDescription(g);
    }

    @Override
    public final @NotNull RendererDescription getListBoxRendererDescription(@NotNull ListBoxConfiguration g)
    {
        if (override != null) {
            return override;
        }
        return impl.getListBoxRendererDescription(g);
    }

    @Override
    public final @NotNull RendererDescription getTextFieldRendererDescription(@NotNull TextFieldConfiguration g)
    {
        if (override != null) {
            return override;
        }
        return impl.getTextFieldRendererDescription(g);
    }

    @Override
    public final @NotNull RendererDescription getScrollBarRendererDescription(@NotNull ScrollBarConfiguration g)
    {
        if (override != null) {
            return override;
        }
        return impl.getScrollBarRendererDescription(g);
    }

    @Override
    public final @NotNull RendererDescription getScrollColumnSizerRendererDescription(@NotNull ScrollColumnSizerConfiguration g)
    {
        if (override != null) {
            return override;
        }
        return new BasicRendererDescription(0, 0, 0, 0);    // obsolete
    }

    @Override
    public final @NotNull RendererDescription getProgressIndicatorRendererDescription(@NotNull ProgressIndicatorConfiguration g)
    {
        if (override != null) {
            return override;
        }
        return impl.getRendererDescription(g);
    }

    @Override
    public final @NotNull RendererDescription getIndeterminateProgressIndicatorRendererDescription(@NotNull IndeterminateProgressIndicatorConfiguration g)
    {
        if (override != null) {
            return override;
        }
        return impl.getRendererDescription(g);
    }

    @Override
    public final @NotNull RendererDescription getTableColumnHeaderRendererDescription(@NotNull TableColumnHeaderConfiguration g)
    {
        if (override != null) {
            return override;
        }
        return impl.getTableColumnHeaderRendererDescription(g);
    }

    @Override
    public final @NotNull RendererDescription getGradientRendererDescription(@NotNull GradientConfiguration g)
    {
        if (override != null) {
            return override;
        }
        return new BasicRendererDescription(0, 0, 0, 0);  // obsolete
    }
}
