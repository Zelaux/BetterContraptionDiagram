package com.zelaux.betterdiagram.mixin.calculator;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zelaux.betterdiagram.Config;
import com.zelaux.betterdiagram.Content;
import com.zelaux.betterdiagram.gui.CenterMassMovingScreen;
import com.zelaux.betterdiagram.gui.widget.BDiagramButton;
import com.zelaux.betterdiagram.index.BCDTextures;
import com.zelaux.betterdiagram.struct.MassStack;
import com.zelaux.betterdiagram.util.CenterMassCalculator;
import com.zelaux.betterdiagram.util.MixinCalculatorUtil;
import com.zelaux.betterdiagram.util.StringUtil;
import com.zelaux.betterdiagram.util.VecUtil;
import dev.ryanhcode.sable.sublevel.ClientSubLevel;
import dev.simulated_team.simulated.content.entities.diagram.DiagramConfig;
import dev.simulated_team.simulated.content.entities.diagram.DiagramEntity;
import dev.simulated_team.simulated.content.entities.diagram.screen.DiagramScreen;
import dev.simulated_team.simulated.index.SimGUITextures;
import dev.simulated_team.simulated.network.packets.contraption_diagram.DiagramDataPacket;
import net.createmod.catnip.gui.AbstractSimiScreen;
import net.createmod.catnip.theme.Color;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

import java.util.List;

import static com.zelaux.betterdiagram.util.VecUtil.vectorToFormatted;

@Mixin(DiagramScreen.class)
public abstract class Calculator_DiagramScreen extends AbstractSimiScreen {

    @Shadow
    @Final
    private static int TOOLTIP_LABEL_COLOR;

    @Shadow
    private @Nullable DiagramDataPacket serverData;

    @Shadow
    @Final
    public static SimGUITextures DIAGRAM_TEXTURE;

    @Shadow
    @Final
    private DiagramEntity diagram;

    @Shadow
    @Final
    public static Quaternionf LOCAL_ORIENTATION;

    @Shadow
    @Final
    private static Vector3d LOCAL_CAMERA_POSITION;

    @Shadow
    @Final
    private static Matrix4f PROJECTION_MAT;

    @Shadow
    @Final
    public List<FormattedText> tooltipList;

    @Shadow
    @Final
    public ClientSubLevel subLevel;

    @Shadow
    protected DiagramConfig config;

    @Inject(at = @At(value = "TAIL"), method = "init")
    private void inject(CallbackInfo ci) {

        final int diagramX = this.width / 2 - DIAGRAM_TEXTURE.width / 2;
        final int diagramY = this.height / 2 - DIAGRAM_TEXTURE.height / 2;

        final var openCenterMass = new BDiagramButton(BCDTextures.DIAGRAM_ICON_CALCULATOR, diagramX + 9, diagramY + 9 + 20 * 4, Component.empty(), () -> {
            CenterMassMovingScreen.open(self());
        });


        addRenderableWidget(openCenterMass);
    }

    @Inject(at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Ldev/simulated_team/simulated/content/entities/diagram/screen/DiagramScreen;renderArrows(Lnet/minecraft/client/gui/GuiGraphics;IIIILorg/joml/Quaternionfc;Lorg/joml/Vector3dc;Lorg/joml/Matrix4fc;II)V"), method = "renderWindow")
    private void renderWindow1(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        //var stacks = ((WithClientData) diagram).betterContraptionDiagram$getClientData(DataKeys.MASS_STACKS, null);
        var stacks = CenterMassCalculator.recalculateStacks(self());
        if(stacks == null) return;
        final int diagramX = this.width / 2 - DIAGRAM_TEXTURE.width / 2;
        final int diagramY = this.height / 2 - DIAGRAM_TEXTURE.height / 2;

        var orientation = LOCAL_ORIENTATION;
        var cameraPos = LOCAL_CAMERA_POSITION;
        var projMatrix = PROJECTION_MAT;
        var areaWidth = DIAGRAM_TEXTURE.width;
        var areaHeight = DIAGRAM_TEXTURE.height;
        //final int color = (255 << 24) | 0x9D293A;
        final int shadowColor = 0xfff9f2de;
        Vector3d tmp = new Vector3d();
        Vector3d offset = VecUtil.minVec3d(subLevel.getPlot().getBoundingBox());
        for(int i = 0; i < stacks.length; i++) {
            var stackDir = stacks[i];
            if(stackDir.isEmpty()) continue;
            int color = Content.AXIS_GROUPS[i].color() | (0xff << 24);
            for(MassStack stack : stackDir) {

                Vector2d originCoords = DiagramScreen.getScreenCoords(tmp.set(stack.position()).add(offset), orientation, cameraPos, projMatrix, areaWidth, areaHeight);


                if(originCoords.distanceSquared(mouseX - diagramX, mouseY - diagramY) < 8.0 * 8.0) {
                    var value = Component.literal(StringUtil.plainDouble(stack.amountOf())).withColor(color);
                    tooltipList.add(Component.translatable("better_contraption_diagram.weight.tooltip",value));
                    //addForceArrowTooltip(forceGroup, pointForce.groupSize().getValue(), forceMagnitude, color, tooltipLines);
                }

                BCDTextures.DIAGRAM_ICON_WEIGHT_SHADOW.render(graphics, (int) originCoords.x - 8, (int) originCoords.y - 8, new Color(shadowColor));
                BCDTextures.DIAGRAM_ICON_WEIGHT.render(graphics, (int) originCoords.x - 8, (int) originCoords.y - 8, new Color(color));
                //LINKED_TYPEWRITER_KEY_ENTRY
            }
        }

    }

    private @NotNull DiagramScreen self() {
        return (DiagramScreen) (Object) this;
    }

    @Inject(at = @At("HEAD"), method = "renderCenterOfMass")
    private void renderExpectedCenterOfMass(GuiGraphics graphics, CallbackInfo ci) {
        final Vector2d screenCoords = MixinCalculatorUtil.screenPositionOfExpectedCOM(self(), LOCAL_ORIENTATION, LOCAL_CAMERA_POSITION, PROJECTION_MAT, DIAGRAM_TEXTURE.width, DIAGRAM_TEXTURE.height);
        if(screenCoords == null) return;

        final var tex = BCDTextures.DIAGRAM_ICON_EXPECTED_COM;

        final PoseStack pose = graphics.pose();
        pose.pushPose();
        pose.translate(screenCoords.x - 8, screenCoords.y - 8, 0);
        graphics.blit(tex.location, 0, 0, 5, tex.startX, tex.startY, tex.width, tex.height, tex.texWidth, tex.texHeight);
        pose.popPose();
    }

    @Inject(at = @At(value = "TAIL"), method = "renderArrows")
    void addCenterOfMassTooltip(GuiGraphics graphics, int mouseX, int mouseY, int areaOriginX, int areaOriginY, Quaternionfc orientation, Vector3dc cameraPos, Matrix4fc projMatrix, int areaWidth, int areaHeight, CallbackInfo ci) {
        if(!config.displayCenterOfMass()) return;
        DiagramScreen self = self();
        final Vector3d eCOM = CenterMassCalculator.expectedCenterOfMass(self);
        if(CenterMassCalculator.equals(eCOM, CenterMassCalculator.centerOfMass(self.subLevel))) return;

        Vector2d screenCoords = DiagramScreen.getScreenCoords(VecUtil.minVec3d(self.subLevel.getPlot().getBoundingBox()).add(eCOM), LOCAL_ORIENTATION, LOCAL_CAMERA_POSITION, PROJECTION_MAT, DIAGRAM_TEXTURE.width, DIAGRAM_TEXTURE.height);


        if(screenCoords.distanceSquared(mouseX - areaOriginX, mouseY - areaOriginY) >= 8.0 * 8.0) return;
        int color = (0xff00_0000) | Config.EXPECTED_CENTER_OF_MASS_COLOR.getAsInt();
        MutableComponent centerOfMassTitle = Component.translatable("better_contraption_diagram.eCOM");
        tooltipList.add(Component.translatable(
            "better_contraption_diagram.eCOM.tooltip",
            centerOfMassTitle.withColor(color),
            vectorToFormatted(eCOM).withColor((0xff << 24) | Config.FORCE_CORDS_COLOR.getAsInt())
        ));
    }
}
