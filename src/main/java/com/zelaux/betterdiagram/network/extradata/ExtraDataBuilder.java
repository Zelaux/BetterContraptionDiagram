package com.zelaux.betterdiagram.network.extradata;

import com.zelaux.betterdiagram.mixin.accessors.FloatingBlockDataAccessors;
import dev.ryanhcode.sable.physics.floating_block.FloatingBlockCluster;
import dev.ryanhcode.sable.physics.floating_block.FloatingBlockMaterial;
import dev.ryanhcode.sable.physics.floating_block.FloatingClusterContainer;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ExtraDataBuilder {

    static @NotNull ObjectArrayList<ClusterInfo> collectClustersInfo(List<FloatingClusterContainer> floatingClusterContainers) {
        var clusters = new ObjectArrayList<ClusterInfo>();
        var rawCluster = new Object2DoubleOpenHashMap<FloatingBlockMaterial>();
        for(FloatingClusterContainer container : floatingClusterContainers) {
            for(FloatingBlockCluster cluster : container.clusters) {
                FloatingBlockDataAccessors accessors = (FloatingBlockDataAccessors) cluster.getBlockData();

                double scale = accessors.bcd$totalScale();
                FloatingBlockMaterial material = cluster.getMaterial();
                if(material.scaleWithPressure()) {
                    scale *= accessors.bcd$latestPressureScale();
                }
                rawCluster.addTo(material, scale);

            }
        }
        for(var entry : rawCluster.object2DoubleEntrySet()) {
            clusters.add(new ClusterInfo(
                entry.getKey(),
                entry.getDoubleValue()
            ));
        }
        return clusters;
    }
}
