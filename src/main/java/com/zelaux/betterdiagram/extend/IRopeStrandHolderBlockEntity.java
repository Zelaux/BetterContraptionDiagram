package com.zelaux.betterdiagram.extend;

import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.simulated_team.simulated.content.blocks.rope.RopeStrandHolderBlockEntity;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.*;

/**
 * @see com.zelaux.betterdiagram.mixin_plugin.BetterContraptionDiagramCompatMixinPlugin
 * @see com.zelaux.betterdiagram.mixin_plugin.postprocessors.VanilaPostprocessor
 * */
public interface IRopeStrandHolderBlockEntity {

    Iterable<@NotNull SubLevel> bcd$sable$getConnectionDependencies();
}
