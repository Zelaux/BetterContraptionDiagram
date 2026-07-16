package com.zelaux.betterdiagram.mixin.feature.client_mass_tracker;

import com.zelaux.betterdiagram.feature.client_mass_tracker.MassTrackerLevelPlotI;
import com.zelaux.betterdiagram.mixin.feature.max_speed.Sail_PlotChunkHolder_Acc;
import com.zelaux.betterdiagram.network.features.ModFeature;
import dev.ryanhcode.sable.api.physics.mass.MassTracker;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.companion.math.BoundingBox3ic;
import dev.ryanhcode.sable.physics.config.block_properties.PhysicsBlockPropertyHelper;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.ryanhcode.sable.sublevel.plot.ClientLevelPlot;
import dev.ryanhcode.sable.sublevel.plot.LevelPlot;
import dev.ryanhcode.sable.sublevel.plot.PlotChunkHolder;
import dev.ryanhcode.sable.util.LevelAccelerator;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

@Mixin(LevelPlot.class)
public abstract class MassTracker_LevelPlot implements MassTrackerLevelPlotI {
    protected MassTracker_LevelPlot() {
        bcd$shouldTrackMass = isClient() && (!ModFeature.SHARE_MASS_TRACKER.supportsOnServer());
    }

    @Shadow
    public abstract SubLevel getSubLevel();

    @Shadow
    @Final
    private @NotNull SubLevel subLevel;
    @Unique
    MassTracker bcd$massTracker;
    @Unique
    final boolean bcd$shouldTrackMass;


    @Override
    public MassTracker bcd$massTracker() {
        return bcd$massTracker;
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    void x(SubLevelContainer container, int x, int z, int logSize, SubLevel subLevel, CallbackInfo ci) {
        if(bcd$shouldTrackMass) {
            bcd$massTracker = new MassTracker();
        }
    }

    private boolean isClient() {
        return ((Object) this) instanceof ClientLevelPlot;
    }

    @Inject(method = "onBlockChange", at = @At("HEAD"))
    private void onBlockChange(BlockPos pos, BlockState state, CallbackInfo ci) {
        if(!bcd$shouldTrackMass) return;
        Level level = subLevel.getLevel();
        BlockState prevState = level.getBlockState(pos);

        bcd$encountBlocks(new LevelAccelerator(level), new BlockPos(pos), state, prevState);
    }

    @Unique
    private void bcd$encountBlocks(LevelAccelerator levelAccelerator, BlockPos pos, BlockState state, BlockState oldState) {
        double mass = PhysicsBlockPropertyHelper.getMass(levelAccelerator, pos, state);
        double massOld = PhysicsBlockPropertyHelper.getMass(levelAccelerator, pos, oldState);
        if(mass != 0) {
            bcd$massTracker.addBlockMass(
                levelAccelerator, state, pos,
                mass,
                PhysicsBlockPropertyHelper.getInertia(levelAccelerator, pos, state)
            );
        }
        if(massOld != 0) {
            bcd$massTracker.addBlockMass(
                levelAccelerator, oldState, pos,
                -massOld,
                PhysicsBlockPropertyHelper.getInertia(levelAccelerator, pos, oldState)
            );
        }
    }

    @Inject(method = "addChunkHolder", at = @At("HEAD"))
    private void addChunkHolder(ChunkPos localChunkPos, PlotChunkHolder holder, boolean initializeLighting, CallbackInfo ci) {

        LevelChunk chunk = holder.getChunk();

        BoundingBox3ic boundingBox = holder.getBoundingBox();
        if(boundingBox == null) {
            if(chunk.isEmpty()) return;
            ((Sail_PlotChunkHolder_Acc) holder).bcd$buildBoundingBox();
            boundingBox = holder.getBoundingBox();
            if(boundingBox == null) return;
        }
        LevelAccelerator levelAccelerator = new LevelAccelerator(subLevel.getLevel());
        BlockPos.betweenClosedStream(boundingBox.toMojang()).forEachOrdered(blockPos -> {
            BlockState state = chunk.getBlockState(blockPos);
            if(state.isAir()) return;
            bcd$encountBlocks(levelAccelerator, chunk.getPos().getBlockAt(blockPos.getX(), blockPos.getY(), blockPos.getZ()), state, Blocks.AIR.defaultBlockState());
        });
    }
}