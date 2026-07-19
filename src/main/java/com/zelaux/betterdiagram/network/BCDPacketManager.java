package com.zelaux.betterdiagram.network;

import com.zelaux.betterdiagram.BetterContraptionDiagram;
import com.zelaux.betterdiagram.network.bcddata.DiagramOpenBCDDataPacket;
import com.zelaux.betterdiagram.network.bcddata.DiagramSaveBCDDataPacket;
import com.zelaux.betterdiagram.network.extradata.DiagramExtraDataPacket;
import com.zelaux.betterdiagram.network.features.ModFeaturesPacket;
import com.zelaux.betterdiagram.network.rope.RopeDataPacket;
import com.zelaux.betterdiagram.network.rope.RopeDataRequest;
import foundry.veil.api.network.VeilPacketManager;

public class BCDPacketManager {
    public static final VeilPacketManager INSTANCE = VeilPacketManager.create(BetterContraptionDiagram.MODID, "1");

    public static void init() {
        INSTANCE.registerClientbound(HelloMessage.TYPE, HelloMessage.CODEC, HelloMessage::handle, true);

        INSTANCE.registerClientbound(DiagramOpenBCDDataPacket.TYPE, DiagramOpenBCDDataPacket.CODEC, DiagramOpenBCDDataPacket::handleClient, true);
        INSTANCE.registerServerbound(DiagramSaveBCDDataPacket.TYPE, DiagramSaveBCDDataPacket.CODEC, DiagramSaveBCDDataPacket::handleServer, true);
        INSTANCE.registerClientbound(DiagramExtraDataPacket.TYPE, DiagramExtraDataPacket.STREAM_CODEC, DiagramExtraDataPacket::handleClient, true);

        INSTANCE.registerClientbound(RopeDataPacket.TYPE, RopeDataPacket.STREAM_CODEC, RopeDataPacket::handleClient, true);
        INSTANCE.registerServerbound(RopeDataRequest.TYPE, RopeDataRequest.STREAM_CODEC, RopeDataRequest::handleServer, true);

        INSTANCE.registerClientbound(ModFeaturesPacket.TYPE_S2C, ModFeaturesPacket.STREAM_CODEC, ModFeaturesPacket::handleClient, true);
        INSTANCE.registerServerbound(ModFeaturesPacket.TYPE_C2S, ModFeaturesPacket.STREAM_CODEC, ModFeaturesPacket::handleServer, true);
    }
}
