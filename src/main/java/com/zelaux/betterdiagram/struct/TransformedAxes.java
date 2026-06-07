package com.zelaux.betterdiagram.struct;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2d;
import org.joml.Vector2i;

@FieldDefaults(level = AccessLevel.PUBLIC, makeFinal = true)
@Getter

public final class TransformedAxes {
    Vector2d[] transformed;
    int[] xyzIndex;
    Vector2d xDouble, yDouble, zDouble;
    Vector2i xInt, yInt, zInt;
    boolean notAligned;

    public TransformedAxes(Vector2d[] transformed, int[] xyzIndex, boolean notAligned) {
        this.transformed = transformed;
        this.xyzIndex = xyzIndex;
        xDouble = transformed[0];
        yDouble = transformed[1];
        zDouble = transformed[2];
        this.notAligned = notAligned;
        xInt = makeInt(xDouble);
        yInt = makeInt(yDouble);
        zInt = makeInt(zDouble);
    }

    private static @NotNull Vector2i makeInt(Vector2d v) {return new Vector2i((int) v.x, (int) v.y);}
}
