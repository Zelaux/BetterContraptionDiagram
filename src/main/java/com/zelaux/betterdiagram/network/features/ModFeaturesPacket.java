package com.zelaux.betterdiagram.network.features;

import com.google.common.collect.ImmutableSet;
import com.zelaux.betterdiagram.BetterContraptionDiagram;
import com.zelaux.betterdiagram.network.OptionalPacketPayload;
import foundry.veil.api.network.handler.ClientPacketContext;
import foundry.veil.api.network.handler.ServerPacketContext;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

import java.util.List;
import java.util.WeakHashMap;

public record ModFeaturesPacket(Type<ModFeaturesPacket> type,
                                ImmutableSet<ModFeature> features) implements CustomPacketPayload, OptionalPacketPayload {
    public static final Type<ModFeaturesPacket> TYPE_C2S = new Type<>(BetterContraptionDiagram.resource("api_features"));
    public static final Type<ModFeaturesPacket> TYPE_S2C = new Type<>(BetterContraptionDiagram.resource("api_features1"));

    public static final ModFeaturesPacket CLIENT_TO_SERVER = new ModFeaturesPacket(TYPE_C2S, ImmutableSet.copyOf(List.of(ModFeature.all)));
    public static final ModFeaturesPacket SERVER_TO_CLIENT = new ModFeaturesPacket(TYPE_S2C, ImmutableSet.copyOf(List.of(ModFeature.all)));

    public static ImmutableSet<ModFeature> serverApiFeature = ModFeature.DEFAULT_FEATURE;

    public static final StreamCodec<RegistryFriendlyByteBuf, ModFeaturesPacket> STREAM_CODEC = StreamCodec.composite(
        ModFeature.FEATURES_SET_CODEC, ModFeaturesPacket::features,
        ByteBufCodecs.BOOL, x -> x.type == TYPE_C2S,
        (version, isC2S) -> new ModFeaturesPacket(isC2S ? TYPE_C2S : TYPE_S2C, version)
    );

    private static final WeakHashMap<ServerGamePacketListenerImpl, ImmutableSet<ModFeature>> playerFeatureMap = new WeakHashMap<>();

    public static void resetServerData() {
        serverApiFeature = ModFeature.DEFAULT_FEATURE;
    }

    public void handleServer(final ServerPacketContext context) {
        playerFeatureMap.put(context.player().connection, features);
    }

    public void handleClient(final ClientPacketContext context) {
        CLIENT_TO_SERVER.trySendServer();
        serverApiFeature = features;
    }

    public static boolean isServerSupports(ModFeature feature) {
        return serverApiFeature.contains(feature);
    }

    public static boolean isPlayerSupports(ServerPlayer player, ModFeature feature) {
        return playerFeatureMap.getOrDefault(player.connection, ModFeature.DEFAULT_FEATURE).contains(feature);
    }

}
