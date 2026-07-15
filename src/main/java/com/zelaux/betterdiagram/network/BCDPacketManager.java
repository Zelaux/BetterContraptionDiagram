package com.zelaux.betterdiagram.network;

import com.zelaux.betterdiagram.BetterContraptionDiagram;
import com.zelaux.betterdiagram.network.bcddata.DiagramOpenBCDDataPacket;
import com.zelaux.betterdiagram.network.bcddata.DiagramSaveBCDDataPacket;
import com.zelaux.betterdiagram.network.extradata.DiagramExtraDataPacket;
import com.zelaux.betterdiagram.network.features.ModFeaturesPacket;
import foundry.veil.api.network.VeilPacketManager;

public class BCDPacketManager {
    public static final VeilPacketManager INSTANCE = VeilPacketManager.create(BetterContraptionDiagram.MODID, "1");

    public static void init() {
        INSTANCE.registerClientbound(HelloMessage.TYPE, HelloMessage.CODEC, HelloMessage::handle, true);

        INSTANCE.registerClientbound(DiagramOpenBCDDataPacket.TYPE, DiagramOpenBCDDataPacket.CODEC, DiagramOpenBCDDataPacket::handleClient, true);
        INSTANCE.registerServerbound(DiagramSaveBCDDataPacket.TYPE, DiagramSaveBCDDataPacket.CODEC, DiagramSaveBCDDataPacket::handleServer, true);
        INSTANCE.registerClientbound(DiagramExtraDataPacket.TYPE, DiagramExtraDataPacket.STREAM_CODEC, DiagramExtraDataPacket::handleClient, true);

        INSTANCE.registerClientbound(ModFeaturesPacket.TYPE_S2C, ModFeaturesPacket.STREAM_CODEC, ModFeaturesPacket::handleClient, true);
        INSTANCE.registerServerbound(ModFeaturesPacket.TYPE_C2S, ModFeaturesPacket.STREAM_CODEC, ModFeaturesPacket::handleServer, true);
    }
}
