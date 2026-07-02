package com.zelaux.betterdiagram.util;

import dev.ryanhcode.sable.api.physics.force.QueuedForceGroup;
import org.joml.Vector3d;
import org.joml.Vector3dc;

public class MergedPointForceCalculator {
    public final Vector3d displacement = new Vector3d();
    public final Vector3d totalForce = new Vector3d();
    public final Vector3d totalTorque = new Vector3d();
    private final Vector3d tmp = new Vector3d();
    private final Vector3d COM = new Vector3d();

    public void begin(Vector3dc COM) {
        totalForce.zero();
        totalTorque.zero();
        displacement.zero();
        this.COM.set(COM);
    }

    public void add(Vector3dc point, Vector3dc force) {
        this.totalForce.add(force);
        Vector3d momentOfForce = tmp.set(point).sub(COM).cross(force);
        totalTorque.add(momentOfForce);


    }

    public void end() {
        double scalar = totalForce.lengthSquared();
        if(scalar == 0) {
            displacement.zero();
            return;
        }
        displacement.set(totalForce).cross(totalTorque).div(scalar);
    }

    public void endPlusCOM() {
        end();
        displacement.add(COM);
    }

    public void add(QueuedForceGroup.PointForce pointForce) {
        add(pointForce.point(), pointForce.force());
    }

    public void addAll(Iterable<QueuedForceGroup.PointForce> forces) {
        forces.forEach(this::add);
    }

    public Vector3dc com() {
        return COM;
    }
}
