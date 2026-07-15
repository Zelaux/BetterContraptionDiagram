package com.zelaux.betterdiagram.mixin.feature.max_speed;

import dev.ryanhcode.sable.sublevel.plot.PlotChunkHolder;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.gen.*;

@Mixin(PlotChunkHolder.class)
public interface Sail_PlotChunkHolder_Acc {
    @Invoker("buildBoundingBox")
    void bcd$buildBoundingBox();

}
