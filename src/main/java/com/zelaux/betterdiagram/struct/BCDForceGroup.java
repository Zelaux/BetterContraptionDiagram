package com.zelaux.betterdiagram.struct;

import com.mojang.serialization.Codec;
import com.zelaux.betterdiagram.index.BCDForceGroups;
import dev.ryanhcode.sable.api.physics.force.ForceGroup;
import dev.ryanhcode.sable.api.physics.force.ForceGroups;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
public
class BCDForceGroup {
    //public static final Codec<BCDForceGroup> NAME_CODEC = ;
    public final ResourceLocation resourceLocation;
    public final ForceGroup group;

    public final Component axisName;
    public final int color;
    public final @NotNull Component name;
    public final @Nullable Component description;


    public BCDForceGroup(ResourceLocation resourceLocation, @NotNull Component name, @Nullable Component description, int color, boolean defaultDisplayed, Component axisName) {
        this.resourceLocation = resourceLocation;
        this.axisName = axisName;
        this.color = color;
        this.name = name;
        this.description = description;
        group = new ForceGroup(name, description, color, defaultDisplayed);
    }
}
