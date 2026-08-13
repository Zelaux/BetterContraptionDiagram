package com.zelaux.betterdiagram.extend;

import com.zelaux.betterdiagram.mixin_plugin.postprocessors.VanilaProcessorProvider;
import dev.ryanhcode.sable.sublevel.SubLevel;
import org.jetbrains.annotations.NotNull;

/**
 * @see com.zelaux.betterdiagram.mixin_plugin.BetterContraptionDiagramCompatMixinPlugin
 * @see VanilaProcessorProvider
 * */
public interface IRopeStrandHolderBlockEntity {

    Iterable<@NotNull SubLevel> bcd$sable$getConnectionDependencies();
}
