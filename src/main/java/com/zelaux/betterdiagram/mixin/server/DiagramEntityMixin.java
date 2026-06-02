package com.zelaux.betterdiagram.mixin.server;

import com.zelaux.betterdiagram.data.BCDData;
import com.zelaux.betterdiagram.extend.ServerSideData;
import com.zelaux.betterdiagram.network.BCDDataTransfer;
import dev.simulated_team.simulated.content.entities.diagram.DiagramEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

import java.util.Optional;

@Mixin(DiagramEntity.class)
public abstract class DiagramEntityMixin implements ServerSideData {

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    public void addAdditionalSaveData(CompoundTag tag, CallbackInfo ci) {
        if(bcd$storredData == null) return;
        tag.put("bcd$data", BCDData.SHORT_CODEC.encodeStart(NbtOps.INSTANCE, Optional.ofNullable(bcd$storredData)).getOrThrow());
    }

    @Unique
    @Nullable
    BCDData bcd$storredData = null;

    @Override
    public BCDData bcd$storredData() {
        return bcd$storredData;
    }


    @Override
    public void bcd$storredData(BCDData bcd$storredData) {
        this.bcd$storredData = bcd$storredData;
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    public void readAdditionalSaveData(CompoundTag tag, CallbackInfo ci) {

        if(tag.contains("bcd$data", Tag.TAG_COMPOUND)) {
            final CompoundTag configTag = tag.getCompound("bcd$data");
            bcd$storredData = BCDData.SHORT_CODEC.parse(NbtOps.INSTANCE, configTag).getOrThrow().orElse(null);
        } else {
            bcd$storredData = null;
        }

    }


    @Inject(method = "interactAt", at = @At(value = "INVOKE", target = "Ldev/simulated_team/simulated/data/advancements/SimulatedAdvancement;awardTo(Lnet/minecraft/world/entity/player/Player;)V"))
    public void interactAt(Player player, Vec3 vec, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        //DiagramEntity diagramEntity = (DiagramEntity) (Object) this;
        BCDDataTransfer
            .whenOpen(bcd$storredData)
            .trySend((ServerPlayer) player);

    }

}
