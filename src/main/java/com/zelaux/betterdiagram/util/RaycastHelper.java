package com.zelaux.betterdiagram.util;

import dev.ryanhcode.sable.companion.math.JOMLConversion;
import dev.ryanhcode.sable.mixinterface.clip_overwrite.ClipContextExtension;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3d;
import org.joml.Vector3dc;

public class RaycastHelper {
    private static final Vector3d endTmp = new Vector3d();

    public static @NotNull HitResult getHitResult(
        Level level,
        Vec3 startVec3,
        Vector3dc direction,
        float maxDistance,
        @NotNull Entity executor,
        boolean hitLiquid,
        boolean hitEntity,
        boolean hitSubLevels
    ) {
        Vector3d end = endTmp;
        double length = direction.length();
        if (length < 1e-5) return BlockHitResult.miss(Vec3.ZERO, Direction.UP, BlockPos.ZERO);
        Vec3 scaledDir = JOMLConversion.toMojang(direction.mul(maxDistance / length, end));
        end.add(startVec3.x, startVec3.y, startVec3.z);


        Vec3 endVec3 = JOMLConversion.toMojang(end);
        ClipContext context = new ClipContext(
            startVec3,
            endVec3,
            ClipContext.Block.OUTLINE,
            !hitLiquid ? ClipContext.Fluid.NONE : ClipContext.Fluid.ANY,
            executor
        );
        ((ClipContextExtension) context).sable$setDoNotProject(!hitSubLevels);
        BlockHitResult blockHit = level.clip(context);
        if(!hitEntity) return blockHit;
        double distToBlock = blockHit.getType() != HitResult.Type.MISS
            ? blockHit.getLocation().distanceTo(startVec3)
            : maxDistance;

        EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(
            level,
            executor,
            startVec3,
            endVec3,
            executor.getBoundingBox().expandTowards(scaledDir).inflate(1.0D),
            e -> !e.isSpectator() && e.isPickable(),
            (float) (distToBlock * distToBlock)
        );

        return (entityHit != null) ? entityHit : blockHit;
    }
}
