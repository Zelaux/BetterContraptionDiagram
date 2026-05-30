package com.zelaux.betterdiagram.mixin.accessors;


import com.zelaux.betterdiagram.extend.accessors.MassTracker$1Accessors;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3dc;
import org.spongepowered.asm.mixin.*;

@Mixin(targets = "dev.ryanhcode.sable.api.physics.mass.MassTracker$1")
public abstract class MassTracker$1Mixin implements MassTracker$1Accessors {
    @Shadow
    protected static  Vector3dc lambda$apply$0(BlockState par1, BlockGetter par2, int par3){throw null;}


    @Unique
    @Override
    public Vector3dc bcd$lambda$apply$0(BlockState par1, BlockGetter par2, int par3){
        return lambda$apply$0(par1, par2, par3);
    }


}
