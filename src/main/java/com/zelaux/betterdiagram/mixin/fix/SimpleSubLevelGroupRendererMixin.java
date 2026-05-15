package com.zelaux.betterdiagram.mixin.fix;

import dev.ryanhcode.sable.sublevel.ClientSubLevel;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.simulated_team.simulated.Simulated;
import dev.simulated_team.simulated.util.SimpleSubLevelGroupRenderer;
import foundry.veil.api.client.render.framebuffer.AdvancedFbo;
import foundry.veil.api.client.render.framebuffer.FramebufferStack;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3d;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

import java.util.Collection;

import static com.zelaux.betterdiagram.Content.simulated$bufferLocation;

@Mixin(SimpleSubLevelGroupRenderer.class)
public class SimpleSubLevelGroupRendererMixin {
    @Inject(method = "renderGroup",at= @At(value = "INVOKE", target = "Lfoundry/veil/api/client/render/framebuffer/AdvancedFbo;unbind()V"))
    private static void x(ClientLevel level, Collection<ClientSubLevel> subLevels, AdvancedFbo fbo, Matrix4f modelView, Matrix4f projectionMat, Vector3d cameraPosition, Quaternionf orientation, float partialTicks, boolean renderPlayers, CallbackInfo ci){
        FramebufferStack.pop(simulated$bufferLocation);
    }
}
