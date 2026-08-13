package com.zelaux.betterdiagram.compat.gadgetsandgizmos;

import com.zelaux.betterdiagram.mixin_plugin.postprocessors.GadgetsProcessorProvider;
import dev.ryanhcode.sable.api.block.BlockEntitySubLevelActor;

public interface IPhysicsGantryShaft extends BlockEntitySubLevelActor {
    /**
     * @see GadgetsProcessorProvider
     * */
    BlockEntitySubLevelActor bcd$hasActor();
}
