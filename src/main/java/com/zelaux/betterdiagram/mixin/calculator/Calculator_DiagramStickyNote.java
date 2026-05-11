package com.zelaux.betterdiagram.mixin.calculator;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zelaux.betterdiagram.index.BCDTextures;
import com.zelaux.betterdiagram.util.CenterMassCalculator;
import com.zelaux.betterdiagram.util.MixinCalculatorUtil;
import dev.engine_room.flywheel.lib.transform.TransformStack;
import dev.simulated_team.simulated.content.entities.diagram.screen.DiagramScreen;
import dev.simulated_team.simulated.content.entities.diagram.screen.DiagramStickyNote;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Direction;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector2d;
import org.joml.Vector3d;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

@Mixin(DiagramStickyNote.class)
public class Calculator_DiagramStickyNote {
    @Shadow
    private DiagramScreen parent;

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

    @Inject(at = @At("HEAD"), method = "renderCustomCOM")
    private void XX(GuiGraphics guiGraphics, PoseStack stack, CallbackInfo ci) {
        if(!(CenterMassCalculator.accessors(parent)).betterContraptionDiagram$config().displayCenterOfMass()) return;

        final Vector2d screenCoords = MixinCalculatorUtil.screenPositionOfExpectedCOM(
            parent,
            NOTE_ORIENTATION, NOTE_LOCAL_CAM_POS, NOTE_PROJ_MAT, SUBLEVEL_RENDER_WIDTH_PIXELS, SUBLEVEL_RENDER_HEIGHT_PIXELS);
        if(screenCoords == null) return;
        stack.pushPose() ;

        var tex = BCDTextures.DIAGRAM_ICON_EXPECTED_COM_TINY;
        final double comOffsetX = (screenCoords.x) - 8;
        final double comOffsetY = (screenCoords.y) - 8;

        if(comOffsetY > 0 && comOffsetX > 0 && comOffsetY < SUBLEVEL_RENDER_HEIGHT_PIXELS && comOffsetX < SUBLEVEL_RENDER_WIDTH_PIXELS) {
            stack.translate(comOffsetX, comOffsetY, 0);
            guiGraphics.blit(tex.location, 0, 0, 5, tex.startX, tex.startY, tex.width, tex.height, tex.texWidth, tex.texHeight);
        } else {
            final float centerX = SUBLEVEL_RENDER_WIDTH_PIXELS / 2f;
            final float centerY = SUBLEVEL_RENDER_HEIGHT_PIXELS / 2f;

            final Vector2d target = new Vector2d(screenCoords.x() - centerX, screenCoords.y - centerY).normalize();

            TransformStack.of(stack)
                          .translate(centerX, centerY, 0)
                          .rotate((float) Math.atan2(target.x, -target.y), Direction.Axis.Z)
                          .translate(-8, -8, 0)
                          .translate(0, -40, 0);

            tex = BCDTextures.DIAGRAM_ICON_EXPECTED_COM_ARROW;
            guiGraphics.blit(tex.location, 0, 0, 5, tex.startX, tex.startY, tex.width, tex.height, tex.texWidth, tex.texHeight);
        }
        stack.popPose();
    }

}
