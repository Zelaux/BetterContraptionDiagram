package com.zelaux.betterdiagram.mixin.accessors;

import dev.simulated_team.simulated.content.blocks.rope.RopeStrandHolderBehavior;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.gen.*;

import java.util.UUID;

@Mixin(RopeStrandHolderBehavior.class)
public interface RopeStrandHolderBehaviorAccessors {
    @Accessor("attachedRopeID")
    UUID bcd$attachedRopeID();
}
