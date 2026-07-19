package com.zelaux.betterdiagram.mixin.debug.fun;

import com.llamalad7.mixinextras.sugar.Local;
import com.zelaux.betterdiagram.annotations.EmptyInRelease;
import com.zelaux.betterdiagram.fun.PlayerWeightJFF;
import dev.ryanhcode.sable.api.sublevel.ServerSubLevelContainer;
import dev.ryanhcode.sable.sublevel.system.SubLevelPhysicsSystem;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

@Mixin(SubLevelPhysicsSystem.class)
@EmptyInRelease
public class PlayerWeightJFF_Mixin {
    @Inject(method = "tickPipelinePhysics",at= @At(value = "TAIL"))
    void tick(ServerSubLevelContainer container, CallbackInfo ci){
        PlayerWeightJFF.tickAfter(container,(SubLevelPhysicsSystem)(Object)this);
    }
    @Inject(method = "tickPipelinePhysics",at= @At(value = "INVOKE", target = "Ldev/ryanhcode/sable/platform/SableEventPublishPlatform;prePhysicsTick(Ldev/ryanhcode/sable/sublevel/system/SubLevelPhysicsSystem;D)V"))
    void tickPipelinePhysics(ServerSubLevelContainer container, CallbackInfo ci, @Local(name = "substepTimeStep") double substepTimeStep){
        PlayerWeightJFF.tickPipelinePhysics(container,(SubLevelPhysicsSystem)(Object)this,substepTimeStep);
    }
}
