package com.zelaux.betterdiagram.util.async;

import com.zelaux.betterdiagram.extend.accessors.BlockPropertiesComputers;
import com.zelaux.betterdiagram.util.CenterMassCache;
import it.unimi.dsi.fastutil.doubles.DoubleImmutableList;
import lombok.AllArgsConstructor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import java.util.Arrays;
import java.util.Collection;

@AllArgsConstructor
public class MassVariants {
    public static final DoubleImmutableList EMPTY_LIST = new DoubleImmutableList(new double[0]);
    public final DoubleImmutableList variants;

    public MassVariants() {
        this.variants = EMPTY_LIST;
    }

    public static MassVariants calc(HolderLookup.RegistryLookup<Block> blocks, Level level) {
        double[] array = blocks
            .listElements()
            .map(it -> it.value().getStateDefinition().getPossibleStates())
            .flatMap(Collection::stream)
            .filter(x-> CenterMassCache.isValidState(x.getBlock(),x))
            .sequential()
            .mapToDouble(state -> BlockPropertiesComputers.getMass(level, BlockPos.ZERO, state))
            .mapToLong(Mass2State::doubleToLongKey)
            .distinct()
            .mapToDouble(Mass2State::longKeyToDouble)
            .toArray();
        Arrays.sort(array);
        return new MassVariants(new DoubleImmutableList(
            array
        ));
    }


}
