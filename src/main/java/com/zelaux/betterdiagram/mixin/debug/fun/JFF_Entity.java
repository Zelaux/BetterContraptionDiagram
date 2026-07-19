package com.zelaux.betterdiagram.mixin.debug.fun;

import com.zelaux.betterdiagram.fun.PlayerWeightJFF;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

@Mixin(Entity.class)
public abstract class JFF_Entity {
    @Shadow
    public abstract Vec3 getDeltaMovement();

    @Shadow
    public float fallDistance;

    @Inject(method = "checkFallDamage",at= @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/Block;fallOn(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/Entity;F)V"))
    void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos, CallbackInfo ci){
        if((Object)this instanceof Player player) {
            PlayerWeightJFF.onPlayerFall(player,y,onGround,state,pos,fallDistance);
        }
    }
}
