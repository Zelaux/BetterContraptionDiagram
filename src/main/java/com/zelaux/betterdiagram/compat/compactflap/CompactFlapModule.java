package com.zelaux.betterdiagram.compat.compactflap;

import com.zelaux.betterdiagram.annotations.JavaClassName;
import com.zelaux.betterdiagram.compat.CompatModule;
import com.zelaux.betterdiagram.feature.include_subsublevel.actor_handler.FixedActor;
import com.zelaux.betterdiagram.feature.include_subsublevel.connector.ActorHandler;
import com.zelaux.betterdiagram.feature.include_subsublevel.connector.OptionalActorHandlers;
import dev.ryanhcode.sable.api.block.BlockEntitySubLevelActor;

import java.util.function.Function;

public class CompactFlapModule extends CompatModule {
    @Override
    protected void load() {
        Function<BlockEntitySubLevelActor, ActorHandler> actor = FixedActor::new;
        String[] classes = {
            "dev/qwxon/compactflap/content/blocks/compact_swivel_flap/CompactSwivelFlapBlockEntity",
            "dev/qwxon/compactflap/content/blocks/analog_swivel_bearing/AnalogSwivelBearingBlockEntity",
        };
        for(String typeName : classes) OptionalActorHandlers.registerOptional(tname(typeName), actor);
    }
}
