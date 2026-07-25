package com.zelaux.betterdiagram.feature.balance_center;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.zelaux.betterdiagram.feature.balance.BalanceListSwitchAnimation;
import com.zelaux.betterdiagram.index.BTextures;
import com.zelaux.betterdiagram.struct.BAnimatedTexture;
import com.zelaux.betterdiagram.util.CenterMassCalculator;
import dev.simulated_team.simulated.content.entities.diagram.screen.DiagramButton;
import dev.simulated_team.simulated.index.SimGUITextures;
import net.createmod.catnip.theme.Color;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;

public class ListOverlay implements Renderable {
    private final DiagramButton button;

    public ListOverlay(DiagramButton button) {this.button = button;}

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        float progress = BalanceListSwitchAnimation.textureProgress();
        if(progress < 0) return;
        float hidingAlpha = 1 - BalanceListSwitchAnimation.hidingProgress();
        if(CenterMassCalculator.equals3(0, hidingAlpha)) return;
        int x = button.getX();
        int y = button.getY();
        BAnimatedTexture texture = BTextures.Anim.linesFill;
        Color white = new Color(1, 1, 1, hidingAlpha);
        texture.render(guiGraphics, x - 8, y - 8, white, (int) (progress * texture.framesAmount));

        guiGraphics.flush();

        progress = BalanceListSwitchAnimation.newIconAppearProgress();
        PoseStack pose = guiGraphics.pose();
        pose.pushPose();
        pose.translate(x, y, 0);
        pose.translate(8, 8, 0);
        pose.mulPose(Axis.ZP.rotationDegrees(45 / 2f));

        float scale = 3 - progress * 3 + 1;
        pose.scale(scale, scale, scale);
        pose.translate(-8, -8, 0);
        final float grayValue = 0x41/255f;
        SimGUITextures.DIAGRAM_ICON_FORCES.render(
            guiGraphics, 0, 0, new Color(grayValue, grayValue, grayValue, progress * hidingAlpha)
        );
        pose.popPose();
    }
}
