package com.zelaux.betterdiagram.mixin.accessors;

import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollOptionBehaviour;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.simulated_team.simulated.content.blocks.swivel_bearing.SwivelBearingBlockEntity;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.gen.*;

@Mixin(SwivelBearingBlockEntity.class)
public interface ISwivelBearingBlockEntity {
    @Invoker("attachConstraints")
     void bcd$attachConstraints(final @Nullable ServerSubLevel plateSubLevel, final Vector3d attachPos);

    @Nullable
    @Invoker("getAttachedSubLevel")
    SubLevel bcd$getAttachedSubLevel();

    @Nullable
    @Invoker("getContainingSubLevel")
    SubLevel bcd$getContainingSubLevel();

    @Accessor("lockedDefaultOption")
     ScrollOptionBehaviour<SwivelBearingBlockEntity.LockingSetting> bcd$lockedDefaultOption();

}
