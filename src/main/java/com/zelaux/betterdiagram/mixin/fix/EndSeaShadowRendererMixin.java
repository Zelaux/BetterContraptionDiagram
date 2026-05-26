package com.zelaux.betterdiagram.mixin.fix;

import dev.simulated_team.simulated.content.end_sea.EndSeaShadowRenderer;
import foundry.veil.api.client.render.MatrixStack;
import foundry.veil.api.client.render.framebuffer.FramebufferStack;
import foundry.veil.api.event.VeilRenderLevelStageEvent;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import org.joml.Matrix4fc;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

import static com.zelaux.betterdiagram.Content.simulated$bufferLocation;

@Mixin(EndSeaShadowRenderer.class)
public class EndSeaShadowRendererMixin {
    @Inject(method = "renderShadowMap", at = @At(value = "INVOKE", target = "Lfoundry/veil/api/client/render/framebuffer/AdvancedFbo;bind(Z)V", shift = At.Shift.BEFORE))
    private static void x(VeilRenderLevelStageEvent.Stage stage, LevelRenderer levelRenderer, MultiBufferSource.BufferSource bufferSource, MatrixStack matrixStack, Matrix4fc frustumMatrix, Matrix4fc projectionMatrix, int renderTick, DeltaTracker deltaTracker, Camera camera, Frustum frustum, CallbackInfo ci) {
        FramebufferStack.push(simulated$bufferLocation);
    }
}
