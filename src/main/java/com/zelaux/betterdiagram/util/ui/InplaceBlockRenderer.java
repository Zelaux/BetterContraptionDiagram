package com.zelaux.betterdiagram.util.ui;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.createmod.ponder.api.level.PonderLevel;
import net.createmod.ponder.render.VirtualRenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.lang.ref.WeakReference;

public class InplaceBlockRenderer {

    public static class Container {
        private BlockEntity myEntity;
        private PonderLevel myLevel;
        private WeakReference<Level> myHolderLevel;

        public BlockEntity getOrCreate(BlockState state, Level level) {
            if(myEntity != null && myEntity.getBlockState() == state && !myEntity.isRemoved() && myHolderLevel != null && myHolderLevel.get() != null) {
                return myEntity;
            }
            myEntity = null;
            if(state.getBlock() instanceof EntityBlock entityBlock) {
                BlockEntity entity = myEntity = entityBlock.newBlockEntity(BlockPos.ZERO, state);
                if(entity != null) {
                    entity.setLevel(level(level));

                }
                return entity;
            }
            return null;
        }

        private PonderLevel level(Level level) {
            if(myLevel != null && myHolderLevel != null && myHolderLevel.get() == level) return myLevel;
            if(myHolderLevel != null) myHolderLevel.clear();
            myHolderLevel = new WeakReference<>(level);
            return myLevel = new PonderLevel(BlockPos.ZERO, level);
        }

        public void clear() {
            myEntity = null;
            myLevel = null;
            if(myHolderLevel != null) myHolderLevel.clear();
        }
    }

    public static void renderInplace(GuiGraphics graphics, BlockState state, Container container, int entityX, int entityY, float partialTick, Level level, int scale) {
        PoseStack ms = graphics.pose();
        ms.pushPose();
        ms.translate(entityX, entityY, -0);
        ms.mulPose(Axis.XP.rotationDegrees(-30f));
        ms.mulPose(Axis.YP.rotationDegrees(-45));
        //ms.scale(48, -48, 48);
        ms.scale(scale, -scale, scale);

        Lighting.setupForEntityInInventory();

        xx(graphics, state, container.getOrCreate(state, level), partialTick);

        //VertexConsumer cutout = graphics.bufferSource().getBuffer(RenderType.cutoutMipped());
        Lighting.setupFor3DItems();
        ms.popPose();
    }

    static void xx(GuiGraphics guiGraphics, BlockState state, BlockEntity orCreate, float partialTick) {
        Lighting.setupForEntityInInventory();
        PoseStack pose = guiGraphics.pose();
        pose.pushPose();
        {
            var renderer = Minecraft.getInstance().getBlockRenderer();

            RenderSystem.runAsFancy(() -> renderer.renderSingleBlock(
                state, pose, guiGraphics.bufferSource(),
                LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY,
                VirtualRenderHelper.VIRTUAL_DATA, null

            ));
        }
        if(orCreate != null) {
            var renderer = Minecraft.getInstance().getBlockEntityRenderDispatcher();

            ;
            BlockEntityRenderer<BlockEntity> renderer1 = renderer.getRenderer(orCreate);
            if(renderer1 != null)
                RenderSystem.runAsFancy(() -> renderer1.render(orCreate, partialTick, pose, guiGraphics.bufferSource(),
                    LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY)
                );
        }
        guiGraphics.flush();
        pose.popPose();
        Lighting.setupFor3DItems();
    }
}
