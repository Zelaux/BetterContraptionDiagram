package com.zelaux.betterdiagram.network;

import com.zelaux.betterdiagram.data.BCDData;
import lombok.NonNull;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;


public class BCDDataTransfer {

    public static OptionalPacketPayload whenOpen(@NonNull BCDData data) {
        return new DiagramOpenBCDDataPacket(data);
    }

    public static OptionalPacketPayload whenSave(int entityId,@Nullable BCDData data) {
        return new DiagramSaveBCDDataPacket(entityId,Optional.ofNullable(data));
    }
}
