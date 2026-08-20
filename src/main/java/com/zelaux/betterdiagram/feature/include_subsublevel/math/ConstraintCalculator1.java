package com.zelaux.betterdiagram.feature.include_subsublevel.math;

import com.zelaux.betterdiagram.func.DoubleSetter;
import com.zelaux.betterdiagram.util.VecUtil;
import dev.ryanhcode.sable.api.physics.constraint.ConstraintJointAxis;
import dev.ryanhcode.sable.api.physics.force.QueuedForceGroup;
import dev.ryanhcode.sable.companion.math.Pose3dc;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.DoubleRange;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaterniond;
import org.joml.Quaterniondc;
import org.joml.Vector3d;
import org.joml.Vector3dc;

import java.util.Map;
import java.util.Set;

public class ConstraintCalculator1 {
    protected final Vector3d forceDirection = new Vector3d();
    protected final Vector3d mask = new Vector3d();
    protected final Vector3d forceTorque = new Vector3d();
    protected final Vector3d forceTorque_A = new Vector3d();
    protected final Quaterniond fullO1 = new Quaterniond();
    protected final Quaterniond fullO1_q = new Quaterniond();

    public Vector3dc worldForce() {
        return forceDirection;
    }

    public Vector3dc worldForceTorque() {
        return forceTorque;
    }


    private static DoubleSetter<Vector3dc>[] translationSetters = new DoubleSetter[]{
        VecUtil.SETTERS_3d[0],
        VecUtil.SETTERS_3d[1],
        VecUtil.SETTERS_3d[2],
        null,
        null,
        null,
    };
    private static DoubleSetter<Vector3dc>[] rotationSetters = new DoubleSetter[]{
        null,
        null,
        null,
        VecUtil.SETTERS_3d[0],
        VecUtil.SETTERS_3d[1],
        VecUtil.SETTERS_3d[2],
    };
    private static final Vector3dc[] MASK = new Vector3dc[0b111+1];

    static {
        for(int i = 0; i < MASK.length; i++) {
            MASK[i] = new Vector3d(
                i & 1, (i >> 1) & 1, (i >> 2) & 1
            );
        }
    }

    public void generic0(
        Pose3dc a,
        Pose3dc b,
        Vector3dc pos1,
        Vector3dc pos2,
        Quaterniondc orientation1,
        Quaterniondc orientation2,
        QueuedForceGroup.PointForce forceIn2,
        Set<ConstraintJointAxis> lockedAxes,
        @Nullable
        Map<ConstraintJointAxis, DoubleRange> ranges
    ) {
        fullO1.set(a.orientation()).mul(orientation1).invert(fullO1_q);

        b.transformNormal(forceIn2.force(), forceDirection);

        filter(lockedAxes, forceDirection, fullO1_q, fullO1, translationSetters);

        forceTorque.set(forceIn2.point())
                   .sub(pos2)
                   .rotate(b.orientation())
                   .cross(forceDirection)
        ;
        filter(lockedAxes, forceTorque, fullO1_q, fullO1, rotationSetters);

        forceTorque.add(
            forceTorque_A.set(pos1)
                         .rotate(a.orientation())
                         .cross(forceDirection)
        );

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
        fullO1.set(a.orientation()).mul(orientation1);
        fullO1.invert(fullO1_q);

        Vector3d worldPos1 = a.transformPosition(pos1, new Vector3d());
        Vector3d worldPos2 = b.transformPosition(pos2, new Vector3d());
        Vector3d currentPosLocal = worldPos2.sub(worldPos1).rotate(fullO1_q);

        Quaterniond currentRotLocal = new Quaterniond(fullO1_q).mul(b.orientation()).mul(orientation2);
        Vector3d currentAnglesLocal = new Vector3d();
        currentRotLocal.getEulerAnglesXYZ(currentAnglesLocal);

        b.transformNormal(forceFrom2.force(), forceDirection);

        int lockedAxes = bakeLocked(lockedAxesSet);
        filter(lockedAxes, ranges, forceDirection, currentPosLocal, ConstraintJointAxis.LINEAR);

        forceTorque.set(forceFrom2.point())
                   .sub(pos2)
                   .rotate(b.orientation())
                   .cross(forceDirection);

        filter(lockedAxes>>3, ranges, forceTorque, currentAnglesLocal, ConstraintJointAxis.ANGULAR);

        forceTorque.add(
            forceTorque_A.set(pos1)
                         .rotate(a.orientation())
                         .cross(forceDirection)
        );
    }

    private void filter(
        int lockedAxes,
        @Nullable Map<ConstraintJointAxis, DoubleRange> ranges,
        Vector3d vec,          // Сила или Момент (в мировых)
        Vector3d currentState, // currentPos или currentAngles (в локальных констрейна)
        ConstraintJointAxis[] axes
    ) {
        vec.rotate(fullO1_q);
        vec.mul(getMask(lockedAxes, ranges, vec, currentState, axes));
        vec.rotate(fullO1);
    }

    private int bakeLocked(Set<ConstraintJointAxis> lockedAxes) {
        int index=0;
        for(ConstraintJointAxis axis : lockedAxes) {
            index|=1<<axis.ordinal();
        }
        return index;
    }

    private Vector3dc getMask(int lockedAxes, @Nullable Map<ConstraintJointAxis, DoubleRange> ranges, Vector3d vector, Vector3d compValues, ConstraintJointAxis[] axes) {
        mask.set(MASK[lockedAxes&0b111]);
        if(ranges==null)return mask;
        lockedAxes=(lockedAxes<<3)|(lockedAxes&0b111);

        for(ConstraintJointAxis axis : axes) {
            int idx = axis.ordinal();
            if(((lockedAxes>>idx)&1)!=0)continue;
            boolean active = false;
            DoubleRange range = ranges.get(axis);
            if(range == null) continue;
            double currentVal = compValues.get(idx % 3);
            double forceVal = vector.get(idx % 3);

            if(currentVal >= range.getMaximum() && forceVal > 0) active = true;
            else if(currentVal <= range.getMinimum() && forceVal < 0) active = true;
            if(active) mask.setComponent(idx,1);

        }

        Vector3d mask1 = mask;
        return mask1;
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
