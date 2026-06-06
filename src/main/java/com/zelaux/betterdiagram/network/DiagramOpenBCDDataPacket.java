package com.zelaux.betterdiagram.network;

import com.zelaux.betterdiagram.BetterContraptionDiagram;
import com.zelaux.betterdiagram.data.BCDData;
import com.zelaux.betterdiagram.extend.IDiagramScreen;
import foundry.veil.api.network.handler.ClientPacketContext;
import lombok.NonNull;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;


public record DiagramOpenBCDDataPacket(@NonNull BCDData data) implements CustomPacketPayload, OptionalPacketPayload {
    public static final Type<DiagramOpenBCDDataPacket> TYPE = new Type<>(BetterContraptionDiagram.resource("transfer_bcddata"));

    public static final StreamCodec<RegistryFriendlyByteBuf, DiagramOpenBCDDataPacket> CODEC = StreamCodec.composite(
        BCDData.STREAM_CODEC, DiagramOpenBCDDataPacket::data,
        DiagramOpenBCDDataPacket::new
    );


    public void handleClient(final ClientPacketContext context) {
        if(Minecraft.getInstance().screen instanceof IDiagramScreen screen) {
            screen.bcd$applyBCDDATA(data);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
