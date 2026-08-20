package com.zelaux.betterdiagram.mixin.feature.extra_render;

import com.zelaux.betterdiagram.extend.accessors.DiagramScreenAccessors;
import com.zelaux.betterdiagram.extend.accessors.ProjectionAccessor;
import com.zelaux.betterdiagram.gui.screen.diagram_ext.CustomRenderForce;
import com.zelaux.betterdiagram.gui.screen.diagram_ext.CustomRenderedForce_Vector3d;
import dev.ryanhcode.sable.api.physics.force.ForceGroup;
import dev.simulated_team.simulated.content.entities.diagram.screen.DiagramScreen;
import dev.simulated_team.simulated.content.entities.diagram.screen.DiagramStickyNote;
import dev.simulated_team.simulated.content.entities.diagram.screen.ForceClusterFinder;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.FormattedText;
import org.joml.Matrix4fc;
import org.joml.Quaternionfc;
import org.joml.Vector3dc;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

import java.util.List;

@Mixin(value = DiagramScreen.class,priority = 2000)
public abstract class ExtraRender_DiagramScreen implements DiagramScreenAccessors {

    @Shadow
    private DiagramStickyNote note;

    @Inject(method = "renderForceArrow", at = @At(value = "INVOKE", target = "Ldev/simulated_team/simulated/content/entities/diagram/screen/DiagramScreen;addForceArrowTooltip(Ldev/ryanhcode/sable/api/physics/force/ForceGroup;IDILjava/util/List;)V"))
    public void renderForceArrow(
        GuiGraphics graphics,
        ForceGroup forceGroup,
        ForceClusterFinder.Cluster pointForce,
        double maxArrowLength,
        int mouseX,
        int mouseY,
        List<FormattedText> tooltipLines,
        Quaternionfc orientation,
        Vector3dc cameraPos,
        Matrix4fc projMatrix,
        int areaWidth,
        int areaHeight,
        CallbackInfo ci
    ) {
        if(pointForce.force() instanceof CustomRenderedForce_Vector3d custom) {
            custom.onHover(
                graphics,
                getProjectionAccessor(),
                mouseX,
                mouseY,
                tooltipLines,
                pointForce,
                forceGroup
            );
        }
    }

    private ProjectionAccessor getProjectionAccessor() {
        return bcd$isDiagramScreen() ? this : (ProjectionAccessor) note;
    }

    @Inject(method = "renderForceArrow", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;popPose()V"))
    private void renderAsDot(
        GuiGraphics graphics,
        ForceGroup forceGroup,
        ForceClusterFinder.Cluster pointForce,
        double maxArrowLength,
        int mouseX,
        int mouseY,
        List<FormattedText> tooltipLines,
        Quaternionfc orientation,
        Vector3dc cameraPos,
        Matrix4fc projMatrix,
        int areaWidth,
        int areaHeight,
        CallbackInfo ci
    ) {
        if(pointForce.force() instanceof CustomRenderForce custom) {
            custom.onRender(graphics,
                pointForce,
                forceGroup,
                mouseX,mouseY,
                maxArrowLength,
                getProjectionAccessor(),
                true
                );
        }
    }

    @Inject(method = "renderForceArrow", at = @At(value = "INVOKE:LAST",
        target = "Lcom/mojang/blaze3d/vertex/VertexConsumer;addVertex(Lorg/joml/Matrix4f;FFF)Lcom/mojang/blaze3d/vertex/VertexConsumer;"
    ))
    private void renderAsLine(
        GuiGraphics graphics,
        ForceGroup forceGroup,
        ForceClusterFinder.Cluster pointForce,
        double maxArrowLength,
        int mouseX,
        int mouseY,
        List<FormattedText> tooltipLines,
        Quaternionfc orientation,
        Vector3dc cameraPos,
        Matrix4fc projMatrix,
        int areaWidth,
        int areaHeight,
        CallbackInfo ci
    ) {
        if(pointForce.force() instanceof CustomRenderForce custom) {
            custom.onRender(graphics,
                pointForce,
                forceGroup,
                mouseX,mouseY,
                maxArrowLength,
                getProjectionAccessor(),
                false
                );
        }
    }
}
