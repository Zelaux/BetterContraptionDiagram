package com.zelaux.betterdiagram.feature.balance;

import dev.ryanhcode.sable.api.physics.force.QueuedForceGroup;
import lombok.Getter;
import org.joml.Vector3d;
import org.joml.Vector3dc;

@Getter
public class CenterMassMerger {
    public final Vector3d weightedCenterOfMass = new Vector3d();
    public final Vector3d centerOfMass = new Vector3d();
    public double mass;

    public void reset() {
        weightedCenterOfMass.zero();
        centerOfMass.zero();
        mass=0;
    }

    public void add(Vector3dc position,double mass) {
        this.mass+=mass;
        weightedCenterOfMass.fma(mass,position);
    }

    public void finish() {
        weightedCenterOfMass.div(mass,centerOfMass);
    }
}
