package com.zelaux.betterdiagram.util;

import dev.ryanhcode.sable.companion.math.BoundingBox3ic;
import dev.simulated_team.simulated.content.entities.diagram.screen.DiagramScreen;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector2d;
import org.joml.Vector3d;

import java.util.function.ObjDoubleConsumer;
import java.util.function.ToDoubleFunction;

public class VecUtil {
    public static final Vector3d X_V = new Vector3d(1, 0, 0);
    public static final Vector3d Y_V = new Vector3d(0, 1, 0);
    public static final Vector3d Z_V = new Vector3d(0, 0, 1);
    public static final Vector3d[] DIRECTIONS = {X_V, Y_V, Z_V};
    public static final Vector3d ZERO_V = new Vector3d(0, 0, 0);
    public static final ToDoubleFunction<Vector3d>[] GETTERS_3d = genArr(Vector3d::x, Vector3d::y, Vector3d::z);
    public static final ObjDoubleConsumer<Vector3d>[] SETTERS_3d = genArr((t, v) -> t.x = v,(t, v) -> t.y = v,(t, v) -> t.z = v);
    public static final ToDoubleFunction<Vector2d>[] GETTERS_2d = genArr(Vector2d::x, Vector2d::y);
    public static final ObjDoubleConsumer<Vector2d>[] SETTERS_2d = genArr((t, v) -> t.x = v,(t, v) -> t.y = v);

    @SafeVarargs
    private static <T> T[] genArr(T... arr) {
        return arr;
    }

    public static double maxAbsComponent(Vector2d v){return Math.max(Math.abs(v.x),Math.abs(v.y));}

    public static @NotNull Vector3d minVec3d(BoundingBox3ic box) {
        return new Vector3d(box.minX(), box.minY(), box.minZ());
    }


    public static @NotNull Vector3d maxVec3d(BoundingBox3ic box) {
        return new Vector3d(box.maxX(), box.maxY(), box.maxZ());
    }

    public static Vector2d @NotNull [] projectAxises(Quaternionf orientation, Matrix4f projMatrix, int areaWidth, int areaHeight) {
        var coords = new Vector2d[3];
        Vector3d tmp = new Vector3d();
        for(int i = 0; i < coords.length; i++) {
            /*Vector2d screenCoords = DiagramScreen
                .getScreenCoords(
                    tmp.set(DIRECTIONS[i]), orientation, ZERO_V, projMatrix, areaWidth, areaHeight
                ).sub(0.5 * areaWidth, 0.5 * areaHeight);*/
            //double scalar = maxAbsComponent(screenCoords);
            orientation.transformInverse(tmp.set(DIRECTIONS[i]));
            double scalar = Math.max(Math.abs(tmp.x),Math.abs(tmp.y));
            if(scalar == 0 || org.joml.Runtime.equals(scalar,0,0.1f)) {scalar = 1;
                tmp.zero();
            }
            //screenCoords.div(scalar);
            coords[i] = new Vector2d(tmp.x/scalar,tmp.y/scalar).round();
        }
        return coords;
    }
}
