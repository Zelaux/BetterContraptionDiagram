package com.zelaux.betterdiagram.network.rope;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zelaux.betterdiagram.BetterContraptionDiagram;
import com.zelaux.betterdiagram.feature.rope_data.IRopeData;
import com.zelaux.betterdiagram.network.OptionalPacketPayload;
import dev.simulated_team.simulated.content.blocks.rope.strand.client.ClientLevelRopeManager;
import dev.simulated_team.simulated.content.blocks.rope.strand.client.ClientRopeStrand;
import dev.simulated_team.simulated.content.blocks.rope.strand.server.ServerLevelRopeManager;
import dev.simulated_team.simulated.content.blocks.rope.strand.server.ServerRopeStrand;
import dev.simulated_team.simulated.content.entities.diagram.screen.DiagramScreen;
import foundry.veil.api.network.handler.ClientPacketContext;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record RopeDataPacket(
    UUID ropeStrandUUID,
    double extension_goal
) implements CustomPacketPayload, OptionalPacketPayload {
    public static final Type<RopeDataPacket> TYPE = new Type<>(BetterContraptionDiagram.resource("rope_data"));


    public static final Codec<RopeDataPacket> CODEC = RecordCodecBuilder.create(i -> i.group(
        UUIDUtil.CODEC.fieldOf("uuid").forGetter(RopeDataPacket::ropeStrandUUID),
        Codec.DOUBLE.fieldOf("extension_goal").forGetter(RopeDataPacket::extension_goal)
    ).apply(i, RopeDataPacket::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, RopeDataPacket> STREAM_CODEC = ByteBufCodecs.fromCodecWithRegistries(CODEC);
    private static MinecraftServer minecraftServer;

    public static void request(@NotNull ServerLevel level, @NotNull ServerPlayer player, UUID ropeUUID) {
        minecraftServer = level.getServer();
        tickets
            .computeIfAbsent(level.dimension(), it1 -> new Object2ObjectOpenHashMap<>())
            .computeIfAbsent(ropeUUID, it -> new ObjectOpenHashSet<>())
            .add(player);
    }

    static int lastTicks = 0;

    public static void tick() {
        int currentTicks = AnimationTickHolder.getTicks(true);
        MinecraftServer server = minecraftServer;
        if(currentTicks - lastTicks >= DiagramScreen.UPDATE_REQUEST_INTERVAL && server != null) {
            lastTicks = currentTicks;

            tickets.forEach((level, ropeWithPlayers) -> {
                ServerLevelRopeManager ropeManager = ServerLevelRopeManager.getOrCreate(server.getLevel(level));
                if(ropeManager == null || ropeManager.getAllStrands().isEmpty()) return;
                ropeWithPlayers.forEach((rope, players) -> {
                    ServerRopeStrand strand = ropeManager.getStrand(rope);
                    if(strand == null) return;
                    RopeDataPacket packet = new RopeDataPacket(rope, strand.getExtension());
                    for(ServerPlayer player : players) {
                        packet.trySend(player);
                    }
                });
            });
            tickets.clear();
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handleClient(ClientPacketContext context) {
        ClientRopeStrand strand = ClientLevelRopeManager.getOrCreate(context.level()).getStrand(ropeStrandUUID);
        if(strand != null) {
            ((IRopeData) strand).bcd$extension_goal(extension_goal);
        }
    }

    private static final Object2ObjectOpenHashMap<ResourceKey<Level>, Object2ObjectOpenHashMap<UUID, ObjectOpenHashSet<ServerPlayer>>> tickets = new Object2ObjectOpenHashMap<>();

}

