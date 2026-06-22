package com.zelaux.betterdiagram.mixin.include_subsublevel;

import com.llamalad7.mixinextras.sugar.Local;
import com.zelaux.betterdiagram.feature.include_subsublevel.IDiagramScreenWithDependencies;
import com.zelaux.betterdiagram.feature.include_subsublevel.SubLevelDependenciesData;
import com.zelaux.betterdiagram.feature.include_subsublevel.connector.ConstraintTree;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.simulated_team.simulated.content.entities.diagram.DiagramConfig;
import dev.simulated_team.simulated.content.entities.diagram.DiagramEntity;
import dev.simulated_team.simulated.content.entities.diagram.screen.DiagramScreen;
import dev.simulated_team.simulated.network.packets.contraption_diagram.DiagramDataPacket;
import dev.simulated_team.simulated.network.packets.contraption_diagram.RequestDiagramDataPacket;
import foundry.veil.api.network.VeilPacketManager;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

import java.util.UUID;

@Mixin(DiagramScreen.class)
public abstract class DiagramScreenMixin implements IDiagramScreenWithDependencies {

    @Shadow
    private @Nullable DiagramDataPacket serverData;

    @Inject(method = "tick", at = @At("TAIL"))
    public void tick(CallbackInfo ci) {
        if(bcd$subLevelDependencies != null) {
            serverData = bcd$subLevelDependencies.mergedForces();
        }
    }
    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Ldev/simulated_team/simulated/network/packets/contraption_diagram/RequestDiagramDataPacket;<init>(Ljava/util/UUID;)V"))
    public void tick1(CallbackInfo ci) {

        if(bcd$subLevelDependencies != null) {
            for(UUID uuid : bcd$subLevelDependencies.keySet()) {
                VeilPacketManager.server().sendPacket(new RequestDiagramDataPacket(uuid));
            }
        }
    }


    @Inject(method = "open", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;setScreen(Lnet/minecraft/client/gui/screens/Screen;)V"))
    private static void open(DiagramEntity diagramEntity, DiagramConfig config, SubLevel subLevel, CallbackInfo ci,
                             @Local(name = "screen") DiagramScreen screen
    ) {
        IDiagramScreenWithDependencies dependencies = (IDiagramScreenWithDependencies) screen;

        ConstraintTree tree = ConstraintTree.build(subLevel, 3);
        if(!tree.children.isEmpty() || !tree.mergedSubLevel.single()) {

            SubLevelDependenciesData data = new SubLevelDependenciesData(tree);
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
