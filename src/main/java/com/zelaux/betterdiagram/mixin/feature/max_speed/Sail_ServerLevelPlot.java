package com.zelaux.betterdiagram.mixin.feature.max_speed;

import com.zelaux.betterdiagram.feature.max_speed.WithLiftProviders;
import dev.ryanhcode.sable.api.block.BlockSubLevelLiftProvider;
import dev.ryanhcode.sable.sublevel.plot.ServerLevelPlot;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import org.spongepowered.asm.mixin.*;

@Mixin(ServerLevelPlot.class)
public class Sail_ServerLevelPlot implements WithLiftProviders {
    @Shadow
    @Final
    private Long2ObjectMap<BlockSubLevelLiftProvider.LiftProviderContext> liftProviders;

    @Override
    public Long2ObjectMap<BlockSubLevelLiftProvider.LiftProviderContext> bcd$liftProviders() {
        return liftProviders;
    }
}
