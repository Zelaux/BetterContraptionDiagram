package com.zelaux.betterdiagram.mixin.accessors;

import com.zelaux.betterdiagram.extend.DiagramScreenAccessors;
import dev.simulated_team.simulated.content.entities.diagram.DiagramConfig;
import dev.simulated_team.simulated.content.entities.diagram.DiagramEntity;
import dev.simulated_team.simulated.content.entities.diagram.screen.DiagramScreen;
import dev.simulated_team.simulated.content.entities.diagram.screen.DiagramStickyNote;
import dev.simulated_team.simulated.network.packets.contraption_diagram.DiagramDataPacket;
import org.jetbrains.annotations.Nullable;
import org.joml.*;
import org.spongepowered.asm.mixin.*;

import java.util.Objects;

@Mixin(DiagramScreen.class)
public abstract class DiagramScreenAccessorsImpl implements DiagramScreenAccessors {
    @Shadow
    @Final
    private DiagramEntity diagram;

    @Shadow
    private @Nullable DiagramDataPacket serverData;

    @Shadow
    @Final
    private static Vector3d LOCAL_CAMERA_POSITION;

    @Shadow
    @Final
    private static Matrix4f PROJECTION_MAT;

    @Shadow
    protected DiagramConfig config;

    @Shadow
    private boolean configDirty;

    @Shadow
    private DiagramStickyNote note;

    @Shadow
    public abstract Vector2d getScreenCoords(Vector3d plotSpacePoint, Quaternionfc orientation, Vector3dc localPosition, Matrix4fc projMatrix, int width, int height);

    @Shadow
    @Final
    public static Quaternionf LOCAL_ORIENTATION;

    @Shadow
    public abstract Vector3d getPlotCoords(Vector2dc diagramSpacePoint, Quaternionfc orientation, Vector3dc localPosition, Matrix4fc projMatrix, int width, int height);

    @Override
    public DiagramStickyNote betterContraptionDiagram$note() {
        return note;
    }

    @Override
    public Vector2d betterContraptionDiagram$getScreenCoords(Vector3d point, Vector3d cameraPos){
        return getScreenCoords(point,
            LOCAL_ORIENTATION,
            Objects.requireNonNullElse(cameraPos, LOCAL_CAMERA_POSITION),
            PROJECTION_MAT,
            DiagramScreen.DIAGRAM_TEXTURE.width,
            DiagramScreen.DIAGRAM_TEXTURE.height
        );
    }
    @Override
    public Vector3d betterContraptionDiagram$getPlotCoords(Vector2d point, Vector3d cameraPos){
        return getPlotCoords(point,
            LOCAL_ORIENTATION,
            Objects.requireNonNullElse(cameraPos, LOCAL_CAMERA_POSITION),
            PROJECTION_MAT,
            DiagramScreen.DIAGRAM_TEXTURE.width,
            DiagramScreen.DIAGRAM_TEXTURE.height
        );
    }

    @Override
    public boolean betterContraptionDiagram$configDirty() {return configDirty;}

    @Override
    public DiagramConfig betterContraptionDiagram$config() {return config;}

    @Override
    public Matrix4f PROJECTION_MAT() {return PROJECTION_MAT;}

    @Override
    public Vector3d LOCAL_CAMERA_POSITION() {return LOCAL_CAMERA_POSITION;}


    @Override
    public @Nullable DiagramDataPacket betterContraptionDiagram$serverData() {return serverData;}

    @Override
    public DiagramEntity betterContraptionDiagram$diagram() {return diagram;}


}
