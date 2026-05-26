package com.zelaux.betterdiagram.mixin.calculator;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zelaux.betterdiagram.index.BCDTextures;
import com.zelaux.betterdiagram.util.CenterMassCalculator;
import com.zelaux.betterdiagram.util.MixinCalculatorUtil;
import com.zelaux.betterdiagram.util.VecUtil;
import dev.engine_room.flywheel.lib.transform.TransformStack;
import dev.simulated_team.simulated.content.entities.diagram.screen.DiagramScreen;
import dev.simulated_team.simulated.content.entities.diagram.screen.DiagramStickyNote;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Direction;
import org.joml.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

import java.lang.Math;

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
    @Shadow
    @Final
    private static int SUBLEVEL_RENDER_X_OFFSET;
    @Shadow
    @Final
    private static int SUBLEVEL_RENDER_Y_OFFSET;
    @Unique
    private static final Vector2d bcd$tmp1 = new Vector2d();
    @Unique
    private static final Vector2i bcd$mouse = new Vector2i();

    @Inject(method = "renderWidget", at = @At(value = "INVOKE", target = "Ldev/simulated_team/simulated/content/entities/diagram/screen/DiagramScreen;renderArrows(Lnet/minecraft/client/gui/GuiGraphics;IIIILorg/joml/Quaternionfc;Lorg/joml/Vector3dc;Lorg/joml/Matrix4fc;II)V"))
    private void saveMouse(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        bcd$mouse.set(mouseX, mouseY);
    }

    @Inject(at = @At("HEAD"), method = "renderCustomCOM")
    private void XX(GuiGraphics guiGraphics, PoseStack stack, CallbackInfo ci) {
        if(!(CenterMassCalculator.accessors(parent)).betterContraptionDiagram$config().displayCenterOfMass()) return;

        final var centerOfMass = CenterMassCalculator.expectedCenterOfMass(parent);
        if(CenterMassCalculator.equals(centerOfMass, CenterMassCalculator.centerOfMass(parent.subLevel))) return;

        Vector2d screenCoords = DiagramScreen.getScreenCoords(new Vector3d(centerOfMass), NOTE_ORIENTATION, NOTE_LOCAL_CAM_POS, NOTE_PROJ_MAT, SUBLEVEL_RENDER_WIDTH_PIXELS, SUBLEVEL_RENDER_HEIGHT_PIXELS);

        stack.pushPose();

        var tex = BCDTextures.Diagram.DIAGRAM_ICON_EXPECTED_COM_TINY;
        final double comOffsetX = (screenCoords.x) - 8;
        final double comOffsetY = (screenCoords.y) - 8;
        var self = (DiagramStickyNote) (Object) this;
        final int areaOffsetX = self.getX() + SUBLEVEL_RENDER_X_OFFSET;
        final int areaOffsetY = self.getY() + SUBLEVEL_RENDER_Y_OFFSET;
        if(comOffsetY > 0 && comOffsetX > 0 && comOffsetY < SUBLEVEL_RENDER_HEIGHT_PIXELS && comOffsetX < SUBLEVEL_RENDER_WIDTH_PIXELS) {
            stack.translate(comOffsetX, comOffsetY, 0);
            guiGraphics.blit(tex.location, 0, 0, 5, tex.startX, tex.startY, tex.width, tex.height, tex.texWidth, tex.texHeight);

            MixinCalculatorUtil.displayECOMTooltip(bcd$mouse.x, bcd$mouse.y,
                areaOffsetX, areaOffsetY,
                screenCoords
                , parent.tooltipList, VecUtil.subMinVec3d(new Vector3d(centerOfMass), parent.subLevel.getPlot().getBoundingBox()))
            ;
        } else {
            final float centerX = SUBLEVEL_RENDER_WIDTH_PIXELS / 2f;
            final float centerY = SUBLEVEL_RENDER_HEIGHT_PIXELS / 2f;

            final Vector2d target = new Vector2d(screenCoords.x() - centerX, screenCoords.y - centerY).normalize();

            MixinCalculatorUtil.displayECOMTooltip(bcd$mouse.x, bcd$mouse.y,
                areaOffsetX, areaOffsetY,
                target.mul(40, bcd$tmp1).add(centerX, centerY)
                , parent.tooltipList, VecUtil.subMinVec3d(new Vector3d(centerOfMass), parent.subLevel.getPlot().getBoundingBox()))
            ;
            TransformStack.of(stack)
                          .translate(centerX, centerY, 0)
                          .rotate((float) Math.atan2(target.x, -target.y), Direction.Axis.Z)
                          .translate(-8, -8, 0)
                          .translate(0, -40, 0);

            tex = BCDTextures.Diagram.DIAGRAM_ICON_EXPECTED_COM_ARROW;
            guiGraphics.blit(tex.location, 0, 0, 5, tex.startX, tex.startY, tex.width, tex.height, tex.texWidth, tex.texHeight);
        }
        stack.popPose();
    }

}
