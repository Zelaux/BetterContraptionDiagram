package com.zelaux.betterdiagram.mixin.feature.balance_center;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zelaux.betterdiagram.Config;
import com.zelaux.betterdiagram.extend.WithClientData;
import com.zelaux.betterdiagram.extend.accessors.ProjectionAccessor;
import com.zelaux.betterdiagram.feature.balance.BalanceModel;
import com.zelaux.betterdiagram.feature.include_subsublevel.IDiagramScreenWithDependencies;
import com.zelaux.betterdiagram.feature.include_subsublevel.SubLevelDependenciesData;
import com.zelaux.betterdiagram.index.BCDTextures;
import com.zelaux.betterdiagram.util.CenterMassCalculator;
import com.zelaux.betterdiagram.util.VecFormat;
import dev.ryanhcode.sable.sublevel.ClientSubLevel;
import dev.simulated_team.simulated.content.entities.diagram.DiagramConfig;
import dev.simulated_team.simulated.content.entities.diagram.DiagramEntity;
import dev.simulated_team.simulated.content.entities.diagram.screen.DiagramScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.Nullable;
import org.joml.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

import java.util.List;

import static com.zelaux.betterdiagram.util.VecUtil.subMinVec3d;

@Mixin(DiagramScreen.class)
public abstract class BalanceCenter_DiagramScreen implements ProjectionAccessor, IDiagramScreenWithDependencies {

    @Shadow
    protected DiagramConfig config;

    @Shadow
    @Final
    public ClientSubLevel subLevel;

    @Shadow
    public static Vector2d getScreenCoords(Vector3d plotSpacePoint, Quaternionfc orientation, Vector3dc localPosition, Matrix4fc projMatrix, int width, int height) {throw new UnsupportedOperationException("Implemented via mixin");}

    @Shadow
    @Final
    public List<FormattedText> tooltipList;

    @Shadow
    @Final
    private DiagramEntity diagram;

    @Inject(at = @At("HEAD"), method = "renderCenterOfMass")
    private void renderExpectedCenterOfMass(GuiGraphics graphics, CallbackInfo ci) {
        var point = bcd$mergedBalancePoint();
        if(point == null) return;
        final Vector2d screenCoords = bcd$getScreenCoords(new Vector3d(point));
        if(screenCoords == null) return;

        final var tex = BCDTextures.Diagram.ICON_BALANCE_CENTER;

        final PoseStack pose = graphics.pose();
        pose.pushPose();
        pose.translate(screenCoords.x - 8, screenCoords.y - 8, 0);
        graphics.blit(tex.location, 0, 0, 5, tex.startX, tex.startY, tex.width, tex.height, tex.texWidth, tex.texHeight);
        pose.popPose();
    }

    @Unique
    private @Nullable Vector3dc bcd$mergedBalancePoint() {
        SubLevelDependenciesData dependenciesData = this.bcd$subLevelDependencies();
        var point = BalanceModel.calculateMergedBalancePoint(dependenciesData,((WithClientData) diagram).bcdDataNotNull_readOnly());
        if(point == null || CenterMassCalculator.equals(point.displacement(),CenterMassCalculator.centerOfMass(subLevel))) return null;
        return point.displacement();
    }

    @Inject(at = @At(value = "TAIL"), method = "renderArrows")
    void addCenterOfMassTooltip(GuiGraphics graphics, int mouseX, int mouseY, int areaOriginX, int areaOriginY, Quaternionfc orientation, Vector3dc cameraPos, Matrix4fc projMatrix, int areaWidth, int areaHeight, CallbackInfo ci) {
        if(!config.displayCenterOfMass()) return;
        final var point = bcd$mergedBalancePoint();
        if(point==null)return;



        Vector3d eCOM = subMinVec3d(new Vector3d(point), subLevel.getPlot().getBoundingBox());
        Vector2d screenCoords = bcd$getScreenCoords(new Vector3d(point));
        if(screenCoords.distanceSquared(mouseX - (float) areaOriginX, mouseY - (float) areaOriginY) >= 8.0 * 8.0) return;
        int color = (0xff00_0000) | Config.BALANCE_CENTER_COLOR.getAsInt();
        MutableComponent title = Component.translatable("better_contraption_diagram.balance-center");
        tooltipList.add(Component.translatable(
            "better_contraption_diagram.eCOM.tooltip",
            title.withColor(color),
            VecFormat.Presets.forceCords(eCOM)
        ));
    }
}
