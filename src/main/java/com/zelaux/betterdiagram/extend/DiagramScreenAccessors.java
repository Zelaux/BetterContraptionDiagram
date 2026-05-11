package com.zelaux.betterdiagram.extend;

import dev.simulated_team.simulated.content.entities.diagram.DiagramConfig;
import dev.simulated_team.simulated.content.entities.diagram.DiagramEntity;
import dev.simulated_team.simulated.content.entities.diagram.screen.DiagramStickyNote;
import dev.simulated_team.simulated.network.packets.contraption_diagram.DiagramDataPacket;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3d;

public interface DiagramScreenAccessors extends ProjectionAccessor{
    DiagramStickyNote betterContraptionDiagram$note();

    boolean betterContraptionDiagram$configDirty();

    DiagramConfig betterContraptionDiagram$config();

    Matrix4f PROJECTION_MAT();

    Vector3d LOCAL_CAMERA_POSITION();

    @Nullable DiagramDataPacket betterContraptionDiagram$serverData();

    DiagramEntity betterContraptionDiagram$diagram();
    default WithClientData betterContraptionDiagram$clientData(){
        return (WithClientData) betterContraptionDiagram$diagram();
    }
}
