package com.zelaux.betterdiagram.mixin.feature.balloon_volume_calculator;

import com.zelaux.betterdiagram.feature.balloon_volume_calculator.BalloonIndexUpdate;
import com.zelaux.betterdiagram.feature.balloon_volume_calculator.api.IBalloonCalc_ClientBalloon;
import dev.eriksonn.aeronautics.content.blocks.hot_air.balloon.Balloon;
import dev.eriksonn.aeronautics.content.blocks.hot_air.balloon.graph.BalloonLayerGraph;
import dev.ryanhcode.sable.api.SubLevelAssemblyHelper;
import dev.ryanhcode.sable.util.LevelAccelerator;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

@Mixin(Balloon.class)
public class BalloonCalc_Balloon_Mixin {
    @Shadow
    protected BlockPos controllerPos;

    @Inject(method = "<init>", at = @At("TAIL"))
    void on_init(Level level, LevelAccelerator accelerator, BlockPos controllerPos, BalloonLayerGraph graph, ObjectArrayList heaters, CallbackInfo ci) {
        BalloonIndexUpdate.ticket((Balloon) (Object) this, null);
    }

    @Inject(method = "moveController", at = @At("HEAD"))
    void on_move(BlockPos newControllerPos, CallbackInfo ci) {
        if(this.controllerPos.equals(newControllerPos)) return;
        BalloonIndexUpdate.ticket((Balloon) (Object) this, controllerPos);
    }
    @Inject(method = "setAssembling", at = @At("HEAD"))
    void on_assemble(SubLevelAssemblyHelper.AssemblyTransform transform, CallbackInfo ci) {
        BalloonIndexUpdate.ticket((Balloon) (Object) this, controllerPos);
    }

}
