package com.zelaux.betterdiagram.feature.max_speed;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.joml.Matrix3d;

public interface FloatingBlockControllerAcc {
    void bcd$matrixCache$size(int bcd$matrixCache$size);

    ObjectArrayList<Matrix3d> bcd$matrixCache();

    void bcd$addMatrix(Matrix3d matrix3d);

    int bcd$matrixCache$size();
}
