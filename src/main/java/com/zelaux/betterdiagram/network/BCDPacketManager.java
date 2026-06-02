package com.zelaux.betterdiagram.network;

import com.zelaux.betterdiagram.BetterContraptionDiagram;
import foundry.veil.api.network.VeilPacketManager;

public class BCDPacketManager {
    public static final VeilPacketManager INSTANCE = VeilPacketManager.create(BetterContraptionDiagram.MODID, "1");

    public static void init() {
        INSTANCE.registerClientbound(HelloMessage.TYPE, HelloMessage.CODEC, HelloMessage::handle, true);

        INSTANCE.registerClientbound(DiagramOpenBCDDataPacket.TYPE, DiagramOpenBCDDataPacket.CODEC, DiagramOpenBCDDataPacket::handleClient, true);
        INSTANCE.registerServerbound(DiagramSaveBCDDataPacket.TYPE, DiagramSaveBCDDataPacket.CODEC, DiagramSaveBCDDataPacket::handleServer, true);
    }
}
