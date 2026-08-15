package com.zelaux.betterdiagram.feature.include_subsublevel.actor_handler;

import com.zelaux.betterdiagram.feature.include_subsublevel.connector.ActorHandler;
import com.zelaux.betterdiagram.feature.include_subsublevel.connector.LocalConstraint;
import com.zelaux.betterdiagram.feature.include_subsublevel.connector.SubLevelGroup;
import com.zelaux.betterdiagram.mixin.accessors.ISwivelBearingBlockEntity;
import dev.ryanhcode.sable.api.physics.constraint.FixedConstraintConfiguration;
import dev.ryanhcode.sable.api.physics.constraint.PhysicsConstraintConfiguration;
import dev.ryanhcode.sable.api.physics.constraint.RotaryConstraintConfiguration;
import dev.ryanhcode.sable.companion.math.JOMLConversion;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.simulated_team.simulated.content.blocks.swivel_bearing.SwivelBearingBlockEntity;
import dev.simulated_team.simulated.content.blocks.swivel_bearing.link_block.SwivelBearingPlateBlock;
import dev.simulated_team.simulated.index.SimBlocks;
import lombok.AllArgsConstructor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaterniond;
import org.joml.Quaterniondc;
import org.joml.Vector3d;

import java.util.List;
import java.util.Map;

@AllArgsConstructor
public class SwivelBearingActor implements ActorHandler {
    public static final RotaryConstraintConfiguration ROTARY_DUMMY = new RotaryConstraintConfiguration(new Vector3d(), new Vector3d(), new Vector3d(), new Vector3d());
    SwivelBearingBlockEntity entity;

    @Override
    public @Nullable List<LocalConstraint<?>> mapToConstraint(SubLevel a, Map<SubLevel, SubLevelGroup> subLevel2Group) {
        LocalConstraint<?> from = constraint(a, subLevel2Group);
        if(from == null) return null;
        return List.of(from);
    }

    public @Nullable LocalConstraint<?> constraint(SubLevel a, Map<SubLevel, SubLevelGroup> subLevel2Group) {
        ISwivelBearingBlockEntity blockEntity = (ISwivelBearingBlockEntity) entity;
        SubLevel b = blockEntity.bcd$getAttachedSubLevel();
        if(a == b) return null;
        SwivelBearingBlockEntity.LockingSetting setting = blockEntity.bcd$lockedDefaultOption().get();
        Level level = entity.getLevel();
        if(level == null) return null;
        final int bestSignal = level.getBestNeighborSignal(entity.getBlockPos());
        boolean shouldLock = setting.shouldLock(bestSignal);

        RotaryConstraintConfiguration rotary = getRotary(level, blockEntity);
        if(rotary == null) return null;

        PhysicsConstraintConfiguration<?> configuration = rotary;

        if(shouldLock) {
            Quaterniondc quatA = a.logicalPose().orientation();
            Quaterniondc quatB = b.logicalPose().orientation();

            // Relative = Inverse(A) * B
            Quaterniond relativeQuat = new Quaterniond(quatA)
                .conjugate()
                .mul(quatB);
            configuration = new FixedConstraintConfiguration(
                rotary.pos1(),
                rotary.pos2(),
                relativeQuat
            );
        }
        LocalConstraint<?> from = LocalConstraint.from(configuration, a, b, subLevel2Group);
        return from;
    }

    private @Nullable RotaryConstraintConfiguration getRotary(Level level, ISwivelBearingBlockEntity blockEntity) {
        BlockPos platePos = entity.getPlatePos();
        if(platePos == null) return null;


        final BlockState plateState = level.getBlockState(platePos);
        if(!plateState.is(SimBlocks.SWIVEL_BEARING_LINK_BLOCK)) return null;

        final Direction plateFacing = plateState.getValue(SwivelBearingPlateBlock.FACING);
        //Vector3dWith<RotaryConstraintConfiguration> vector3dWith = new Vector3dWith<>();
        //vector3dWith.second = ROTARY_DUMMY;
        return blockEntity.bcd$attachConstraints(null, JOMLConversion.toJOML(platePos.relative(plateFacing).getCenter()));

        //return vector3dWith.second;
    }

    public interface Access {
        @Nullable
        RotaryConstraintConfiguration bcd$attachConstraints(final SubLevel plateSubLevel, final Vector3d attachPos);
    }
}
