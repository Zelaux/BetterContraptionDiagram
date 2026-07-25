package com.zelaux.betterdiagram.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zelaux.betterdiagram.index.BCodecs;
import com.zelaux.betterdiagram.index.BForceGroups;
import com.zelaux.betterdiagram.struct.BCDForceGroup;
import dev.ryanhcode.sable.api.physics.force.ForceGroup;
import dev.ryanhcode.sable.api.physics.force.ForceGroups;
import foundry.veil.api.util.CodecUtil;
import lombok.With;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.joml.Vector3dc;

import java.util.Objects;
import java.util.Optional;

@With
public record BalanceForceFilter(
    Optional<Vector3dc> location,
    Optional<ResourceLocation> forceGroup,
    CompoundTag futureData
) {
    public static final BalanceForceFilter GRAVITY_FORCE_FILTER = ofGroup(ForceGroups.GRAVITY.get());
    public static Codec<BalanceForceFilter> CODEC = BCodecs.wrapWithCompound(
        RecordCodecBuilder.create(i -> i.group(
            CodecUtil.VECTOR3DC_CODEC.optionalFieldOf("location").forGetter(BalanceForceFilter::location),
            ResourceLocation.CODEC.optionalFieldOf("forceGroup").forGetter(BalanceForceFilter::forceGroup)
        ).apply(i, BalanceForceFilter::raw)),
        BalanceForceFilter::futureData,
        BalanceForceFilter::withFutureData
    );

    private static BalanceForceFilter raw(Optional<Vector3dc> location, Optional<ResourceLocation> forceGroup) {
        return new BalanceForceFilter(location, forceGroup, null);
    }

    public static BalanceForceFilter ofGroup(ForceGroup group) {
        return raw(Optional.empty(), Optional.ofNullable(BForceGroups.getPathAny(group)));
    }
    public static BalanceForceFilter ofGroup(BCDForceGroup group) {
        return raw(Optional.empty(), Optional.ofNullable(BForceGroups.getPath(group)));
    }

    @Override
    public boolean equals(Object o) {
        if(!(o instanceof BalanceForceFilter that)) return false;
        return Objects.equals(location, that.location) && Objects.equals(forceGroup, that.forceGroup);
    }

    @Override
    public int hashCode() {
        return Objects.hash(location, forceGroup);
    }
}
