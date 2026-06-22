package com.zelaux.betterdiagram.feature.include_subsublevel.math;

import org.joml.*;

import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;

public class Vector3dWith<T> extends Vector3d {
    public T second;

    public Vector3dWith() {
    }

    public Vector3dWith(double d) {
        super(d);
    }

    public Vector3dWith(double x, double y, double z) {
        super(x, y, z);
    }

    public Vector3dWith(Vector3fc v) {
        super(v);
    }

    public Vector3dWith(Vector3ic v) {
        super(v);
    }

    public Vector3dWith(Vector2fc v, double z) {
        super(v, z);
    }

    public Vector3dWith(Vector2ic v, double z) {
        super(v, z);
    }

    public Vector3dWith(Vector3dc v) {
        super(v);
    }

    public Vector3dWith(Vector2dc v, double z) {
        super(v, z);
    }

    public Vector3dWith(double[] xyz) {
        super(xyz);
    }

    public Vector3dWith(float[] xyz) {
        super(xyz);
    }

    public Vector3dWith(ByteBuffer buffer) {
        super(buffer);
    }

    public Vector3dWith(int index, ByteBuffer buffer) {
        super(index, buffer);
    }

    public Vector3dWith(DoubleBuffer buffer) {
        super(buffer);
    }

    public Vector3dWith(int index, DoubleBuffer buffer) {
        super(index, buffer);
    }
}
