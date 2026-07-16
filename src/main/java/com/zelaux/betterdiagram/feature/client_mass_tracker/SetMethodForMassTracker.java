package com.zelaux.betterdiagram.feature.client_mass_tracker;

import dev.ryanhcode.sable.api.physics.mass.MassTracker;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3dc;
import org.joml.Vector3dc;

public interface SetMethodForMassTracker {
    MassTracker bcd$set(@Nullable Vector3dc centerOfMass, double mass, Matrix3dc inertiaTensor);
}
