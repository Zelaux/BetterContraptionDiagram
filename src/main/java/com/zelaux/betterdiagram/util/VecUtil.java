package com.zelaux.betterdiagram.util;

import com.zelaux.betterdiagram.func.DoubleGetter;
import com.zelaux.betterdiagram.func.DoubleSetter;
import com.zelaux.betterdiagram.struct.TransformedAxes;
import dev.ryanhcode.sable.companion.math.BoundingBox3ic;
import net.createmod.catnip.theme.Color;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.NotNull;
import org.joml.*;
import org.spongepowered.asm.mixin.*;

import java.io.Serializable;
import java.lang.Math;
import java.util.Comparator;

public class VecUtil {
    public static final Vector3d X_V = new Vector3d(1, 0, 0);
    public static final Vector3d Y_V = new Vector3d(0, 1, 0);
    public static final Vector3d Z_V = new Vector3d(0, 0, 1);
    public static final Vector3d[] DIRECTIONS = {X_V, Y_V, Z_V};
    public static final Vector3d ZERO_V = new Vector3d(0, 0, 0);
    public static final DoubleGetter<Vector3dc>[] GETTERS_3d = genArr(Vector3dc::x, Vector3dc::y, Vector3dc::z);
    public static final DoubleSetter<Vector3d>[] SETTERS_3d = genArr((t, v) -> t.x = v, (t, v) -> t.y = v, (t, v) -> t.z = v);

    public static final DoubleGetter<Vector2d>[] GETTERS_2d = genArr(Vector2d::x, Vector2d::y);
    public static final DoubleSetter<Vector2d>[] SETTERS_2d = genArr((t, v) -> t.x = v, (t, v) -> t.y = v);
    public static final Comparator<? super Vector3dc> CMP_AS_ARR = (o1, o2) -> {
        int i;
        i = Double.compare(o1.x(), o2.x());
        if(i != 0) return i;
        i = Double.compare(o1.y(), o2.y());
        if(i != 0) return i;
        i = Double.compare(o1.z(), o2.z());
        return i;
    };
    public static final Comparator<? super Vector3dc> CMP_LEN = Comparator.comparingDouble(Vector3dc::lengthSquared);
    public static final Comparator<? super Vector3dc> CMP_LEN_05 = (Comparator<Vector3dc> & Serializable)
        (c1, c2) -> Double.compare(c1.distanceSquared(0.5, 0.5, 0.5), c2.distanceSquared(0.5, 0.5, 0.5));

    @SafeVarargs
    private static <T> T[] genArr(T... arr) {
        return arr;
    }

    public static double maxAbsComponent(Vector2d v) {return Math.max(Math.abs(v.x), Math.abs(v.y));}

    public static @NotNull Vector3d minVec3d(BoundingBox3ic box) {
        return new Vector3d(box.minX(), box.minY(), box.minZ());
    }

    public static @NotNull Vector3d minVec3d(BoundingBox3ic box, Vector3d vec) {
        return vec.set(box.minX(), box.minY(), box.minZ());
    }

    public static @NotNull Vector3d subMinVec3d(Vector3d vec, BoundingBox3ic box) {
        return vec.sub(box.minX(), box.minY(), box.minZ());
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

            double scalar = maxAbsXY(tmp);
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

    public static double maxAbsXY(Vector3d tmp) {
        return Math.max(Math.abs(tmp.x), Math.abs(tmp.y));
    }


    public static @NotNull MutableComponent vectorToFormatted(Vector3dc force) {
        return Component.literal(vecToString(force));
    }

    public static @NotNull MutableComponent vectorToFormatted(Vector3dc force, Color numberColor) {
        return vectorToFormatted(force,numberColor,numberColor,numberColor);
    }
    public static @NotNull MutableComponent vectorToFormatted(Vector3dc force, Color xColor, Color yColor, Color zColor) {
        double x = force.x();
        double y = force.y();
        double z = force.z();
        var sx = StringUtil.plainDouble(x);
        var sy = StringUtil.plainDouble(y);
        var sz = StringUtil.plainDouble(z);


        return Component
            .literal("(")
            .append(Component.literal(sx).withColor(xColor.getRGB()))
            .append(", ")
            .append(Component.literal(sy).withColor(yColor.getRGB()))
            .append(", ")
            .append(Component.literal(sz).withColor(zColor.getRGB()))
            .append(")")
            ;
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
    private static @NotNull String vecToString(Vector3dc force) {
        return vecToString(force.x(), force.y(), force.z());
    }

    @Unique
    private static @NotNull String vecToString(double x, double y, double z) {
        var sx = StringUtil.plainDouble(x);
        var sy = StringUtil.plainDouble(y);
        var sz = StringUtil.plainDouble(z);

        return "(" + sx + ", " + sy + ", " + sz + ")";
    }
}
