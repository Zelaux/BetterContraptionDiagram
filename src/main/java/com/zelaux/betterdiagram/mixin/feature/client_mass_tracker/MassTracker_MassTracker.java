package com.zelaux.betterdiagram.mixin.feature.client_mass_tracker;

import com.zelaux.betterdiagram.feature.client_mass_tracker.SetMethodForMassTracker;
import dev.ryanhcode.sable.api.physics.mass.MassTracker;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3d;
import org.joml.Matrix3dc;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.spongepowered.asm.mixin.*;

@Mixin(MassTracker.class)
public class MassTracker_MassTracker implements SetMethodForMassTracker {

    @Shadow
    private @Nullable Vector3d centerOfMass;

    @Shadow
    private double mass;

    @Shadow
    private Matrix3d inertiaTensor;

    @Shadow
    private double inverseMass;

    @Shadow
    private Matrix3d inverseInertiaTensor;

    @Override
    public MassTracker bcd$set(@Nullable Vector3dc centerOfMass, double mass, Matrix3dc inertiaTensor) {
        this.centerOfMass = new Vector3d(centerOfMass);
        this.mass = mass;
        this.inverseMass = 1 / mass;
        this.inertiaTensor = new Matrix3d(inertiaTensor);
        this.inverseInertiaTensor = new Matrix3d(inertiaTensor).invert();
        return (MassTracker) (Object) this;
    }
}
