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
    static Map<Vector3dc, Map<Block, List<Pair>>> COM2_Block2States = null;
    static HashMap<Block, HashMap<Vector3dc, ArrayList<Pair>>> Block2Pairs = null;
    private static long COM2_Block2States_time;
    private static long Block2Pairs_nano;

    public static @NotNull Map<Vector3dc, Map<Block, List<Pair>>> getCOM2_Block2States(HolderLookup.RegistryLookup<Block> blocks, Level level) {
        if(COM2_Block2States != null) return COM2_Block2States;
        long nano=System.nanoTime();
        var collect = new HashMap<Vector3dc, Map<Block, List<Pair>>>();
        for(var blockListEntry : getBlock2Pairs(blocks, level).entrySet()) {
            Block block = blockListEntry.getKey();
            for(ArrayList<Pair> pairs : blockListEntry.getValue().values()) {
                for(Pair pair : pairs) {
                    collect
                        .computeIfAbsent(pair.COM, it -> new HashMap<>())
                        .computeIfAbsent(block, it -> new ArrayList<>())
                        .add(pair);
                }
            }

        }
        long endNano=System.nanoTime();
        COM2_Block2States_time=endNano-nano;
        BetterContraptionDiagram.LOGGER.debug("Time to build com2_block2states cache {}ms", (endNano - nano) / 1_000_000.);
        return COM2_Block2States = collect;
    }

    public static @NotNull HashMap<Block, HashMap<Vector3dc, ArrayList<Pair>>> getBlock2Pairs(HolderLookup.RegistryLookup<Block> blocks, Level level) {
        if(CenterMassCache.Block2Pairs != null) return CenterMassCache.Block2Pairs;

        long nano=System.nanoTime();
        var map = new HashMap<Block, HashMap<Vector3dc, ArrayList<Pair>>>();
        blocks.listElements()
              .sequential()
              .forEach(ref -> {
                  Block value = ref.value();
                  var pairs1 = new HashMap<Vector3dc, ArrayList<Pair>>();
                  forEachCOM(level, value, false, (state, centerOfMass) -> {

                      pairs1.computeIfAbsent(centerOfMass, it -> new ArrayList<>()).add(new Pair(state, centerOfMass));
                  });
                  var pairs = pairs1;
                  map.put(value, pairs);

              });

        long endNano=System.nanoTime();
        Block2Pairs_nano=endNano-nano;
        BetterContraptionDiagram.LOGGER.debug("Time to build block2pairs cache {}ms", (endNano - nano) / 1_000_000.);
        return CenterMassCache.Block2Pairs = map;
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

    public static @NotNull HashMap<Block, HashMap<Vector3dc, ArrayList<Pair>>> getBlock2Pairs(Player player) {
        return getBlock2Pairs(player.level());
    }

    public static @NotNull HashMap<Block, HashMap<Vector3dc, ArrayList<Pair>>> getBlock2Pairs(Level level) {
        return getBlock2Pairs(
            level.registryAccess().lookup(Registries.BLOCK).orElse(null),
            level
        );
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
