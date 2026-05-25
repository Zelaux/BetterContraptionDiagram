package com.zelaux.betterdiagram.util.ui;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.simibubi.create.content.schematics.client.SchematicRenderer;
import net.createmod.catnip.levelWrappers.SchematicLevel;
import net.createmod.catnip.render.DefaultSuperRenderTypeBuffer;
import net.createmod.catnip.render.SuperRenderTypeBuffer;
import net.createmod.ponder.api.level.PonderLevel;
import net.createmod.ponder.render.VirtualRenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.util.Lazy;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;

public class InplaceBlockRenderer {

    public static class WorldContainer {
        private BlockEntity myEntity;
        private PonderLevel myLevel;
        private WeakReference<Level> myHolderLevel;
        private final Lazy<SchematicRenderer> myLevelRenderer = Lazy.of(() -> myLevel == null ? null : new SchematicRenderer(myLevel));

        public SchematicRenderer levelRenderer() {
            return myLevelRenderer.get();
        }

        @Nullable
        public SchematicLevel level() {
            return myLevel;
        }

        public BlockEntity getOrCreate(BlockState state, Level level) {
            if(myEntity != null && myEntity.getBlockState() == state && !myEntity.isRemoved() && myHolderLevel != null && myHolderLevel.get() != null) {
                return myEntity;
            }
            myEntity = null;
            if(myLevel != null) myLevel.restore();
            myLevelRenderer.invalidate();
            PonderLevel myLevel = level(level);
            myLevel.restore();


            myLevel.setBlock(BlockPos.ZERO, state, Block.UPDATE_NONE);
            myEntity = myLevel.getBlockEntity(BlockPos.ZERO);
            return myEntity;
        }

        private PonderLevel level(Level originalLevel) {
            if(myLevel != null && myHolderLevel != null && myHolderLevel.get() == originalLevel) return myLevel;
            if(myHolderLevel != null) myHolderLevel.clear();
            myLevelRenderer.invalidate();
            myHolderLevel = new WeakReference<>(originalLevel);
            var level = myLevel = new PonderLevel(BlockPos.ZERO, originalLevel);
            level.pushFakeLight(15);
            level.createBackup();
            return level;
        }

        public void clear() {
            myEntity = null;
            myLevel = null;
            if(myHolderLevel != null) myHolderLevel.clear();
        }

        public void updateBlockstateIfMatch(BlockState state) {
            if(myEntity == null) return;
            if(myEntity.getBlockState().getBlock() == state.getBlock()) {
                myEntity.setBlockState(state);
            }
        }
    }

    public static void renderInplace(GuiGraphics graphics, BlockState state, WorldContainer worldContainer, int entityX, int entityY, float partialTick, Level level, int scale) {
        PoseStack ms = graphics.pose();
        ms.pushPose();
        ms.translate(entityX, entityY, -0);
        ms.mulPose(Axis.XP.rotationDegrees(-30f));
        ms.mulPose(Axis.YP.rotationDegrees(-45));
        //ms.scale(48, -48, 48);
        ms.scale(scale, -scale, scale);

        //Lighting.setupForEntityInInventory();
        Lighting.setupLevel();


        BlockEntity orCreate = worldContainer.getOrCreate(state, level);
        xx(graphics, state,orCreate, worldContainer.levelRenderer(), partialTick);

        //VertexConsumer cutout = graphics.bufferSource().getBuffer(RenderType.cutoutMipped());
        Lighting.setupFor3DItems();
        ms.popPose();
    }

    static void xx(GuiGraphics guiGraphics, BlockState state, BlockEntity orCreate, SchematicRenderer schematicRenderer, float partialTick) {
        Lighting.setupForEntityInInventory();
        PoseStack pose = guiGraphics.pose();
        pose.pushPose();
        if(false){
            var renderer = Minecraft.getInstance().getBlockRenderer();

            RenderSystem.enableCull();
            RenderSystem.runAsFancy(() -> renderer.renderSingleBlock(
                state, pose, guiGraphics.bufferSource(),
                LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY,
                VirtualRenderHelper.VIRTUAL_DATA, null

            ));
        }
        if(schematicRenderer!=null){
            //RenderSystem.enableCull();
            Lighting.setupLevel();
            //RenderSystem.enableDepthTest();
            //RenderSystem.enableColorLogicOp();
            //RenderSystem.enableBlend();
            DefaultSuperRenderTypeBuffer instance = DefaultSuperRenderTypeBuffer.getInstance();
            schematicRenderer.render(pose, instance);

            instance.draw();
            //RenderSystem.enableCull();
        }else if(orCreate != null) {
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
