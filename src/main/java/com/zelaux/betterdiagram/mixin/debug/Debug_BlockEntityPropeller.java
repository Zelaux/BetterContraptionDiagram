package com.zelaux.betterdiagram.mixin.debug;

import com.simibubi.create.content.kinetics.fan.EncasedFanBlockEntity;
import com.zelaux.betterdiagram.DebugRuntime;
import com.zelaux.betterdiagram.annotations.DeleteInRelease;
import dev.eriksonn.aeronautics.content.blocks.propeller.small.BasePropellerBlockEntity;
import dev.ryanhcode.sable.api.block.propeller.BlockEntityPropeller;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

@Mixin(value = {BasePropellerBlockEntity.class, EncasedFanBlockEntity.class},priority = 2000)
public abstract class Debug_BlockEntityPropeller implements BlockEntityPropeller {
    @Inject(method = "getAirflow", at = @At("HEAD"), cancellable = true)
    @DeleteInRelease
    void getAirFlow(CallbackInfoReturnable<Double> cir) {
        if(DebugRuntime.disableAirflowScaling) {
            cir.setReturnValue(0.0);
            cir.cancel();
        }
    }

}
