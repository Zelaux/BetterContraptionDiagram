package com.zelaux.betterdiagram.mixin.COM_view;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.zelaux.betterdiagram.util.MixinCalculatorUtil;
import dev.ryanhcode.sable.mixinhelpers.block_outline_render.SubLevelCamera;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {

    private final @Unique SubLevelCamera bcd$sable$sublevelCamera = new SubLevelCamera();
    private final @Unique Quaternionf bcd$sable$orientationStorage = new Quaternionf();

    @Shadow
    private ClientLevel level;

    @Shadow
    protected abstract void renderHitOutline(PoseStack poseStack, VertexConsumer consumer, Entity entity, double camX, double camY, double camZ, BlockPos pos, BlockState state);

    @Inject(method = "renderLevel", at = @At("TAIL"))
    private void renderLevel(final DeltaTracker deltaTracker, final boolean bl, final Camera camera, final GameRenderer gameRenderer, final LightTexture lightTexture, final Matrix4f matrix4f, final Matrix4f matrix4f2, final CallbackInfo ci) {

        ClientLevel level1 = level;
        MixinCalculatorUtil.renderLevel(deltaTracker,
            bl,
            camera,
            gameRenderer,
            lightTexture,
            matrix4f,
            matrix4f2,
            ci,
            level1,
            bcd$sable$sublevelCamera,
            bcd$sable$orientationStorage);
    }

}