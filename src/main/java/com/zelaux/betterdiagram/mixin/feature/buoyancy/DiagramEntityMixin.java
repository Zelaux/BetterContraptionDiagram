package com.zelaux.betterdiagram.mixin.feature.buoyancy;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.zelaux.betterdiagram.ModFeature;
import com.zelaux.betterdiagram.network.ModAPIVersionPacket;
import com.zelaux.betterdiagram.util.MixinCalculatorUtil;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.simulated_team.simulated.content.entities.diagram.DiagramEntity;
import dev.simulated_team.simulated.network.packets.contraption_diagram.DiagramDataPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(DiagramEntity.class)
public class DiagramEntityMixin {
    @WrapOperation(method = "postPhysicsTick", at = @At(value = "INVOKE", target = "Ldev/simulated_team/simulated/content/entities/diagram/DiagramEntity;makeDiagramDataPacket(Ldev/ryanhcode/sable/sublevel/ServerSubLevel;)Ldev/simulated_team/simulated/network/packets/contraption_diagram/DiagramDataPacket;"))
    private static DiagramDataPacket postPhysicsTick_calcBuoyancy(
        ServerSubLevel subLevel,
        Operation<DiagramDataPacket> original,
        @Share("bcd$second") LocalRef<DiagramDataPacket[]> secondPacket
    ) {

        DiagramDataPacket dataPacket = original.call(subLevel);

        secondPacket.set(new DiagramDataPacket[]{MixinCalculatorUtil.caclPacketWithExtraForces(subLevel, dataPacket)});

        return dataPacket;
    }
    @ModifyArg(method = "postPhysicsTick", at = @At(value = "INVOKE", target = "Lfoundry/veil/api/network/VeilPacketManager$PacketSink;sendPacket([Lnet/minecraft/network/protocol/common/custom/CustomPacketPayload;)V"),index = 0)
    private static CustomPacketPayload[] postPhysicsTick_send(
        CustomPacketPayload[] payloads,
        @Share("bcd$second") LocalRef<DiagramDataPacket[]> secondPacket,
        @Local(index = 8)ServerPlayer player
        ) {
        if(ModAPIVersionPacket.ModAPIVersion.get(player).supports(ModFeature.Buoyancy)){
            return secondPacket.get();
        }
        return payloads;
    }

}
