package com.zelaux.betterdiagram.feature.balloon_volume_calculator.api;

import dev.ryanhcode.sable.api.SubLevelAssemblyHelper;
import net.minecraft.core.BlockPos;
import org.joml.Vector3d;
import org.joml.Vector3dc;

public interface IBalloonCalc_ClientBalloon {

    Vector3dc bcd$averagePosition();

    Vector3dc bcd$physicsOrigin();
    Vector3d bcd$getCenter(Vector3d dest);

    void bcd$handle_moveController(BlockPos newControllerPos);
    void bcd$handle_setAssembling_Tail(SubLevelAssemblyHelper.AssemblyTransform transform, BlockPos apply);

}
