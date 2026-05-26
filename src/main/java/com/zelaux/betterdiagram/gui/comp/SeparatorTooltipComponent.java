package com.zelaux.betterdiagram.gui.comp;

import com.mojang.blaze3d.vertex.PoseStack;
import net.createmod.catnip.theme.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import org.joml.Matrix4f;

public class SeparatorTooltipComponent implements ListenerClientTooltipComponent,WrappedTooltipComponent.Wrappable {
    private int width;
    private int borderColorStart;
    private int height;
    private int tooltipY;
    private int borderColorEnd;

    @Override
    public int getHeight() {
        return 4;
    }

    @Override
    public int getWidth(Font font) {
        return 0;
    }

    @Override
    public void beforeRender(GuiGraphics graphics, int tooltipX, int tooltipY, int width, int height, int borderColorStart, int borderColorEnd, int backgroundColor) {
        this.width = width;
        this.height = height;
        this.tooltipY = tooltipY;
        this.borderColorStart = borderColorStart;
        this.borderColorEnd = borderColorEnd;
    }

    @Override
    public void renderText(Font font, int mouseX, int mouseY, Matrix4f matrix, MultiBufferSource.BufferSource bufferSource) {
        if(width==0 || height==0)return;
        GuiGraphics guiGraphics = new GuiGraphics(Minecraft.getInstance(), bufferSource);
        PoseStack pose = guiGraphics.pose();
        pose.pushPose();
        pose.setIdentity();
        pose.mulPose(matrix);

        int color = Color.mixColors(borderColorStart, borderColorEnd, (mouseY - tooltipY) / (float) height);
        guiGraphics.hLine(mouseX - 3, mouseX + width + 2, mouseY,
            color
        );

        pose.popPose();
    }
}
