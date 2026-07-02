package com.zelaux.betterdiagram.network;

import com.zelaux.betterdiagram.BetterContraptionDiagram;
import foundry.veil.api.network.handler.ClientPacketContext;
import foundry.veil.api.network.handler.ServerPacketContext;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

import java.util.WeakHashMap;

public record ModAPIVersionPacket(Type<ModAPIVersionPacket> type,ModAPIVersion version)implements CustomPacketPayload,OptionalPacketPayload {
    public static final Type<ModAPIVersionPacket> TYPE_C2S = new Type<>(BetterContraptionDiagram.resource("api_version"));
    public static final Type<ModAPIVersionPacket> TYPE_S2C = new Type<>(BetterContraptionDiagram.resource("api_version1"));

    public static final ModAPIVersionPacket CLIENT_TO_SERVER = new ModAPIVersionPacket(TYPE_C2S, ModAPIVersion.LATEST);
    public static final ModAPIVersionPacket SERVER_TO_CLIENT = new ModAPIVersionPacket(TYPE_S2C, ModAPIVersion.LATEST);

    public static ModAPIVersion SERVER_API_VERSION=ModAPIVersion.NONE;
    public static final StreamCodec<RegistryFriendlyByteBuf, ModAPIVersionPacket> STREAM_CODEC = StreamCodec.composite(
        ModAPIVersion.CODEC,ModAPIVersionPacket::version,
        ByteBufCodecs.BOOL,x->x.type==TYPE_C2S,
        (version,isC2S)->new ModAPIVersionPacket(isC2S?TYPE_C2S:TYPE_S2C,version)
    );

    private static final WeakHashMap<ServerGamePacketListenerImpl,ModAPIVersion> playerVersionMap=new WeakHashMap<>();
    public void handleServer(final ServerPacketContext context) {
        playerVersionMap.put(context.player().connection,version);
    }
    public void handleClient(final ClientPacketContext context){
        CLIENT_TO_SERVER.trySendServer();
        SERVER_API_VERSION=version;
    }

    public enum ModAPIVersion{
        NONE,
        API_0,
        NEWER
        ;

        @Getter
        public final int id=ordinal();
        public final static ModAPIVersion[] all=values();
        public static final ModAPIVersion LATEST = all[NEWER.id-1];
        public static final StreamCodec<ByteBuf, ModAPIVersion> CODEC =
            ByteBufCodecs.INT.map(i -> all[Math.min(NEWER.id, i)],
                ModAPIVersion::id);

    }
}
