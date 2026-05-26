package com.zelaux.betterdiagram.event;

import com.zelaux.betterdiagram.gui.OffCenteredBlockTooltipHandler;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(Dist.CLIENT)
public class BCD_ClientEvents {
    @SubscribeEvent
    public static void onTickPre(ClientTickEvent.Pre event) {
        OffCenteredBlockTooltipHandler.tick();
    }
}
