package com.zelaux.betterdiagram.network.extradata;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zelaux.betterdiagram.BetterContraptionDiagram;
import com.zelaux.betterdiagram.feature.include_subsublevel.IDiagramScreenWithDependencies;
import com.zelaux.betterdiagram.feature.include_subsublevel.SubLevelDependenciesData;
import com.zelaux.betterdiagram.mixin.feature.extra_info_deliver.FloatingBlockControllerExt;
import com.zelaux.betterdiagram.network.OptionalPacketPayload;
import com.zelaux.betterdiagram.index.BCodecs;
import dev.ryanhcode.sable.api.block.BlockSubLevelLiftProvider;
import dev.ryanhcode.sable.api.physics.mass.MassTracker;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import foundry.veil.api.network.handler.ClientPacketContext;
import foundry.veil.api.util.CodecUtil;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.joml.Matrix3dc;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public record DiagramExtraDataPacket(
    UUID sublevel,
    Matrix3dc inertiaTensor,
    List<ClusterInfo> clusters,
    Optional<List<BlockSubLevelLiftProvider.LiftProviderContext>> liftProviders,
    Optional<MassTracker> massTracker
) implements CustomPacketPayload, OptionalPacketPayload {
    public static final Type<DiagramExtraDataPacket> TYPE = new Type<>(BetterContraptionDiagram.resource("extra_diagram"));


    public static final Codec<DiagramExtraDataPacket> CODEC = RecordCodecBuilder.create(i -> i.group(
        UUIDUtil.CODEC.fieldOf("subLevel").forGetter(DiagramExtraDataPacket::sublevel),
        CodecUtil.MATRIX3D_CODEC.fieldOf("inertiaTensor").forGetter(DiagramExtraDataPacket::inertiaTensor),
        ClusterInfo.CODEC.listOf().fieldOf("clusters").forGetter(DiagramExtraDataPacket::clusters),
        BCodecs.LIFT_PROVIDER_CONTEXT_CODEC.listOf().optionalFieldOf("liftProviders").forGetter(DiagramExtraDataPacket::liftProviders),
        BCodecs.MASS_TRACKER_CODEC.optionalFieldOf("massTracker").forGetter(DiagramExtraDataPacket::massTracker)
    ).apply(i, DiagramExtraDataPacket::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, DiagramExtraDataPacket> STREAM_CODEC = ByteBufCodecs.fromCodecWithRegistries(CODEC);

    public static DiagramExtraDataPacket make(ServerSubLevel subLevel) {

        var floatingClusterContainers = ((FloatingBlockControllerExt) subLevel.getFloatingBlockController()).bcd$containers();
        var clusters = ExtraDataBuilder.collectClustersInfo(floatingClusterContainers);
        return new DiagramExtraDataPacket(
            subLevel.getUniqueId(),
            subLevel.getMassTracker().getInertiaTensor(),
            clusters,
            Optional.of(subLevel.getPlot().getLiftProviders()).map(ObjectArrayList::new),
            Optional.of(subLevel.getSelfMassTracker())
        );
    }

    public void handleClient(final ClientPacketContext context) {
        if(Minecraft.getInstance().screen instanceof IDiagramScreenWithDependencies screenWithDependencies) {
            SubLevelDependenciesData dependenciesData = screenWithDependencies.bcd$subLevelDependencies();
            if(dependenciesData != null) {
                dependenciesData.updateData(sublevel, this);
            }
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

}
