package com.zelaux.betterdiagram.extend.accessors;

import dev.ryanhcode.sable.api.physics.mass.MassTracker;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3dc;
import org.spongepowered.asm.mixin.*;

public interface MassTracker$1Accessors {
    public static MassTracker$1Accessors get(){
        return ((MassTracker$1Accessors) MassTracker.BLOCK_CENTER_OF_MASS);
    }

    @Unique
    Vector3dc bcd$lambda$apply$0(BlockState par1, BlockGetter par2, int par3);
}
