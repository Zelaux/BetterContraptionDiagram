package com.zelaux.betterdiagram.data;

import dev.ryanhcode.sable.sublevel.SubLevel;
import lombok.EqualsAndHashCode;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3d;

import java.util.Objects;


public record OffCenteredBlock(
    SubLevel subLevel,
    BlockPos pos,
    BlockState state,
    Vector3d COM,
    double mass,
    double[] axisImpact
) {
    @Override
    public boolean equals(Object o) {
        if(!(o instanceof OffCenteredBlock that)) return false;
        return Objects.equals(pos, that.pos) && Objects.equals(state, that.state);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pos, state);
    }

    public static OffCenteredBlock create(SubLevel subLevel, BlockPos pos, BlockState state, double mass, Vector3d com) {
        return new OffCenteredBlock(subLevel,pos,state, com,mass,new double[3]);
    }
}
