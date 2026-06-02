package com.zelaux.betterdiagram.mixin.server;

import com.zelaux.betterdiagram.BetterContraptionDiagramClient;
import com.zelaux.betterdiagram.data.BCDData;
import com.zelaux.betterdiagram.extend.IDiagramScreen;
import com.zelaux.betterdiagram.extend.WithClientData;
import com.zelaux.betterdiagram.network.BCDDataTransfer;
import dev.ryanhcode.sable.sublevel.ClientSubLevel;
import dev.simulated_team.simulated.content.entities.diagram.DiagramEntity;
import dev.simulated_team.simulated.content.entities.diagram.screen.DiagramScreen;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

import java.util.Objects;

@Mixin(DiagramScreen.class)
public abstract class DiagramScreenMixin implements IDiagramScreen {
    @Shadow
    @Final
    private DiagramEntity diagram;
    @Shadow
    public static int UPDATE_REQUEST_INTERVAL;
    @Shadow
    @Final
    public ClientSubLevel subLevel;


    @Unique
    private BCDData bcd$prevData;
    @Unique
    private int bcd$ticksWithoutUpdate;

    @Override
    public void bcd$applyBCDDATA(BCDData data) {
        if(diagram == null) return;
        ((WithClientData) diagram).bcdiagram$updateData(data);
        bcd$prevData = data;

    }

    @Inject(
        method = "<init>",
        at=@At("CTOR_HEAD")
    )
    private static void resetData(DiagramEntity diagramEntity, ClientSubLevel subLevel, CallbackInfo ci) {
        if(!BetterContraptionDiagramClient.isServerSideInstalled) return;
        ((WithClientData) diagramEntity).bcdiagram$setDataSilent(null);
    }

    @Inject(method = "tick",
        at = @At("HEAD")
    )
    public void tick(CallbackInfo ci) {
        WithClientData clientData = (WithClientData) diagram;
        BCDData data = clientData.bcdiagram$dataOrNull();
        if(!Objects.equals(data, bcd$prevData)) {
            if(BetterContraptionDiagramClient.isServerSideInstalled) {
                BCDDataTransfer.whenSave(diagram.getId(), data)
                               .trySendServer();
            }
        }
        bcd$prevData = data;
    }


    @Inject(method = "onClose", at = @At("TAIL"))
    public void onClose(CallbackInfo ci) {
        if(!BetterContraptionDiagramClient.isServerSideInstalled) return;
        WithClientData withClientData = (WithClientData) diagram;
        withClientData.bcdiagram$setDataSilent(null);
    }

}
