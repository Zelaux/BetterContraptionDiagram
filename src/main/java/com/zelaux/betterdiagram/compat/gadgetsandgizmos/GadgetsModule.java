package com.zelaux.betterdiagram.compat.gadgetsandgizmos;

import com.zelaux.betterdiagram.annotations.JavaClassName;
import com.zelaux.betterdiagram.compat.CompatModule;
import com.zelaux.betterdiagram.feature.include_subsublevel.actor_handler.FixedActor;
import com.zelaux.betterdiagram.feature.include_subsublevel.connector.ActorHandler;
import com.zelaux.betterdiagram.feature.include_subsublevel.connector.OptionalActorHandlers;
import dev.ryanhcode.sable.api.block.BlockEntitySubLevelActor;

import java.util.function.Function;

public class GadgetsModule extends CompatModule {
    @Override
    protected void load() {
        Function<BlockEntitySubLevelActor, ActorHandler> actor = FixedActor::new;
        String[] classes = {
            //tname("com/rieno/gadgetsandgizmos/content/VectorBearingBlockEntity"),//!
            tname("com/rieno/gadgetsandgizmos/content/VectorBearingLinkBlockEntity"),

            tname("com/rieno/gadgetsandgizmos/content/ScissorPistonBlockEntity"),
            tname("com/rieno/gadgetsandgizmos/content/ScissorPistonLinkBlockEntity"),

            //tname("com/rieno/gadgetsandgizmos/content/AileronBearingBlockEntity"),//!
            tname("com/rieno/gadgetsandgizmos/content/AileronBearingLinkBlockEntity"),

            tname("com/rieno/gadgetsandgizmos/content/PhysicsGantryCarriageBlockEntity"),
            //tname("com/rieno/gadgetsandgizmos/content/PhysicsGantryShaftBlockEntity"),

            tname("com/rieno/gadgetsandgizmos/content/ThrusterBearingBlockEntity"),
            tname("com/rieno/gadgetsandgizmos/content/ThrusterBearingLinkBlockEntity"),
        };
        for(@JavaClassName String typeName : classes) OptionalActorHandlers.registerOptional(typeName, actor);
        OptionalActorHandlers.registerOptional(tname("com/rieno/gadgetsandgizmos/content/VectorBearingBlockEntity"), VectorBearingActor::new);
        OptionalActorHandlers.registerOptional(tname("com/rieno/gadgetsandgizmos/content/AileronBearingBlockEntity"), AileronBearingActor::new);
        OptionalActorHandlers.registerOptional(tname("com/rieno/gadgetsandgizmos/content/PhysicsGantryShaftBlockEntity"), PhysicsGantryShaftActor::new);
        OptionalActorHandlers.registerOptional(tname("com/rieno/gadgetsandgizmos/content/ClawBlockEntity"), ClawActor::new);
    }
}
