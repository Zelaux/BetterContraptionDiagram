package com.zelaux.betterdiagram.mixin.feature.include_subsublevel;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.zelaux.betterdiagram.compat.CompatHandles;
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
import org.spongepowered.asm.mixin.injection.callback.*;

import java.util.Comparator;
import java.util.List;

@Mixin(DiagramDataPacket.class)
public abstract class DiagramDataPacketMixin {
    @WrapOperation(
        method = "handle(Ldev/simulated_team/simulated/network/packets/contraption_diagram/DiagramDataPacket;)V",
        at = @At(value = "INVOKE", target = "Ldev/simulated_team/simulated/content/entities/diagram/screen/DiagramScreen;updateData(Ldev/simulated_team/simulated/network/packets/contraption_diagram/DiagramDataPacket;)V")
    )
    private static void handle(final DiagramScreen instance, final DiagramDataPacket data, Operation<Void> original, @Share("bcd$visited")LocalBooleanRef ref) {

        if(!tryToHandlePacket(instance, data)) {
            original.call(instance, data);
        }else ref.set(true);
    }
    @Inject(
        method = "handle(Ldev/simulated_team/simulated/network/packets/contraption_diagram/DiagramDataPacket;)V",
        at = @At(value = "TAIL")
    )
    private static void handle_tail(
        DiagramDataPacket packet, CallbackInfo ci, @Share("bcd$visited")LocalBooleanRef ref) {
        if(!ref.get() && CompatHandles.gadgetsScreen != null) {
            tryToHandlePacket(CompatHandles.gadgetsScreen, packet);
        }
    }

    private static boolean tryToHandlePacket(DiagramScreen instance, DiagramDataPacket data) {
        List<QueuedForceGroup.PointForce> gravity = data.forces().get(ForceGroups.GRAVITY.get());
        if(gravity == null || gravity.size() != 1) {
            return false;
        }
        QueuedForceGroup.PointForce force = gravity.getFirst();
        Vector3dc packetCOM = force.point();
        if(!(instance instanceof IDiagramScreenWithDependencies dependencies)) {
            return false;
        }
        SubLevelDependenciesData dependenciesData = dependencies.bcd$subLevelDependencies();
        if(dependenciesData == null) {
            return false;
        }
        var found = dependenciesData.allSublevels()
                                    .map(x -> new SubLevelWithDistance(x, CenterMassCalculator.centerOfMass(x).distance(packetCOM)))
                                    .filter(x -> x.distance() < 0.0625f)
                                    .min(Comparator.comparingDouble(SubLevelWithDistance::distance));
        if(found.isEmpty()) {
            return true;
        }
        SubLevel subLevel = found.get().subLevel();
        dependenciesData.updateData(subLevel, data);
        return true;
    }

}
