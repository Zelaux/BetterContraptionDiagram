package com.zelaux.betterdiagram.mixin.accessors;

import dev.simulated_team.simulated.content.blocks.docking_connector.DockingConnectorBlockEntity;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.gen.*;

@Mixin(DockingConnectorBlockEntity.class)
public interface IDockingConnectorBlockEntity {

    @Accessor("state")
    DockingConnectorBlockEntity.DockingConnectorState bcd$state();

}
