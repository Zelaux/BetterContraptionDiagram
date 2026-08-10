package com.zelaux.betterdiagram.mixin;

import dev.simulated_team.simulated.content.blocks.rope.RopeStrandHolderBlockEntity;
import org.spongepowered.asm.mixin.*;

@Mixin(
    value = {RopeStrandHolderBlockEntity.class},
    targets = "com.rieno.gadgetsandgizmos.content.PhysicsGantryShaftBlockEntity"
)
public interface RunAutoGenMixin {
}
