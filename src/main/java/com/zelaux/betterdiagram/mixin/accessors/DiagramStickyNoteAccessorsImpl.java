package com.zelaux.betterdiagram.mixin.accessors;

import com.zelaux.betterdiagram.extend.accessors.DiagramStickyNoteAccessors;
import dev.simulated_team.simulated.content.entities.diagram.screen.DiagramScreen;
import dev.simulated_team.simulated.content.entities.diagram.screen.DiagramStickyNote;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector2d;
import org.joml.Vector3d;
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

    @Shadow
    @Final
    private static int SUBLEVEL_RENDER_X_OFFSET;

    @Shadow
    @Final
    private static int SUBLEVEL_RENDER_Y_OFFSET;

    @Shadow
    @Final
    private int renderXStart;

    @Shadow
    private DiagramScreen parent;

    @Override
    public Vector2d betterContraptionDiagram$getScreenCoords(Vector3d point, Vector3d cameraPos) {
        //NOTE_ORIENTATION, NOTE_LOCAL_CAM_POS, NOTE_PROJ_MAT, SUBLEVEL_RENDER_WIDTH_PIXELS, SUBLEVEL_RENDER_HEIGHT_PIXELS
        return DiagramScreen.getScreenCoords(point,
            NOTE_ORIENTATION,
            Objects.requireNonNullElse(cameraPos, NOTE_LOCAL_CAM_POS),
            NOTE_PROJ_MAT,
            SUBLEVEL_RENDER_WIDTH_PIXELS,
            SUBLEVEL_RENDER_HEIGHT_PIXELS
        );
    }


    private boolean canDrawArrowAt(final int x, final int y, final int width, final int height) {
        final int padding = 8;
        return x >= padding && x < width - padding && y >= padding && y < height - padding;
    }

    @Override
    public Vector3d betterContraptionDiagram$getPlotCoords(Vector2d point, Vector3d cameraPos) {
        return DiagramScreen.getPlotCoords(point,
            NOTE_ORIENTATION,
            Objects.requireNonNullElse(cameraPos, NOTE_LOCAL_CAM_POS),
            NOTE_PROJ_MAT,
            SUBLEVEL_RENDER_WIDTH_PIXELS,
            SUBLEVEL_RENDER_HEIGHT_PIXELS
        );
    }

    @Override
    public boolean bcd$canDrawAt(int x, int y) {
        return canDrawArrowAt(
            x, y,
            SUBLEVEL_RENDER_WIDTH_PIXELS,
            SUBLEVEL_RENDER_HEIGHT_PIXELS
        );
    }

    @Override
    public int bcd$originX() {
        return ((DiagramStickyNote) (Object) this).getX() + SUBLEVEL_RENDER_X_OFFSET;
    }

    @Override
    public int bcd$originY() {
        return ((DiagramStickyNote) (Object) this).getY() + SUBLEVEL_RENDER_Y_OFFSET;
    }

    public Quaternionf NOTE_ORIENTATION() {return NOTE_ORIENTATION;}

    public Vector3d NOTE_LOCAL_CAM_POS() {return NOTE_LOCAL_CAM_POS;}

    public Matrix4f NOTE_PROJ_MAT() {return NOTE_PROJ_MAT;}

    public int SUBLEVEL_RENDER_WIDTH_PIXELS() {return SUBLEVEL_RENDER_WIDTH_PIXELS;}

    public int SUBLEVEL_RENDER_HEIGHT_PIXELS() {return SUBLEVEL_RENDER_HEIGHT_PIXELS;}

    @Override
    public int SUBLEVEL_RENDER_X_OFFSET() {return SUBLEVEL_RENDER_X_OFFSET;}

    @Override
    public int SUBLEVEL_RENDER_Y_OFFSET() {return SUBLEVEL_RENDER_Y_OFFSET;}

    @Override
    public int renderXStart() {return renderXStart;}
}
