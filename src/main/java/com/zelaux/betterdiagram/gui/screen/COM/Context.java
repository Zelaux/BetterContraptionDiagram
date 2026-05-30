package com.zelaux.betterdiagram.gui.screen.COM;

import com.zelaux.betterdiagram.util.CenterMassCache;
import com.zelaux.betterdiagram.util.VecUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.util.Lazy;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3dc;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

@AllArgsConstructor
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class Context {
    private final COMPairs pairs = new COMPairs(this);
    public Entry[] entries;
    public boolean inverted;
    public int selectedPair = -1;
    public boolean buildOnInit = false;
    public boolean searching;
    @NotNull
    private ItemStack filterItem = ItemStack.EMPTY;
    @Getter
    @Nullable
    private BlockItem blockItem = null;
    @Getter
    @Nullable
    private Block block = null;

    public Context(Entry[] entries, ItemStack filterItem) {
        this.entries = entries;
        filterItem(filterItem);
    }

    //TODO replace with BlockState or Block direcly
    public Context filterItem(@Nullable ItemStack filterItem) {
        filterItem = Objects.requireNonNullElse(filterItem, ItemStack.EMPTY);
        if(filterItem.getItem() != this.filterItem.getItem()) {
            selectedPair = -1;
            this.filterItem = filterItem;
            pairsReload.invalidate();
        }

        blockItem = null;
        block = null;

        if(!this.filterItem.isEmpty() && this.filterItem.getItem() instanceof BlockItem blockItem) {
            this.blockItem = blockItem;
            block = blockItem.getBlock();
        }

        return this;
    }

    public static Context newContext() {
        return new Context(new Entry[]{
            Entry.makeEntry(Direction.Axis.X), Entry.makeEntry(Direction.Axis.Y), Entry.makeEntry(Direction.Axis.Z)
        }, ItemStack.EMPTY);
    }    private final Lazy<COMPairs> pairsReload = Lazy.of(() -> {
        pairs.reload();
        return pairs;
    });

    public COMPairs pairs() {return pairsReload.get();}

    public ItemStack filterItem() {
        return filterItem;
    }

    public void setEntries(Vector3dc center) {
        entries[0].setValue(center.x());
        entries[1].setValue(center.y());
        entries[2].setValue(center.z());
    }

    public static class COMPairs {
        @Getter
        private final ArrayList<COMPair> pairs = new ArrayList<>();
        private final Context context;
        @Getter
        private boolean waitToLoad = false;
        @Nullable
        private Block prevBlock = null;
        @Nullable
        private Consumer<ArrayList<COMPair>> afterLoad;

        public COMPairs(Context context) {this.context = context;}


        /**
         * @return is loaded
         */
        public boolean reload() {
            pairs.clear();
            waitToLoad = false;
            if(context.block != prevBlock) afterLoad = null;
            if(context.blockItem == null) return false;

            var foundMass = CenterMassCache.getBlock2Pairs(Minecraft.getInstance().player.level());
            foundMass.ifLeft(all -> {
                var centers = all.get(context.block);
                if(centers == null || centers.isEmpty()) return;
                centers.forEach((com, blockStates) -> pairs.add(new COMPair(com, blockStates)));
                pairs.sort(Comparator.comparing(COMPair::center, VecUtil.CMP_AS_ARR));
                waitToLoad = true;
                if(context.block == prevBlock) {
                    if(afterLoad != null) afterLoad.accept(pairs);
                    afterLoad = null;
                }
                prevBlock = null;

            }).ifRight(right -> {
                prevBlock = context.block;
                right.thenAccept(t -> context.pairsReload.invalidate());
            });
            return waitToLoad;

        }
/*
        public COMPair get(int i) {
            //return pairs.get(i);
        }*/

        public void afterLoad(Consumer<ArrayList<COMPair>> pairConsumer) {
            if(waitToLoad) {
                pairConsumer.accept(pairs);
                return;
            }
            this.afterLoad = pairConsumer;
        }

        public boolean isNotLoaded() {
            return !waitToLoad;
        }
    }

    public record COMPair(Vector3dc center, ArrayList<BlockState> states) {
        public static final COMPair[] EMPTY_ARRAY = new COMPair[0];

        public static COMPair make(Map.Entry<Vector3dc, ArrayList<BlockState>> pair) {
            return new COMPair(pair.getKey(), pair.getValue());
        }
    }




}
