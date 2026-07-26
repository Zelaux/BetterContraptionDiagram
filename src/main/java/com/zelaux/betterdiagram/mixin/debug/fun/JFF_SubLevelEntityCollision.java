package com.zelaux.betterdiagram.mixin.debug.fun;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.zelaux.betterdiagram.annotations.EmptyInRelease;
import com.zelaux.betterdiagram.fun.PlayerWeightJFF;
import dev.ryanhcode.sable.api.math.LevelReusedVectors;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.ryanhcode.sable.sublevel.entity_collision.SubLevelEntityCollision;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

@Mixin(SubLevelEntityCollision.class)
@EmptyInRelease
public class JFF_SubLevelEntityCollision {
    @WrapOperation(method = "collide", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;setDeltaMovement(Lnet/minecraft/world/phys/Vec3;)V", ordinal = 0))
    private static void collide(Entity instance, Vec3 deltaMovement, Operation<Void> original, @Local(name = "collisionInfo") SubLevelEntityCollision.CollisionInfo collisionInfo) {
        PlayerWeightJFF.subLevelCollide(
            instance, deltaMovement, collisionInfo, 0
        );
        original.call(instance, deltaMovement);
    }

    @Inject(method = "collide", at = @At(value = "INVOKE", target = "Lit/unimi/dsi/fastutil/objects/Object2ObjectArrayMap;<init>()V"))
    private static void setup(Entity entity, Vec3 collisionMotionMoj, Vec3 velocityMotionMoj, LevelReusedVectors sink, CallbackInfoReturnable<SubLevelEntityCollision.CollisionInfo> cir) {

    }

    @WrapOperation(method = "collide", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;setDeltaMovement(Lnet/minecraft/world/phys/Vec3;)V", ordinal = 1))
    private static void collide(
        Entity instance,
        Vec3 deltaMovement,
        Operation<Void> original,
        @Local(name = "subLevel") SubLevel subLevel,
        @Share("bcd$collides") LocalRef<ObjectArrayList<PlayerWeightJFF.WeightPayload>> playerWeights
    ) {
        var weightPayloads = playerWeights.get();
        if(weightPayloads == null) playerWeights.set(weightPayloads = new ObjectArrayList<>());
        weightPayloads.add(PlayerWeightJFF.constructPayload(instance, deltaMovement, subLevel));
        original.call(instance, deltaMovement);
    }
    @Inject(method = "collide", at = @At(value = "TAIL"))
    private static void collide_finisH(
        Entity entity, Vec3 collisionMotionMoj, Vec3 velocityMotionMoj, LevelReusedVectors sink, CallbackInfoReturnable<SubLevelEntityCollision.CollisionInfo> cir,
        @Share("bcd$collides") LocalRef<ObjectArrayList<PlayerWeightJFF.WeightPayload>> playerWeights
    ){
        var list = playerWeights.get();
        if(list == null) return;
        PlayerWeightJFF.collideFinish(entity,list);
    }

}
