package com.zelaux.betterdiagram.struct;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.List;

@FieldDefaults(level = AccessLevel.PUBLIC)
@Getter
public class Weights {
    public static final Codec<Weights> CODEC = RecordCodecBuilder.create(i -> i.group(
        MassStack.CODEC.listOf().fieldOf("stacks").forGetter(it -> it.stacks),
        MassStack.CODEC.listOf().fieldOf("smallStacks").forGetter(it -> it.smallStacks),
        Codec.DOUBLE.fieldOf("totalWeight").forGetter(it -> it.totalWeight)
    ).apply(i, Weights::make));

    private static Weights make(List<MassStack> massStacks, List<MassStack> smallStacks, double totalWeight) {
        Weights weights = new Weights();
        weights.totalWeight = totalWeight;
        weights.stacks.addAll(massStacks);
        weights.smallStacks.addAll(smallStacks);
        return weights;
    }

    final ArrayList<MassStack> stacks = new ArrayList<>();
    final ArrayList<MassStack> smallStacks = new ArrayList<>();
    double totalWeight;

    public void clear() {
        stacks.clear();
        smallStacks.clear();
        totalWeight = 0;
    }

    public boolean isEmpty() {return stacks.isEmpty() && smallStacks.isEmpty();}

    public MassStack get(int i) {return stacks.get(i);}

    public MassStack getSmall(int i) {return stacks.get(i);}


    public Vector3d position() {
        return stacks.isEmpty() ? smallStacks.getFirst().position() : stacks.getFirst().position();
    }

    public double totalWeight() {
        return totalWeight;
    }
}
