package com.zelaux.betterdiagram.gui.comp;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.zelaux.betterdiagram.util.ui.InplaceBlockRenderer;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix4f;

public class InTextBlockRenderer implements ClientTooltipComponent, WrappedTooltipComponent.Wrappable {
    public final InplaceBlockRenderer.WorldContainer worldContainer = new InplaceBlockRenderer.WorldContainer();
    public BlockState state;

    public InTextBlockRenderer(BlockState state) {
        this.state = state;
    }

    @Override
    public int getHeight() {
        return 20;
    }

    @Override
    public int getWidth(Font font) {
        return 20;
    }

    @Override
    public void renderText(Font font, int mouseX, int mouseY, Matrix4f matrix, MultiBufferSource.BufferSource bufferSource) {
        //ClientTooltipComponent.super.renderText(font, mouseX, mouseY, matrix, bufferSource);
        GuiGraphics graphics = new GuiGraphics(Minecraft.getInstance(), bufferSource);
        graphics.flush();
        RenderSystem.enableDepthTest();
        PoseStack pose = graphics.pose();
        pose.pushPose();
        pose.setIdentity();
        pose.mulPose(matrix);
        renderBlock(mouseX, mouseY, graphics);
        pose.popPose();
        RenderSystem.disableDepthTest();

    }

    @Override
    public void renderImage(Font font, int x, int y, GuiGraphics guiGraphics) {
        //renderBlock(x, y, guiGraphics);
    }

    public void renderBlock(int x, int y, GuiGraphics guiGraphics) {
        int h = getHeight();
        int w = getWidth(null);
        InplaceBlockRenderer.renderInplace(guiGraphics,
            state, worldContainer,
            x + w / 2, y + h / 2,
            AnimationTickHolder.getPartialTicks(),
            Minecraft.getInstance().level, 12
        );
    }
}
