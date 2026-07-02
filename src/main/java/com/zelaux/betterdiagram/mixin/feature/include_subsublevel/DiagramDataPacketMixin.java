package com.zelaux.betterdiagram.mixin.feature.include_subsublevel;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.zelaux.betterdiagram.feature.include_subsublevel.IDiagramScreenWithDependencies;
import com.zelaux.betterdiagram.feature.include_subsublevel.SubLevelDependenciesData;
import com.zelaux.betterdiagram.struct.SubLevelWithDistance;
import com.zelaux.betterdiagram.util.CenterMassCalculator;
import dev.ryanhcode.sable.api.physics.force.ForceGroups;
import dev.ryanhcode.sable.api.physics.force.QueuedForceGroup;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.simulated_team.simulated.content.entities.diagram.screen.DiagramScreen;
import dev.simulated_team.simulated.network.packets.contraption_diagram.DiagramDataPacket;
import org.joml.Vector3dc;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;

import java.util.Comparator;
import java.util.List;

@Mixin(DiagramDataPacket.class)
public abstract class DiagramDataPacketMixin {
    @WrapOperation(
        method = "handle(Ldev/simulated_team/simulated/network/packets/contraption_diagram/DiagramDataPacket;)V",
        at = @At(value = "INVOKE", target = "Ldev/simulated_team/simulated/content/entities/diagram/screen/DiagramScreen;updateData(Ldev/simulated_team/simulated/network/packets/contraption_diagram/DiagramDataPacket;)V")
    )
    private static void handle(final DiagramScreen instance,final DiagramDataPacket data, Operation<Void> original) {
        List<QueuedForceGroup.PointForce> gravity = data.forces().get(ForceGroups.GRAVITY.get());
        if(gravity == null || gravity.size() != 1) {
            original.call(instance, data);
            return;
        }
        QueuedForceGroup.PointForce force = gravity.getFirst();
        Vector3dc packetCOM = force.point();
        if(!(instance instanceof IDiagramScreenWithDependencies dependencies)) {
            original.call(instance, data);
            return;
        }
        SubLevelDependenciesData dependenciesData = dependencies.bcd$subLevelDependencies();
        if(dependenciesData==null) {
            original.call(instance, data);
            return;
        }
        var found = dependenciesData.allSublevels()
                                    .map(x -> new SubLevelWithDistance(x, CenterMassCalculator.centerOfMass(x).distance(packetCOM)))
                                    .filter(x -> x.distance() < 0.0625f)
                                    .min(Comparator.comparingDouble(SubLevelWithDistance::distance));
        if(found.isEmpty()) {
            return;
        }
        SubLevel subLevel = found.get().subLevel();
        dependenciesData.updateData(subLevel, data);

    }

}
