package com.zelaux.betterdiagram.network;

import com.zelaux.betterdiagram.BetterContraptionDiagram;
import com.zelaux.betterdiagram.data.BCDData;
import com.zelaux.betterdiagram.extend.ServerSideData;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.simulated_team.simulated.content.entities.diagram.DiagramEntity;
import foundry.veil.api.network.handler.ServerPacketContext;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

import java.util.Optional;


public record DiagramSaveBCDDataPacket(int entityID, Optional<BCDData> config) implements CustomPacketPayload ,OptionalPacketPayload{
    public static final Type<DiagramSaveBCDDataPacket> TYPE = new Type<>(BetterContraptionDiagram.resource("save_bcddata"));

    public static final StreamCodec<RegistryFriendlyByteBuf, DiagramSaveBCDDataPacket> CODEC = StreamCodec.composite(
        ByteBufCodecs.INT, DiagramSaveBCDDataPacket::entityID,
        BCDData.STREAM_OPTIONAL_CODEC, DiagramSaveBCDDataPacket::config,
        DiagramSaveBCDDataPacket::new
    );


    public void handleServer(final ServerPacketContext context) {
        final Level level = context.level();

        final Entity entity = level.getEntity(this.entityID());

        if(entity instanceof final DiagramEntity diagram && entity.distanceToSqr(context.player()) < 64.0 * 64.0) {
            final SubLevel subLevel = Sable.HELPER.getContaining(diagram);
            if(subLevel == null) return;

            ((ServerSideData) diagram).bcd$storredData(config.orElse(null));

        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
