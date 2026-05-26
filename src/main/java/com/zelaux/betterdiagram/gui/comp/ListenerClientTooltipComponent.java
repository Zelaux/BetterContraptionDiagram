package com.zelaux.betterdiagram.gui.comp;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;

public interface ListenerClientTooltipComponent extends ClientTooltipComponent {
    void beforeRender(GuiGraphics graphics,
                      int tooltipX,
                      int tooltipY,
                      int width,
                      int height,
                      int borderColorStart,
                      int borderColorEnd,
                      int backgroundColor);


}
