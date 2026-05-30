package com.zelaux.betterdiagram.mixin.accessors.blockpropcalc;


import com.zelaux.betterdiagram.extend.accessors.BlockPropertiesComputers;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.*;

@Mixin(targets = {
    "dev.ryanhcode.sable.physics.chunk.VoxelNeighborhoodState$1",
})
public abstract class VoxelNeighborhoodLambdas implements BlockPropertiesComputers.Lambda0Access_State_BlockGetter_X<Boolean> {
    @Shadow
    protected static  boolean lambda$apply$0(BlockState par1, BlockGetter par2, int par3){throw null;}


    @Unique
    @Override
    public Boolean bcd$lambda$apply$0(BlockState par1, BlockGetter par2, int par3){
        return lambda$apply$0(par1, par2, par3);
    }


}
