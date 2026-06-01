package com.zelaux.betterdiagram.mixin.leveldata;

import com.zelaux.betterdiagram.leveldata.LevelDatas;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

import javax.annotation.Nullable;

@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin {
    @Shadow
    @Nullable
    public abstract ServerData getServerData();

    @Inject(
        method = "handleLogin",
        at = @At(
            value = "INVOKE",
            shift = At.Shift.AFTER,
            target = "Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/util/thread/BlockableEventLoop;)V"
        )
    )
    public void onOnGameJoin(ClientboundLoginPacket packet, CallbackInfo info) {
        LevelDatas.locateLevelData(getServerData());
    }
}
