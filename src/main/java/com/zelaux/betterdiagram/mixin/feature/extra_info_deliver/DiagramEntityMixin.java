package com.zelaux.betterdiagram.mixin.feature.extra_info_deliver;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.zelaux.betterdiagram.network.DiagramExtraDataPacket;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.simulated_team.simulated.content.entities.diagram.DiagramEntity;
import dev.simulated_team.simulated.network.packets.contraption_diagram.DiagramDataPacket;
import foundry.veil.api.network.VeilPacketManager;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(DiagramEntity.class)
public abstract class DiagramEntityMixin {
    @WrapOperation(method = "postPhysicsTick", at = @At(value = "INVOKE", target = "Ldev/simulated_team/simulated/content/entities/diagram/DiagramEntity;makeDiagramDataPacket(Ldev/ryanhcode/sable/sublevel/ServerSubLevel;)Ldev/simulated_team/simulated/network/packets/contraption_diagram/DiagramDataPacket;"))
    private static DiagramDataPacket postPhysicsTick_makePacket(
        ServerSubLevel subLevel,
        Operation<DiagramDataPacket> original,
        @Share("bcd$extra_data") LocalRef<DiagramExtraDataPacket> packetRef
    ) {
        packetRef.set(DiagramExtraDataPacket.make(subLevel));
        return original.call(subLevel);
    }

    @WrapOperation(method = "postPhysicsTick", at = @At(value = "INVOKE", target = "Lfoundry/veil/api/network/VeilPacketManager$PacketSink;sendPacket([Lnet/minecraft/network/protocol/common/custom/CustomPacketPayload;)V"))
    private static void postPhysicsTick_sendPackat(
        VeilPacketManager.PacketSink instance,
        CustomPacketPayload[] customPacketPayloads,
        Operation<Void> original,
        @Share("bcd$extra_data") LocalRef<DiagramExtraDataPacket> packetRef
    ) {
        DiagramExtraDataPacket packet = packetRef.get();
        if(packet != null && instance instanceof ServerPlayer player) {
            packet.trySend(player);
        }
        original.call(instance, customPacketPayloads);
    }
}
