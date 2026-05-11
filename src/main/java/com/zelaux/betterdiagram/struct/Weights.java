package com.zelaux.betterdiagram.struct;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.Iterator;

@FieldDefaults(level = AccessLevel.PUBLIC)
@Getter

public class Weights {
    final ArrayList<MassStack> stacks = new ArrayList<>();
    final ArrayList<MassStack> smallStacks = new ArrayList<>();
    double totalWeight;
    public void clear() {
        stacks.clear();
        smallStacks.clear();
        totalWeight=0;
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
