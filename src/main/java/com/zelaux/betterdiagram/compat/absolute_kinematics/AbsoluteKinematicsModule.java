package com.zelaux.betterdiagram.compat.absolute_kinematics;

import com.zelaux.betterdiagram.compat.CompatModule;
import com.zelaux.betterdiagram.feature.include_subsublevel.actor_handler.FixedActor;
import com.zelaux.betterdiagram.feature.include_subsublevel.connector.ActorHandler;
import com.zelaux.betterdiagram.feature.include_subsublevel.connector.OptionalActorHandlers;
import dev.ryanhcode.sable.api.block.BlockEntitySubLevelActor;

import java.util.function.Function;

public class AbsoluteKinematicsModule extends CompatModule {
    @Override
    protected void load() {
        Function<BlockEntitySubLevelActor, ActorHandler> actor = FixedActor::new;

        //OptionalActorHandlers.registerOptional(tname("com/rcx/absolutekinematics/blockentity/BallJointBlockEntity"), actor);
        //OptionalActorHandlers.registerOptional(tname("com/rcx/absolutekinematics/blockentity/BallJointPlateBlockEntity"), actor);
        //OptionalActorHandlers.registerOptional(tname("com/rcx/absolutekinematics/blockentity/HingeBlockEntity"), actor);
        //OptionalActorHandlers.registerOptional(tname("com/rcx/absolutekinematics/blockentity/HingeLeafBlockEntity"), actor);

    }
}
