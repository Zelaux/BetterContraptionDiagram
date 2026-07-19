package com.zelaux.betterdiagram.network;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.registration.NetworkRegistry;

@SuppressWarnings("UnstableApiUsage")
public interface OptionalPacketPayload {
    default void trySend(ServerPlayer player) {
        if(isPlayerSupports(player)) {
            player.connection.send(new ClientboundCustomPayloadPacket((CustomPacketPayload) this));
        }
    }

    default boolean isPlayerSupports(ServerPlayer player) {
        CustomPacketPayload payload = (CustomPacketPayload) this;
        CustomPacketPayload.Type<? extends CustomPacketPayload> type = payload.type();
        return isPlayerSupports(player, type);
    }

    static boolean isPlayerSupports(ServerPlayer player, CustomPacketPayload.Type<? extends CustomPacketPayload> type) {
        return NetworkRegistry.hasChannel(player.connection, type.id());
    }

    default void trySendServer() {
        if(isServerSupports()) {
            sendServerWithoutCheck();
        }
    }

     default void sendServerWithoutCheck() {
        Minecraft.getInstance().getConnection().send(new ServerboundCustomPayloadPacket((CustomPacketPayload) this));
    }

    default boolean isServerSupports() {
        CustomPacketPayload payload = (CustomPacketPayload) this;
        return isServerSupports(payload.type());
    }

    static boolean isServerSupports(CustomPacketPayload.Type<? extends CustomPacketPayload> type) {
        Minecraft instance = Minecraft.getInstance();
        if(instance == null) return false;
        ClientPacketListener connection = instance.getConnection();
        if(connection == null) return false;
        return NetworkRegistry.hasChannel(connection, type.id());
    }
}
