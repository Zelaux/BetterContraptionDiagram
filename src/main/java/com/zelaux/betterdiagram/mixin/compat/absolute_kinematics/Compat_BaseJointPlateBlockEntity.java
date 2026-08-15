package com.zelaux.betterdiagram.mixin.compat.absolute_kinematics;

import com.zelaux.betterdiagram.compat.absolute_kinematics.IBaseJointPlateBlockEntity;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.*;

import java.util.UUID;

@Mixin(targets = "com.rcx.absolutekinematics.blockentity.BaseJointPlateBlockEntity")
public class Compat_BaseJointPlateBlockEntity implements IBaseJointPlateBlockEntity {

    @Shadow
    public BlockPos parent;
    @Shadow
    public UUID parentSubLevelId;

    @Override
    public BlockPos bcd$parent() {
        return parent;
    }

    @Override
    public UUID bcd$parentSubLevelId() {
        return parentSubLevelId;
    }
}
