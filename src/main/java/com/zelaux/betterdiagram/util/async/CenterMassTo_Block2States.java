package com.zelaux.betterdiagram.util.async;

import com.mojang.datafixers.util.Either;
import com.zelaux.betterdiagram.BetterContraptionDiagram;
import com.zelaux.betterdiagram.util.CenterMassCache;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.SneakyThrows;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3dc;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;

public class CenterMassTo_Block2States extends HashMap<@NotNull Vector3dc, Map<@NotNull Block, List<BlockState>>> {
    @SneakyThrows
    public static @NotNull CenterMassTo_Block2States calc(HolderLookup.RegistryLookup<Block> blocks, Level level, ConcurrentLinkedQueue<Part> x) {
        long nano = System.nanoTime();
        var resultMap = new CenterMassTo_Block2States();
        var lists = new ObjectArrayList<ObjectArrayList<BlockState>>();

        Either<BlockTo_CenterMass2State, CompletableFuture<BlockTo_CenterMass2State>> block2Pairs = CenterMassCache.getBlock2Pairs(blocks, level);
        Optional<BlockTo_CenterMass2State> left = block2Pairs.left();
        BlockTo_CenterMass2State blockToCenterMass2State = left.orElse(null);
        if(left.isEmpty()) {
            var empty = block2Pairs.right().orElse(null);
            if(empty == null) return new CenterMassTo_Block2States();
            blockToCenterMass2State = empty.get();

        }
        if(blockToCenterMass2State != null)
            for(var blockListEntry : blockToCenterMass2State.entrySet()) {
                Block block = blockListEntry.getKey();
                var value = blockListEntry.getValue();

                for(var COMAndBlockStates : value.entrySet()) {
                    var COM = COMAndBlockStates.getKey();
                    var blockStates = COMAndBlockStates.getValue();
                    resultMap
                        .computeIfAbsent(COM, it -> new HashMap<>())
                        .computeIfAbsent(block, it -> CenterMassCache.getSaved(lists))
                        .addAll(blockStates);

                    x.add(new Part(
                        COM, block, blockStates, false
                    ));
                }
                for(var list : lists) {
                    list.trim(list.size());
                }
                lists.clear();

            }

        x.add(new Part(
            null, null, null, true
        ));
        long endNano = System.nanoTime();
        //COM2_Block2States_time = endNano - nano;
        BetterContraptionDiagram.LOGGER.debug("Time to build com2_block2states cache {}ms", (endNano - nano) / 1_000_000.);
        return resultMap;
    }

    public record Part(Vector3dc COM, Block block, List<BlockState> map, boolean finish) {}

    @Nullable
    public Map<@NotNull Block, List<BlockState>> get(Vector3dc key) {
        return super.get(key);
    }

    @Override
    @Nullable
    public Map<@NotNull Block, List<BlockState>> get(Object key) {
        return super.get(key);
    }
}
