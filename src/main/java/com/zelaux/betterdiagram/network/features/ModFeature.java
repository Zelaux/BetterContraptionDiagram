package com.zelaux.betterdiagram.network.features;

import com.google.common.collect.ImmutableSet;
import com.zelaux.betterdiagram.network.OptionalPacketPayload;
import com.zelaux.betterdiagram.network.extradata.DiagramExtraDataPacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;


public enum ModFeature {
    SEND_INERTIA(DiagramExtraDataPacket.TYPE),
    SEND_CLUSTERS(DiagramExtraDataPacket.TYPE),
    SEND_LIFT_BLOCK,
    ;

    public final static ModFeature[] all = values();
    public final CustomPacketPayload.Type<? extends CustomPacketPayload> relatedPacketType;

    ModFeature(CustomPacketPayload.Type<? extends CustomPacketPayload> relatedPacketType) {
        this.relatedPacketType = relatedPacketType;
    }

    ModFeature() {
        this(null);
    }

    public final static ImmutableSet<ModFeature> DEFAULT_FEATURE = ImmutableSet.of();


    public static final StreamCodec<ByteBuf, Optional<ModFeature>> CODEC =
        ByteBufCodecs.INT.map(i -> i < 0 || i >= all.length ? Optional.empty() : Optional.of(all[i]),
            x -> x.map(Enum::ordinal).orElse(-1));

    public static final StreamCodec<ByteBuf, ImmutableSet<ModFeature>> FEATURES_SET_CODEC =
        CODEC.apply(ByteBufCodecs.list(128))
             .map(x ->
                     ImmutableSet.copyOf(x.stream().filter(Optional::isPresent).map(Optional::get).toList()),
                 x -> x.stream().map(Optional::of).toList()
             );

    public boolean supportsOnServer() {
        if(relatedPacketType != null) return OptionalPacketPayload.isServerSupports(relatedPacketType);
        return ModFeaturesPacket.isServerSupports(this);
    }

    public boolean supportsOnClient(ServerPlayer player) {
        if(relatedPacketType != null) return OptionalPacketPayload.isPlayerSupports(player,relatedPacketType);
        return ModFeaturesPacket.isPlayerSupports(player, this);
    }
}
