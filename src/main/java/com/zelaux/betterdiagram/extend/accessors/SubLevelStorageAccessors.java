package com.zelaux.betterdiagram.extend.accessors;

import dev.ryanhcode.sable.sublevel.storage.region.SubLevelRegionFile;
import net.minecraft.world.level.ChunkPos;

import java.io.IOException;

public interface SubLevelStorageAccessors {
    SubLevelRegionFile bcd$getRegionFile(ChunkPos chunkPos) ;
}
