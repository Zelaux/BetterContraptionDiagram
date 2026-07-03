package com.zelaux.betterdiagram.data;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zelaux.betterdiagram.index.BCDForceGroups;
import com.zelaux.betterdiagram.struct.BCDForceGroup;
import dev.ryanhcode.sable.api.physics.force.ForceGroup;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@FieldDefaults(makeFinal = true, level = AccessLevel.PUBLIC)
@AllArgsConstructor
public class EnabledForces {
    ImmutableSet<BCDForceGroup> display;
    ImmutableSet<ForceGroup> balance;

    public static final Codec<EnabledForces> CODEC = RecordCodecBuilder.create(i -> i.group(
            ResourceLocation.CODEC.listOf().optionalFieldOf("display", List.of()).forGetter(x -> getResourceLocations(x.display, BCDForceGroups::getPath)),
            ResourceLocation.CODEC.listOf().optionalFieldOf("balance", List.of()).forGetter(x -> getResourceLocations(x.balance, BCDForceGroups::getPathAny))
        ).apply(i, (display, balance) -> new EnabledForces(
            getValues(display, BCDForceGroups::byPath),
            getValues(balance, BCDForceGroups::byPathAny)
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

    public static EnabledForces empty() {
        return new EnabledForces(ImmutableSet.of(), ImmutableSet.of());
    }

    public EnabledForces removeDisplay(BCDForceGroup group) {
        if(!display.contains(group)) return this;
        return new EnabledForces(removeFromSet(display, group), balance);
    }

    public EnabledForces removeBalance(ForceGroup group) {
        if(!balance.contains(group)) return this;
        return new EnabledForces(display, removeFromSet(balance, group));
    }

    public EnabledForces addDisplay(BCDForceGroup group) {
        if(display.contains(group)) return this;
        return new EnabledForces(addToSet(display, group), balance);
    }

    public EnabledForces addBalance(ForceGroup group) {
        if(balance.contains(group)) return this;
        return new EnabledForces(display, addToSet(balance, group));
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
