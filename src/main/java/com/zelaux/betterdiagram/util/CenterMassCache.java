package com.zelaux.betterdiagram.util;

import com.mojang.datafixers.util.Either;
import com.zelaux.betterdiagram.extend.accessors.BlockPropertiesComputers;
import com.zelaux.betterdiagram.util.async.*;
import dev.ryanhcode.sable.companion.math.JOMLConversion;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Lombok;
import lombok.SneakyThrows;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3dc;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class CenterMassCache {
    public final static LevelCache<CenterMassTo_Block2States> COM2_Block2States = new LevelCache<>(CenterMassTo_Block2States::calc);
    public final static LevelCache<BlockTo_CenterMass2State> Block2Pairs = new LevelCache<>(BlockTo_CenterMass2State::calc);
    public final static LevelCache<MassVariants> massVariants = new LevelCache<>((blocks, level, list) ->
        MassVariants.calc(blocks,level)
    );

    public static Either<CenterMassTo_Block2States, CompletableFuture<CenterMassTo_Block2States>> getCOM2_Block2States(Level level) {
        return getCOM2_Block2States(level.registryAccess().lookup(Registries.BLOCK).orElse(null), level);
    }

    public static Either<CenterMassTo_Block2States, CompletableFuture<CenterMassTo_Block2States>> getCOM2_Block2States(HolderLookup.RegistryLookup<Block> blocks, Level level) {
        return COM2_Block2States.get(blocks,level);
    }

    public static Either<BlockTo_CenterMass2State, CompletableFuture<BlockTo_CenterMass2State>> getBlock2Pairs(HolderLookup.RegistryLookup<Block> blocks, Level level) {
        return CenterMassCache.Block2Pairs.get(blocks, level);

    }

    public static void forEachCOM(Level level, Block block, boolean ignoreHalf, RawPairConsumer consumer) {

        for(BlockState state : block.getStateDefinition().getPossibleStates()) {
            double mass = BlockPropertiesComputers.getMass(level, BlockPos.ZERO, state);
            if(CenterMassCalculator.equals(mass, 0)) continue;

            //Vector3dc blockCenterOfMass = MassTracker.BLOCK_CENTER_OF_MASS.apply(level, state);
            Vector3dc blockCenterOfMass = BlockPropertiesComputers.centerOfMass(level, state);
            if(ignoreHalf && blockCenterOfMass.equals(JOMLConversion.HALF)) continue;
            consumer.consume(state, blockCenterOfMass);
        }
    }

    public static @NotNull Either<BlockTo_CenterMass2State, CompletableFuture<BlockTo_CenterMass2State>> getBlock2Pairs(@NotNull Level level) {
        return getBlock2Pairs(
            level.registryAccess().lookup(Registries.BLOCK).orElse(null),
            level
        );
    }

    public static void resetCache(boolean shouldLoad) {

        if(!shouldLoad) {
            forEachCache((it,type)->{
                try {
                    it.put(type.getConstructor().newInstance());
                } catch(Throwable e) {
                    throw Lombok.sneakyThrow(e);
                }
            });
        } else {
            forEachCache((it,type)-> it.holder.invalidate());
        }

    }

    @SneakyThrows
    private static void forEachCache(BiConsumer<LevelCache<? super Object>,Class<?>> consumer) {
        Field[] myFields = CenterMassCache.class.getDeclaredFields();
        for(Field field : myFields) {
            if(LevelCache.class.isAssignableFrom(field.getType())) {
                field.setAccessible(true);
                var genericFieldType = field.getGenericType();

                if (genericFieldType instanceof ParameterizedType) {
                    ParameterizedType aType = (ParameterizedType) genericFieldType;

                    var fieldArgTypes = aType.getActualTypeArguments();

                    if (fieldArgTypes.length > 0) {
                        Class<?> fieldArgClass = (Class<?>) fieldArgTypes[0];
                        consumer.accept((LevelCache<? super Object>) field.get(null),fieldArgClass);
                    }
                }
            }
        }
    }
    public static boolean isValidState(Block block, BlockState state){
        if(block instanceof WallBlock) {
            var up = state.getValue( BlockStateProperties.UP);
            var eastWall = state.getValue( BlockStateProperties.EAST_WALL);
            var northWall = state.getValue( BlockStateProperties.NORTH_WALL);
            var southWall = state.getValue( BlockStateProperties.SOUTH_WALL);
            var westWall = state.getValue( BlockStateProperties.WEST_WALL);

            if(!up && (eastWall.ordinal()|northWall.ordinal()|southWall.ordinal()|westWall.ordinal())==0){
                return false;
            }
        }else if(block instanceof SlabBlock) {

        }
        return true;
    }

   public static <T> @NotNull ObjectArrayList<T> getSaved(ObjectArrayList<ObjectArrayList<T>> allocatedLists) {
        ObjectArrayList<T> e = new ObjectArrayList<>();
        allocatedLists.add(e);
        return e;
    }

    public interface WaitOnLoad<T> {
        void onLoaded(Consumer<T> t);
    }

    public interface RawPairConsumer {
        void consume(BlockState state, Vector3dc centerOfMass);
    }
}
