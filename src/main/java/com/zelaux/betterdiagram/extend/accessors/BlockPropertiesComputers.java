package com.zelaux.betterdiagram.extend.accessors;

import dev.ryanhcode.sable.api.physics.mass.MassTracker;
import dev.ryanhcode.sable.mixinterface.block_properties.BlockStateExtension;
import dev.ryanhcode.sable.physics.chunk.VoxelNeighborhoodState;
import dev.ryanhcode.sable.physics.config.block_properties.PhysicsBlockPropertyTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
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
        Lambda0Access_State_BlockGetter_X<Vector3dc> centerOfMass = get(MassTracker.BLOCK_CENTER_OF_MASS);
        Lambda0Access_State_BlockGetter_X<Boolean> isFullBlock = get(VoxelNeighborhoodState.IS_FULL_BLOCK);
        Lambda0Access_State_BlockGetter_X<Boolean> isSolid = get(VoxelNeighborhoodState.IS_SOLID_MEMOIZED);

        @SuppressWarnings("unchecked")
        static <T> Lambda0Access_State_BlockGetter_X<T> get(BiFunction<BlockGetter, BlockState, T> biFunction) {
            return ((Lambda0Access_State_BlockGetter_X<T>) biFunction);
        }

        @Unique
        T bcd$lambda$apply$0(BlockState par1, BlockGetter par2, int par3);
    }
}
