package com.zelaux.betterdiagram.extend.accessors;

import dev.ryanhcode.sable.api.block.BlockSubLevelCustomCenterOfMass;
import dev.ryanhcode.sable.companion.math.JOMLConversion;
import dev.ryanhcode.sable.mixinterface.block_properties.BlockStateExtension;
import dev.ryanhcode.sable.physics.config.block_properties.PhysicsBlockPropertyTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.piston.MovingPistonBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3dc;
import org.spongepowered.asm.mixin.*;

import java.util.function.BiFunction;

//TODO add transfer from server
//I know that mass technically is transferred
public interface BlockPropertiesComputers {

    static Vector3dc centerOfMass(BlockGetter blockGetter, BlockState blockState) {
        return Lambda0Access_State_BlockGetter_X.centerOfMass.bcd$lambda$apply$0(blockState, blockGetter, 0);
    }

    static boolean isFullBlock(BlockGetter blockGetter, BlockState blockState) {
        return Lambda0Access_State_BlockGetter_X.isFullBlock.bcd$lambda$apply$0(blockState, blockGetter, 0);
    }

    static double getMass(final BlockGetter level, final BlockPos pos, final BlockState state) {
        final boolean solid = isSolid(level, state /* pos,*/);
        if(!solid) {
            // TODO: Doing this means that sub-levels can end up with an existent bounding box but without any mass, invalidating them
            return 0.0;
        }
        return ((BlockStateExtension) state).sable$getProperty(PhysicsBlockPropertyTypes.MASS.get());
    }

    static boolean isSolid(BlockGetter blockGetter, BlockState blockState) {
        return Lambda0Access_State_BlockGetter_X.isSolid.bcd$lambda$apply$0(blockState, blockGetter, 0);
    }

    @Nullable
    static Vec3 getInertia(final BlockGetter level, final BlockPos pos, final BlockState state) {
        final boolean solid = isSolid(level,/* pos,*/ state);

        if(!solid) {
            return null;
        }

        return ((BlockStateExtension) state).sable$getProperty(PhysicsBlockPropertyTypes.INERTIA.get());
    }

    interface Lambda0Access_State_BlockGetter_X<T> {
        AABB UNIT_BOUNDS = new AABB(0, 0, 0, 1, 1, 1);
        Lambda0Access_State_BlockGetter_X<Vector3dc> centerOfMass = (state, blockGetter, par3) -> {
            if(state.isAir()) {
                return JOMLConversion.HALF;
            }

            if(state.getBlock() instanceof final BlockSubLevelCustomCenterOfMass customCenterOfMass) {
                return customCenterOfMass.getCenterOfMass(blockGetter, state);
            }

            final VoxelShape shape = state.getCollisionShape(blockGetter, BlockPos.ZERO);

            if(shape.isEmpty()) {
                return JOMLConversion.HALF;
            }

            if(state.isCollisionShapeFullBlock(blockGetter, BlockPos.ZERO)) {
                return JOMLConversion.HALF;
            }

            final AABB bounds = shape.bounds().intersect(UNIT_BOUNDS);
            return JOMLConversion.toJOML(bounds.getCenter());
        };
        Lambda0Access_State_BlockGetter_X<Boolean> isFullBlock = (state, blockGetter, par3) -> {
            // TODO add the blockgetter and position as context
            if(state.isAir())
                return false;

            return state.isCollisionShapeFullBlock(blockGetter, BlockPos.ZERO);

        };
        //Lambda0Access_State_BlockGetter_X<Boolean> isSolid = get(VoxelNeighborhoodState.IS_SOLID_MEMOIZED);
        Lambda0Access_State_BlockGetter_X<Boolean> isSolid = (state, blockGetter, par3) -> {
            // TODO add the blockgetter and position as context
            if(state.isAir())
                return false;

            if(state.getBlock() instanceof MovingPistonBlock)
                return true;

            return !state.getCollisionShape(blockGetter, BlockPos.ZERO).isEmpty();
        };

        @SuppressWarnings("unchecked")
        static <T> Lambda0Access_State_BlockGetter_X<T> get(BiFunction<BlockGetter, BlockState, T> biFunction) {
            return ((Lambda0Access_State_BlockGetter_X<T>) biFunction);
        }

        @Unique
        T bcd$lambda$apply$0(BlockState par1, BlockGetter par2, int par3);
    }
}
