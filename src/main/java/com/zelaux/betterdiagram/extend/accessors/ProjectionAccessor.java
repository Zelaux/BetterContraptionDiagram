package com.zelaux.betterdiagram.extend.accessors;

import org.joml.Vector2d;
import org.joml.Vector3d;

public interface ProjectionAccessor {

    Vector2d betterContraptionDiagram$getScreenCoords(Vector3d point, Vector3d cameraPos);

    default Vector2d betterContraptionDiagram$getScreenCoords(Vector3d point) {return betterContraptionDiagram$getScreenCoords(point, null);}

    Vector3d betterContraptionDiagram$getPlotCoords(Vector2d point, Vector3d cameraPos);

    default Vector3d betterContraptionDiagram$getPlotCoords(Vector2d point) {return betterContraptionDiagram$getPlotCoords(point, null);}

    boolean bcd$canDrawAt(int x, int y);

    int bcd$originX();

    int bcd$originY();

}
