package com.zelaux.betterdiagram.mixin.feature.easy_balloon_access;

import dev.eriksonn.aeronautics.content.blocks.hot_air.BlockEntityLiftingGasProvider;
import dev.ryanhcode.sable.sublevel.plot.LevelPlot;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.gen.*;

@Mixin(value = LevelPlot.class,priority = 2000)
public interface IStoreBlockEntityLiftingGasProvider {

    @Accessor("bcd$blockEntityLiftingGasProviders")
    Long2ObjectMap<BlockEntityLiftingGasProvider> bcd$blockEntityLiftingGasProviders();
}
