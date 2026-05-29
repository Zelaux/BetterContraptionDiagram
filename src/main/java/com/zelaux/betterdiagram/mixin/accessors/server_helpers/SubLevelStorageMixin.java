package com.zelaux.betterdiagram.mixin.accessors.server_helpers;

import com.zelaux.betterdiagram.extend.accessors.SubLevelStorageAccessors;
import dev.ryanhcode.sable.sublevel.storage.region.SubLevelRegionFile;
import dev.ryanhcode.sable.sublevel.storage.serialization.SubLevelStorage;
import lombok.SneakyThrows;
import net.minecraft.world.level.ChunkPos;
import org.spongepowered.asm.mixin.*;

import java.io.IOException;

@Mixin(SubLevelStorage.class)
public abstract class SubLevelStorageMixin implements SubLevelStorageAccessors {
    @Shadow
    protected abstract SubLevelRegionFile getRegionFile(ChunkPos chunkPos) throws IOException;

    @Override
    @SneakyThrows
    public SubLevelRegionFile bcd$getRegionFile(ChunkPos chunkPos) {
        return getRegionFile(chunkPos);
    }

}
