package com.zelaux.betterdiagram.compat.gadgetsandgizmos;

import dev.ryanhcode.sable.api.block.BlockEntitySubLevelActor;

public interface IPhysicsGantryShaft extends BlockEntitySubLevelActor {
    /**
     * @see com.zelaux.betterdiagram.mixin_plugin.postprocessors.GadgetsPostprocessor
     * */
    BlockEntitySubLevelActor bcd$hasActor();
}
