package com.zelaux.betterdiagram.mixin.accessors;

import com.zelaux.betterdiagram.extend.DiagramStickyNoteAccessors;
import dev.simulated_team.simulated.content.entities.diagram.screen.DiagramScreen;
import dev.simulated_team.simulated.content.entities.diagram.screen.DiagramStickyNote;
import org.joml.*;
import org.spongepowered.asm.mixin.*;

import java.util.Objects;

@Mixin(DiagramStickyNote.class)
public abstract class DiagramStickyNoteAccessorsImpl implements DiagramStickyNoteAccessors {

    @Shadow
    @Final
    private static Quaternionf NOTE_ORIENTATION;


    @Shadow
    @Final
    private static Vector3d NOTE_LOCAL_CAM_POS;

    @Shadow
    @Final
    private static Matrix4f NOTE_PROJ_MAT;

    @Shadow
    @Final
    private static int SUBLEVEL_RENDER_WIDTH_PIXELS;

    @Shadow
    @Final
    private static int SUBLEVEL_RENDER_HEIGHT_PIXELS;

    @Override
    public Vector2d betterContraptionDiagram$getScreenCoords(Vector3d point, Vector3d cameraPos){
        //NOTE_ORIENTATION, NOTE_LOCAL_CAM_POS, NOTE_PROJ_MAT, SUBLEVEL_RENDER_WIDTH_PIXELS, SUBLEVEL_RENDER_HEIGHT_PIXELS
        return DiagramScreen.getScreenCoords(point,
            NOTE_ORIENTATION,
            Objects.requireNonNullElse(cameraPos, NOTE_LOCAL_CAM_POS),
            NOTE_PROJ_MAT,
            SUBLEVEL_RENDER_WIDTH_PIXELS,
            SUBLEVEL_RENDER_HEIGHT_PIXELS
        );
    }

    @Override
    public Vector3d betterContraptionDiagram$getPlotCoords(Vector2d point, Vector3d cameraPos){
        return DiagramScreen.getPlotCoords(point,
            NOTE_ORIENTATION,
            Objects.requireNonNullElse(cameraPos, NOTE_LOCAL_CAM_POS),
            NOTE_PROJ_MAT,
            SUBLEVEL_RENDER_WIDTH_PIXELS,
            SUBLEVEL_RENDER_HEIGHT_PIXELS
        );
    }


}
