package com.zelaux.betterdiagram.mixin.feature.clientside_forces;

import com.google.common.collect.Iterables;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalDoubleRef;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.zelaux.betterdiagram.extend.WithClientData;
import com.zelaux.betterdiagram.feature.clientside_forces.DiagramBCDForceGroupToggle;
import com.zelaux.betterdiagram.feature.clientside_forces.DiagramForceGroupToggleList;
import com.zelaux.betterdiagram.index.BCDForceGroups;
import com.zelaux.betterdiagram.struct.BCDForceGroup;
import dev.ryanhcode.sable.api.physics.force.ForceGroup;
import dev.simulated_team.simulated.content.entities.diagram.DiagramConfig;
import dev.simulated_team.simulated.content.entities.diagram.DiagramEntity;
import dev.simulated_team.simulated.content.entities.diagram.screen.DiagramForceGroupToggle;
import dev.simulated_team.simulated.content.entities.diagram.screen.DiagramScreen;
import dev.simulated_team.simulated.content.entities.diagram.screen.ForceClusterFinder;
import dev.simulated_team.simulated.index.SimGUITextures;
import dev.simulated_team.simulated.network.packets.contraption_diagram.DiagramDataPacket;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.createmod.catnip.gui.AbstractSimiScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.FormattedText;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4fc;
import org.joml.Quaternionfc;
import org.joml.Vector3dc;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Mixin(DiagramScreen.class)
public abstract class DiagramScreenMixin extends AbstractSimiScreen {
    @Shadow
    private @Nullable DiagramDataPacket serverData;

    @Shadow
    protected DiagramConfig config;

    @Shadow
    protected abstract void renderForceArrow(GuiGraphics graphics, ForceGroup forceGroup, ForceClusterFinder.Cluster pointForce, double maxArrowLength, int mouseX, int mouseY, List<FormattedText> tooltipLines, Quaternionfc orientation, Vector3dc cameraPos, Matrix4fc projMatrix, int areaWidth, int areaHeight);

    @Shadow
    @Final
    public List<FormattedText> tooltipList;


    @Shadow
    @Final
    private List<DiagramForceGroupToggle> forceToggleWidgets;
    @Unique
    private DiagramForceGroupToggleList bcd$forceToggleList;

    @Shadow
    @Final
    public static int MAX_PAPER_OFFSET;

    @Shadow
    @Final
    private DiagramEntity diagram;

    @Shadow
    protected abstract void renderWindow(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks);

    @ModifyVariable(method = "addForceToggleWidgets", at = @At(value = "STORE", ordinal = 0))
    protected Iterable<ForceGroup> addForceToggleWidgets_addBCDForces(Iterable<ForceGroup> forceGroups) {
        return Iterables.concat(forceGroups, Iterables.transform(BCDForceGroups.CUSTOM_FORCES, BCDForceGroup::group));
    }

    @WrapOperation(method = "addForceToggleWidgets", at = @At(value = "NEW", target = "(Ldev/simulated_team/simulated/content/entities/diagram/screen/DiagramScreen;Ldev/ryanhcode/sable/api/physics/force/ForceGroup;II)Ldev/simulated_team/simulated/content/entities/diagram/screen/DiagramForceGroupToggle;"))
    protected DiagramForceGroupToggle addForceToggleWidgets_makeBCDForcesToggle(DiagramScreen diagramScreen, ForceGroup forceGroup, int x, int y, Operation<DiagramForceGroupToggle> original) {
        BCDForceGroup group = BCDForceGroups.wrappedMap.get(forceGroup);
        if(group != null) return new DiagramBCDForceGroupToggle(diagramScreen, group, x, y);

        return original.call(diagramScreen, forceGroup, x, y);
    }

    @Inject(method = "addForceToggleWidgets", at = @At("TAIL"))
    protected void addForceToggleWidgets_scroll(int diagramX, int diagramY, CallbackInfo ci) {
        var list = forceToggleWidgets;
        bcd$forceToggleList = new DiagramForceGroupToggleList(Minecraft.getInstance(), SimGUITextures.DIAGRAM_PAPER.width, 11 * 8, 11);
        bcd$forceToggleList.setPosition(list.get(0).getX(), list.get(0).getY());
        for(DiagramForceGroupToggle toggle : list) {
            bcd$forceToggleList.addEntry(toggle);
        }
        removeWidgets(list);
        addWidget(bcd$forceToggleList);
    }

    @Inject(method = "renderWindow", at = @At(value = "FIELD", target = "Ldev/simulated_team/simulated/content/entities/diagram/screen/DiagramScreen;forceToggleWidgets:Ljava/util/List;", ordinal = 0))
    protected void renderWindow_initPosition(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        bcd$forceToggleList.renderTabs(graphics, mouseX, mouseY, partialTicks);

    }

    @WrapOperation(method = "renderWindow", at = @At(value = "INVOKE", target = "Ldev/simulated_team/simulated/content/entities/diagram/screen/DiagramForceGroupToggle;renderTab(Lnet/minecraft/client/gui/GuiGraphics;IIF)V"))
    protected void renderWindow_initPosition(DiagramForceGroupToggle instance, GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks, Operation<Void> original) {
    }

    @Inject(method = "renderWindow", at = @At(value = "FIELD", target = "Ldev/simulated_team/simulated/content/entities/diagram/screen/DiagramScreen;forceToggleWidgets:Ljava/util/List;", ordinal = 1))
    protected void renderWindow_renderScroll(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        bcd$forceToggleList.renderItems(graphics, mouseX, mouseY, partialTicks);
    }

    @WrapOperation(method = "renderWindow", at = @At(value = "INVOKE", target = "Ljava/util/List;iterator()Ljava/util/Iterator;", ordinal = 1))
    private Iterator<DiagramForceGroupToggle> renderWindow_doNotRenderDefault(List<DiagramForceGroupToggle> instance, Operation<Iterator<DiagramForceGroupToggle>> original) {
        original.call(instance);
        return Collections.emptyIterator();
    }

    @Inject(method = "renderArrows", at = @At(value = "INVOKE", ordinal = 1, target = "Ldev/simulated_team/simulated/content/entities/diagram/DiagramConfig;enabledForceGroups()Ljava/util/List;"))
    private void renderArrows_prepare_clusters(
        GuiGraphics graphics,
        int mouseX,
        int mouseY,
        int areaOriginX,
        int areaOriginY,
        Quaternionfc orientation,
        Vector3dc cameraPos,
        Matrix4fc projMatrix,
        int areaWidth,
        int areaHeight,
        CallbackInfo ci,
        //@Local(index = 13) Map<ForceGroup, List<ForceClusterFinder.Cluster>> clusters,
        @Share("bcd$clusters") LocalRef<Map<ForceGroup, List<ForceClusterFinder.Cluster>>> clustersRef,
        @Local(index = 11) LocalDoubleRef maxArrowLengthSquared
    ) {
        if(serverData == null) return;
        var clusters = new Object2ObjectOpenHashMap<ForceGroup, List<ForceClusterFinder.Cluster>>();
        clustersRef.set(clusters);

        for(BCDForceGroup forceGroup : ((WithClientData) diagram).bcdDataNotNull_readOnly().enabledForces.display) {

            ForceGroup group = forceGroup.group();
            var forces = serverData.forces().get(group);
            if(forces == null) continue;

            final List<ForceClusterFinder.Cluster> cluster = this.config.mergeForces() ? ForceClusterFinder.getMergedClusters(forces) : ForceClusterFinder.passThrough(forces);

            clusters.put(group, cluster);

            for(final ForceClusterFinder.Cluster force : cluster) {
                maxArrowLengthSquared.set(Math.max(maxArrowLengthSquared.get(), force.force().lengthSquared()));
            }
        }

    }


    @Inject(
        method = "renderArrows",
        at = @At(value = "INVOKE", ordinal = 1, target = "Ldev/simulated_team/simulated/content/entities/diagram/DiagramConfig;enabledForceGroups()Ljava/util/List;"),
        order = 2000
    )
    private void renderArrows_render(
        GuiGraphics graphics,
        int mouseX,
        int mouseY,
        int areaOriginX,
        int areaOriginY,
        Quaternionfc orientation,
        Vector3dc cameraPos,
        Matrix4fc projMatrix,
        int areaWidth,
        int areaHeight,
        CallbackInfo ci,
        //@Local(index = 13) Map<ForceGroup, List<ForceClusterFinder.Cluster>> clusters,
        @Share("bcd$clusters") LocalRef<Map<ForceGroup, List<ForceClusterFinder.Cluster>>> clustersRef,
        @Local(index = 11) double maxArrowLengthSquared
    ) {
        if(serverData == null) return;
        var clusters = clustersRef.get();

        double sqrt = Math.sqrt(maxArrowLengthSquared);
        for(var entry : clusters.entrySet()) {
            ForceGroup group = entry.getKey();
            List<ForceClusterFinder.Cluster> cluster = entry.getValue();
            for(ForceClusterFinder.Cluster force : cluster) {
                this.renderForceArrow(graphics,
                    group,
                    force,
                    sqrt,
                    mouseX - areaOriginX,
                    mouseY - areaOriginY,
                    this.tooltipList,
                    orientation,
                    cameraPos,
                    projMatrix,
                    areaWidth,
                    areaHeight);
            }
        }
    }

}

