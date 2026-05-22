package com.zelaux.betterdiagram.struct;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.ryanhcode.sable.companion.impl.SableCompanionUtil;
import org.joml.Vector3d;

public record MassStack(Vector3d position, double amountOf) {
    public static final Codec<MassStack> CODEC = RecordCodecBuilder.create(i -> i.group(
        SableCompanionUtil.VECTOR_3D_CODEC.fieldOf("position").forGetter(MassStack::position),
        Codec.DOUBLE.fieldOf("amountOf").forGetter(MassStack::amountOf)
    ).apply(i,MassStack::new));
}
