package com.zelaux.betterdiagram.mixin.feature.extra_info_deliver;

import dev.ryanhcode.sable.physics.floating_block.FloatingBlockController;
import dev.ryanhcode.sable.physics.floating_block.FloatingClusterContainer;
import org.joml.Matrix3d;
import org.joml.Vector3dc;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.gen.*;

import java.util.List;

@Mixin(FloatingBlockController.class)
public interface FloatingBlockControllerExt {
    @Accessor("containers")
    List<FloatingClusterContainer> bcd$containers();
    @Invoker("getGravityMatrix")
     Matrix3d bcd$getGravityMatrix(final Vector3dc g, final double verticalDrag, final double horizontalDrag, final Matrix3d target);
}
