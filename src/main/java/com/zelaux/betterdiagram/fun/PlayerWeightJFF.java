package com.zelaux.betterdiagram.fun;

import com.zelaux.betterdiagram.Config;
import com.zelaux.betterdiagram.annotations.DebugOnly;
import com.zelaux.betterdiagram.annotations.DeleteInRelease;
import com.zelaux.betterdiagram.util.VecUtil;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.api.physics.force.ForceTotal;
import dev.ryanhcode.sable.api.physics.mass.MassData;
import dev.ryanhcode.sable.api.sublevel.ServerSubLevelContainer;
import dev.ryanhcode.sable.companion.math.JOMLConversion;
import dev.ryanhcode.sable.physics.config.dimension_physics.DimensionPhysicsData;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.ryanhcode.sable.sublevel.entity_collision.SubLevelEntityCollision;
import dev.ryanhcode.sable.sublevel.system.SubLevelPhysicsSystem;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;

@DebugOnly
@DeleteInRelease
public class PlayerWeightJFF {

    private static final Object2ObjectMap<Player, FallOn> playerFallOn = new Object2ObjectOpenHashMap<>();

    public static void tickPipelinePhysics(ServerSubLevelContainer container, SubLevelPhysicsSystem subLevelPhysicsSystem, double substepTimeStep) {
        ServerLevel level = container.getLevel();
        Vector3d playerPos = new Vector3d();
        Vector3d gravity = new Vector3d();
        var forces = new Object2ObjectOpenHashMap<ServerSubLevel, ForceTotal>();

        Vector3d deltaMovement = new Vector3d();
        double playerImpulseScale = Config.DEBUG_PLAYER_IMPULSE_SCALE.getAsDouble();
        double playerMass = Config.DEBUG_PLAYER_MASS.getAsDouble();

        for(ServerPlayer player : level.players()) {
            ServerSubLevel subLevel = (ServerSubLevel) Sable.HELPER.getTrackingSubLevel(player);

            boolean shouldAddMass = player.verticalCollisionBelow;
            if(subLevel == null) {
                FallOn fallOn = playerFallOn.get(player);
                if(fallOn != null && fallOn.subLevel!=null) {
                    shouldAddMass=false;
                    subLevel=fallOn.subLevel;
                }else return;
            }
            if(subLevel.isRemoved())continue;
            playerPos.set(player.getX(), player.getY(), player.getZ());

            DimensionPhysicsData.getGravity(level, playerPos, gravity);

            VecUtil.unprojectPosToLocal(subLevel.logicalPose(), playerPos);

            ForceTotal forceTotal = forces.computeIfAbsent(subLevel, it -> new ForceTotal());

            MassData massTracker = subLevel.getMassTracker();

            if(shouldAddMass && playerMass!=0) {
                Vector3d mul = gravity.mul(playerMass * substepTimeStep);
                forceTotal.applyImpulseAtPoint(massTracker, playerPos, VecUtil.unprojectDirectionToLocal(subLevel.logicalPose(),mul));
            }
            FallOn fallOn = playerFallOn.get(player);
            if(fallOn == null) continue;
            Vector3d div = JOMLConversion.toJOML(fallOn.deltaMovement, deltaMovement).mul(playerImpulseScale).div(substepTimeStep);
            forceTotal.applyImpulseAtPoint(massTracker, playerPos, VecUtil.unprojectDirectionToLocal(subLevel.logicalPose(),div));


        }


        forces.forEach((subLevel, forceTotal) -> {
            forceTotal.applyForces(subLevelPhysicsSystem.getPhysicsHandle(subLevel));
        });
    }

    public static void subLevelCollide(Entity instance, Vec3 deltaMovement, SubLevelEntityCollision.CollisionInfo collisionInfo, int i) {

    }

    public static WeightPayload constructPayload(Entity instance, Vec3 deltaMovement, SubLevel subLevel) {
        return new WeightPayload(
            instance.getDeltaMovement(),
            deltaMovement,
            subLevel
        );
    }

    public static void collideFinish(Entity entity, ObjectArrayList<WeightPayload> list) {

    }

    record FallOn(double y, float fallDistance, net.minecraft.world.phys.Vec3 deltaMovement, boolean onGround,
                  BlockState state, BlockPos pos, ServerSubLevel subLevel) {

    }

    public static void onPlayerFall(Player player, double y, boolean onGround, BlockState state, BlockPos pos, float fallDistance) {
        var subLevel = Sable.HELPER.getContaining(player.level(), pos) instanceof ServerSubLevel s?s:null;
        playerFallOn.put(player, new FallOn(y, fallDistance, player.getDeltaMovement(),onGround,state,pos, subLevel));
    }

    public static void tickAfter(ServerSubLevelContainer container, SubLevelPhysicsSystem subLevelPhysicsSystem) {
        playerFallOn.clear();
    }

    public record WeightPayload(Vec3 deltaMovement, Vec3 movement, SubLevel subLevel) {}
}
