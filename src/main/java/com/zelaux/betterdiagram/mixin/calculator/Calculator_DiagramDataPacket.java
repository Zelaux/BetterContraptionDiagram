package com.zelaux.betterdiagram.mixin.calculator;

import com.zelaux.betterdiagram.gui.CenterMassMovingScreen;
import dev.simulated_team.simulated.content.entities.diagram.screen.DiagramScreen;
import dev.simulated_team.simulated.network.packets.contraption_diagram.DiagramDataPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

@Mixin(DiagramDataPacket.class)
public class Calculator_DiagramDataPacket {
    @Inject(method = "handle(Ldev/simulated_team/simulated/network/packets/contraption_diagram/DiagramDataPacket;)V",at=@At("TAIL"))
    private static void mixin_handle(DiagramDataPacket packet, CallbackInfo ci){
        final Minecraft minecraft = Minecraft.getInstance();
        final Screen screen = minecraft.screen;

        if (screen instanceof final CenterMassMovingScreen diagramScreen) {
            diagramScreen.diagramScreen.updateData(packet);
        }
    }
}
