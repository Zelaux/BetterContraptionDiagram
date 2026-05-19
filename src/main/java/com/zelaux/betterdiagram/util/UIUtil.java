package com.zelaux.betterdiagram.util;

import com.simibubi.create.foundation.gui.widget.Label;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class UIUtil {
    public static FrameLayout stackFill(LayoutElement... widgets) {
        var layout =  new FrameLayout(0, 0);;
        for(var widget : widgets) layout.addChild(widget);
        return layout;
    }
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

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static List<FormattedText> formattedText(List<Component> components) {
        return (List) components;
    }
}
