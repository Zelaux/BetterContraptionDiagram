package com.zelaux.betterdiagram.network.extradata;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.ryanhcode.sable.physics.floating_block.FloatingBlockMaterial;

public record ClusterInfo(FloatingBlockMaterial material, double pressureTimesTotalScale) {
    public static final Codec<ClusterInfo> CODEC = RecordCodecBuilder.create(i -> i.group(
            FloatingBlockMaterial.CODEC.fieldOf("material").forGetter(ClusterInfo::material),
            Codec.DOUBLE.fieldOf("pressureTimesTotalScale").forGetter(ClusterInfo::pressureTimesTotalScale)
        ).apply(i, ClusterInfo::new)
    );
}
