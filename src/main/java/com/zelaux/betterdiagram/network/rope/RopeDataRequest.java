package com.zelaux.betterdiagram.network.rope;

import com.zelaux.betterdiagram.BetterContraptionDiagram;
import com.zelaux.betterdiagram.network.OptionalPacketPayload;
import dev.simulated_team.simulated.content.blocks.rope.strand.client.ClientRopeStrand;
import dev.simulated_team.simulated.content.entities.diagram.screen.DiagramScreen;
import foundry.veil.api.network.handler.ServerPacketContext;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;

import java.util.UUID;

public record RopeDataRequest(UUID ropeUUID) implements CustomPacketPayload, OptionalPacketPayload {
    public static final Type<RopeDataRequest> TYPE = new Type<>(BetterContraptionDiagram.resource("rope_data_request"));

    public static final StreamCodec<ByteBuf, RopeDataRequest> STREAM_CODEC = UUIDUtil.STREAM_CODEC.map(
        RopeDataRequest::new,
        RopeDataRequest::ropeUUID
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handleServer(ServerPacketContext context) {
        if(!(context.level() instanceof ServerLevel level)) return;
        RopeDataPacket.request(
            level,
            context.player(),
            ropeUUID
        );
    }

    static int lastTicks = 0;

    public static void tick() {
        if(!OptionalPacketPayload.isServerSupports(TYPE)) return;

        int currentTicks = AnimationTickHolder.getTicks(true);
        if(currentTicks - lastTicks >= DiagramScreen.UPDATE_REQUEST_INTERVAL) {
            lastTicks = currentTicks;
            ObjectSet<UUID> ropes = RopeDataRequest.requestedRopes;
            for(var rope : ropes) {
                new RopeDataRequest(rope).sendServerWithoutCheck();
            }
            ropes.clear();
        }
    }

    private static final ObjectSet<UUID> requestedRopes = new ObjectOpenHashSet<>();


    public static void requestRope(ClientRopeStrand strand) {
        requestedRopes.add(strand.getUuid());
    }
}
