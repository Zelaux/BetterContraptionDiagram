package com.zelaux.betterdiagram.index;

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
}
