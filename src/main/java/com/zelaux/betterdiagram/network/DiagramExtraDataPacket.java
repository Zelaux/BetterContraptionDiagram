package com.zelaux.betterdiagram.network;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zelaux.betterdiagram.BetterContraptionDiagram;
import com.zelaux.betterdiagram.extend.IDiagramScreen;
import com.zelaux.betterdiagram.feature.include_subsublevel.IDiagramScreenWithDependencies;
import com.zelaux.betterdiagram.feature.include_subsublevel.SubLevelDependenciesData;
import com.zelaux.betterdiagram.mixin.accessors.FloatingBlockDataAccessors;
import com.zelaux.betterdiagram.mixin.feature.extra_info_deliver.FloatingBlockControllerExt;
import dev.ryanhcode.sable.physics.floating_block.FloatingBlockCluster;
import dev.ryanhcode.sable.physics.floating_block.FloatingBlockMaterial;
import dev.ryanhcode.sable.physics.floating_block.FloatingClusterContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import foundry.veil.api.network.handler.ClientPacketContext;
import foundry.veil.api.util.CodecUtil;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3dc;

import java.util.List;
import java.util.UUID;

public record DiagramExtraDataPacket(UUID sublevel, Matrix3dc inertiaTensor,
                                     List<ClusterInfo> clusters) implements CustomPacketPayload, OptionalPacketPayload {
    public static final Type<DiagramExtraDataPacket> TYPE = new Type<>(BetterContraptionDiagram.resource("extra_diagram"));


    public static final Codec<DiagramExtraDataPacket> CODEC = RecordCodecBuilder.create(i -> i.group(
        UUIDUtil.CODEC.fieldOf("subLevel").forGetter(DiagramExtraDataPacket::sublevel),
        CodecUtil.MATRIX3D_CODEC.fieldOf("inertiaTensor").forGetter(DiagramExtraDataPacket::inertiaTensor),
        ClusterInfo.CODEC.listOf().fieldOf("clusters").forGetter(DiagramExtraDataPacket::clusters)
    ).apply(i, DiagramExtraDataPacket::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, DiagramExtraDataPacket> STREAM_CODEC = ByteBufCodecs.fromCodecWithRegistries(CODEC);

    public static DiagramExtraDataPacket make(ServerSubLevel subLevel) {

        var floatingClusterContainers = ((FloatingBlockControllerExt) subLevel.getFloatingBlockController()).bcd$containers();
        var clusters = collectClustersInfo(floatingClusterContainers);
        return new DiagramExtraDataPacket(
            subLevel.getUniqueId(),
            subLevel.getMassTracker().getInertiaTensor(),
            clusters
        );
    }

    private static @NotNull ObjectArrayList<ClusterInfo> collectClustersInfo(List<FloatingClusterContainer> floatingClusterContainers) {
        var clusters = new ObjectArrayList<ClusterInfo>();
        var rawCluster=new Object2DoubleOpenHashMap<FloatingBlockMaterial>();
        for(FloatingClusterContainer container : floatingClusterContainers) {
            for(FloatingBlockCluster cluster : container.clusters) {
                FloatingBlockDataAccessors accessors = (FloatingBlockDataAccessors) cluster.getBlockData();

                double scale = accessors.bcd$totalScale();
                FloatingBlockMaterial material = cluster.getMaterial();
                if (material.scaleWithPressure()) {
                    scale *= accessors.bcd$latestPressureScale();
                }
                rawCluster.addTo(material,scale);

            }
        }
        for(var entry : rawCluster.object2DoubleEntrySet()) {
            clusters.add(new ClusterInfo(
                entry.getKey(),
                entry.getDoubleValue()
            ));
        }
        ;
        return clusters;
    }

    public void handleClient(final ClientPacketContext context) {
        if(Minecraft.getInstance().screen instanceof IDiagramScreenWithDependencies screenWithDependencies) {
            SubLevelDependenciesData dependenciesData = screenWithDependencies.bcd$subLevelDependencies();
            if(dependenciesData!=null){
                dependenciesData.updateData(sublevel,this);
            }
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public record ClusterInfo(FloatingBlockMaterial material, double pressureTimesTotalScale) {
        public static final Codec<ClusterInfo> CODEC = RecordCodecBuilder.create(i -> i.group(
                FloatingBlockMaterial.CODEC.fieldOf("material").forGetter(ClusterInfo::material),
                Codec.DOUBLE.fieldOf("pressureTimesTotalScale").forGetter(ClusterInfo::pressureTimesTotalScale)
            ).apply(i, ClusterInfo::new)
        );
    }
}
