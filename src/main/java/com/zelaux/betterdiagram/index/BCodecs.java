package com.zelaux.betterdiagram.index;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zelaux.betterdiagram.feature.client_mass_tracker.SetMethodForMassTracker;
import dev.ryanhcode.sable.api.block.BlockSubLevelLiftProvider.LiftProviderContext;
import dev.ryanhcode.sable.api.physics.mass.MassTracker;
import foundry.veil.api.util.CodecUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

public interface BCodecs {
    static <T> Codec<T> wrapWithCompound(Codec<T> codec, Function<T, CompoundTag> getter, BiFunction<T,CompoundTag,T> setter){
        return CompoundTag.CODEC.xmap(
            nbt -> codec
                .parse(NbtOps.INSTANCE, nbt)
                .map(data -> setter.apply(data,nbt))
                .getOrThrow(),
            obj -> {
                CompoundTag nbt = (CompoundTag) codec.encodeStart(NbtOps.INSTANCE, obj).getOrThrow();
                CompoundTag compoundTag = getter.apply(obj);
                if(compoundTag != null) {
                    CompoundTag result = compoundTag.copy();
                    result.merge(nbt);
                    return result;
                }
                return nbt;
            }
        );
    }

    Codec<LiftProviderContext> LIFT_PROVIDER_CONTEXT_CODEC = RecordCodecBuilder.create(i ->
        i.group(
            BlockPos.CODEC.fieldOf("pos").forGetter(LiftProviderContext::pos),
            BlockState.CODEC.fieldOf("state").forGetter(LiftProviderContext::state),
            Vec3.CODEC.fieldOf("dir").forGetter(LiftProviderContext::dir)
        ).apply(i, LiftProviderContext::new)
    );
    Codec<MassTracker> MASS_TRACKER_CODEC = RecordCodecBuilder.create(i ->
        i.group(
            CodecUtil.VECTOR3DC_CODEC.optionalFieldOf("centerOfMass").forGetter(it -> Optional.ofNullable(it.getCenterOfMass())),
            Codec.DOUBLE.fieldOf("mass").forGetter(MassTracker::getMass),
            CodecUtil.MATRIX3D_CODEC.fieldOf("inertiaTensor").forGetter(MassTracker::getInertiaTensor)
        ).apply(i, (centerOfMass, mass, inertiaTensor) ->
            ((SetMethodForMassTracker) new MassTracker()).bcd$set(centerOfMass.orElse(null), mass, inertiaTensor)
        )
    );
}
