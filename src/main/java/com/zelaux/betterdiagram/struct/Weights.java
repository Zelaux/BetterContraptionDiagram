package com.zelaux.betterdiagram.struct;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;

@FieldDefaults(level = AccessLevel.PUBLIC)
@Getter

public class Weights implements Iterable<MassStack>{
    final ArrayList<MassStack> stacks = new ArrayList<>();
    MassStack smallStack;

    public void clear() {
        stacks.clear();
        smallStack = null;
    }

    public boolean isEmpty() {return stacks.isEmpty();}

    public MassStack get(int i) {return stacks.get(i);}

    @Override
    public @NotNull Iterator<MassStack> iterator() {
        return stacks.iterator();
    }
}
