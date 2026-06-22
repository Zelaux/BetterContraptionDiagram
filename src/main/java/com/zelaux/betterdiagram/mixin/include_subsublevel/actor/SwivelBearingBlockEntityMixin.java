package com.zelaux.betterdiagram.mixin.include_subsublevel.actor;

import com.llamalad7.mixinextras.sugar.Local;
import com.zelaux.betterdiagram.feature.include_subsublevel.math.Vector3dWith;
import dev.ryanhcode.sable.api.physics.constraint.PhysicsConstraintConfiguration;
import dev.ryanhcode.sable.api.physics.constraint.RotaryConstraintConfiguration;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.simulated_team.simulated.content.blocks.swivel_bearing.SwivelBearingBlockEntity;
import org.joml.Vector3d;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

@Mixin(SwivelBearingBlockEntity.class)
public abstract class SwivelBearingBlockEntityMixin {
    @SuppressWarnings("rawtypes")
    @Inject(method = "attachConstraints", at = @At(
        value = "INVOKE",
        target = "Ldev/simulated_team/simulated/content/blocks/swivel_bearing/SwivelBearingBlockEntity;getLevel()Lnet/minecraft/world/level/Level;"
    ),
        cancellable = true)
    public void attachConstraints(
        ServerSubLevel plateSubLevel,
        Vector3d attachPos,
        CallbackInfo ci,
        @Local(name = "constraint") RotaryConstraintConfiguration config) {
        if(attachPos instanceof Vector3dWith vector3dWith) {
            if(vector3dWith.second instanceof PhysicsConstraintConfiguration<?>) {
                vector3dWith.second = config;
                ci.cancel();
            }
        }
    }

}
