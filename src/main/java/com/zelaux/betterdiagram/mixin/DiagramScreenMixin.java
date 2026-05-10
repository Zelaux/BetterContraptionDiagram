package com.zelaux.betterdiagram.mixin;

import com.zelaux.betterdiagram.Config;
import com.zelaux.betterdiagram.Content;
import com.zelaux.betterdiagram.struct.BlackHoleList;
import com.zelaux.betterdiagram.struct.CounterBlackHoleList;
import com.zelaux.betterdiagram.util.VecUtil;
import dev.ryanhcode.sable.api.physics.force.ForceGroup;
import dev.ryanhcode.sable.companion.math.BoundingBox3ic;
import dev.ryanhcode.sable.sublevel.ClientSubLevel;
import dev.simulated_team.simulated.content.entities.diagram.DiagramConfig;
import dev.simulated_team.simulated.content.entities.diagram.screen.DiagramScreen;
import dev.simulated_team.simulated.content.entities.diagram.screen.ForceClusterFinder;
import dev.simulated_team.simulated.data.SimLang;
import dev.simulated_team.simulated.index.SimGUITextures;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import org.apache.commons.lang3.mutable.MutableInt;
import org.joml.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

import java.util.List;

@Mixin(DiagramScreen.class)
public abstract class DiagramScreenMixin {


    @Shadow
    @Final
    public static SimGUITextures DIAGRAM_TEXTURE;
    @Shadow
    @Final
    private static int TOOLTIP_LABEL_COLOR;
    @Shadow
    @Final
    public List<FormattedText> tooltipList;
    @Shadow
    @Final
    public ClientSubLevel subLevel;

    @Shadow
    protected abstract void renderForceArrow(GuiGraphics graphics, ForceGroup forceGroup, ForceClusterFinder.Cluster pointForce, double maxArrowLength, int mouseX, int mouseY, List<FormattedText> tooltipLines, Quaternionfc orientation, Vector3dc cameraPos, Matrix4fc projMatrix, int areaWidth, int areaHeight);



    @Shadow
    protected DiagramConfig config;

    @Inject(at = @At(value = "TAIL"), method = "renderArrows")
    void addCenterOfMassTooltip(GuiGraphics graphics, int mouseX, int mouseY, int areaOriginX, int areaOriginY, Quaternionfc orientation, Vector3dc cameraPos, Matrix4fc projMatrix, int areaWidth, int areaHeight, CallbackInfo ci) {
        if(!config.displayCenterOfMass()) return;

        final Vector3d centerOfMass = new Vector3d(this.subLevel.logicalPose().rotationPoint());
        Vector3d plotSpacePoint = new Vector3d(centerOfMass);
        final Vector2d screenCoords = DiagramScreen.getScreenCoords(plotSpacePoint, orientation, cameraPos, projMatrix, areaWidth, areaHeight);


        if(screenCoords.distanceSquared(mouseX-areaOriginX,mouseY-areaOriginY) >= 8.0 * 8.0) return;

        //Pose3d pose3d = this.subLevel.logicalPose();
        BoundingBox3ic box = subLevel.getPlot().getBoundingBox();
        /*BetterContraptionDiagramClient.LOGGER.debug(
            "{}|{}|{}|[{},{}]",
            vecToString(pose3d.position()),
            vecToString(pose3d.rotationPoint()),
            vecToString(pose3d.scale()),
            vecToString(box.minX(), box.minY(), box.minZ()),
            vecToString(box.maxX(), box.maxY(), box.maxZ())
        );*/
        centerOfMass.sub(box.minX(), box.minY(), box.minZ());
        int color = (0xff << 24) | Config.CENTER_OF_MASS_COLOR.getAsInt();
        MutableComponent centerOfMassTitle = Component.translatable("better_contraption_diagram.center_of_mass");
        tooltipList.add(Component.translatable(
            "better_contraption_diagram.center_of_mass.tooltip", centerOfMassTitle, VecUtil.vectorToFormatted(centerOfMass).withColor(color)
        ));
    }

    @Inject(at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Ldev/simulated_team/simulated/content/entities/diagram/DiagramConfig;enabledForceGroups()Ljava/util/List;", ordinal = 1), method = "renderArrows")
    void drawAxisLines(GuiGraphics graphics, int mouseX, int mouseY, int areaOriginX, int areaOriginY, Quaternionfc orientation, Vector3dc cameraPos, Matrix4fc projMatrix, int areaWidth, int areaHeight, CallbackInfo ci) {
        DiagramScreen thisObject = (DiagramScreen) (Object) this;

        final int diagramX = thisObject.width / 2 - DIAGRAM_TEXTURE.width / 2;
        final int diagramY = thisObject.height / 2 - DIAGRAM_TEXTURE.height / 2;

        final var axisScale = Config.AXIS_SCALE.getAsDouble();
        final var axisOffset = Config.AXIS_OFFSET.getAsDouble();
        final var boundingBox = subLevel.getPlot().getBoundingBox();
        final var tmpCluster = new ForceClusterFinder.Cluster(
            VecUtil.minVec3d(boundingBox).sub(axisOffset, axisOffset, axisOffset),
            new Vector3d(0, 0, 0),
            new MutableInt(1)
        );
        double[] forces = {
            1, 0, 0, boundingBox.width(),
            0, 1, 0, boundingBox.height(),
            0, 0, 1, boundingBox.length()
        };
        ForceGroup[] groups = Content.AXIS_GROUPS;
        Vector3d force = tmpCluster.force();

        var counterBlackHoleList = new CounterBlackHoleList<FormattedText>();
        for(int i = 0; i < groups.length; i++) {
            int off = i << 2;
            force.set(forces[off], forces[off + 1], forces[off + 2]);

            ForceGroup group = groups[i];
            renderForceArrow(
                graphics, group,
                tmpCluster,
                1 / axisScale,
                mouseX - diagramX, mouseY - diagramY,
                counterBlackHoleList,
                orientation, cameraPos, projMatrix,
                areaWidth, areaHeight
            );
            if(counterBlackHoleList.counter > 0) {
                counterBlackHoleList.counter = 0;
                final var nameText = SimLang.builder().add(group.name()).color((255 << 24) | group.color()).component();
            final     var axisValue = Component.translatable("better_contraption_diagram.axis_value", String.format("%,d", (int) forces[off + 3])).withColor(0xffffffff);

                tooltipList.add(
                    Component.translatable("better_contraption_diagram.axis_arrow", nameText, axisValue).withColor(TOOLTIP_LABEL_COLOR)
                );
            }
        }
    }



    @Inject(at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Ldev/simulated_team/simulated/content/entities/diagram/screen/DiagramScreen;addForceArrowTooltip(Ldev/ryanhcode/sable/api/physics/force/ForceGroup;IDILjava/util/List;)V"), method = "renderForceArrow")
    private void addForceDirectionIntoTooltips(GuiGraphics graphics, ForceGroup forceGroup, ForceClusterFinder.Cluster pointForce, double maxArrowLength, int mouseX, int mouseY, List<FormattedText> tooltipLines, Quaternionfc orientation, Vector3dc cameraPos, Matrix4fc projMatrix, int areaWidth, int areaHeight, CallbackInfo ci) {
        Vector3d force = pointForce.force();
        var e = VecUtil.vectorToFormatted(force).withColor(Config.FORCE_CORDS_COLOR.getAsInt());

        if(tooltipLines instanceof BlackHoleList<FormattedText>) return;
        FormattedText last = tooltipLines.getLast();

        tooltipLines.set(
            tooltipLines.size() - 1,
            FormattedText.composite(
                last,
                FormattedText.of(" "),
                e
            )
        );
    }
}
