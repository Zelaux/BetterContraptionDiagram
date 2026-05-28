package com.zelaux.betterdiagram.util;

import com.zelaux.betterdiagram.BetterContraptionDiagram;
import dev.ryanhcode.sable.api.physics.mass.MassTracker;
import dev.ryanhcode.sable.companion.math.JOMLConversion;
import dev.ryanhcode.sable.physics.config.block_properties.PhysicsBlockPropertyHelper;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3dc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CenterMassCache {
    private static final Object LOCK = new Object();
    static Map<Vector3dc, Map<Block, List<BlockState>>> COM2_Block2States = null;
    static HashMap<Block, HashMap<Vector3dc, ArrayList<BlockState>>> Block2Pairs = null;
    private static long COM2_Block2States_time;
    private static long Block2Pairs_nano;

    public static @NotNull Map<Vector3dc, Map<Block, List<BlockState>>> getCOM2_Block2States(HolderLookup.RegistryLookup<Block> blocks, Level level) {
        if(COM2_Block2States != null && Block2Pairs != null) return COM2_Block2States;
        synchronized(LOCK) {
            if(COM2_Block2States != null && Block2Pairs != null) return COM2_Block2States;
            var resultMap = calc_COM2_Block2States(blocks, level);
            return COM2_Block2States = resultMap;
        }
    }

    private static @NotNull HashMap<Vector3dc, Map<Block, List<BlockState>>> calc_COM2_Block2States(HolderLookup.RegistryLookup<Block> blocks, Level level) {
        long nano = System.nanoTime();
        var resultMap = new HashMap<Vector3dc, Map<Block, List<BlockState>>>();
        var lists = new ArrayList<ArrayList<BlockState>>();

        for(var blockListEntry : getBlock2Pairs(blocks, level).entrySet()) {
            Block block = blockListEntry.getKey();

            for(var COMAndBlockStates : blockListEntry.getValue().entrySet()) {
                var COM = COMAndBlockStates.getKey();
                var blockStates = COMAndBlockStates.getValue();
                resultMap
                    .computeIfAbsent(COM, it -> new HashMap<>())
                    .computeIfAbsent(block, it -> getSaved(lists))
                    .addAll(blockStates);
            }
            for(ArrayList<BlockState> list : lists) {
                list.trimToSize();
            }
            lists.clear();

        }
        long endNano = System.nanoTime();
        COM2_Block2States_time = endNano - nano;
        BetterContraptionDiagram.LOGGER.debug("Time to build com2_block2states cache {}ms", (endNano - nano) / 1_000_000.);
        return resultMap;
    }

    public static @NotNull HashMap<Block, HashMap<Vector3dc, ArrayList<BlockState>>> getBlock2Pairs(HolderLookup.RegistryLookup<Block> blocks, Level level) {
        if(CenterMassCache.Block2Pairs != null) return CenterMassCache.Block2Pairs;
        synchronized(LOCK) {
            if(Block2Pairs != null) return Block2Pairs;
            var map = cacl_Block2Pairs(blocks, level);
            return CenterMassCache.Block2Pairs = map;
        }
    }

    private static @NotNull ArrayList<BlockState> getSaved(ArrayList<ArrayList<BlockState>> allocatedLists) {
        ArrayList<BlockState> e = new ArrayList<>();
        allocatedLists.add(e);
        return e;
    }

    private static @NotNull HashMap<Block, HashMap<Vector3dc, ArrayList<BlockState>>> cacl_Block2Pairs(HolderLookup.RegistryLookup<Block> blocks, Level level) {
        long nano = System.nanoTime();
        var map = new HashMap<Block, HashMap<Vector3dc, ArrayList<BlockState>>>();
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
                map.put(block, pairs);

            });

        long endNano = System.nanoTime();
        Block2Pairs_nano = endNano - nano;
        BetterContraptionDiagram.LOGGER.debug("Time to build block2pairs cache {}ms", (endNano - nano) / 1_000_000.);
        return map;
    }

    public static void forEachCOM(Level level, Block block, boolean ignoreHalf, RawPairConsumer consumer) {
        for(BlockState state : block.getStateDefinition().getPossibleStates()) {
            double mass = PhysicsBlockPropertyHelper.getMass(level, BlockPos.ZERO, state);
            if(CenterMassCalculator.equals(mass, 0)) continue;

            Vector3dc blockCenterOfMass = MassTracker.BLOCK_CENTER_OF_MASS.apply(level, state);
            if(ignoreHalf && blockCenterOfMass.equals(JOMLConversion.HALF)) continue;
            consumer.consume(state, blockCenterOfMass);
        }
    }

    public static @NotNull HashMap<Block, HashMap<Vector3dc, ArrayList<BlockState>>> getBlock2Pairs(Player player) {
        return getBlock2Pairs(player.level());
    }

    public static @NotNull HashMap<Block, HashMap<Vector3dc, ArrayList<BlockState>>> getBlock2Pairs(@NotNull Level level) {
        return getBlock2Pairs(
            level.registryAccess().lookup(Registries.BLOCK).orElse(null),
            level
        );
    }

    public static void resetCache() {
        Block2Pairs = null;
        COM2_Block2States = null;
    }

    public interface RawPairConsumer {
        void consume(BlockState state, Vector3dc centerOfMass);
    }

    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PUBLIC, makeFinal = true)
    public static class Pair {
        BlockState state;
        Vector3dc COM;
    }
}
