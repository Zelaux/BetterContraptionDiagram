package com.zelaux.betterdiagram.mixin.feature.balloon_volume_calculator;

import com.zelaux.betterdiagram.feature.balloon_volume_calculator.api.IBalloonCalc_ClientBalloon;
import dev.eriksonn.aeronautics.content.blocks.hot_air.BlockEntityLiftingGasProvider;
import dev.eriksonn.aeronautics.content.blocks.hot_air.balloon.Balloon;
import dev.eriksonn.aeronautics.content.blocks.hot_air.balloon.ClientBalloon;
import dev.eriksonn.aeronautics.content.blocks.hot_air.balloon.graph.BalloonLayerData;
import dev.eriksonn.aeronautics.content.blocks.hot_air.balloon.graph.BalloonLayerGraph;
import dev.ryanhcode.sable.api.SubLevelAssemblyHelper;
import dev.ryanhcode.sable.util.LevelAccelerator;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

import java.util.Iterator;
import java.util.List;

@Mixin(ClientBalloon.class)
public abstract class BalloonCalc_ClientBalloon_Mixin extends Balloon implements IBalloonCalc_ClientBalloon {

    @Unique
    private static final Vector3d bcd$POSITION_TEMP = new Vector3d();
    @Unique
    private final Vector3d bcd$averagePosition = new Vector3d();
    @Unique
    private final Vector3d bcd$physicsOrigin = new Vector3d();

    @Override
    public Vector3d bcd$getCenter(Vector3d dest) {
        return dest.set(bcd$averagePosition).add(bcd$physicsOrigin);
    }

    @Override
    public Vector3dc bcd$physicsOrigin() {
        return bcd$physicsOrigin;
    }

    @Override
    public Vector3dc bcd$averagePosition() {
        return bcd$averagePosition;
    }

    protected BalloonCalc_ClientBalloon_Mixin(Level level, LevelAccelerator accelerator, BlockPos controllerPos, BalloonLayerGraph graph, ObjectArrayList<BlockEntityLiftingGasProvider> heaters) {
        super(level, accelerator, controllerPos, graph, heaters);
    }

    @Inject(method = "onRemoved", at = @At("HEAD"))
    void onRemoved(CallbackInfo ci) {
        bcd$averagePosition.zero();
        for(final List<BalloonLayerData> layers : this.graph.getAllLayers()) {
            for(final BalloonLayerData layer : layers) {
                final Iterator<BlockPos> iter = layer.nonSolidBlockIterator();

                while(iter.hasNext()) {
                    final BlockPos pos = iter.next();

                    bcd$POSITION_TEMP.set(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5).sub(this.bcd$physicsOrigin);
                    this.bcd$averagePosition.add(bcd$POSITION_TEMP);
                }
            }
        }

        this.bcd$averagePosition.div(this.getCapacity());

    }


    @Inject(method = "<init>", at = @At("TAIL"))
    void onInit(Level level, LevelAccelerator accelerator, BlockPos controllerPos, BalloonLayerGraph graph, ObjectArrayList<?> heaters, CallbackInfo ci) {
        bcd$physicsOrigin.set(controllerPos.getX(), controllerPos.getY(), controllerPos.getZ());
    }

    @Override
    public void bcd$handle_moveController(BlockPos newControllerPos) {
        bcd$physicsOrigin.set(newControllerPos.getX(), newControllerPos.getY(), newControllerPos.getZ());
    }


    @Override
    public void bcd$handle_setAssembling_Tail(SubLevelAssemblyHelper.AssemblyTransform transform, BlockPos newControllerPos) {
        bcd$physicsOrigin.set(newControllerPos.getX(), newControllerPos.getY(), newControllerPos.getZ());
    }
}
