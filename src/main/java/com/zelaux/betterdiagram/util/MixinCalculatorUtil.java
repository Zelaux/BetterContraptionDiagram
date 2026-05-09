package com.zelaux.betterdiagram.util;

import dev.simulated_team.simulated.content.entities.diagram.screen.DiagramScreen;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector2d;
import org.joml.Vector3d;

public class MixinCalculatorUtil {


    public static @Nullable Vector2d screenPositionOfExpectedCOM(DiagramScreen self, Quaternionf localOrientation, Vector3d localCameraPosition, Matrix4f projectionMat, int areaWidth, int areaHeight) {
        final Vector3d centerOfMass = CenterMassCalculator.expectedCenterOfMass(self);
        if(CenterMassCalculator.equals(centerOfMass, CenterMassCalculator.centerOfMass(self.subLevel))) return null;
        return DiagramScreen.getScreenCoords(VecUtil.minVec3d(self.subLevel.getPlot().getBoundingBox()).add(centerOfMass), localOrientation, localCameraPosition, projectionMat, areaWidth, areaHeight);
    }
}
