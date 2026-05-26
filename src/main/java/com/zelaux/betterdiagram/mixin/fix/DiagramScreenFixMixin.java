package com.zelaux.betterdiagram.mixin.fix;

import com.zelaux.betterdiagram.Content;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.simulated_team.simulated.content.entities.diagram.screen.DiagramScreen;
import foundry.veil.api.client.render.framebuffer.AdvancedFbo;
import foundry.veil.api.client.render.framebuffer.FramebufferStack;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3d;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

@Mixin(DiagramScreen.class)
public class DiagramScreenFixMixin {
    @Inject(method = "draw", at = @At("HEAD"))
    private static void x(SubLevel subLevel, float partialTicks, Quaternionf localOrientation, Matrix4f projMatrix, Vector3d cameraPos, float inWidth, float inHeight, AdvancedFbo fbo, AdvancedFbo outlineFbo, AdvancedFbo finalFbo, float paletteOffset, float fadeScale, int lineColor, int lineShadowColor, CallbackInfo ci) {
        FramebufferStack.push(Content.simulated$bufferLocation);
    }

    @Inject(method = "draw", at = @At("TAIL"))
    private static void x1(SubLevel subLevel, float partialTicks, Quaternionf localOrientation, Matrix4f projMatrix, Vector3d cameraPos, float inWidth, float inHeight, AdvancedFbo fbo, AdvancedFbo outlineFbo, AdvancedFbo finalFbo, float paletteOffset, float fadeScale, int lineColor, int lineShadowColor, CallbackInfo ci) {
        FramebufferStack.pop(Content.simulated$bufferLocation);
    }
}
