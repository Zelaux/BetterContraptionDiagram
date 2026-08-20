package com.zelaux.betterdiagram.util;

import com.zelaux.betterdiagram.func.DoubleGetter;
import com.zelaux.betterdiagram.func.DoubleSetter;
import com.zelaux.betterdiagram.struct.TransformedAxes;
import dev.ryanhcode.sable.api.physics.force.QueuedForceGroup;
import dev.ryanhcode.sable.companion.math.BoundingBox3ic;
import dev.ryanhcode.sable.companion.math.Pose3dc;
import it.unimi.dsi.fastutil.Hash;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.*;

import java.io.Serializable;
import java.lang.Math;
import java.util.Comparator;
import java.util.Objects;

public class VecUtil {
    public static final Vector3dc X_V = new Vector3d(1, 0, 0);
    public static final Vector3dc Y_V = new Vector3d(0, 1, 0);
    public static final Vector3dc Z_V = new Vector3d(0, 0, 1);
    public static final Vector3dc NX_V = new Vector3d(-1, 0, 0);
    public static final Vector3dc NY_V = new Vector3d(0, -1, 0);
    public static final Vector3dc NZ_V = new Vector3d(0, 0, -1);
    public static final Vector3dc[] DIRECTIONS = {X_V, Y_V, Z_V};
    public static final Vector3dc ZERO_V = new Vector3d(0, 0, 0);
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
    public static final  Hash.Strategy<@Nullable Vector3dc> STRATEGY = new Hash.Strategy<>() {
            @Override
            public int hashCode(Vector3dc o) {
                if(o==null)return 0;
                return Objects.hash(o.x(), o.y(), o.z());
            }

            @Override
            public boolean equals(Vector3dc a, Vector3dc b) {
                if(a==b)return true;
                if(a==null || b==null)return false;
                return a.equals(b,0.00001f);
            }
        };
    public static final Vector3d NAN_3 = new Vector3d(Double.NaN, Double.NaN, Double.NaN);
    public static Vector3d displacementFromForceAndTorque(Vector3dc force,Vector3dc torque,Vector3d dest){
        double scalar = force.lengthSquared();
        if(scalar == 0) return dest.zero();
        return force.cross(torque,dest).div(scalar);
    }

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
        var coords0 = new Vector2d[3];
        int[] xyz = new int[3];
        xyz[2] = -1;
        Vector3d tmp = new Vector3d();
        for(int i = 0; i < coords.length; i++) {
            orientation.transformInverse(tmp.set(DIRECTIONS[i]));

            double scalar = maxAbsXY(tmp);
            if(scalar == 0 || org.joml.Runtime.equals(scalar, 0, 0.1f)) {
                scalar = 1;
                tmp.zero();
            }
            //screenCoords.div(scalar);
            coords0[i] = new Vector2d(tmp.x, tmp.y);
            var v = coords[i] = new Vector2d(tmp.x / scalar, tmp.y / scalar).round();
            if(v.x != 0) xyz[0] = i;
            else if(v.y != 0) xyz[1] = i;
            else xyz[2] = i;
        }
        boolean succeedOnlyOnSecondTry = xyz[2] == -1;
        // Enhanced Diagram moment
        if(xyz[2] == -1) {

            for(int i = 0; i < coords.length; i++) {
                Vector2d cur = coords0[i];
                if(Math.abs(coords0[xyz[0]].x) < Math.abs(cur.x) && Math.abs(cur.x) >= Math.abs(cur.y)) xyz[0] = i;
                if(Math.abs(coords0[xyz[1]].y) < Math.abs(cur.y) && Math.abs(cur.x) < Math.abs(cur.y) && xyz[0] != i)
                    xyz[1] = i;
            }
            for(int i = 0; i < 3; i++) {
                if(xyz[0] != i && xyz[1] != i) {
                    xyz[2] = i;
                    coords[i].zero();
                    break;
                }
            }
        }
        return new TransformedAxes(coords, xyz, succeedOnlyOnSecondTry);
    }

    public static double maxAbsXY(Vector3d tmp) {
        return Math.max(Math.abs(tmp.x), Math.abs(tmp.y));
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

    public static void applyTransforms(Vector3d tmp, Mirror mirror, Rotation rotation) {
        switch(mirror) {
            case NONE -> {}
            case LEFT_RIGHT -> tmp.z = -tmp.z;
            case FRONT_BACK -> tmp.x = -tmp.x;
        }
        switch(rotation) {
            case NONE -> {}
            case CLOCKWISE_90 -> tmp.rotateY(Math.toRadians(90));
            case CLOCKWISE_180 -> tmp.rotateY(Math.toRadians(180));
            case COUNTERCLOCKWISE_90 -> tmp.rotateY(Math.toRadians(270));
        }
    }

    public static QueuedForceGroup.PointForce projectForce(Pose3dc poseB, Pose3dc poseA, QueuedForceGroup.PointForce forceInA) {
        Vector3d point = new Vector3d(forceInA.point());
        Vector3d force = new Vector3d(forceInA.force());
        //force to world
        projectForce(poseB, poseA, point, force);

        return new QueuedForceGroup.PointForce(point, force);
    }

    @Contract(mutates = "param3,param4")
    public static void projectForce(Pose3dc poseB, Pose3dc poseA, Vector3d pointInA, Vector3d forceInA) {
        Quaterniondc orientationA = poseA.orientation();
        Vector3d worldPoint = pointInA
            .sub(poseA.rotationPoint())
            .mul(poseA.scale())
            .rotate(orientationA)
            .add(poseA.position());

        Vector3d worldForce = forceInA
            .rotate(orientationA);


        //world to poseB
        Quaterniond invOrientB = new Quaterniond(poseB.orientation()).conjugate();

        /*Vector3d localPointB =*/
        worldPoint
            .sub(poseB.position())
            .rotate(invOrientB)
            .div((Vector3d) poseB.scale())
            .add(poseB.rotationPoint());

        /*Vector3d localForceB =*/
        worldForce
            .rotate(invOrientB);
    }

    @Contract("_,_,_->new")
    public static Vector3d projectPos(Pose3dc poseB, Pose3dc poseA, Vector3dc posInA) {
        if(poseB==poseA)return new Vector3d(posInA);
        //force to world
        Vector3d worldPoint = projectToWorld(poseA, new Vector3d(posInA));


        //world to poseB


        return unprojectPosToLocal(poseB, worldPoint);
    }
    @Contract("_,_->new")
    public static QueuedForceGroup.PointForce unprojectForceToLocal(Pose3dc pose, QueuedForceGroup.PointForce force) {
        return new QueuedForceGroup.PointForce(
            pose.transformPositionInverse(force.point(),new Vector3d()),
            pose.transformNormalInverse(force.force(),new Vector3d())
        );
    }
    @Contract("_,_->param2")
    public static Vector3d unprojectPosToLocal(Pose3dc pose, Vector3d worldPoint) {
        Quaterniond invOrientB = new Quaterniond(pose.orientation()).conjugate();

        return worldPoint
            .sub(pose.position())
            .rotate(invOrientB)
            .div((Vector3d) pose.scale())
            .add(pose.rotationPoint());
    }
    @Contract("_,_->param2")
    public static Vector3d projectToWorld(Pose3dc poseA, Vector3d posInA) {
        Quaterniondc orientationA = poseA.orientation();
        return posInA
            .sub(poseA.rotationPoint())
            .mul(poseA.scale())
            .rotate(orientationA)
            .add(poseA.position());
    }
    @Contract("_,_,_->new")
    public static Vector3d projectDirection(Pose3dc poseB, Pose3dc poseA, Vector3dc forceInA) {
        if(poseB==poseA)return new Vector3d(forceInA);
        //force to world
        Vector3d worldForce = projectForceToWorld(poseA, new Vector3d(forceInA));


        //world to poseB

        return unprojectDirectionToLocal(poseB, worldForce);
    }

    public static Vector3d unprojectDirectionToLocal(Pose3dc poseB, Vector3d worldForce) {
        Quaterniond invOrientB = new Quaterniond(poseB.orientation()).conjugate();
        return worldForce
            .rotate(invOrientB);
    }

    @Contract(value = "_,_->param2",mutates = "param2")
    public static Vector3d projectForceToWorld(Pose3dc poseA, Vector3d forceInA) {
        Quaterniondc orientationA = poseA.orientation();

        return forceInA
            .rotate(orientationA);
    }

    public static Vector3d center(BoundingBox3ic box, Vector3d dest) {
        return dest.set((box.minX() + box.maxX()) / 2.0, (box.minY() + box.maxY()) / 2.0, (box.minZ() + box.maxZ()) / 2.0);
    }
    public static Vector3d centerRound(BoundingBox3ic box, Vector3d dest) {
        return dest.set((box.minX() + box.maxX()) / 2, (box.minY() + box.maxY()) / 2, (box.minZ() + box.maxZ()) / 2);
    }
}
