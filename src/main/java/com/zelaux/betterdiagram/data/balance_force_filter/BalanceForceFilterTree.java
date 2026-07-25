package com.zelaux.betterdiagram.data.balance_force_filter;

import com.mojang.serialization.Codec;
import com.zelaux.betterdiagram.data.BalanceForceFilter;
import com.zelaux.betterdiagram.index.BForceGroups;
import dev.ryanhcode.sable.api.physics.force.ForceGroup;
import dev.ryanhcode.sable.api.physics.force.ForceGroups;
import dev.ryanhcode.sable.api.physics.force.QueuedForceGroup;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.EqualsAndHashCode;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@EqualsAndHashCode
public class BalanceForceFilterTree {
    public static final BalanceForceFilterTree NIL = new BalanceForceFilterTree();
    public static final ResourceLocation NULL_LOCATION = ResourceLocation.fromNamespaceAndPath("_", "_");
    Object2ObjectOpenHashMap<@NotNull ResourceLocation, BalanceForceFilterSubTree> map;

    public static final Codec<BalanceForceFilterTree> CODEC = Codec.unboundedMap(
        ResourceLocation.CODEC,
        BalanceForceFilterSubTree.CODEC
    ).xmap(
        map -> new BalanceForceFilterTree(new Object2ObjectOpenHashMap<>(map)),
        it -> it.map
    );


    public BalanceForceFilterTree() {
        map = new Object2ObjectOpenHashMap<>();
    }

    BalanceForceFilterTree(Object2ObjectOpenHashMap<@NotNull ResourceLocation, BalanceForceFilterSubTree> map) {
        this.map = map;
    }

    public MatchResult match(@NotNull ForceGroup forceGroup, QueuedForceGroup.PointForce force) {
        return match(BForceGroups.getPathAny(forceGroup), force);
    }

    public MatchResult match(@Nullable ResourceLocation resourceLocation, QueuedForceGroup.PointForce force) {
        if(resourceLocation == null) resourceLocation = NULL_LOCATION;
        var tree = map.get(NULL_LOCATION);
        if(tree != null && tree.match(force).matched()) return tree.match(force);
        var subTree = map.get(resourceLocation);
        if(subTree == null) return MatchResult.NOT_MATCHED;
        return subTree.match(force);
    }

    public BalanceForceFilterTree withFilters(BalanceForceFilter... filters) {
        if(filters.length == 0) return this;
        var newTree = copy();
        newTree.addAll(filters);
        return newTree;
    }

    public BalanceForceFilterTree withoutFilters(BalanceForceFilter... filters) {
        if(filters.length == 0) return this;
        boolean any = false;
        for(BalanceForceFilter filter : filters) {
            if(contains(filter)) {
                any = true;
                break;
            }
        }
        if(!any) return this;
        var newTree = copy();
        newTree.removeFilters(filters);
        return newTree;
    }

    public boolean contains(BalanceForceFilter filter) {
        var tree = map.get(filter.forceGroup().orElse(NULL_LOCATION));
        if(tree == null) return false;
        return tree.contains(filter.location());
    }

    private void removeFilters(BalanceForceFilter... filters) {
        for(BalanceForceFilter filter : filters) {
            var subTree = map
                .get(filter.forceGroup().orElse(NULL_LOCATION));
            if(subTree == null) continue;
            subTree.remove(filter.location());
        }
    }

    public @NotNull BalanceForceFilterTree copy() {
        var newTree = new BalanceForceFilterTree();
        map.forEach((resourceLocation, oldSubTree) -> {
            var newSubTree = new BalanceForceFilterSubTree(resourceLocation);
            newSubTree.set.addAll(oldSubTree.set);
            newTree.map.put(resourceLocation, newSubTree);
        });
        return newTree;
    }

    private void addAll(BalanceForceFilter[] filters) {
        for(BalanceForceFilter filter : filters) {
            ResourceLocation key = filter.forceGroup().orElse(NULL_LOCATION);
            map.computeIfAbsent(key, BalanceForceFilterSubTree::new)
               .add(filter.location());
        }
    }

    public boolean onlyGravity() {
        if(map.size() != 1) return false;
        return map.containsKey(ForceGroups.GRAVITY.getId());
    }

    public boolean couldMatch(ForceGroup key) {
        return map.containsKey(key == null ? NULL_LOCATION : BForceGroups.getPathAny(key));
    }
}
