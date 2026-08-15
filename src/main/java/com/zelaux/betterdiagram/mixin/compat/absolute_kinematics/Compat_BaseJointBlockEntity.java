package com.zelaux.betterdiagram.mixin.compat.absolute_kinematics;

import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollOptionBehaviour;
import com.zelaux.betterdiagram.compat.absolute_kinematics.IBaseJointBlockEntity;
import dev.ryanhcode.sable.api.physics.constraint.GenericConstraintHandle;
import dev.simulated_team.simulated.content.blocks.swivel_bearing.SwivelBearingBlockEntity;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.*;

import java.util.UUID;

@Mixin(targets = "com.rcx.absolutekinematics.blockentity.BaseJointBlockEntity")
public abstract class Compat_BaseJointBlockEntity implements IBaseJointBlockEntity {

    @Shadow
    public ScrollOptionBehaviour<SwivelBearingBlockEntity.LockingSetting> lockedDefaultOption;

    @Override
    public ScrollOptionBehaviour<SwivelBearingBlockEntity.LockingSetting> bcd$lockedDefaultOption() {
        return lockedDefaultOption;
    }

    @Shadow
    public UUID subLevelID;
    @Shadow
    public BlockPos jointPlatePos;

    @Override
    public BlockPos bcd$jointPlatePos() {
        return jointPlatePos;
    }

    @Override
    public UUID bcd$subLevelID() {
        return subLevelID;
    }

    @Shadow
    private GenericConstraintHandle handle;

    @Override
    public void bcd$handle(GenericConstraintHandle handle) {
        this.handle = handle;
    }

    @Override
    public GenericConstraintHandle bcd$handle() {
        return handle;
    }
}
