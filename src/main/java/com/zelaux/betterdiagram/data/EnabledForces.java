package com.zelaux.betterdiagram.data;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zelaux.betterdiagram.data.balance_force_filter.BalanceForceFilterTree;
import com.zelaux.betterdiagram.index.BForceGroups;
import com.zelaux.betterdiagram.struct.BCDForceGroup;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.With;
import lombok.experimental.FieldDefaults;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.util.Lazy;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@FieldDefaults(makeFinal = true, level = AccessLevel.PUBLIC)
@AllArgsConstructor
@Getter
@With
public class EnabledForces {
    public final ImmutableSet<BCDForceGroup> display;
    public final BalanceForceFilterTree tree;

    public static final Codec<EnabledForces> CODEC = RecordCodecBuilder.create(i -> i.group(
            ResourceLocation.CODEC.listOf().optionalFieldOf("display", List.of()).forGetter(x -> getResourceLocations(x.display, BForceGroups::getPath)),
            BalanceForceFilterTree.CODEC.optionalFieldOf("balanceForceFilterTree", BalanceForceFilterTree.NIL).forGetter(EnabledForces::tree)
        ).apply(i, (display, balance) -> new EnabledForces(
            getValues(display, BForceGroups::byPath),
            balance == BalanceForceFilterTree.NIL ? new BalanceForceFilterTree() : balance
        ))
    );


    private static <T> @NotNull ImmutableSet<T> getValues(List<ResourceLocation> locations, Function<ResourceLocation, Optional<T>> toValue) {
        var values = ImmutableSet.<T>builder();
        for(var location : locations) {
            var value = toValue.apply(location);
            if(value.isEmpty()) continue;
            values.add(value.get());
        }
        return values.build();
    }

    private static <T> @NotNull ObjectArrayList<ResourceLocation> getResourceLocations(ImmutableSet<T> set, Function<T, ResourceLocation> toLocation) {
        ObjectArrayList<ResourceLocation> resourceLocations = new ObjectArrayList<>(set.size());
        for(T value : set) {
            ResourceLocation resourceLocation = toLocation.apply(value);
            if(resourceLocation == null) continue;
            resourceLocations.add(resourceLocation);
        }
        return resourceLocations;
    }

    public static EnabledForces defaultValue() {
        return new EnabledForces(defaultDisplay.get(), new BalanceForceFilterTree());
    }
    public static Lazy<ImmutableSet<BCDForceGroup>> defaultDisplay= Lazy.of(()->{
        var builder = ImmutableSet.<BCDForceGroup>builder();
        for(BCDForceGroup force : BForceGroups.CUSTOM_FORCES) {
            if(force.group.defaultDisplayed()) {
                builder.add(force);
            }
        }
        return builder.build();
    });

    public EnabledForces removeDisplay(BCDForceGroup group) {
        if(!display.contains(group)) return this;
        return withDisplay(removeFromSet(display, group));
    }

    public EnabledForces addDisplay(BCDForceGroup group) {
        if(display.contains(group)) return this;
        return withDisplay(addToSet(display, group));
    }

    public EnabledForces removeBalance(BalanceForceFilter group) {
        if(!tree.contains(group)) return this;
        return new EnabledForces(display, tree.withoutFilters(group));
    }

    public EnabledForces addBalance(BalanceForceFilter group) {
        if(tree.contains(group)) return this;
        return new EnabledForces(display, tree.withFilters(group));
    }

    private <T> @NotNull ImmutableSet<T> addToSet(ImmutableSet<T> oldSet, T newValue) {
        return ImmutableSet.<T>builder().addAll(oldSet).add(newValue).build();
    }

    private <T> @NotNull ImmutableSet<T> removeFromSet(ImmutableSet<T> oldSet, T removeValue) {
        var builder = ImmutableSet.<T>builder();
        for(T value : oldSet) {
            if(value == removeValue) continue;
            builder.add(value);
        }
        return builder.build();
    }

    public boolean display(BCDForceGroup forceGroup) {
        return display.contains(forceGroup);
    }
}
