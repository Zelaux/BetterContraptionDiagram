package com.zelaux.betterdiagram.extend;

import dev.simulated_team.simulated.content.entities.diagram.DiagramConfig;
import dev.simulated_team.simulated.content.entities.diagram.DiagramEntity;
import dev.simulated_team.simulated.content.entities.diagram.screen.DiagramStickyNote;
import dev.simulated_team.simulated.network.packets.contraption_diagram.DiagramDataPacket;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector2d;
import org.joml.Vector3d;

public interface DiagramStickyNoteAccessors {

    Vector2d betterContraptionDiagram$getScreenCoords(Vector3d point, Vector3d cameraPos);

    Vector3d betterContraptionDiagram$getPlotCoords(Vector2d point, Vector3d cameraPos);
}
