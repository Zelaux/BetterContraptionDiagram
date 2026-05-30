package com.zelaux.betterdiagram.util;

import com.mojang.datafixers.util.Either;
import com.zelaux.betterdiagram.BetterContraptionDiagram;
import com.zelaux.betterdiagram.extend.accessors.MassTracker$1Accessors;
import dev.ryanhcode.sable.companion.math.JOMLConversion;
import dev.ryanhcode.sable.physics.config.block_properties.PhysicsBlockPropertyHelper;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3dc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

public class CenterMassCache {
    final static AsyncComputeHolder<CenterMassTo_Block2States, CenterMassTo_Block2States.Part> COM2_Block2States = new AsyncComputeHolder<>();
    final static AsyncComputeHolder<BlockTo_CenterMass2State, BlockTo_CenterMass2State.Part> Block2Pairs = new AsyncComputeHolder<>();

    public static Either<CenterMassTo_Block2States, CompletableFuture<CenterMassTo_Block2States>> getCOM2_Block2States(Level level) {
        return getCOM2_Block2States(level.registryAccess().lookup(Registries.BLOCK).orElse(null), level);
    }

    public static Either<CenterMassTo_Block2States, CompletableFuture<CenterMassTo_Block2States>> getCOM2_Block2States(HolderLookup.RegistryLookup<Block> blocks, Level level) {
        return COM2_Block2States.getAsync(
            x -> calc_COM2_Block2States(blocks, level, x),
            Util.backgroundExecutor()

        ).mapRight(AsyncComputeHolder.Pair::future);
    }

    private static @NotNull CenterMassTo_Block2States calc_COM2_Block2States(HolderLookup.RegistryLookup<Block> blocks, Level level, ConcurrentLinkedQueue<CenterMassTo_Block2States.Part> x) {
        long nano = System.nanoTime();
        var resultMap = new CenterMassTo_Block2States();
        var lists = new ArrayList<ArrayList<BlockState>>();

        for(var blockListEntry : getBlock2Pairs(blocks, level).left().get().entrySet()) {
            Block block = blockListEntry.getKey();
            Map<Vector3dc, ArrayList<BlockState>> value = blockListEntry.getValue();

            for(var COMAndBlockStates : value.entrySet()) {
                var COM = COMAndBlockStates.getKey();
                var blockStates = COMAndBlockStates.getValue();
                resultMap
                    .computeIfAbsent(COM, it -> new HashMap<>())
                    .computeIfAbsent(block, it -> getSaved(lists))
                    .addAll(blockStates);

                x.add(new CenterMassTo_Block2States.Part(
                    COM, block, blockStates, false
                ));
            }
            for(ArrayList<BlockState> list : lists) {
                list.trimToSize();
            }
            lists.clear();

        }

        x.add(new CenterMassTo_Block2States.Part(
            null, null, null, true
        ));
        long endNano = System.nanoTime();
        //COM2_Block2States_time = endNano - nano;
        BetterContraptionDiagram.LOGGER.debug("Time to build com2_block2states cache {}ms", (endNano - nano) / 1_000_000.);
        return resultMap;
    }

    public static Either<BlockTo_CenterMass2State, CompletableFuture<BlockTo_CenterMass2State>> getBlock2Pairs(HolderLookup.RegistryLookup<Block> blocks, Level level) {
        return CenterMassCache.Block2Pairs
            .getAsync(x -> cacl_Block2Pairs(blocks, level, x), Util.backgroundExecutor())
            .mapRight(AsyncComputeHolder.Pair::future);

    }

    private static @NotNull ArrayList<BlockState> getSaved(ArrayList<ArrayList<BlockState>> allocatedLists) {
        ArrayList<BlockState> e = new ArrayList<>();
        allocatedLists.add(e);
        return e;
    }

    private static @NotNull BlockTo_CenterMass2State cacl_Block2Pairs(HolderLookup.RegistryLookup<Block> blocks, Level level, ConcurrentLinkedQueue<BlockTo_CenterMass2State.Part> x) {
        long nano = System.nanoTime();
        var map = new BlockTo_CenterMass2State();
        var allocatedLists = new ArrayList<ArrayList<BlockState>>();

        blocks
            .listElements()
            .sequential()
            .forEach(ref -> {
                Block block = ref.value();

                var pairs = new HashMap<Vector3dc, ArrayList<BlockState>>();
                forEachCOM(level, block, false, (state, centerOfMass) -> {
                    pairs.computeIfAbsent(centerOfMass, it -> getSaved(allocatedLists)).add(state);
                });
                for(ArrayList<BlockState> list : allocatedLists) {
                    list.trimToSize();
                }
                allocatedLists.clear();
                x.add(new BlockTo_CenterMass2State.Part(block, pairs, false));
                map.put(block, pairs);

            });

        x.add(new BlockTo_CenterMass2State.Part(null, null, true));
        long endNano = System.nanoTime();
        //Block2Pairs_nano = endNano - nano;
        BetterContraptionDiagram.LOGGER.debug("Time to build block2pairs cache {}ms", (endNano - nano) / 1_000_000.);
        return map;
    }

    public static void forEachCOM(Level level, Block block, boolean ignoreHalf, RawPairConsumer consumer) {
        MassTracker$1Accessors accessors = MassTracker$1Accessors.get();
        for(BlockState state : block.getStateDefinition().getPossibleStates()) {
            double mass = PhysicsBlockPropertyHelper.getMass(level, BlockPos.ZERO, state);
            if(CenterMassCalculator.equals(mass, 0)) continue;

            //Vector3dc blockCenterOfMass = MassTracker.BLOCK_CENTER_OF_MASS.apply(level, state);
            Vector3dc blockCenterOfMass = accessors.bcd$lambda$apply$0(state, level, 0);
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
            Block2Pairs.put(new BlockTo_CenterMass2State());
            COM2_Block2States.put(new CenterMassTo_Block2States());
        } else {
            Block2Pairs.invalidate();
            COM2_Block2States.invalidate();
        }

    }

    public interface WaitOnLoad<T> {
        void onLoaded(Consumer<T> t);
    }

    public interface RawPairConsumer {
        void consume(BlockState state, Vector3dc centerOfMass);
    }

    public static class CenterMassTo_Block2States extends HashMap<Vector3dc, Map<Block, List<BlockState>>> {
        public record Part(Vector3dc COM, Block block, List<BlockState> map, boolean finish) {}
    }

    public static class BlockTo_CenterMass2State extends HashMap<Block, Map<Vector3dc, ArrayList<BlockState>>> {
        /**
         * @param block unique per queue
         *
         */
        public record Part(Block block, Map<Vector3dc, ArrayList<BlockState>> COM2BLockState, boolean finish) {}
    }

    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PUBLIC, makeFinal = true)
    public static class Pair {
        BlockState state;
        Vector3dc COM;
    }
}
