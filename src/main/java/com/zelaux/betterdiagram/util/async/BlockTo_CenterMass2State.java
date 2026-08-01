package com.zelaux.betterdiagram.util.async;

import com.google.common.collect.ImmutableMap;
import com.zelaux.betterdiagram.BetterContraptionDiagram;
import com.zelaux.betterdiagram.BetterContraptionDiagramClient;
import com.zelaux.betterdiagram.util.CenterMassCache;
import com.zelaux.betterdiagram.util.CenterMassCalculator;
import it.unimi.dsi.fastutil.doubles.Double2DoubleRBTreeMap;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3d;
import org.joml.Vector3dc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import static com.zelaux.betterdiagram.util.CenterMassCache.getSaved;

public class BlockTo_CenterMass2State extends HashMap<@NotNull Block, Map<@NotNull Vector3dc, ArrayList<BlockState>>> {
    public static @NotNull BlockTo_CenterMass2State calc(HolderLookup.RegistryLookup<Block> blocks, Level level, ConcurrentLinkedQueue<Part> x) {
        long nano = System.nanoTime();
        var map = new BlockTo_CenterMass2State();
        var allocatedLists = new ArrayList<ArrayList<BlockState>>();

        var cordsMap = new Double2DoubleRBTreeMap((f1, f2) -> CenterMassCalculator.equals(f1, f2) ? 0 : Double.compare(f1, f2));
        for(int i = 0; i < 8; i++) {
            cordsMap.put(i / 8.0, i / 8.0);
        }
        blocks
            .listElements()
            .sequential()
            .forEach(ref -> {
                Block block = ref.value();
                var collectorTree = new HashMap<Vector3dc, ArrayList<BlockState>>();
                CenterMassCache.forEachCOM(level, block, false, (state, centerOfMass) -> collectorTree.computeIfAbsent(internVec3(centerOfMass, cordsMap), it -> getSaved(allocatedLists)).add(state));
                for(ArrayList<BlockState> list : allocatedLists) {
                    list.trimToSize();
                }
                allocatedLists.clear();
                //x.add(new BlockTo_CenterMass2State.Part(block, pairs, false));
                map.put(block, ImmutableMap.copyOf(collectorTree));

            });

        //x.add(new BlockTo_CenterMass2State.Part(null, null, true));
        long endNano = System.nanoTime();
        //Block2Pairs_nano = endNano - nano;
        BetterContraptionDiagram.LOGGER.debug("Time to build block2pairs cache {}ms", (endNano - nano) / 1_000_000.);
        return map;
    }

    private static @NotNull Vector3dc internVec3(Vector3dc centerOfMass, Double2DoubleRBTreeMap cordsMap) {
        double x = cordsMap.putIfAbsent(centerOfMass.x(), centerOfMass.x());
        double y = cordsMap.putIfAbsent(centerOfMass.y(), centerOfMass.y());
        double z = cordsMap.putIfAbsent(centerOfMass.z(), centerOfMass.z());
        if(x == centerOfMass.x() && y == centerOfMass.y() && z == centerOfMass.z())
            return centerOfMass;
        return new Vector3d(
            x,
            y,
            z
        );
    }

    /**
     * @param block unique per queue
     *
     */
    public record Part(Block block, Map<@NotNull Vector3dc, ArrayList<BlockState>> COM2BLockState,
                       boolean finish) {}


    public Map<@NotNull Vector3dc, ArrayList<BlockState>> get(Block key) {
        return get((Object) key);
    }

    @Override
    public Map<@NotNull Vector3dc, ArrayList<BlockState>> get(Object key) {
        var map = super.get(key);

        //Somehow? Idk
        if(map == null) {
            BetterContraptionDiagramClient.LOGGER.error("{} has no CoM cache", key);
            return Map.of();
        }
        return map;
    }
}
