package com.zelaux.betterdiagram.mixin.accessors;

import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollOptionBehaviour;
import com.zelaux.betterdiagram.feature.include_subsublevel.actor_handler.SwivelBearingActor;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.simulated_team.simulated.content.blocks.swivel_bearing.SwivelBearingBlockEntity;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.gen.*;

@Mixin(SwivelBearingBlockEntity.class)
public interface ISwivelBearingBlockEntity extends SwivelBearingActor.Access {
    //@Invoker("attachConstraints")
    //void bcd$attachConstraints(final @Nullable ServerSubLevel plateSubLevel, final Vector3d attachPos);
    //@Invoker("attachConstraints")

    @Nullable
    @Invoker("getAttachedSubLevel")
    SubLevel bcd$getAttachedSubLevel();

    @Nullable
    @Invoker("getContainingSubLevel")
    SubLevel bcd$getContainingSubLevel();

    @Accessor("lockedDefaultOption")
    ScrollOptionBehaviour<SwivelBearingBlockEntity.LockingSetting> bcd$lockedDefaultOption();

}
