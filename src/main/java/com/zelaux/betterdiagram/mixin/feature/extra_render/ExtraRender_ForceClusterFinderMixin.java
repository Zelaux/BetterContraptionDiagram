package com.zelaux.betterdiagram.mixin.feature.extra_render;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.zelaux.betterdiagram.gui.screen.diagram_ext.CustomRenderedForce_Vector3d;
import dev.simulated_team.simulated.content.entities.diagram.screen.ForceClusterFinder;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(ForceClusterFinder.class)
public class ExtraRender_ForceClusterFinderMixin {
    @WrapOperation(method = "passThrough", at = @At(value = "NEW", target = "(Lorg/joml/Vector3dc;)Lorg/joml/Vector3d;"))
    private static Vector3d passThrough(Vector3dc v, Operation<Vector3d> original) {
        if(v instanceof CustomRenderedForce_Vector3d custom) {
            return custom;
        }
        return original.call(v);
    }

}
