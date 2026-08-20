package com.zelaux.betterdiagram.feature.include_subsublevel.math;

import com.zelaux.betterdiagram.util.VecUtil;
import dev.ryanhcode.sable.api.physics.constraint.ConstraintJointAxis;
import dev.ryanhcode.sable.api.physics.force.QueuedForceGroup;
import dev.ryanhcode.sable.companion.math.Pose3d;
import org.apache.commons.lang3.DoubleRange;
import org.joml.Quaterniond;
import org.joml.Vector3d;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

public class RotationTest extends Helpers {
    static final double rad90 = Math.PI / 2;
    static final double rad45 = Math.PI / 4;

    private final Set<ConstraintJointAxis> FIXED_EXCEPT_LINEAR_X = Set.of(
        ConstraintJointAxis.LINEAR_Y, ConstraintJointAxis.LINEAR_Z,
        ConstraintJointAxis.ANGULAR_X, ConstraintJointAxis.ANGULAR_Y, ConstraintJointAxis.ANGULAR_Z
    );
    private final Set<ConstraintJointAxis> FIXED_EXCEPT_ANGULAR_X = Set.of(
        ConstraintJointAxis.LINEAR_X, ConstraintJointAxis.LINEAR_Y, ConstraintJointAxis.LINEAR_Z,
        ConstraintJointAxis.ANGULAR_Y, ConstraintJointAxis.ANGULAR_Z
    );

    @Test
    void fixedJoint_BodyBRotated() {
        // Body A is at (0,0,0) identity
        // Body B is at (1,0,0) rotated 90 degrees around Z axis
        // In Body B's local space, "Forward" is X.
        // Because of 90deg Z rotation, B's local X is World's Y.

        Quaterniond rotB = new Quaterniond().rotateZ(rad90);

        QueuedForceGroup.PointForce force = project(
            pose(zero(), quatIdt(), zero()), // A
            pose(vec3(1, 0, 0), rotB, zero()), // B
            vec3(1, 0, 0), // Anchor A (at B's position)
            zero(),        // Anchor B (at CoM B)
            quatIdt(), quatIdt(),
            Set.of(ConstraintJointAxis.ALL),
            Map.of(),
            // Apply force along local X of body B.
            // Since B is rotated 90deg Z, local X becomes world Y.
            pointForce(zero(), vec3(1, 0, 0))
        );

        // Result in A:
        // Force should be (0, 1, 0) because of rotation.
        // Point should be (1, 0, 0) relative to A.
        assertEquals(pointForce(vec3(1, 0, 0), vec3(0, 1, 0)), force,
            "Force along B's local X should become Y in A's space if B is rotated 90deg Z");
    }

    @Test
    void sliderX_withRotatedJointFrame() {
        // Body A and B are at the same spot.
        // Joint is configured to allow movement along X, BUT the joint frame itself is rotated 90deg Z.
        // This means the "Slider" axis is now world Y.

        Quaterniond jointRotation = new Quaterniond().rotateZ(rad90);

        // We apply a force along world Y (which is joint's local X).
        // Since Linear X is FREE, this force should NOT be transmitted.
        QueuedForceGroup.PointForce forceAlongFreeAxis = project(
            pose(zero(), quatIdt(), zero()),
            pose(zero(), quatIdt(), zero()),
            zero(), zero(),
            jointRotation, jointRotation, // Joint frame rotated
            FIXED_EXCEPT_LINEAR_X,
            Map.of(),
            pointForce(zero(), vec3(0, 1, 0)) // Force along world Y
        );

        assertEquals(pointForce(zero(), zero()), forceAlongFreeAxis,
            "Force along the rotated free slider axis should not be transmitted");

        // Now apply force along world X (which is joint's local -Y).
        // Since Linear Y is LOCKED, it should be transmitted fully.
        QueuedForceGroup.PointForce forceAlongLockedAxis = project(
            pose(zero(), quatIdt(), zero()),
            pose(zero(), quatIdt(), zero()),
            zero(), zero(),
            jointRotation, jointRotation,
            FIXED_EXCEPT_LINEAR_X,
            Map.of(),
            pointForce(zero(), vec3(1, 0, 0)) // Force along world X
        );

        assertEquals(pointForce(zero(), vec3(1, 0, 0)), forceAlongLockedAxis,
            "Lateral force (relative to rotated joint frame) should be transmitted");
    }

    @Test
    void hingeX_withLeverArmAndRotation() {
        // Body A at (0,0,0).
        // Body B at (1,0,0).
        // Hinge axis is joint's X. Joint frame is identity.
        // Apply force to B at its CoM (1,0,0) in world, directed along Z.
        // This creates torque around Y (1,0,0) x (0,0,1) = (0, -1, 0).
        // Since only X-rotation is free, the Y-torque should be TRANSMITTED.

        Set<ConstraintJointAxis> hingeX = FIXED_EXCEPT_ANGULAR_X;

        QueuedForceGroup.PointForce force = project(
            pose(zero(), quatIdt(), zero()),
            pose(vec3(1, 0, 0), quatIdt(), zero()),
            vec3(1, 0, 0), zero(),
            quatIdt(), quatIdt(),
            hingeX,
            Map.of(),
            pointForce(zero(), vec3(0, 0, 1)) // Force in B (Z-up)
        );

        // In A's space:
        // Force is (0,0,1) applied at (1,0,0).
        // This is transmitted because translation is locked and Y-torque is locked.
        assertEquals(pointForce(vec3(1, 0, 0), vec3(0, 0, 1)), force,
            "Hinge should transmit forces and torques that are not on its free axis");
    }

    @Test
    void fixedJoint_Simple90DegreeRotation() {
        // Body A at (0,0,0), identity rotation
        // Body B at (1,0,0), rotated 90 deg around Z
        // Force applied at Body B CoM, directed along B's local X-axis

        Quaterniond rotB = new Quaterniond().rotateZ(rad90);
        Vector3d posA = zero();
        Vector3d posB = vec3(1, 0, 0);

        // Point force at Body B's CoM (zero local)
        QueuedForceGroup.PointForce inputForce = pointForce(zero(), vec3(1, 0, 0));

        QueuedForceGroup.PointForce result = project(
            pose(posA, quatIdt(), zero()),
            pose(posB, rotB, zero()),
            vec3(1, 0, 0), zero(), // Anchor at posB
            quatIdt(), quatIdt(),
            Set.of(ConstraintJointAxis.ALL),
            Map.of(),
            inputForce
        );

        // Expected:
        // Force: Local (1,0,0) rotated by 90deg Z becomes (0,1,0)
        // Point: B's CoM is at (1,0,0) in World. In A's local (identity) it's (1,0,0)
        assertEquals(pointForce(vec3(1, 0, 0), vec3(0, 1, 0)), result,
            "90 degree rotation should swap X and Y axes for the force");
    }

    @Test
    void fixedJoint_ComplexRotation() {
        // To avoid confusion with rotateXYZ order, we define rotations explicitly
        Quaterniond rotA = new Quaterniond().rotateY(rad45).rotateX(rad45);
        Quaterniond rotB = new Quaterniond().rotateZ(rad90).rotateX(rad45);

        Vector3d posA = vec3(10, 5, -2);
        Vector3d posB = vec3(12, 5, -2); // Distance = 2.0 along world X

        // Anchors at the midpoint (11, 5, -2)
        Vector3d worldAnchor = vec3(11, 5, -2);
        Vector3d anchorA = worldToLocal(posA, rotA, zero(), worldAnchor);
        Vector3d anchorB = worldToLocal(posB, rotB, zero(), worldAnchor);

        Vector3d localForceB = vec3(0, 0, 1); // Up in B's local space

        Pose3d poseA = pose(posA, rotA, zero());
        Pose3d poseB = pose(posB, rotB, zero());
        QueuedForceGroup.PointForce result = project(
            poseA,
            poseB,
            anchorA, anchorB,
            quatIdt(), quatIdt(),
            Set.of(ConstraintJointAxis.ALL),
            Map.of(),
            pointForce(zero(), localForceB)
        );

        // --- Expected Calculation ---
        // 1. Force from B-local to World
        Vector3d worldForce = new Vector3d(localForceB).rotate(rotB);
        // 2. Force from World to A-local
        Vector3d expectedForce = new Vector3d(worldForce).rotate(new Quaterniond(rotA).conjugate());

        // 3. Point: B's CoM in world is posB (since rotationPoint is zero)
        // Point in A's local: worldToLocal(posA, rotA, CoM_A, worldPosB)
        Vector3d expectedPoint = poseA.transformPositionInverse(posB, new Vector3d());

        assertEqualsWithTorque(pointForce(expectedPoint, expectedForce), result, zero(),
            "Force and point should be correctly transformed in complex orientation");
    }

    @Test
    void hinge_RotatedJointFrame() {
        // Body A and B at same position
        // Joint is a hinge on X-axis, but the joint frame is rotated 90 deg around Y.
        // This means the hinge axis is now World Z.
        Quaterniond jointFrame = new Quaterniond().rotateY(rad90);

        // Hinge: All linear locked, Angular Y and Z locked. Angular X is FREE.
        Set<ConstraintJointAxis> hingeX = FIXED_EXCEPT_ANGULAR_X;

        // Apply torque (via force at offset) around World Z
        // In the joint frame, World Z is local X.
        // Since Angular X is FREE, this torque should be filtered out.
        QueuedForceGroup.PointForce forceOnFreeAxis = project(
            pose(zero(), quatIdt(), zero()),
            pose(zero(), quatIdt(), zero()),
            zero(), zero(),
            jointFrame, jointFrame,
            hingeX,
            Map.of(),
            // Force at (1,0,0) directed (0,1,0) -> World torque (0,0,1)
            pointForce(vec3(1, 0, 0), vec3(0, 1, 0))
        );

        // The point should be adjusted to CoM to eliminate the torque on the free axis
        assertEquals(pointForce(zero(), vec3(0, 1, 0)), forceOnFreeAxis,
            "Torque around the free hinge axis (World Z) should be removed");
    }

    @Test
    void hinge_JointFrame() {
        // Body A and B at same position
        // Joint is a hinge on X-axis, but the joint frame is rotated 90 deg around Y.
        // This means the hinge axis is now World Z.


        // Apply torque (via force at offset) around World Z
        // In the joint frame, World Z is local X.
        // Since Angular X is FREE, this torque should be filtered out.
        Pose3d poseA = pose(zero(), quatIdt(), zero());
        Pose3d poseB = pose(vec3(0,0,0.5).rotateX(-rad45), quatIdt().rotateX(-rad45), zero());
        QueuedForceGroup.PointForce pointForceB = pointForce(vec3(0, 0, 1), vec3(0, 1, 0));
        QueuedForceGroup.PointForce forceOnFreeAxis = project(
            poseA,
            poseB,
            poseA.rotationPoint(), poseB.transformPositionInverse(poseA.position(), new Vector3d()),
            quatIdt(), quatIdt(),
            FIXED_EXCEPT_LINEAR_X,
            Map.of(
                ConstraintJointAxis.ANGULAR_X, DoubleRange.of(-rad45,rad45)
            ),
            pointForceB
        );
        assertEquals(VecUtil.projectForce(poseA,poseB,pointForceB), forceOnFreeAxis,
            "Torque around the free hinge axis (World Z) should be removed");
    }

    private Vector3d worldToLocal(Vector3d pos, Quaterniond rot, Vector3d com, Vector3d worldPoint) {
        return new Vector3d(worldPoint)
            .sub(pos)
            .rotate(new Quaterniond(rot).conjugate())
            .add(com);
    }
}