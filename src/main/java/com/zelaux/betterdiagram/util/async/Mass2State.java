package com.zelaux.betterdiagram.util.async;

import com.zelaux.betterdiagram.extend.accessors.BlockPropertiesComputers;
import com.zelaux.betterdiagram.util.CenterMassCache;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import lombok.RequiredArgsConstructor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.concurrent.ConcurrentLinkedQueue;

public class Mass2State {
    Long2ObjectOpenHashMap<ObjectList<BlockState>> map = new Long2ObjectOpenHashMap<>();
    public static final double MASS_SCALE = 1_000_000;

    public ObjectList<BlockState> get(double key) {
        return map.get(doubleToLongKey(key));
    }

    public static double longKeyToDouble(long key) {
        return key / MASS_SCALE;
    }

    public static long doubleToLongKey(double key) {
        return (long) (key * MASS_SCALE);
    }

    public static ObjectList<BlockEntry> calc(HolderLookup.RegistryLookup<Block> blocks, Level level, double mass, ConcurrentLinkedQueue<BlockEntry> x) {

        var result = new ObjectArrayList<BlockEntry>();
        var massKey = doubleToLongKey(mass);
        blocks
            .listElements()
            .map(Holder.Reference::value)
            .sequential()
            .forEach(block -> {
                BlockEntry entry = new BlockEntry(block);
                entry.isSingleMass = true;
                for(BlockState state : block.getStateDefinition().getPossibleStates()) {
                    if(!CenterMassCache.isValidState(block, state)) continue;
                    double _mass = BlockPropertiesComputers.getMass(level, BlockPos.ZERO, state);
                    long key = doubleToLongKey(_mass);
                    if(key != massKey) entry.isSingleMass = false;
                    else {
                        entry.states.add(state);
                    }
                }
                if(!entry.states.isEmpty()) {
                    result.add(entry);
                }


            });
        return result;
    }

    //public record Part(double mass, BlockState state) {}
    @RequiredArgsConstructor
    public static final class BlockEntry {
        public final Block block;
        public final ObjectList<BlockState> states = new ObjectArrayList<>();
        public boolean isSingleMass;

    }


}
