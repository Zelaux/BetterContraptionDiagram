package com.zelaux.betterdiagram.mixin.accessors;

import dev.simulated_team.simulated.content.blocks.swivel_bearing.link_block.SwivelBearingPlateBlockEntity;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.gen.*;

import java.util.UUID;

@Mixin(SwivelBearingPlateBlockEntity.class)
public interface ISwivelBearingPlateBlockEntity {
    @Accessor("parent")
    BlockPos bcd$parent();

    @Accessor("parentSubLevelId")
    UUID bcd$parentSubLevelId();

}
