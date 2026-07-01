package com.zelaux.betterdiagram.mixin.accessors;

import dev.ryanhcode.sable.physics.floating_block.FloatingBlockData;
import org.joml.Matrix3d;
import org.joml.Vector3d;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.gen.*;

@Mixin(FloatingBlockData.class)
public interface FloatingBlockDataAccessors {
    @Accessor("blockCount")
    int bcd$blockCount();
    @Accessor("blockCount")
    void bcd$blockCount(int value);

    @Accessor("latestPressureScale")
    double bcd$latestPressureScale();
    @Accessor("latestPressureScale")
    void bcd$latestPressureScale(double value);

    @Accessor("weightedPosition")
    Vector3d bcd$weightedPosition();
    @Accessor("weightedPosition")
    void bcd$weightedPosition(Vector3d value);

    @Accessor("outerProduct")
    Matrix3d bcd$outerProduct();
    @Accessor("outerProduct")
    void bcd$outerProduct(Matrix3d value);

    @Accessor("totalScale")
    double bcd$totalScale();
    @Accessor("totalScale")
    void bcd$totalScale(double value);


}
