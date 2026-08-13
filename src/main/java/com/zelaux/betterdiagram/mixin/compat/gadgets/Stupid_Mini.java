package com.zelaux.betterdiagram.mixin.compat.gadgets;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.zelaux.betterdiagram.extend.WithClientData;
import com.zelaux.betterdiagram.feature.leveldata.DiagramEntityData;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

import java.util.UUID;

@Mixin(targets = "com.rieno.gadgetsandgizmos.neoforge.client.CTPhysicsGogglesClient$SimulatedDiagramMiniRenderer")
//@Mixin(BlockEntity.class)
public class Stupid_Mini  {
    @Shadow
    private boolean failed;


    @Inject(method = "prepare_head", at = @At("HEAD"))
    private void prepare_head(CallbackInfo ci) {
        if(AnimationTickHolder.getTicks(true) % 60 == 0) {
            failed = false;
        }
    }
/*
    @WrapMethod(method = "getPosFromTag")
    private static BlockPos createDiagramEntity(CompoundTag tag, Operation<BlockPos> original) {

        return original.call(tag);
    }*/

    @WrapMethod(method = "createDiagramEntity")
    private Object createDiagramEntity(Level level,Operation<Object> original) {
        Object result = original.call(level);
        if(result instanceof WithClientData data) {
            data.bcd$saveType(DiagramEntityData.SaveSubLocation.GadgetsOverlay);
        }
        return result;
    }

}
