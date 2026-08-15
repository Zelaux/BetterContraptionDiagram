package com.zelaux.betterdiagram.compat.absolute_kinematics;

import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollOptionBehaviour;
import dev.ryanhcode.sable.api.block.BlockEntitySubLevelActor;
import dev.ryanhcode.sable.api.physics.constraint.GenericConstraintHandle;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.simulated_team.simulated.content.blocks.swivel_bearing.SwivelBearingBlockEntity;
import net.minecraft.core.BlockPos;
import org.joml.Vector3d;

import java.util.UUID;

public interface IBaseJointBlockEntity extends BlockEntitySubLevelActor {

     UUID bcd$subLevelID();

    ScrollOptionBehaviour<SwivelBearingBlockEntity.LockingSetting> bcd$lockedDefaultOption();

    BlockPos bcd$jointPlatePos();
     GenericConstraintHandle bcd$handle() ;
     void bcd$handle(GenericConstraintHandle handle) ;
     void bcd$attachConstraints(SubLevel sublevel, Vector3d o1) ;
}
