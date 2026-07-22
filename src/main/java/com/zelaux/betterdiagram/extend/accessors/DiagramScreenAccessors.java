package com.zelaux.betterdiagram.extend.accessors;

import com.zelaux.betterdiagram.extend.WithClientData;
import com.zelaux.betterdiagram.feature.include_subsublevel.IDiagramScreenWithDependencies;
import dev.simulated_team.simulated.content.entities.diagram.DiagramConfig;
import dev.simulated_team.simulated.content.entities.diagram.DiagramEntity;
import dev.simulated_team.simulated.content.entities.diagram.screen.DiagramStickyNote;
import dev.simulated_team.simulated.network.packets.contraption_diagram.DiagramDataPacket;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3d;

public interface DiagramScreenAccessors extends ProjectionAccessor, IDiagramScreenWithDependencies {

    int TOOLTIP_LABEL_COLOR();

    DiagramStickyNote bcd$note();

    boolean bcd$configDirty();

    DiagramConfig bcd$config();

    Matrix4f PROJECTION_MAT();

    Vector3d LOCAL_CAMERA_POSITION();

    @Nullable DiagramDataPacket bcd$serverData();

    DiagramEntity bcd$diagram();

    default WithClientData bcd$clientData() {
        return (WithClientData) bcd$diagram();
    }
}
