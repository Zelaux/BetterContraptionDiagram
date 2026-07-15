package com.zelaux.betterdiagram.mixin.feature.max_speed;

import com.zelaux.betterdiagram.feature.max_speed.WithLiftProviders;
import dev.ryanhcode.sable.api.block.BlockEntitySubLevelActor;
import dev.ryanhcode.sable.api.block.BlockSubLevelLiftProvider;
import dev.ryanhcode.sable.companion.math.BoundingBox3ic;
import dev.ryanhcode.sable.sublevel.plot.ClientLevelPlot;
import dev.ryanhcode.sable.sublevel.plot.LevelPlot;
import dev.ryanhcode.sable.sublevel.plot.PlotChunkHolder;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

@Mixin(LevelPlot.class)
public class Sail_LevelPlot implements WithLiftProviders {
    @Unique
    private final Long2ObjectMap<BlockSubLevelLiftProvider.LiftProviderContext> bcd$liftProviders = new Long2ObjectOpenHashMap<>();

    @Override
    public Long2ObjectMap<BlockSubLevelLiftProvider.LiftProviderContext> bcd$liftProviders() {
        return bcd$liftProviders;
    }

    /**
     * Handles a change in block-state in the plot at global block position x, y, z.
     *
     * @param state the new block-state
     */
    @Inject(method = "onBlockChange", at = @At("TAIL"))
    private void onBlockChange(BlockPos pos, BlockState state, CallbackInfo ci) {
        //noinspection ConstantValue
        if(!((Object)this instanceof ClientLevelPlot)) return;
        addBlockList(new BlockPos(pos), state);
    }

    private void addBlockList(BlockPos pos, BlockState state) {
        bcd$liftProviders.remove(pos.asLong());

        if(state.getBlock() instanceof final BlockSubLevelLiftProvider prov) {
            bcd$liftProviders.put(pos.asLong(), new BlockSubLevelLiftProvider.LiftProviderContext(pos, state, Vec3.atLowerCornerOf(prov.sable$getNormal(state).getNormal())));
        }
    }

    @Inject(method = "addChunkHolder",at=@At("TAIL"))
    private void addChunkHolder(ChunkPos localChunkPos, PlotChunkHolder holder, boolean initializeLighting, CallbackInfo ci) {

        LevelChunk chunk = holder.getChunk();

        BoundingBox3ic boundingBox = holder.getBoundingBox();
        if(boundingBox==null){
            if(chunk.isEmpty())return;
            ((Sail_PlotChunkHolder_Acc) holder).bcd$buildBoundingBox();
            boundingBox=holder.getBoundingBox();
            if(boundingBox==null)return;
        }
        BlockPos.betweenClosedStream(boundingBox.toMojang()).forEachOrdered(blockPos -> {
            BlockState state = chunk.getBlockState(blockPos);
            if(state.isAir())return;
            addBlockList(chunk.getPos().getBlockAt(blockPos.getX(),blockPos.getY(),blockPos.getZ()),state);
        });
    }
}
