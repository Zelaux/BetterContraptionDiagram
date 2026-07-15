package com.zelaux.betterdiagram.feature.max_speed;


import dev.ryanhcode.sable.api.block.BlockSubLevelLiftProvider;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;

public interface WithLiftProviders {
    Long2ObjectMap<BlockSubLevelLiftProvider.LiftProviderContext> bcd$liftProviders();
}
