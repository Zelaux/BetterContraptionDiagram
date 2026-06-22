package com.zelaux.betterdiagram.mixin.accessors;

import dev.simulated_team.simulated.content.blocks.docking_connector.DockingConnectorPair;
import dev.simulated_team.simulated.content.blocks.redstone_magnet.SimMagnet;
import org.joml.Quaterniond;
import org.joml.Vector3d;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.gen.*;

@Mixin(DockingConnectorPair.class)
public interface IDockingConnectorPair {
    @Invoker("getRelativeOrientationDifference")
    Vector3d bcd$getRelativeOrientationDifference(final SimMagnet magnet1, final SimMagnet magnet2, final Vector3d t);

    @Accessor("target")
    Quaterniond bcd$target();
}
