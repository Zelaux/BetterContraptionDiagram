package com.zelaux.betterdiagram.extend.accessors;

import org.joml.Vector2d;
import org.joml.Vector3d;

public interface ProjectionAccessor {

    Vector2d bcd$getScreenCoords(Vector3d point, Vector3d cameraPos);

    default Vector2d bcd$getScreenCoords(Vector3d point) {return bcd$getScreenCoords(point, null);}

    Vector3d bcd$getPlotCoords(Vector2d point, Vector3d cameraPos);

    float bcd$viewportRadius();

    default Vector3d bcd$getPlotCoords(Vector2d point) {return bcd$getPlotCoords(point, null);}

    boolean bcd$canDrawAt(int x, int y);

    int bcd$originX();

    int bcd$originY();

}
