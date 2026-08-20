package com.zelaux.betterdiagram.feature.include_subsublevel.math;

import dev.ryanhcode.sable.api.physics.constraint.ConstraintJointAxis;
import dev.ryanhcode.sable.api.physics.force.QueuedForceGroup;
import org.apache.commons.lang3.DoubleRange;
import org.joml.Quaterniond;
import org.joml.Vector3d;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import java.util.Map;
import java.util.Set;

public class ConstraintCalculatorTest extends Helpers {
    private final Set<ConstraintJointAxis> FIXED_EXPECT_X = Set.of(
        ConstraintJointAxis.LINEAR_Y, ConstraintJointAxis.LINEAR_Z,
        ConstraintJointAxis.ANGULAR_X, ConstraintJointAxis.ANGULAR_Y, ConstraintJointAxis.ANGULAR_Z
    );
    private final Set<ConstraintJointAxis> FIXED_EXCEPT_ROTARY_X = Set.of(
        ConstraintJointAxis.LINEAR_X, ConstraintJointAxis.LINEAR_Y, ConstraintJointAxis.LINEAR_Z,
        ConstraintJointAxis.ANGULAR_Y, ConstraintJointAxis.ANGULAR_Z
    );

    @Test
    void fixedAsGeneric() {
        QueuedForceGroup.PointForce force = project(
            pose(vec3(0, 1, 0), quatIdt(), half()),
            pose(vec3(1, 1, 0), quatIdt(), half()),
            vec3(1, 0, 0),
            zero(),
            quatIdt(),
            quatIdt(),
            Set.of(ConstraintJointAxis.ALL),
            Map.of(),
            pointForce(half(), vec3(0, 0, 1))
        );

        //displacement + COM = (1,0,0) + half
        assertEquals(pointForce(vec3(1, 0, 0).add(half()), vec3(0, 0, 1)), force, "Force transmission in a fixed joint");
    }

    @Test
    void sliderX_Free() {
        // Constraint: Line (Linear X - Free, others Locked)
        // Applying force ALONG the free X axis
        QueuedForceGroup.PointForce force = project(
            pose(zero(), quatIdt(), half()),
            pose(vec3(1, 0, 0), quatIdt(), half()),
            half(), half(), // Connected at centers of mass
            quatIdt(), quatIdt(),
            FIXED_EXPECT_X,
            Map.of(),
            pointForce(half(), vec3(1, 0, 0)) // Force along X
        );

        // Result: Force is not transmitted because the axis is free
        assertEquals(pointForce(half(), zero()), force, "Force on free axis should be zero");
    }

    @Test
    void sliderX_BlockedByLateral() {
        // Constraint: Same slider along X
        // Applying LATERAL force (along Y). It should be transmitted fully.
        QueuedForceGroup.PointForce force = project(
            pose(zero(), quatIdt(), half()),
            pose(vec3(1, 0, 0), quatIdt(), half()),
            half(), half(),
            quatIdt(), quatIdt(),
            FIXED_EXPECT_X,
            Map.of(),
            pointForce(half(), vec3(0, 1, 0)) // Force along Y
        );

        // Result: Force is transmitted, application point is CoM of A (as there is no lever arm)
        assertEquals(pointForce(half(), vec3(0, 1, 0)), force, "Lateral force should be transmitted");
    }

    @Test
    void linearRange_WallEffect() {
        // Constraint: Free X axis with limit [0, 10]
        // Body is at 0 (lower bound)
        Map<ConstraintJointAxis, DoubleRange> limits = Map.of(
            ConstraintJointAxis.LINEAR_X, DoubleRange.of(0.0, 10.0)
        );

        // 1. Push into the "wall" (X < 0)
        QueuedForceGroup.PointForce pushIn = project(
            pose(zero(), quatIdt(), half()),
            pose(zero(), quatIdt(), half()), // Body B is at zero
            half(), half(),
            quatIdt(), quatIdt(),
            Set.of(), // No hard locks
            limits,
            pointForce(half(), vec3(-1, 0, 0)) // Pushing into the wall
        );
        assertEquals(pointForce(half(), vec3(-1, 0, 0)), pushIn, "Should be blocked by MIN limit");

        // 2. Pull away from the "wall" (X > 0)
        QueuedForceGroup.PointForce pullOut = project(
            pose(zero(), quatIdt(), half()),
            pose(zero(), quatIdt(), half()),
            half(), half(),
            quatIdt(), quatIdt(),
            Set.of(),
            limits,
            pointForce(half(), vec3(1, 0, 0)) // Pulling from the wall
        );
        assertEquals(pointForce(half(), zero()), pullOut, "Should slide freely away from limit");
    }

    @Test
    void hingeX_FreeRotation() {
        // Constraint: Hinge (Rotary). All translations Locked, X-rotation - Free.
        // Applying force that creates torque around X.
        QueuedForceGroup.PointForce force = project(
            pose(zero(), quatIdt(), half()),
            pose(zero(), quatIdt(), half()),
            half(), half(),
            quatIdt(), quatIdt(),
            FIXED_EXCEPT_ROTARY_X,
            Map.of(),
            // Apply force along Y with Z-offset (lever arm for rotation around X)
            pointForce(vec3(0.5, 0.5, 1.0), vec3(0, 1, 0))
        );

        // Result: Translation (force 0,1,0) is transmitted, but torque must be zeroed out.
        // The actual point will be offset to create ZERO torque around X.
        assertEquals(pointForce(vec3(0.5, 0.5, 0.5), vec3(0, 1, 0)), force, "Hinge should not transmit X-torque");
    }

    @Test
    void fixed_WithLeverArm() {
        // Rigid connection (Weld). Bodies are separated.
        // A at (0,0,0), B at (1,0,0). Welded at Anchor A=(1,0,0) and Anchor B=(0,0,0)
        QueuedForceGroup.PointForce force = project(
            pose(zero(), quatIdt(), zero()), // CoM A at 0,0,0
            pose(vec3(1, 0, 0), quatIdt(), zero()), // CoM B at 1,0,0
            vec3(1, 0, 0), // Anchor A at 1,0,0
            zero(),        // Anchor B at 0,0,0
            quatIdt(), quatIdt(),
            Set.of(ConstraintJointAxis.ALL),
            Map.of(),
            pointForce(zero(), vec3(0, 1, 0)) // Upward force at center of B
        );

        // Expectation: Force (0,1,0) applied in world space at (1,0,0).
        // Relative to CoM A (0,0,0), this is point (1,0,0).
        assertEquals(pointForce(vec3(1, 0, 0), vec3(0, 1, 0)), force, "Weld should transmit force with world offset");
    }
}