package com.zelaux.betterdiagram.mixin.feature.clientside_forces;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalDoubleRef;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.zelaux.betterdiagram.index.BCDForceGroups;
import dev.ryanhcode.sable.api.physics.force.ForceGroup;
import dev.simulated_team.simulated.content.entities.diagram.DiagramConfig;
import dev.simulated_team.simulated.content.entities.diagram.screen.DiagramScreen;
import dev.simulated_team.simulated.content.entities.diagram.screen.ForceClusterFinder;
import dev.simulated_team.simulated.network.packets.contraption_diagram.DiagramDataPacket;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.FormattedText;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4fc;
import org.joml.Quaternionfc;
import org.joml.Vector3dc;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

import java.util.List;
import java.util.Map;

@Mixin(DiagramScreen.class)
public abstract class DiagramScreenMixin {
    @Shadow
    private @Nullable DiagramDataPacket serverData;

    @Shadow
    protected DiagramConfig config;

    @Shadow
    protected abstract void renderForceArrow(GuiGraphics graphics, ForceGroup forceGroup, ForceClusterFinder.Cluster pointForce, double maxArrowLength, int mouseX, int mouseY, List<FormattedText> tooltipLines, Quaternionfc orientation, Vector3dc cameraPos, Matrix4fc projMatrix, int areaWidth, int areaHeight);

    @Shadow
    @Final
    public List<FormattedText> tooltipList;

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
        for(BCDForceGroups.BCDForceGroup forceGroup : BCDForceGroups.CUSTOM_FORCES) {

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
        for(BCDForceGroups.BCDForceGroup forceGroup : BCDForceGroups.CUSTOM_FORCES) {

            ForceGroup group = forceGroup.group();
            List<ForceClusterFinder.Cluster> cluster = clusters.get(group);
            if(cluster == null) continue;
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

