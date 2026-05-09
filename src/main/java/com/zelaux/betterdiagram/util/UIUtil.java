package com.zelaux.betterdiagram.util;

import com.simibubi.create.foundation.gui.widget.Label;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.NotNull;

public class UIUtil {
    public static LinearLayout horizontal(int spacing, LayoutElement... widgets) {
        LinearLayout layout = LinearLayout.horizontal().spacing(spacing);
        for(var widget : widgets) layout.addChild(widget);
        return layout;
    }

    public static LinearLayout vertical(int spacing, LayoutElement... widgets) {
        LinearLayout layout = LinearLayout.vertical().spacing(spacing);
        for(var widget : widgets) layout.addChild(widget);
        return layout;
    }

    public static @NotNull Label label(MutableComponent text) {
        var label = new Label(0, 0, text);
        label.text = text;
        return label;
    }
}
