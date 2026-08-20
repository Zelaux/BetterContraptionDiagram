package com.zelaux.betterdiagram.feature.include_subsublevel.math;

import com.zelaux.betterdiagram.func.DoubleSetter;
import com.zelaux.betterdiagram.util.VecUtil;
import dev.ryanhcode.sable.api.physics.constraint.ConstraintJointAxis;
import dev.ryanhcode.sable.api.physics.force.QueuedForceGroup;
import dev.ryanhcode.sable.companion.math.Pose3d;
import dev.ryanhcode.sable.companion.math.Pose3dc;
import org.apache.commons.lang3.DoubleRange;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaterniond;
import org.joml.Quaterniondc;
import org.joml.Vector3d;
import org.joml.Vector3dc;

import java.util.Map;
import java.util.Set;

public class ConstraintCalculator {
    public static final ConstraintCalculator INSTANCE = new ConstraintCalculator();
    protected final Vector3d forceDirection = new Vector3d();
    protected final Vector3d mask = new Vector3d();
    protected final Vector3d forceTorque = new Vector3d();
    protected final Vector3d forceTorque_A = new Vector3d();
    protected final Quaterniond full_1_to_world = new Quaterniond();
    protected final Quaterniond full_world_to_1 = new Quaterniond();
    //protected final Vector3d tmpV = new Quaterniond();
    protected final Quaterniond tmpQ = new Quaterniond();
    private final Vector3d worldPos1 = new Vector3d();
    private final Vector3d worldPos2 = new Vector3d();
    private final Vector3d currentAnglesLocal = new Vector3d();
    private Pose3d calculatedPoseB =new Pose3d(new Vector3d(),new Quaterniond(),new Vector3d(),new Vector3d(1,1,1));

    public Vector3dc worldForce() {
        return forceDirection;
    }

    public Vector3dc worldForceTorque() {
        return forceTorque;
    }

    public void getForceAndPoint(Pose3dc pose, Vector3d forceDest, Vector3d pointDest) {
        pose.transformNormalInverse(forceDirection, forceDest);
        VecUtil.displacementFromForceAndTorque(forceDirection, forceTorque, pointDest);
        pose.transformPositionInverse(pointDest.add(pose.position()));
    }


    private static final Vector3dc[] MASK = new Vector3dc[0b111 + 1];

    static {
        for(int i = 0; i < MASK.length; i++) {
            MASK[i] = new Vector3d(
                i & 1, (i >> 1) & 1, (i >> 2) & 1
            );
        }
    }
    public void generic(
        Pose3dc a,
        Vector3dc rotationPointB,
        Vector3dc pos1,
        Vector3dc pos2,
        Quaterniondc orientation1,
        Quaterniondc orientation2,
        QueuedForceGroup.PointForce worldForceFor2,
        Set<ConstraintJointAxis> lockedAxesSet,
        @Nullable Map<ConstraintJointAxis, DoubleRange> ranges
    ) {
        Pose3d poseB = calculatePoseB(a, pos1, pos2, orientation1, orientation2, rotationPointB);

        generic(a, poseB, pos1, pos2, orientation1, orientation2,
            VecUtil.unprojectForceToLocal(poseB,worldForceFor2),
            lockedAxesSet, ranges);
    }

    private Pose3d calculatePoseB(Pose3dc a, Vector3dc pos1, Vector3dc pos2, Quaterniondc orientation1, Quaterniondc orientation2, Vector3dc rotationPointB) {
        Quaterniond qB = a.orientation()
                          .mul(orientation1, calculatedPoseB.orientation())
                          .mul(tmpQ.set(orientation2).invert());

        Vector3d worldAnchor = a.orientation().transform(
            pos1.sub(a.rotationPoint(),worldPos1)
        ).add(a.position());

        // pB = Anchor - Q_b * (pos2 - rotationPointB)
        qB.transform(
            pos2.sub(rotationPointB,calculatedPoseB.position())
        ).negate().add(worldAnchor);

        calculatedPoseB.rotationPoint().set(rotationPointB);
        return calculatedPoseB;
    }

    public void generic(
        Pose3dc a,
        Pose3dc b,
        Vector3dc pos1,
        Vector3dc pos2,
        Quaterniondc orientation1,
        Quaterniondc orientation2,
        QueuedForceGroup.PointForce forceFrom2,
        Set<ConstraintJointAxis> lockedAxesSet,
        @Nullable Map<ConstraintJointAxis, DoubleRange> ranges
    ) {
        full_1_to_world.set(a.orientation()).mul(orientation1).invert(full_world_to_1);

        Vector3d worldPos1 = a.transformPosition(pos1, this.worldPos1);
        Vector3d worldPos2 = b.transformPosition(pos2, this.worldPos2);
        Vector3d currentPosLocal = worldPos2.sub(worldPos1).rotate(full_world_to_1);


        b.transformNormal(forceFrom2.force(), forceDirection);

        int lockedAxes = bakeLocked(lockedAxesSet);
        filter(lockedAxes, ranges, forceDirection, currentPosLocal, ConstraintJointAxis.LINEAR);

        forceTorque.set(forceFrom2.point())
                   .sub(pos2)
                   .rotate(b.orientation())
                   .cross(forceDirection);

        Vector3d currentAnglesLocal = getAngles(
            tmpQ.set(full_world_to_1)
                .mul(b.orientation())
                .mul(orientation2)
        );
        filter(lockedAxes >> 3, ranges, forceTorque, currentAnglesLocal, ConstraintJointAxis.ANGULAR);

        forceTorque.add(
            forceTorque_A.set(pos1)
                         .sub(a.rotationPoint())
                         .rotate(a.orientation())
                         .cross(forceDirection)
        );
    }

    private @NotNull Vector3d getAngles(Quaterniond currentRotLocal) {
        currentRotLocal.getEulerAnglesXYZ(currentAnglesLocal);
        return currentAnglesLocal;
    }

    private void filter(
        int lockedAxes,
        @Nullable Map<ConstraintJointAxis, DoubleRange> ranges,
        Vector3d worldVector,
        Vector3dc currentState,
        ConstraintJointAxis[] axes
    ) {
        worldVector.rotate(full_world_to_1);
        worldVector.mul(getMask(lockedAxes, ranges, worldVector, currentState, axes));
        worldVector.rotate(full_1_to_world);
    }

    private int bakeLocked(Set<ConstraintJointAxis> lockedAxes) {
        int index = 0;
        for(ConstraintJointAxis axis : lockedAxes) {
            index |= 1 << axis.ordinal();
        }
        return index;
    }

    private Vector3dc getMask(
        int lockedAxes,
        @Nullable Map<ConstraintJointAxis, DoubleRange> ranges,
        Vector3dc vector,
        Vector3dc compValues,
        ConstraintJointAxis[] axes
    ) {
        mask.set(MASK[lockedAxes & 0b111]);
        if(ranges == null) return mask;

        for(ConstraintJointAxis axis : axes) {
            int idx = axis.ordinal() % 3;
            if(((lockedAxes >> idx) & 1) != 0) continue;
            boolean active = false;
            DoubleRange range = ranges.get(axis);
            if(range == null) continue;
            double currentVal = compValues.get(idx);
            double forceVal = vector.get(idx);

            if(currentVal >= range.getMaximum()-0.001 && forceVal > -0.001) active = true;
            else if(currentVal <= range.getMinimum()+0.001 && forceVal < 0.001) active = true;
            if(active) mask.setComponent(idx, 1);

        }

        return mask;
    }

    private void filter(Set<ConstraintJointAxis> lockedAxes, Vector3d forceDirection, Quaterniond inverseRotation, Quaterniond directRotation, DoubleSetter<Vector3dc>[] setters) {
        forceDirection.rotate(inverseRotation);
        forceDirection.mul(getMask(lockedAxes, setters));
        forceDirection.rotate(directRotation);
    }

    private Vector3dc getMask(Set<ConstraintJointAxis> lockedAxes, DoubleSetter<Vector3dc>[] setters) {
        mask.zero();
        for(ConstraintJointAxis lockedAx : lockedAxes) {
            setters[lockedAx.ordinal()].set(mask, 1);
        }
        return mask;
    }
}
