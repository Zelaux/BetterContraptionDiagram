package com.zelaux.betterdiagram.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.zelaux.betterdiagram.extend.accessors.SubLevelStorageAccessors;
import com.zelaux.betterdiagram.mixin.accessors.server_helpers.SubLevelStorageFileAccessors;
import dev.ryanhcode.sable.api.command.SableCommandHelper;
import dev.ryanhcode.sable.api.physics.PhysicsPipeline;
import dev.ryanhcode.sable.api.sublevel.ServerSubLevelContainer;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.companion.math.BoundingBox3d;
import dev.ryanhcode.sable.companion.math.BoundingBox3dc;
import dev.ryanhcode.sable.companion.math.BoundingBox3i;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.storage.holding.SavedSubLevelPointer;
import dev.ryanhcode.sable.sublevel.storage.holding.SubLevelHoldingChunk;
import dev.ryanhcode.sable.sublevel.storage.holding.SubLevelHoldingChunkMap;
import dev.ryanhcode.sable.sublevel.storage.region.SubLevelRegionFile;
import dev.ryanhcode.sable.sublevel.storage.region.SubLevelStorageFile;
import dev.ryanhcode.sable.sublevel.storage.serialization.SubLevelData;
import dev.ryanhcode.sable.sublevel.storage.serialization.SubLevelStorage;
import dev.ryanhcode.sable.util.SableNBTUtils;
import lombok.SneakyThrows;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;

import java.io.File;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ServerHelpers {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext buildContext) {

        dispatcher.register(Commands
            .literal("sublevel")
                      .requires(x->x.hasPermission(2))
            .then(Commands
                .literal("resque")
                .executes(context -> {
                    return checkLoaded(context) + checkSaved(context);
                })
            )

        );
    }

    @SneakyThrows
    @SuppressWarnings("UnstableApiUsage")
    private static int checkLoaded(CommandContext<CommandSourceStack> context) {
        ServerLevel level = context.getSource().getLevel();

        final ServerSubLevelContainer container = SubLevelContainer.getContainer(level);
        assert container != null : "No sub-level container";
        int counter = 0;

        CommandSourceStack source = context.getSource();
        double y = source.getPosition().y;

        SableCommandHelper.requireSubLevelPhysicsSystem(context).setPaused(true);
        final PhysicsPipeline pipeline = SableCommandHelper.requireSubLevelPhysicsPipeline(context);
        for(ServerSubLevel subLevel : container.getAllSubLevels()) {
            if(!(subLevel.latestLinearVelocity.y < 0)) continue;
            ;
            Vector3d position = subLevel.logicalPose().position();
            if(!(position.y < level.getMinBuildHeight() - 64 - 40)) continue;
            position.y = y;

            pipeline.teleport(subLevel,new Vector3d(position),subLevel.logicalPose().orientation());
            pipeline.resetVelocity(subLevel);
            counter++;
        }
        if(counter > 0) {
            int counter0 = counter;
            source.sendSuccess(() -> Component.literal(counter0+" of loaded sublevels are above world"), true);
            source.sendSuccess(() -> Component.literal("Simulation paused").append(
                Component.literal("Click to get command to resume simulation")
                    .withStyle(s->s.withUnderlined(true)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,
                            "/sable paused false"
                        ))
                    )
            ), true);
            container.getHoldingChunkMap().saveAll();
        } else {
            SableCommandHelper.requireSubLevelPhysicsSystem(context).setPaused(false);
            source.sendSuccess(() -> Component.literal("All loaded sublevels are above world"), true);
        }


        return counter;
    }

    @SuppressWarnings("UnstableApiUsage")
    private static int checkSaved(CommandContext<CommandSourceStack> context) {
        ServerLevel level = context.getSource().getLevel();

        final ServerSubLevelContainer container = SubLevelContainer.getContainer(level);
        assert container != null : "No sub-level container";

        final SubLevelHoldingChunkMap holdingChunkMap = container.getHoldingChunkMap();
        SubLevelStorage storage = holdingChunkMap.getStorage();
        var chunks = collectChunkPos(storage).toList();
        if(chunks == null) return 0;
        SubLevelStorageAccessors accessors = (SubLevelStorageAccessors) storage;
        List<NestedChunkEntry> list = chunks
            .stream()
            .map(x -> of(x, accessors))
            .map(entry -> {
                BitSet bitSet = entry.accessors.usedIndices();
                ArrayList<ChunkPos> newChunk = new ArrayList<>();
                for(int i = bitSet.nextSetBit(0); i < entry.file.getTotalIndexCapacity(); i = bitSet.nextSetBit(i)) {
                    // return localX | (localZ << LOG_SIDE_LENGTH);
                    int z = i >> SubLevelRegionFile.LOG_SIDE_LENGTH;
                    int x = i & (-1 << SubLevelRegionFile.LOG_SIDE_LENGTH);
                    newChunk.add(new ChunkPos(
                        entry.chunkPos.x + x,
                        entry.chunkPos.z + z
                    ));
                }
                return new NestedChunkEntry(entry, newChunk);
            }).toList();
        Map<ChunkPos, SubLevelHoldingChunk> collected = list.stream()
                                                            .flatMap(x -> x.subchunks.stream())
                                                            .collect(Collectors.toMap(x -> x, storage::attemptLoadHoldingChunk));
        collected.forEach((chunkPos, holdingChunk) -> {

            Vector3d tmp = new Vector3d();
            for(final SavedSubLevelPointer pointer : holdingChunk.getSubLevelPointers()) {
                final SubLevelData data = storage.attemptLoadSubLevel(chunkPos, pointer);
                Vector3d position = data.pose().position();
                BoundingBox3d bounds = data.bounds();

                final BoundingBox3i chunkBounds = getChunkBounds(bounds);
                ;
                int middleChunk = (int) ((bounds.center(tmp).y) / 16);
                boolean shouldSave = false;
                if(!chunkBounds.contains(chunkPos.x, middleChunk, chunkPos.z)) {

                    //SableNBTUtils.readBoundingBox(tag.getCompound("world_bounds")),
                    tmp.set(chunkPos.getMiddleBlockX(), position.y, chunkPos.getMiddleBlockZ());
                    bounds.set(
                        tmp.x - 0.1, tmp.y - 0.1, tmp.z - 0.1,
                        tmp.x + 0.1, tmp.y + 0.1, tmp.z + 0.1
                    );
                    data.fullTag().put(
                        "world_bounds", SableNBTUtils.writeBoundingBox(bounds)
                    );
                    shouldSave = true;
                }

                if(position.y < level.getMinBuildHeight() - 64 - 40) {
                    //    SableNBTUtils.readPose3d(tag.getCompound("pose")),

                    position.y = context.getSource().getPosition().y;
                    data.fullTag().put(
                        "pose", SableNBTUtils.writePose3d(data.pose())
                    );
                    shouldSave = true;
                }


                if(shouldSave) {
                    context.getSource().sendSuccess(() -> Component.literal("Try to resque sublevel"), true);
                    storage.attemptSaveSubLevel(chunkPos, data);
                }
                //logFoundSubLevel(pointer, data, chunkPos, source, level);
            }
        });


        return 1;
    }

    private static @Nullable Stream<ChunkPos> collectChunkPos(SubLevelStorage storage) {
        final File[] regionFiles = storage.getFolder().toFile().listFiles((dir, name) -> name.startsWith("r.") && name.endsWith(SubLevelRegionFile.FILE_EXTENSION));

        if(regionFiles == null) return null;
        return Arrays
            .stream(regionFiles)
            .map(File::getName)
            //return "r." + entry.getRegionX() + "." + entry.getRegionZ();
            .map(x -> x.substring("r.".length()).split("\\."))
            .filter(x -> x.length == 2)
            .map(x -> new ChunkPos(Integer.parseInt(x[0]) << 5, Integer.parseInt(x[1]) << 5));
    }

    private static @NotNull ServerHelpers.SubLevelStorageEntry of(ChunkPos x, SubLevelStorageAccessors accessors) {
        SubLevelRegionFile file = accessors.bcd$getRegionFile(x);
        return new SubLevelStorageEntry(x, file, ((SubLevelStorageFileAccessors) file));
    }

    private static @NotNull BoundingBox3i getChunkBounds(BoundingBox3dc bounds) {
        final BoundingBox3i chunkBounds = new BoundingBox3i(
            Mth.floor(bounds.minX() - 1.0) >> 4,
            Mth.floor(bounds.minY() - 1.0) >> 4,
            Mth.floor(bounds.minZ() - 1.0) >> 4,
            Mth.floor(bounds.maxX() + 1.0) >> 4,
            Mth.floor(bounds.maxY() + 1.0) >> 4,
            Mth.floor(bounds.maxZ() + 1.0) >> 4
        );
        return chunkBounds;
    }

    static <A, B> ArrayList<B> map(ArrayList<A> a, Function<A, B> mapper) {
        ArrayList<B> bs = new ArrayList<>(a.size());
        for(A a1 : a) bs.add(mapper.apply(a1));
        return bs;
    }

    private static Object mapFile(File file) {return null;}

    record SubLevelStorageEntry(ChunkPos chunkPos, SubLevelStorageFile file, SubLevelStorageFileAccessors accessors) {}

    record NestedChunkEntry(@NotNull SubLevelStorageEntry entry, List<ChunkPos> subchunks) {}
}
