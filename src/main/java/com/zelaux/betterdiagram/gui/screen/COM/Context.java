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
import net.neoforged.neoforge.common.util.Lazy;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3dc;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

@AllArgsConstructor
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class Context {
    public Entry[] entries;
    public boolean inverted;
    @NotNull
    private ItemStack filterItem=ItemStack.EMPTY;

    public int selectedPair=-1;
    @Getter
    @Nullable
    private BlockItem blockItem = null;

    public boolean buildOnInit=false;

    private final Lazy<COMPair[]> pairs = Lazy.of(() -> {
        if(blockItem == null) return COMPair.EMPTY_ARRAY;
        var centers = CenterMassCache.getBlock2Pairs(Minecraft.getInstance().player).get(blockItem.getBlock());
        if(centers == null || centers.isEmpty()) {return COMPair.EMPTY_ARRAY;}
        return centers
            .entrySet()
            .stream()
            .sorted(Map.Entry.comparingByKey(VecUtil.CMP_AS_ARR))
            .map(COMPair::make).toArray(COMPair[]::new);
    });

    public static Context newContext() {
        return new Context(new Entry[]{
            Entry.makeEntry(Direction.Axis.X), Entry.makeEntry(Direction.Axis.Y), Entry.makeEntry(Direction.Axis.Z)
        },ItemStack.EMPTY);
    }

    public COMPair[] pairs() {return pairs.get();}


    public Context(Entry[] entries, ItemStack filterItem) {
        this.entries = entries;
        filterItem(filterItem);
    }

    public ItemStack filterItem() {
        return filterItem;
    }

    //TODO replace with BlockState or Block direcly
    public Context filterItem(@Nullable ItemStack filterItem) {
        filterItem = Objects.requireNonNullElse(filterItem, ItemStack.EMPTY);
        if(filterItem.getItem() != this.filterItem.getItem()){
            selectedPair=-1;
            this.filterItem = filterItem;
            pairs.invalidate();
        }

        blockItem = null;

        if(!this.filterItem.isEmpty() && this.filterItem.getItem() instanceof BlockItem blockItem) {
            this.blockItem = blockItem;
        }

        return this;
    }

    public void setEntries(Vector3dc center) {
        entries[0].setValue(center.x());
        entries[1].setValue(center.y());
        entries[2].setValue(center.z());
    }


    public record COMPair(Vector3dc center, ArrayList<CenterMassCache.Pair> pairs) {
        public static final COMPair[] EMPTY_ARRAY = new COMPair[0];

        public static COMPair make(Map.Entry<Vector3dc, ArrayList<CenterMassCache.Pair>> pair) {
            return new COMPair(pair.getKey(), pair.getValue());
        }
    }
}
