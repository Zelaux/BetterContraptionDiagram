package com.zelaux.betterdiagram.gui.screen.COM;

import com.zelaux.betterdiagram.util.CenterMassCache;
import com.zelaux.betterdiagram.util.VecUtil;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
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
@FieldDefaults(level = AccessLevel.PUBLIC)
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class Context {
    Entry[] entries;
    @NotNull
    private ItemStack filterItem=ItemStack.EMPTY;


    int selectedPair=-1;


    @Getter
    @Nullable
    private BlockItem blockItem = null;

    private final Lazy<COMPair[]> pairs = Lazy.of(() -> {
        if(blockItem == null) return null;
        var centers = CenterMassCache.getBlock2Pairs(Minecraft.getInstance().player).get(blockItem.getBlock());
        if(centers == null || centers.isEmpty()) {return null;}

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



    public record COMPair(Vector3dc center, ArrayList<CenterMassCache.Pair> pairs) {
        public static COMPair make(Map.Entry<Vector3dc, ArrayList<CenterMassCache.Pair>> pair) {
            return new COMPair(pair.getKey(), pair.getValue());
        }
    }
}
