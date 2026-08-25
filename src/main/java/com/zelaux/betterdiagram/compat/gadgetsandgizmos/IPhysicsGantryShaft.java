package com.zelaux.betterdiagram.compat.gadgetsandgizmos;

import com.zelaux.betterdiagram.mixin_plugin.postprocessors.GadgetsProcessorProvider;
import dev.ryanhcode.sable.api.block.BlockEntitySubLevelActor;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.List;

public interface IPhysicsGantryShaft extends BlockEntitySubLevelActor {
    /**
     * @see GadgetsProcessorProvider
     * */
    @Deprecated
    BlockEntitySubLevelActor bcd$hasActor();
    /**
     * @see GadgetsProcessorProvider
     * */
    default List<? extends BlockEntitySubLevelActor> getConnectedCarriages(){
        BlockEntitySubLevelActor actor = bcd$hasActor();
        return actor==null?List.of():List.of(actor);
    }
}
