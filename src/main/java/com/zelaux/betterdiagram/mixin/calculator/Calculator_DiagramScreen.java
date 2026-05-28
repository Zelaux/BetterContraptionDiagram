package com.zelaux.betterdiagram.mixin.calculator;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zelaux.betterdiagram.index.ForceGroups;
import com.zelaux.betterdiagram.extend.accessors.ProjectionAccessor;
import com.zelaux.betterdiagram.extend.WithClientData;
import com.zelaux.betterdiagram.gui.CenterMassMovingScreen;
import com.zelaux.betterdiagram.gui.OffCenteredBlockTooltipHandler;
import com.zelaux.betterdiagram.gui.tooltip.DiagramInfoTooltip;
import com.zelaux.betterdiagram.gui.widget.BDiagramButton;
import com.zelaux.betterdiagram.index.BCDTextures;
import com.zelaux.betterdiagram.struct.BCDTexture;
import com.zelaux.betterdiagram.struct.MassStack;
import com.zelaux.betterdiagram.util.CenterMassCalculator;
import com.zelaux.betterdiagram.util.MixinCalculatorUtil;
import com.zelaux.betterdiagram.util.StringUtil;
import com.zelaux.betterdiagram.util.UIUtil;
import dev.ryanhcode.sable.sublevel.ClientSubLevel;
import dev.simulated_team.simulated.content.entities.diagram.DiagramConfig;
import dev.simulated_team.simulated.content.entities.diagram.DiagramEntity;
import dev.simulated_team.simulated.content.entities.diagram.screen.DiagramScreen;
import dev.simulated_team.simulated.content.entities.diagram.screen.DiagramStickyNote;
import dev.simulated_team.simulated.index.SimGUITextures;
import dev.simulated_team.simulated.network.packets.contraption_diagram.DiagramDataPacket;
import net.createmod.catnip.gui.AbstractSimiScreen;
import net.createmod.catnip.theme.Color;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.*;
import org.joml.Runtime;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

import java.lang.Math;
import java.util.ArrayList;
import java.util.List;

import static com.zelaux.betterdiagram.util.VecUtil.*;

@Mixin(DiagramScreen.class)
public abstract class Calculator_DiagramScreen extends AbstractSimiScreen implements ProjectionAccessor {

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


    @Shadow
    protected abstract boolean canDrawArrowAt(int x, int y, int width, int height);

    @Shadow
    private DiagramStickyNote note;

    @Inject(at = @At(value = "TAIL"), method = "init")
    private void inject(CallbackInfo ci) {

        final int diagramX = this.width / 2 - DIAGRAM_TEXTURE.width / 2;
        final int diagramY = this.height / 2 - DIAGRAM_TEXTURE.height / 2;
        var layout = UIUtil.vertical(4,
            new BDiagramButton(BCDTextures.Diagram.DIAGRAM_ICON_CALCULATOR, 0, 0, Component.translatable("better_contraption_diagram.diagram-button"), () -> {
                CenterMassMovingScreen.open(self());
            }),
            new BDiagramButton(BCDTextures.Diagram.DIAGRAM_ICON_INFO, 0, 0,
                Component.translatable("better_contraption_diagram.extra-info.diagram-button"), () -> {})
                .withTooltip(new DiagramInfoTooltip(self(), this.subLevel, this.serverData))

        );
        layout.setPosition(diagramX + 9, diagramY + 9 + 20 * 4);
        layout.arrangeElements();
        layout.visitWidgets(this::addRenderableWidget);
    }

    @Unique
    private boolean bcd$isDiagramScreen = false;

    @Inject(at = @At(value = "INVOKE", shift = At.Shift.BEFORE, target = "Ldev/simulated_team/simulated/content/entities/diagram/screen/DiagramScreen;renderArrows(Lnet/minecraft/client/gui/GuiGraphics;IIIILorg/joml/Quaternionfc;Lorg/joml/Vector3dc;Lorg/joml/Matrix4fc;II)V"), method = "renderWindow")
    private void markRenderArrowsFromDiagramScreen(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        bcd$isDiagramScreen = true;

    }

    private void renderOffCentered(GuiGraphics graphics,
                                   int mouseX,
                                   int mouseY,
                                   int areaOriginX,
                                   int areaOriginY,
                                   Quaternionfc orientation,
                                   Vector3dc cameraPos,
                                   Matrix4fc projMatrix,
                                   int areaWidth,
                                   int areaHeight, boolean shouldClipWeights) {
        ProjectionAccessor accessor = shouldClipWeights ? (ProjectionAccessor) note : this;

        var newHovered = MixinCalculatorUtil.renderOffCentered((WithClientData) diagram, self(), tooltipList, accessor, graphics, mouseX, mouseY, shouldClipWeights);
        if(!shouldClipWeights) {
            OffCenteredBlockTooltipHandler.addTooltip(
                self(),
                newHovered,
                tooltipList
            );
        }


    }

    @Inject(method = "renderArrows", at = @At(value = "TAIL"))
    private void renderWeightAfterArrows(GuiGraphics graphics, int mouseX, int mouseY, int areaOriginX, int areaOriginY, Quaternionfc orientation, Vector3dc cameraPos, Matrix4fc projMatrix, int areaWidth, int areaHeight, CallbackInfo ci) {
        //var stacks = ((WithClientData) diagram).betterContraptionDiagram$getClientData(DataKeys.MASS_STACKS, null);
        var stacks = CenterMassCalculator.recalculateStacks(self());
        boolean shouldClipWeights = !this.bcd$isDiagramScreen;
        renderOffCentered(graphics, mouseX, mouseY, areaOriginX, areaOriginY, orientation, cameraPos, projMatrix, areaWidth, areaHeight, shouldClipWeights);
        this.bcd$isDiagramScreen = false;
        if(stacks == null) return;
        mouseX -= areaOriginX;
        mouseY -= areaOriginY;
        ProjectionAccessor accessor = shouldClipWeights ? (ProjectionAccessor) note : this;

        //final int color = (255 << 24) | 0x9D293A;
        final int shadowColor = 0xfff9f2de;
        Vector3d tmp = new Vector3d(), projectedAxis = new Vector3d();
        Vector3d offset = minVec3d(subLevel.getPlot().getBoundingBox());
        var eCOM = CenterMassCalculator.expectedCenterOfMass(self());
        WithClientData clientData = (WithClientData) diagram;
        for(int i = 0; i < stacks.length; i++) {
            var weights = stacks[i];
            if(weights.isEmpty() || !clientData.axisStates(i)) continue;
            var group = ForceGroups.AXIS_GROUPS[i];
            int color = group.color() | (0xff << 24);
            Vector3d AXIS = DIRECTIONS[i];
            projectedAxis.set(orientation.transformInverse(tmp.set(AXIS)));
            if(Runtime.equals(maxAbsXY(projectedAxis), 0, 0.1f)) {
                Vector2d originCoords = DiagramScreen.getScreenCoords(tmp.set(weights.position()), orientation, cameraPos, projMatrix, areaWidth, areaHeight);
                if(shouldClipWeights && !canDrawArrowAt((int) originCoords.x, (int) originCoords.y, areaWidth, areaHeight))
                    continue;
                if(originCoords.distanceSquared(mouseX, mouseY) < 8.0 * 8.0) {
                    var value = Component.literal(StringUtil.plainDouble(weights.totalWeight())).withColor(color);
                    tooltipList.add(Component.translatable("better_contraption_diagram.weight.wrong-axis.tooltip", group.axisName().copy().withColor(color), value));
                    //addForceArrowTooltip(forceGroup, pointForce.groupSize().getValue(), forceMagnitude, color, tooltipLines);
                }

                BCDTextures.Diagram.DIAGRAM_ICON_WEIGHT_SHADOW.render(graphics, (int) originCoords.x - 8, (int) originCoords.y - 8, new Color(shadowColor));
                BCDTextures.Diagram.DIAGRAM_ICON_WEIGHT.render(graphics, (int) originCoords.x - 8, (int) originCoords.y - 8, new Color(color));
                continue;
            }
            renderMassStacks(graphics,
                weights.stacks,
                mouseX, mouseY,
                tmp,
                offset,
                color, shadowColor,
                BCDTextures.Diagram.DIAGRAM_ICON_WEIGHT, BCDTextures.Diagram.DIAGRAM_ICON_WEIGHT_SHADOW,
                (list, m, v) -> list.add(Component.translatable("better_contraption_diagram.weight.tooltip", v)),
                shouldClipWeights, accessor);
            renderMassStacks(graphics,
                weights.smallStacks,
                mouseX, mouseY,
                tmp,
                offset,
                color, shadowColor,
                BCDTextures.Diagram.DIAGRAM_ICON_SMALL_WEIGHT, BCDTextures.Diagram.DIAGRAM_ICON_SMALL_WEIGHT_SHADOW,
                (list, mass, v) -> list.add(//TODO maybe multi line with tab?
                    Component.translatable("better_contraption_diagram.small-weight.tooltip", v,
                        Component.translatable("better_contraption_diagram.small-weight.distance.tooltip",
                                     Component.literal(StringUtil.plainDouble(Math.abs(mass.position().dot(AXIS) - eCOM.dot(AXIS))))
                                              .withColor(ChatFormatting.GRAY.getColor())
                                 )
                                 .withColor(ChatFormatting.DARK_GRAY.getColor())
                    )
                ),
                shouldClipWeights, accessor);
        }

    }

    private void renderMassStacks(GuiGraphics graphics,
                                  ArrayList<MassStack> stacks1,
                                  int MX, int MY,
                                  Vector3d tmp,
                                  Vector3d offset,
                                  int color, int shadowColor,
                                  BCDTexture diagramIconWeight,
                                  BCDTexture diagramIconWeightShadow,
                                  MixinCalculatorUtil.TooltipAdder componentMutableComponentFunction,
                                  boolean shouldClipWeights, ProjectionAccessor accessor
    ) {
        for(MassStack stack : stacks1) {

            renderMassStack(graphics,
                MX, MY,
                stack,
                tmp,
                offset,
                color, shadowColor,
                diagramIconWeight, diagramIconWeightShadow,
                componentMutableComponentFunction, shouldClipWeights, accessor);
            //LINKED_TYPEWRITER_KEY_ENTRY
        }
    }

    private void renderMassStack(GuiGraphics graphics,
                                 int MX, int MY,
                                 MassStack stack,
                                 Vector3d tmp,
                                 Vector3d offset,
                                 int color, int shadowColor,
                                 BCDTexture icon, BCDTexture shadowIcon,
                                 MixinCalculatorUtil.TooltipAdder tooltip,
                                 boolean shouldClipWeights,
                                 ProjectionAccessor accessor) {
        Vector2d originCoords = accessor.betterContraptionDiagram$getScreenCoords(tmp.set(stack.position()));
        if(shouldClipWeights && !accessor.bcd$canDrawAt((int) originCoords.x, (int) originCoords.y))
            return;
        //if(!this.canDrawArrowAt((int) originCoords.x, (int) originCoords.y, areaWidth, areaHeight)) continue;

        if(originCoords.distanceSquared(MX, MY) < 8.0 * 8.0) {
            var value = Component.literal(StringUtil.plainDouble(stack.amountOf())).withColor(color);
            tooltip.apply(tooltipList, stack, value);
        }

        shadowIcon.render(graphics, (int) originCoords.x - 8, (int) originCoords.y - 8, new Color(shadowColor));
        icon.render(graphics, (int) originCoords.x - 8, (int) originCoords.y - 8, new Color(color));
    }

    private @NotNull DiagramScreen self() {
        return (DiagramScreen) (Object) this;
    }

    @Inject(at = @At("HEAD"), method = "renderCenterOfMass")
    private void renderExpectedCenterOfMass(GuiGraphics graphics, CallbackInfo ci) {
        final Vector2d screenCoords = MixinCalculatorUtil.screenPositionOfExpectedCOM(self(), LOCAL_ORIENTATION, LOCAL_CAMERA_POSITION, PROJECTION_MAT, DIAGRAM_TEXTURE.width, DIAGRAM_TEXTURE.height);
        if(screenCoords == null) return;

        final var tex = BCDTextures.Diagram.DIAGRAM_ICON_EXPECTED_COM;

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
        final var eCOM = CenterMassCalculator.expectedCenterOfMass(self);
        if(CenterMassCalculator.equals(eCOM, CenterMassCalculator.centerOfMass(self.subLevel))) return;

        Vector2d screenCoords = DiagramScreen.getScreenCoords(new Vector3d(eCOM), LOCAL_ORIENTATION, LOCAL_CAMERA_POSITION, PROJECTION_MAT, DIAGRAM_TEXTURE.width, DIAGRAM_TEXTURE.height);


        MixinCalculatorUtil.displayECOMTooltip(mouseX, mouseY, areaOriginX, areaOriginY, screenCoords, tooltipList
            , subMinVec3d(new Vector3d(eCOM), subLevel.getPlot().getBoundingBox())
        );
    }

}
