package com.zelaux.betterdiagram.mixin.compat.simulatedcoasters;

import com.zelaux.betterdiagram.compat.simulatedcoasters.RivetClientIndex;
import dev.silvergold.simulatedcoasters.rivet.RivetAnchorKey;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

import java.util.UUID;

@Mixin(targets = "dev.silvergold.simulatedcoasters.rivet.RivetBlockEntity")
public abstract class RivetIndex_RivetBlockEntity extends BlockEntity implements RivetClientIndex.IRivetEntity {
    @Unique
    RivetAnchorKey bcd$prevKey;

    @Unique
    UUID bcd$oldSublevel;


    @Override
    public UUID bcd$locatedSublevel() {
        return bcd$oldSublevel;
    }

    @Override
    public void bcd$locatedSublevel(UUID bcd$oldSublevel) {
        this.bcd$oldSublevel = bcd$oldSublevel;
    }

    @Override
    public RivetAnchorKey bcd$prevKey() {
        return bcd$prevKey;
    }

    @Override
    public void bcd$prevKey(RivetAnchorKey bcd$prevKey) {
        this.bcd$prevKey = bcd$prevKey;
    }

    public RivetIndex_RivetBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    @Shadow
    public abstract RivetAnchorKey anchorKey();

    @Inject(method = "setRemoved", at = @At("TAIL"))
    void setRemove(CallbackInfo ci) {
        if(getLevel() instanceof ServerLevel) return;
        bcd$prevKey = RivetClientIndex.setRemoved(this, anchorKey(), bcd$prevKey);
    }

    /*@Inject(
        method = "saveAdditional",
        at = @At("TAIL"))
    void saveAdditional(CompoundTag tag, HolderLookup.Provider registries, CallbackInfo ci){
        bcd$prevKey=RivetClientIndex.saveAdditional(anchorKey(),bcd$prevKey);
    }*/
    @Inject(
        method = "loadAdditional",
        at = @At("TAIL"))
    void loadAdditional(CompoundTag tag, HolderLookup.Provider registries, CallbackInfo ci) {
        if(getLevel() instanceof ServerLevel) return;
        bcd$prevKey = RivetClientIndex.loadAdditional(this, anchorKey(), bcd$prevKey);
    }
}
