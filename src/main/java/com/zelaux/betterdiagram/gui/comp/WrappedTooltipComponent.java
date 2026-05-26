package com.zelaux.betterdiagram.gui.comp;

import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

public class WrappedTooltipComponent implements MockFormattedText,TooltipComponent{
    public final ClientTooltipComponent component;

    public WrappedTooltipComponent(ClientTooltipComponent component) {this.component = component;}

    public interface Wrappable extends ClientTooltipComponent{
        default WrappedTooltipComponent wrap(){
            return new WrappedTooltipComponent(this);
        }
    }
}
