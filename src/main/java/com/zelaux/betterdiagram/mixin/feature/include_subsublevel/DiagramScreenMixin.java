package com.zelaux.betterdiagram.mixin.feature.include_subsublevel;

import com.zelaux.betterdiagram.Config;
import com.zelaux.betterdiagram.extend.WithClientData;
import com.zelaux.betterdiagram.feature.include_subsublevel.IDiagramScreenWithDependencies;
import com.zelaux.betterdiagram.feature.include_subsublevel.SubLevelDependenciesData;
import com.zelaux.betterdiagram.util.CenterMassCalculator;
import dev.ryanhcode.sable.sublevel.ClientSubLevel;
import dev.simulated_team.simulated.content.entities.diagram.DiagramEntity;
import dev.simulated_team.simulated.content.entities.diagram.screen.DiagramScreen;
import dev.simulated_team.simulated.network.packets.contraption_diagram.DiagramDataPacket;
import dev.simulated_team.simulated.network.packets.contraption_diagram.RequestDiagramDataPacket;
import foundry.veil.api.network.VeilPacketManager;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

import java.util.UUID;

@Mixin(DiagramScreen.class)
public abstract class DiagramScreenMixin implements IDiagramScreenWithDependencies {

    @Shadow
    private @Nullable DiagramDataPacket serverData;

    @Shadow
    @Final
    private DiagramEntity diagram;

    @Shadow
    @Final
    public ClientSubLevel subLevel;

    @ModifyVariable(method = "renderCenterOfMass", at = @At(value = "STORE", ordinal = 0))
    protected Vector3d renderCenterOfMass(Vector3d COM) {
        SubLevelDependenciesData dependenciesData = bcd$subLevelDependencies();
        return CenterMassCalculator.locateMergedCenterOfMass(COM, dependenciesData, ((WithClientData)diagram).bcdDataNotNull_readOnly());
    }

    @Inject(method = "tick", at = @At("TAIL"))
    public void tick(CallbackInfo ci) {
        bcd$tryUpdateData();
    }

    @Override
    public void bcd$tryUpdateData() {
        if(bcd$subLevelDependencies != null) {
            if(((WithClientData) diagram).bcdDataNotNull_readOnly().shouldMergedForcesWithConnectedSublevels){
                serverData = bcd$subLevelDependencies.mergedForces();
            }else{
                serverData = bcd$subLevelDependencies.get(subLevel).data;
            }
        }
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Ldev/simulated_team/simulated/network/packets/contraption_diagram/RequestDiagramDataPacket;<init>(Ljava/util/UUID;)V"))
    public void tick1(CallbackInfo ci) {

        if(bcd$subLevelDependencies != null) {
            for(UUID uuid : bcd$subLevelDependencies.sublevelDataMap.keySet()) {
                VeilPacketManager.server().sendPacket(new RequestDiagramDataPacket(uuid));
            }
        }
    }


    @Inject(method = "<init>", at = @At(value = "TAIL"))
    private void open(DiagramEntity diagramEntity, ClientSubLevel subLevel, CallbackInfo ci
    ) {
        IDiagramScreenWithDependencies dependencies = (IDiagramScreenWithDependencies) this;

        var data = SubLevelDependenciesData.tryConstruct(subLevel, Config.SUBLEVEL_TREE_DEPTH.getAsInt());
        if(data != null) {
            dependencies.bcd$subLevelDependencies(data);
        }

    }


    @Unique
    SubLevelDependenciesData bcd$subLevelDependencies;

    @Override
    public @Nullable SubLevelDependenciesData bcd$subLevelDependencies() {
        return bcd$subLevelDependencies;
    }

    @Override
    public void bcd$subLevelDependencies(SubLevelDependenciesData data) {
        bcd$subLevelDependencies = data;

    }
}
