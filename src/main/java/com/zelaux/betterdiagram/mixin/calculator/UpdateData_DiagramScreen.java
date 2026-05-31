package com.zelaux.betterdiagram.mixin.calculator;

import com.zelaux.betterdiagram.data.BCDData;
import com.zelaux.betterdiagram.extend.WithClientData;
import dev.simulated_team.simulated.content.entities.diagram.DiagramEntity;
import dev.simulated_team.simulated.content.entities.diagram.screen.DiagramScreen;
import dev.simulated_team.simulated.network.packets.contraption_diagram.DiagramDataPacket;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

@Mixin(DiagramScreen.class)
public abstract class UpdateData_DiagramScreen{

    @Shadow
    @Final
    private DiagramEntity diagram;

    @Shadow
    private @Nullable DiagramDataPacket serverData;

    @Inject(method = "updateData", at = @At("HEAD"))
    private void updateData(DiagramDataPacket packet, CallbackInfo ci) {
        WithClientData clientData = (WithClientData) diagram;
        BCDData data = clientData.bcdiagram$dataOrNull();
        if(data==null || serverData!=null && serverData.mass()==packet.mass())return;
        clientData.bcdiagram$updateData(data.withCache(null));
    }

}
