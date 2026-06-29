package com.zelaux.betterdiagram.index;

import org.joml.Vector3d;
import org.joml.Vector3dc;

public interface SomeConstants {
    float ropePointMass=0.35f;

    static double gravityScalar() {
        return 11;
    }

    static Vector3dc gravityVector() {return new Vector3d(0,-11,0);
    }
}
