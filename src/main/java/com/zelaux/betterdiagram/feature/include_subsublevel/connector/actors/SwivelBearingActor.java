package com.zelaux.betterdiagram.feature.include_subsublevel.connector.actors;

import com.zelaux.betterdiagram.feature.include_subsublevel.connector.ActorMapper;
import com.zelaux.betterdiagram.feature.include_subsublevel.connector.LocalConstraint;
import com.zelaux.betterdiagram.feature.include_subsublevel.math.Vector3dWith;
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

@AllArgsConstructor
public class SwivelBearingActor implements ActorMapper {
    public static final RotaryConstraintConfiguration ROTARY_DUMMY = new RotaryConstraintConfiguration(new Vector3d(), new Vector3d(), new Vector3d(), new Vector3d());
    SwivelBearingBlockEntity entity;

    @Override
    public @Nullable List<LocalConstraint> mapToConstraint(SubLevel a) {
        ISwivelBearingBlockEntity blockEntity = (ISwivelBearingBlockEntity) entity;
        SubLevel b = blockEntity.bcd$getAttachedSubLevel();
        SwivelBearingBlockEntity.LockingSetting setting = blockEntity.bcd$lockedDefaultOption().get();
        Level level = entity.getLevel();
        if(level == null) return null;
        final int bestSignal = level.getBestNeighborSignal(entity.getBlockPos());
        boolean shouldLock = setting.shouldLock(bestSignal);

        RotaryConstraintConfiguration rotaryConstraintConfiguration = getRotary(level, blockEntity);
        if(rotaryConstraintConfiguration == ROTARY_DUMMY) return null;

        PhysicsConstraintConfiguration<?> configuration = rotaryConstraintConfiguration;

        if(shouldLock) {
            Quaterniondc quatA = a.logicalPose().orientation();
            Quaterniondc quatB = b.logicalPose().orientation();

            // Relative = Inverse(A) * B
            Quaterniond relativeQuat = new Quaterniond(quatA)
                .conjugate()
                .mul(quatB);
            configuration = new FixedConstraintConfiguration(
                rotaryConstraintConfiguration.pos1(),
                rotaryConstraintConfiguration.pos2(),
                relativeQuat
            );
        }
        return List.of(LocalConstraint.from(configuration, a, b));
    }

    private @Nullable RotaryConstraintConfiguration getRotary(Level level, ISwivelBearingBlockEntity blockEntity) {
        BlockPos platePos = entity.getPlatePos();
        if(platePos == null) return ROTARY_DUMMY;


        final BlockState plateState = level.getBlockState(platePos);
        if(!plateState.is(SimBlocks.SWIVEL_BEARING_LINK_BLOCK)) return ROTARY_DUMMY;

        final Direction plateFacing = plateState.getValue(SwivelBearingPlateBlock.FACING);
        Vector3dWith<RotaryConstraintConfiguration> vector3dWith = new Vector3dWith<>(JOMLConversion.toJOML(platePos.relative(plateFacing).getCenter()));
        vector3dWith.second = ROTARY_DUMMY;
        blockEntity.bcd$attachConstraints(null, vector3dWith);

        return vector3dWith.second;
    }
}
