package com.zelaux.betterdiagram.util.mock;

import dev.ryanhcode.sable.api.physics.constraint.ConstraintJointAxis;
import dev.ryanhcode.sable.api.physics.constraint.PhysicsConstraintHandle;
import org.joml.Vector3d;

public class MockConstantHandle /*implements PhysicsConstraintHandle*/ {

    private boolean contactsEnabled;

    public void getJointImpulses(Vector3d linearImpulseDest, Vector3d angularImpulseDest) {

    }

    //@Override
    public void setContactsEnabled(boolean enabled) {
    this.contactsEnabled=enabled;
    }

    //@Override
    public void setMotor(ConstraintJointAxis axis, double target, double stiffness, double damping, boolean hasMaxForce, double maxForce) {

    }

    //@Override
    public void remove() {

    }

    //@Override
    public boolean isValid() {
        return false;
    }
}
