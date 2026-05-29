package com.zelaux.betterdiagram.mixin.accessors.server_helpers;

import dev.ryanhcode.sable.sublevel.storage.region.SubLevelStorageFile;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.gen.*;

import java.nio.IntBuffer;
import java.util.BitSet;

@Mixin(SubLevelStorageFile.class)
public interface SubLevelStorageFileAccessors {
    @Accessor("sectorSpans")
    IntBuffer sectorSpans();
    @Accessor("usedIndices")
    BitSet usedIndices();
    @Accessor("usedSectors")
    BitSet usedSectors();
}
