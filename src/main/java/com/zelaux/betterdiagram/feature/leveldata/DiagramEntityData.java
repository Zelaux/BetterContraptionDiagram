package com.zelaux.betterdiagram.feature.leveldata;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.zelaux.betterdiagram.BetterContraptionDiagramClient;
import com.zelaux.betterdiagram.Config;
import com.zelaux.betterdiagram.data.BCDData;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;


public class DiagramEntityData {
    public static final DiagramEntityData NULL = new DiagramEntityData(null, null);
    public static final Codec<Optional<Map<UUID, DataEntry>>> MAP_CODEC =
        Codec.dispatchedMap(UUIDUtil.STRING_CODEC, k -> DataEntry.CODEC).optionalFieldOf("dataMap")
             .codec();
    static final Cache<Level, Cache<SaveSubLocation, DiagramEntityData>> CACHE =
        CacheBuilder.newBuilder()
                    .weakKeys()
                    .<Level, Cache<SaveSubLocation, DiagramEntityData>>removalListener(notification -> {
                        var value = notification.getValue();
                        if(value == null || value.size() == 0) return;
                        value.invalidateAll();
                    })
                    .build();
    private static Callable<Cache<SaveSubLocation, DiagramEntityData>> SubCacheMaker = () ->
        CacheBuilder.newBuilder()
                    .<SaveSubLocation, DiagramEntityData>removalListener(it -> {
                        try {
                            it.getValue().trySave();
                        } catch(Exception e) {
                            BetterContraptionDiagramClient.LOGGER.error("Failed to save diagram entity info", e);
                        }
                    }).build();

    private final Object LOCK = new Object();
    private final Path path;
    private final LevelData levelData;
    /**
     * Null only of in {@link DiagramEntityData#NULL}
     *
     */
    @Nullable
    public final ConcurrentHashMap<UUID, @Nullable DataEntry> dataMap;
    private volatile boolean updated = false;

    @SneakyThrows
    private DiagramEntityData(Path path, LevelData levelData) {
        this.path = path;
        this.levelData = levelData;
        if(NULL != null) Objects.requireNonNull(path, "path cannot be null");
        if(path == null) {
            dataMap = null;
            return;
        }
        File file = path.toFile();
        if(!file.exists()) {
            dataMap = new ConcurrentHashMap<>();
            return;

        }

        CompoundTag compoundTag = NbtIo.readCompressed(path, NbtAccounter.create(20 * 1024 * 1024));
        int version = compoundTag.getInt("version");
        if(version != 0) {
            dataMap = new ConcurrentHashMap<>();
            return;
        }
        var map = MAP_CODEC.decode(NbtOps.INSTANCE, compoundTag);
        dataMap = map.result()
                     .map(Pair::getFirst)
                     .flatMap(Function.identity())
                     .map(ConcurrentHashMap::new)
                     .orElseGet(ConcurrentHashMap::new);
    }


    @SneakyThrows
    public static DiagramEntityData get(Level level,@Nullable DiagramEntityData.SaveSubLocation saveSubLocation) {
        LevelData levelData = LevelDatas.levelData();
        if(levelData == null) return NULL;

        return CACHE.get(level, SubCacheMaker).get(saveSubLocation, () -> {
            try {
                Path path = levelData.locateLevelDirectory(level).resolve("diagrams");
                path = saveSubLocation.wrap(path);
                return new DiagramEntityData(path.resolveSibling(path.getFileName()+".bat"), levelData);
            } catch(Exception e) {
                BetterContraptionDiagramClient.LOGGER.error("Cannot create diagram entity data", e);
                return NULL;
            }
        });

    }

    public static void saveAll(Iterable<? extends Level> allLevels) {
        for(var level : allLevels) {
            var present = CACHE.getIfPresent(level);
            if(present == null) continue;
            present.asMap().values().forEach(DiagramEntityData::trySave);
        }
    }

    @SneakyThrows
    public void trySave() {
        if(dataMap == null) return;
        boolean updated1 = updated;
        if(!updated1) return;
        synchronized(LOCK) {
            updated1 = updated;
            if(!updated1) return;
            updated = false;
            CompoundTag tag = new CompoundTag();
            tag.putInt("version", 0);
            Tag result = MAP_CODEC.encode(Optional.of(dataMap), NbtOps.INSTANCE, tag).getOrThrow();
            if(result instanceof CompoundTag compoundTag) {
                Files.createDirectories(path.getParent());
                NbtIo.writeCompressed(compoundTag, path);
            }
        }

    }

    public BCDData locateDataOrNull(UUID uuid) {
        if(dataMap == null || dataMap.isEmpty()) return null;
        return unpack(dataMap.computeIfPresent(uuid, (u, exits) -> {
            updated = true;
            exits.creationTick(levelData.currentTick);
            return exits;
        }));
    }

    private static @Nullable BCDData unpack(@Nullable DataEntry entry) {
        return entry == null ? null : entry.data();
    }

    @Nullable
    public BCDData saveData(UUID uuid, @Nullable BCDData data) {
        if(dataMap == null || (data == null || data.equals(BCDData.DEFAULT_VALUE)) && dataMap.isEmpty()) return data;
        int size = dataMap.size();
        DataEntry compute = dataMap.compute(uuid, (it, old) -> {
            if(old != null || data != null) this.updated = true;
            return data == null || data.equals(BCDData.DEFAULT_VALUE) ? null :
                Objects.requireNonNullElseGet(old, DataEntry::new).set(data, levelData.currentTick)
                ;
        });
        int maxPerLevel = Config.MAX_DIAGRAM_STORED_PER_LEVEL.getAsInt();
        if(dataMap.size() > size && size >= maxPerLevel) {
            long currentTick = levelData.currentTick;
            UUID[] deleteCandidates = dataMap
                .entrySet().stream()
                .sorted(Comparator.comparingLong(x -> x.getValue().creationTick() - currentTick))
                .limit(size + 1 - maxPerLevel)
                .map(Map.Entry::getKey)
                .toArray(UUID[]::new);
            for(UUID deleteCandidate : deleteCandidates) {
                dataMap.remove(deleteCandidate);
            }
        }
        return unpack(compute);
    }

    @AllArgsConstructor
    public enum SaveSubLocation {
        None(""),
        GadgetsOverlay("_gadgets"),
        ;
        public final String postfix;

        public Path wrap(Path path) {
            return path.resolveSibling(path.getFileName() + postfix);
        }
    }
}
