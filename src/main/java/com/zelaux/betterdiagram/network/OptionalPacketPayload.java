package com.zelaux.betterdiagram.network;

import foundry.veil.Veil;
import foundry.veil.api.network.VeilPacketManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.registration.NetworkRegistry;

@SuppressWarnings("UnstableApiUsage")
public interface OptionalPacketPayload {
    default void trySend(ServerPlayer player){
        CustomPacketPayload payload = (CustomPacketPayload) this;
        if(NetworkRegistry.hasChannel(player.connection,payload.type().id())) {
            player.connection.send(new ClientboundCustomPayloadPacket(payload));
        }
    }
    default void trySendServer(){
        Minecraft instance = Minecraft.getInstance();
        if(instance==null)return;
        ClientPacketListener connection = instance.getConnection();
        if(connection==null)return;
        VeilPacketManager.server().sendPacket();
        CustomPacketPayload payload = (CustomPacketPayload) this;
        if(NetworkRegistry.hasChannel(connection,payload.type().id())) {
            connection.send(new ServerboundCustomPayloadPacket(payload));
        }
    }
}
