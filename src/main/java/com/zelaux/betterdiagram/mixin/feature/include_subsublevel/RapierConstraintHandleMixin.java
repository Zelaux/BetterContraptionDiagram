package com.zelaux.betterdiagram.mixin.feature.include_subsublevel;

import dev.ryanhcode.sable.api.physics.constraint.ConstraintJointAxis;
import dev.ryanhcode.sable.physics.impl.rapier.constraint.RapierConstraintHandle;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

@Mixin(RapierConstraintHandle.class)
public abstract class RapierConstraintHandleMixin {
    @Inject(method = "remove",at=@At("HEAD"))
    private void remove(CallbackInfo ci){
        if(!isValid())return;

    }

    @Shadow
    public abstract boolean isValid();

}
