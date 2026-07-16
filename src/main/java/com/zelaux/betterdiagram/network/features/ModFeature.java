package com.zelaux.betterdiagram.network.features;

import com.google.common.collect.ImmutableSet;
import com.zelaux.betterdiagram.network.OptionalPacketPayload;
import com.zelaux.betterdiagram.network.extradata.DiagramExtraDataPacket;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.Getter;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;


@Getter
public enum ModFeature {
    SHARE_INERTIA("inertia", DiagramExtraDataPacket.TYPE),
    SHARE_CLUSTERS("clusters", DiagramExtraDataPacket.TYPE),
    SHARE_MASS_TRACKER("mass_tracker"),
    SHARE_LIFT_BLOCK("lift_block"),
    ;
    private static final Object2ObjectMap<String,ModFeature> idMap=new Object2ObjectOpenHashMap<>();
    public final static ModFeature[] all = values();
    public final CustomPacketPayload.Type<? extends CustomPacketPayload> relatedPacketType;
    public  final String identifier;
    static{
        for(ModFeature modFeature : all) idMap.put(modFeature.identifier, modFeature);
    }

    ModFeature(String id, CustomPacketPayload.Type<? extends CustomPacketPayload> relatedPacketType) {
        this.relatedPacketType = relatedPacketType;
        this.identifier = id;
    }

    ModFeature(String id) {
        this(id, null);
    }

    public final static ImmutableSet<ModFeature> DEFAULT_FEATURE = ImmutableSet.of();


    public static final StreamCodec<ByteBuf, Optional<ModFeature>> CODEC =
        ByteBufCodecs.STRING_UTF8.map(i -> Optional.ofNullable(idMap.get(i)),
            x -> x.map(ModFeature::identifier).orElse(""));

    public static final StreamCodec<ByteBuf, ImmutableSet<ModFeature>> FEATURES_SET_CODEC =
        CODEC.apply(ByteBufCodecs.list(256))
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
