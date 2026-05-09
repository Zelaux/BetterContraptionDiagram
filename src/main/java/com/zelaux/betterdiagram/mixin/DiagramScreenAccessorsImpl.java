package com.zelaux.betterdiagram.mixin;

import com.zelaux.betterdiagram.extend.DiagramScreenAccessors;
import dev.simulated_team.simulated.content.entities.diagram.DiagramConfig;
import dev.simulated_team.simulated.content.entities.diagram.DiagramEntity;
import dev.simulated_team.simulated.content.entities.diagram.screen.DiagramScreen;
import dev.simulated_team.simulated.network.packets.contraption_diagram.DiagramDataPacket;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3d;
import org.spongepowered.asm.mixin.*;

@Mixin(DiagramScreen.class)
public class DiagramScreenAccessorsImpl implements DiagramScreenAccessors {
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

    @Override
    public boolean betterContraptionDiagram$configDirty() {
        return configDirty;
    }

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
