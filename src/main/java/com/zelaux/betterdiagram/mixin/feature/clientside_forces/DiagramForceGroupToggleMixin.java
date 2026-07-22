package com.zelaux.betterdiagram.mixin.feature.clientside_forces;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.zelaux.betterdiagram.extend.BAccessors;
import com.zelaux.betterdiagram.extend.WithClientData;
import com.zelaux.betterdiagram.feature.clientside_forces.DiagramBCDForceGroupToggle;
import com.zelaux.betterdiagram.index.BCDForceGroups;
import com.zelaux.betterdiagram.index.BCDTextures;
import com.zelaux.betterdiagram.struct.BTexture;
import dev.ryanhcode.sable.api.physics.force.ForceGroup;
import dev.simulated_team.simulated.content.entities.diagram.screen.DiagramForceGroupToggle;
import dev.simulated_team.simulated.content.entities.diagram.screen.DiagramScreen;
import dev.simulated_team.simulated.index.SimGUITextures;
import dev.simulated_team.simulated.network.packets.contraption_diagram.DiagramDataPacket;
import net.createmod.catnip.theme.Color;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

@Mixin(DiagramForceGroupToggle.class)
public abstract class DiagramForceGroupToggleMixin extends AbstractWidget {
    private static final Color DISABLED_TEXT = new Color(0xaaaaaaaa);
    private static final Color ENABLED_TEXT = new Color(0xffe2d9c3);
    @Shadow
    @Final
    private ForceGroup group;

    @Shadow
    @Final
    private ResourceLocation groupId;

    @Shadow
    private int forceCount;

    public DiagramForceGroupToggleMixin(int x, int y, int width, int height, Component message) {
        super(x, y, width, height, message);
    }

    @WrapOperation(method = {"renderWidget", "renderTab", "playDownSound"}, at = @At(value = "INVOKE", target = "Ldev/simulated_team/simulated/content/entities/diagram/screen/DiagramForceGroupToggle;isEnabled()Z"))
    protected boolean isEnabledFor_BCD_group(DiagramForceGroupToggle instance, Operation<Boolean> original) {
        if(instance instanceof DiagramBCDForceGroupToggle forceGroupToggle) return forceGroupToggle.isEnabled();
        return original.call(instance);
    }

    @Inject(method = "toggleActive", at = @At("HEAD"), cancellable = true)
    private void toggleActive_doNothingIfNoId(CallbackInfo ci) {
        if(groupId == null) ci.cancel();
    }


    @Inject(method = "renderWidget", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;popPose()V"))
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        boolean usedInBalance = bcd$clientData.bcdDataNotNull_readOnly().enabledForces.balance.contains(group);
        BTexture texture = usedInBalance ? BCDTextures.Diagram.ICON_WEIGHT : BCDTextures.Diagram.ICON_WEIGHT_CROSS;

        int dx = SimGUITextures.DIAGRAM_PAPER.width - texture.width - 8;
        int dy = -4;

        //texture.render(guiGraphics, dx,dy, usedInBalance ? ENABLED_TEXT : DISABLED_TEXT);
    }

    @Unique
    WithClientData bcd$clientData;

    @Inject(method = "<init>", at = @At(value = "TAIL"))
    private void _init_(DiagramScreen diagramScreen, ForceGroup forceGroup, int x, int y, CallbackInfo ci) {
        bcd$clientData = ((WithClientData) BAccessors.accessors(diagramScreen).bcd$diagram());
    }


    @Inject(method = "updateForceState", at = @At("RETURN"))
    void updateForceState(DiagramDataPacket serverData, CallbackInfo ci) {
        if(this.group == BCDForceGroups.MERGED_FORCE_GROUP.group) {
            forceCount = 1;
        }
    }

}
