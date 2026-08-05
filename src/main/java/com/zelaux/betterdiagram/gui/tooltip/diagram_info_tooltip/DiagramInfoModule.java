package com.zelaux.betterdiagram.gui.tooltip.diagram_info_tooltip;

import net.minecraft.network.chat.Component;

import java.util.List;

public interface DiagramInfoModule {

    void appendInto(List<Component> list, int tooltipColor);

    default boolean onClick(){
        return false;
    }
    default boolean canClick(){
        return false;
    }
}
