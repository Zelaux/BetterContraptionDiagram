package com.zelaux.betterdiagram.mixin.feature.balance_center;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.zelaux.betterdiagram.Config;
import com.zelaux.betterdiagram.extend.WithClientData;
import com.zelaux.betterdiagram.extend.accessors.ProjectionAccessor;
import com.zelaux.betterdiagram.feature.balance.BalanceModel;
import com.zelaux.betterdiagram.feature.balance_center.ListSwitchOverlay;
import com.zelaux.betterdiagram.feature.include_subsublevel.IDiagramScreenWithDependencies;
import com.zelaux.betterdiagram.feature.include_subsublevel.SubLevelDependenciesData;
import com.zelaux.betterdiagram.index.BTextures;
import com.zelaux.betterdiagram.util.CenterMassCalculator;
import com.zelaux.betterdiagram.util.format.VecFormat;
import dev.ryanhcode.sable.sublevel.ClientSubLevel;
import dev.simulated_team.simulated.content.entities.diagram.DiagramConfig;
import dev.simulated_team.simulated.content.entities.diagram.DiagramEntity;
import dev.simulated_team.simulated.content.entities.diagram.screen.DiagramButton;
import dev.simulated_team.simulated.content.entities.diagram.screen.DiagramScreen;
import net.createmod.catnip.gui.AbstractSimiScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
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
public abstract class BalanceCenter_DiagramScreen extends AbstractSimiScreen implements ProjectionAccessor, IDiagramScreenWithDependencies {

    @Shadow
    protected DiagramConfig config;

    @Shadow
    @Final
    public ClientSubLevel subLevel;
    private ListSwitchOverlay bcd$listSwitchOverlay;

    @Shadow
    public static Vector2d getScreenCoords(Vector3d plotSpacePoint, Quaternionfc orientation, Vector3dc localPosition, Matrix4fc projMatrix, int width, int height) {throw new UnsupportedOperationException("Implemented via mixin");}

    @Shadow
    @Final
    public List<FormattedText> tooltipList;

    @Shadow
    @Final
    private DiagramEntity diagram;

    @WrapOperation(method = "init",at = @At(value = "INVOKE", target = "Ldev/simulated_team/simulated/content/entities/diagram/screen/DiagramScreen;addRenderableWidget(Lnet/minecraft/client/gui/components/events/GuiEventListener;)Lnet/minecraft/client/gui/components/events/GuiEventListener;",ordinal = 0))
    GuiEventListener init(DiagramScreen instance, GuiEventListener guiEventListener, Operation<GuiEventListener> original){
        GuiEventListener call = original.call(instance, guiEventListener);

        bcd$listSwitchOverlay = new ListSwitchOverlay((DiagramButton) guiEventListener);
        return call;
    }

    @Inject(method = "renderWindowForeground",at=@At("HEAD"))
    void renderWindowForeground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks, CallbackInfo ci){
        bcd$listSwitchOverlay.render(graphics,mouseX,mouseY,partialTicks);
    }

    @Inject(at = @At("HEAD"), method = "renderCenterOfMass")
    private void renderExpectedCenterOfMass(GuiGraphics graphics, CallbackInfo ci) {
        var point = bcd$mergedBalancePoint();
        if(point == null) return;
        final Vector2d screenCoords = bcd$getScreenCoords(new Vector3d(point));
        if(screenCoords == null) return;

        final var tex = BTextures.Diagram.ICON_BALANCE_CENTER;

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
        if(point == null || point.justGravity() || CenterMassCalculator.equals(point.displacement(),CenterMassCalculator.centerOfMass(subLevel))) return null;
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
        MutableComponent title = Component.translatable("bcd.balance-center");
        tooltipList.add(Component.translatable(
            "bcd.eCOM.tooltip",
            title.withColor(color),
            VecFormat.Presets.forceCords(eCOM)
        ));
    }

}
