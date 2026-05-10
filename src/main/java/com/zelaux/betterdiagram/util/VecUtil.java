package com.zelaux.betterdiagram.util;

import com.zelaux.betterdiagram.struct.TransformedAxes;
import dev.ryanhcode.sable.companion.math.BoundingBox3ic;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.NotNull;
import org.joml.*;
import org.spongepowered.asm.mixin.*;

import java.lang.Math;
import java.util.function.ObjDoubleConsumer;
import java.util.function.ToDoubleFunction;

public class VecUtil {
    public static final Vector3d X_V = new Vector3d(1, 0, 0);
    public static final Vector3d Y_V = new Vector3d(0, 1, 0);
    public static final Vector3d Z_V = new Vector3d(0, 0, 1);
    public static final Vector3d[] DIRECTIONS = {X_V, Y_V, Z_V};
    public static final Vector3d ZERO_V = new Vector3d(0, 0, 0);
    public static final ToDoubleFunction<Vector3d>[] GETTERS_3d = genArr(Vector3d::x, Vector3d::y, Vector3d::z);
    public static final ObjDoubleConsumer<Vector3d>[] SETTERS_3d = genArr((t, v) -> t.x = v, (t, v) -> t.y = v, (t, v) -> t.z = v);
    public static final ToDoubleFunction<Vector2d>[] GETTERS_2d = genArr(Vector2d::x, Vector2d::y);
    public static final ObjDoubleConsumer<Vector2d>[] SETTERS_2d = genArr((t, v) -> t.x = v, (t, v) -> t.y = v);

    @SafeVarargs
    private static <T> T[] genArr(T... arr) {
        return arr;
    }

    public static double maxAbsComponent(Vector2d v) {return Math.max(Math.abs(v.x), Math.abs(v.y));}

    public static @NotNull Vector3d minVec3d(BoundingBox3ic box) {
        return new Vector3d(box.minX(), box.minY(), box.minZ());
    }


    public static @NotNull Vector3d maxVec3d(BoundingBox3ic box) {
        return new Vector3d(box.maxX(), box.maxY(), box.maxZ());
    }

    public static TransformedAxes projectAxises(Quaternionf orientation) {
        var coords = new Vector2d[3];
        int[] xyz = new int[3];
        Vector3d tmp = new Vector3d();
        for(int i = 0; i < coords.length; i++) {
            orientation.transformInverse(tmp.set(DIRECTIONS[i]));

            double scalar = Math.max(Math.abs(tmp.x), Math.abs(tmp.y));
            if(scalar == 0 || org.joml.Runtime.equals(scalar, 0, 0.1f)) {
                scalar = 1;
                tmp.zero();
            }
            //screenCoords.div(scalar);
            var v = coords[i] = new Vector2d(tmp.x / scalar, tmp.y / scalar).round();
            if(v.x != 0) xyz[0] = i;
            else if(v.y != 0) xyz[1] = i;
            else xyz[2] = i;
        }
        return new TransformedAxes(coords, xyz);
    }

    @Unique
    public static @NotNull MutableComponent vectorToFormatted(Vector3d force) {
        return Component.literal(vecToString(force));
    }

    private static @NotNull String vecToString(Quaterniond force) {
        return vecToString(force.x, force.y, force.z, force.w);
    }

    private static @NotNull String vecToString(double x, double y, double z, double... extra) {
        String[] strings = new String[extra.length + 3];
        strings[0] = StringUtil.plainDouble(x);
        strings[1] = StringUtil.plainDouble(y);
        strings[2] = StringUtil.plainDouble(z);
        for(int i = 0; i < extra.length; i++) {
            strings[i + 3] = StringUtil.plainDouble(extra[i]);
        }

        return "(" + String.join(",", strings) + ")";
    }

    @Unique
    private static @NotNull String vecToString(Vector3d force) {
        return vecToString(force.x, force.y, force.z);
    }

    @Unique
    private static @NotNull String vecToString(double x, double y, double z) {
        var sx = StringUtil.plainDouble(x);
        var sy = StringUtil.plainDouble(y);
        var sz = StringUtil.plainDouble(z);

        return "(" + sx + ", " + sy + ", " + sz + ")";
    }
}
