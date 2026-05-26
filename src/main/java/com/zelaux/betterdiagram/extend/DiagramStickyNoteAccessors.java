package com.zelaux.betterdiagram.extend;

import dev.simulated_team.simulated.content.entities.diagram.screen.DiagramStickyNote;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3d;

public interface DiagramStickyNoteAccessors extends ProjectionAccessor {

    Quaternionf NOTE_ORIENTATION();

    Vector3d NOTE_LOCAL_CAM_POS();

    Matrix4f NOTE_PROJ_MAT();

    int SUBLEVEL_RENDER_WIDTH_PIXELS();

    int SUBLEVEL_RENDER_HEIGHT_PIXELS();

    int SUBLEVEL_RENDER_X_OFFSET();

    int SUBLEVEL_RENDER_Y_OFFSET();

    int renderXStart();

    default int originXWhenActive() {
        return renderXStart() + DiagramStickyNote.MAX_OFFSET;
    }
}
