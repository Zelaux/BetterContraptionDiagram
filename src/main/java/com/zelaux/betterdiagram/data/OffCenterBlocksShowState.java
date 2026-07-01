package com.zelaux.betterdiagram.data;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

public enum OffCenterBlocksShowState implements StringRepresentable {
    none,
    show,
    showAll;
    public final static OffCenterBlocksShowState[] all = values();
    public static final Codec<OffCenterBlocksShowState> CODEC = StringRepresentable.fromEnum(OffCenterBlocksShowState::values);

    public OffCenterBlocksShowState next() {
        return all[(ordinal() + 1) % all.length];
    }

    public OffCenterBlocksShowState prev() {
        return all[(ordinal() + all.length - 1) % all.length];
    }

    @Override
    public String getSerializedName() {
        return name();
    }
}
