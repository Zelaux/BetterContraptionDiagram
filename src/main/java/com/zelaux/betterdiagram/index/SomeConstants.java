package com.zelaux.betterdiagram.index;

import dev.ryanhcode.sable.physics.config.dimension_physics.BezierResourceFunction;
import dev.ryanhcode.sable.physics.config.dimension_physics.DimensionPhysics;
import dev.ryanhcode.sable.physics.config.dimension_physics.DimensionPhysicsData;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.Level;
import org.joml.Vector3dc;

public interface SomeConstants {
    float ropePointMass = 0.35f;
    int DIAGRAM_TOGGLE_SIZE = 11;


    static double gravityScalar(ClientLevel level) {
        return gravityVector(level).length();
    }

    static Vector3dc gravityVector(Level level) {
        return DimensionPhysicsData.getGravity(level);
    }

    static float universalDrag(Level level) {
        final DimensionPhysics physics = DimensionPhysicsData.of(level);
        final DimensionPhysics defaultPhysics = DimensionPhysicsData.getDefault(level);

        return physics.universalDrag().orElseGet(defaultPhysics.universalDrag()::orElseThrow);
    }

    static PressureCurve getPressureCurve(Level level) {
        final DimensionPhysics physics = DimensionPhysicsData.of(level);
        final DimensionPhysics defaultPhysics = DimensionPhysicsData.getDefault(level);

        final double pressure = physics.basePressure().orElseGet(defaultPhysics.basePressure()::orElseThrow);
        final BezierResourceFunction curve = physics.pressureFunction().orElseGet(defaultPhysics.pressureFunction()::orElseThrow);


        return new PressureCurve(pressure, curve);
    }

     record PressureCurve(double pressure, BezierResourceFunction curve) {

        public float floatPressureAt(float y){
            return (float) (pressure * curve.evaluateFunction(y));
        }
    }
}
