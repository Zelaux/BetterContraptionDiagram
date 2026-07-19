package com.zelaux.betterdiagram.mixin.feature.easy_balloon_access;

import com.zelaux.betterdiagram.mixin.feature.max_speed.Sail_PlotChunkHolder_Acc;
import dev.eriksonn.aeronautics.content.blocks.hot_air.BlockEntityLiftingGasProvider;
import dev.ryanhcode.sable.api.block.BlockEntitySubLevelActor;
import dev.ryanhcode.sable.api.block.BlockSubLevelLiftProvider;
import dev.ryanhcode.sable.companion.math.BoundingBox3ic;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.ryanhcode.sable.sublevel.plot.LevelPlot;
import dev.ryanhcode.sable.sublevel.plot.PlotChunkHolder;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

@Mixin(LevelPlot.class)
public abstract class StoreBlockEntityLiftingGasProvider_LevelPlot {
    @Shadow
    public abstract Iterable<BlockEntitySubLevelActor> getBlockEntityActors();

    @Shadow
    @Final
    private @NotNull SubLevel subLevel;
    @Unique
    private final Long2ObjectMap<BlockEntityLiftingGasProvider> bcd$blockEntityLiftingGasProviders = new Long2ObjectOpenHashMap<>();
    /**
     * Handles a change in block-state in the plot at global block position x, y, z.
     *
     * @param state the new block-state
     */
    @Inject(method = "onBlockChange", at = @At("TAIL"))
    private void onBlockChange(BlockPos pos, BlockState state, CallbackInfo ci) {
        bcd$handleBlockPos(new BlockPos(pos), state);
    }

    private void bcd$handleBlockPos(BlockPos pos, BlockState state) {
        bcd$blockEntityLiftingGasProviders.remove(pos.asLong());

        if(state.getBlock() instanceof EntityBlock) {
            if(subLevel.getLevel().getBlockEntity(pos)  instanceof final BlockEntityLiftingGasProvider prov) {
                bcd$blockEntityLiftingGasProviders.put(pos.asLong(), prov);
            }
        }
    }

    @Inject(method = "addChunkHolder", at = @At("TAIL"))
    private void addChunkHolder(ChunkPos localChunkPos, PlotChunkHolder holder, boolean initializeLighting, CallbackInfo ci) {

        LevelChunk chunk = holder.getChunk();

        BoundingBox3ic boundingBox = holder.getBoundingBox();
        if(boundingBox == null) {
            if(chunk.isEmpty()) return;
            ((Sail_PlotChunkHolder_Acc) holder).bcd$buildBoundingBox();
            boundingBox = holder.getBoundingBox();
            if(boundingBox == null) return;
        }
        BlockPos.betweenClosedStream(boundingBox.toMojang()).forEachOrdered(blockPos -> {
            BlockState state = chunk.getBlockState(blockPos);
            if(state.isAir()) return;
            bcd$handleBlockPos(chunk.getPos().getBlockAt(blockPos.getX(), blockPos.getY(), blockPos.getZ()), state);
        });
    }
}
