package com.zelaux.betterdiagram.compat.absolute_kinematics;

import com.zelaux.betterdiagram.compat.CompatModule;
import com.zelaux.betterdiagram.feature.include_subsublevel.actor_handler.FixedActor;
import com.zelaux.betterdiagram.feature.include_subsublevel.connector.ActorHandler;
import com.zelaux.betterdiagram.feature.include_subsublevel.connector.OptionalActorHandlers;
import com.zelaux.betterdiagram.util.mock.MockGenericConstraintHandle;
import dev.ryanhcode.sable.api.block.BlockEntitySubLevelActor;
import dev.ryanhcode.sable.api.physics.constraint.GenericConstraintConfiguration;
import dev.ryanhcode.sable.api.physics.constraint.GenericConstraintHandle;

import java.util.function.Function;

public class AbsoluteKinematicsModule extends CompatModule {
    public static GenericConstraintHandle makeHandle(GenericConstraintConfiguration constraintConfiguration) {
        return new MockGenericConstraintHandle(constraintConfiguration);
    }

    @Override
    protected void load() {

        Function<IBaseJointBlockEntity, ActorHandler> base = BaseJointActor::new;
        OptionalActorHandlers.registerOptional(tname("com/rcx/absolutekinematics/blockentity/BallJointBlockEntity"), base);
        OptionalActorHandlers.registerOptional(tname("com/rcx/absolutekinematics/blockentity/HingeBlockEntity"), base);

        Function<IBaseJointPlateBlockEntity, ActorHandler> plate = BaseJointPlateActor::new;
        OptionalActorHandlers.registerOptional(tname("com/rcx/absolutekinematics/blockentity/BallJointPlateBlockEntity"), plate);
        OptionalActorHandlers.registerOptional(tname("com/rcx/absolutekinematics/blockentity/HingeLeafBlockEntity"), plate);

    }

}
