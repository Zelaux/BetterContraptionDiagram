package com.zelaux.betterdiagram.network;

import com.zelaux.betterdiagram.BetterContraptionDiagram;
import com.zelaux.betterdiagram.BetterContraptionDiagramClient;
import foundry.veil.api.network.handler.ClientPacketContext;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public enum HelloMessage implements CustomPacketPayload, OptionalPacketPayload {
    INSTANCE;
    public static final CustomPacketPayload.Type<HelloMessage> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(BetterContraptionDiagram.MODID, "hello"));
    public static final StreamCodec<? super FriendlyByteBuf, HelloMessage> CODEC = StreamCodec.unit(INSTANCE);

    public static void handle(HelloMessage message, ClientPacketContext player) {
        BetterContraptionDiagramClient.isServerSideInstalled = true;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
