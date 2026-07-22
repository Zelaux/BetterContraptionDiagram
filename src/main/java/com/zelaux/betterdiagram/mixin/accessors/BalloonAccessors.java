package com.zelaux.betterdiagram.mixin.accessors;

import dev.eriksonn.aeronautics.content.blocks.hot_air.balloon.Balloon;
import dev.ryanhcode.sable.util.LevelAccelerator;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.gen.*;

@Mixin(Balloon.class)
public interface BalloonAccessors {
    @Accessor("accelerator")
    LevelAccelerator bcd$accelerator();
}
