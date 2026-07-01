package com.zelaux.betterdiagram.index;

import com.mojang.serialization.Codec;
import com.zelaux.betterdiagram.BetterContraptionDiagram;
import com.zelaux.betterdiagram.struct.BCDForceGroup;
import dev.ryanhcode.sable.api.physics.force.ForceGroup;
import dev.ryanhcode.sable.api.physics.force.ForceGroups;
import dev.simulated_team.simulated.Simulated;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.util.strategy.IdentityStrategy;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface BCDForceGroups {

    Object2ObjectOpenHashMap<ResourceLocation, BCDForceGroup> REGISTRY = new Object2ObjectOpenHashMap<>();
    Map<ForceGroup, BCDForceGroup> wrappedMap = new Object2ObjectOpenCustomHashMap<>(IdentityStrategy.IDENTITY);


    BCDForceGroup AXIS_X_GROUP = registerGroup("axis_group_x", Component.translatable("better_contraption_diagram.axis_group.x"), 0xE65751, false, Component.literal("X"));
    BCDForceGroup AXIS_Y_GROUP = registerGroup("axis_group_y", Component.translatable("better_contraption_diagram.axis_group.y"), 0x88E661, false, Component.literal("Y"));
    BCDForceGroup AXIS_Z_GROUP = registerGroup("axis_group_z", Component.translatable("better_contraption_diagram.axis_group.z"), 0x8E88E6, false, Component.literal("Z"));
    BCDForceGroup ROPE_FORCE = registerGroup("rope", Component.translatable("force-group.rope"), 0xe6ab56, false);
    BCDForceGroup ROPE_MASS_FORCE = registerGroup("rope-mass", Component.translatable("force-group.rope-mass"), 0x946F3A, false);
    BCDForceGroup MERGED_FORCE_GROUP = registerGroup("merged-force", Component.translatable("force-group.merged-force"), Colors.DISPLACEMENT_GREEN_COLORS[3].getRGB(), false);
    BCDForceGroup BALANCE_FORCE_GROUP = registerGroup("balance-force", Component.translatable("bcd.balance-point"), Colors.DISPLACEMENT_GREEN_COLORS[3].getRGB(), false);
    BCDForceGroup CONTRAPTION_GRAVITY = registerGroup("contraption_gravity", Component.translatable("force-group.contraption-gravity"), 0x32a580, false);


    BCDForceGroup[] AXIS_GROUPS = {AXIS_X_GROUP, AXIS_Y_GROUP, AXIS_Z_GROUP};

    ObjectArrayList<BCDForceGroup> CUSTOM_FORCES = new ObjectArrayList<>(List.of(
        ROPE_FORCE, MERGED_FORCE_GROUP, ROPE_MASS_FORCE, CONTRAPTION_GRAVITY
    ));

    static BCDForceGroup registerGroup(String innerName, Component name, Component description, int color, boolean defaultDisplayed, Component axisName) {
        ResourceLocation resource = BetterContraptionDiagram.resource(innerName);
        BCDForceGroup group = new BCDForceGroup(resource, name, description, color, defaultDisplayed, axisName);
        wrappedMap.put(group.group, group);
        REGISTRY.put(resource, group);
        return group;
    }

    static BCDForceGroup registerGroup(String innerName, Component name, int color, boolean defaultDisplayed, Component axisName) {
        return registerGroup(innerName, name, null, color, defaultDisplayed, axisName);
    }

    static BCDForceGroup registerGroup(String innerName, Component name, int color, boolean defaultDisplayed) {
        return registerGroup(innerName, name, null, color, defaultDisplayed, null);
    }

    ResourceLocation simulated$bufferLocation = Simulated.path("diagram_buffer");

    static Optional<BCDForceGroup> byPath(ResourceLocation resourceLocation) {
        return Optional.ofNullable(REGISTRY.get(resourceLocation));
    }

    static Optional<ForceGroup> byPathAny(ResourceLocation resourceLocation) {
        return byPath(resourceLocation)
            .map(BCDForceGroup::group)
            .or(() -> ForceGroups.REGISTRY.getOptional(resourceLocation));
    }

    static @Nullable ResourceLocation getPath(BCDForceGroup group) {
        return group.resourceLocation;
    }

    @Nullable
    static ResourceLocation getPathAny(ForceGroup forceGroup) {
        ResourceLocation keyOrNull = ForceGroups.REGISTRY.getKeyOrNull(forceGroup);
        if(keyOrNull != null) return keyOrNull;
        BCDForceGroup group = wrappedMap.get(forceGroup);
        if(group == null) return null;
        return group.resourceLocation;
    }

    static Iterable<BCDForceGroup> all() {
        return REGISTRY.values();
    }
}
