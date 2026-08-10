package com.zelaux.betterdiagram.compat.gadgetsandgizmos;

import com.zelaux.betterdiagram.feature.include_subsublevel.connector.ActorHandler;
import com.zelaux.betterdiagram.feature.include_subsublevel.connector.SubLevelGroup;
import com.zelaux.betterdiagram.feature.include_subsublevel.force_mapper.AbstractForceMapper;
import com.zelaux.betterdiagram.feature.include_subsublevel.force_mapper.FixedForceMapper;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.api.block.BlockEntitySubLevelActor;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.SubLevel;
import lombok.AllArgsConstructor;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

@AllArgsConstructor
public class PhysicsGantryShaftActor implements ActorHandler {
    BlockEntitySubLevelActor actor;
    @Override
    public @Nullable Iterable<? extends AbstractForceMapper> mapToConstraint(SubLevel a, Map<SubLevel, SubLevelGroup> subLevel2Group) {
        if(actor instanceof IPhysicsGantryShaft shaft) {
            BlockEntitySubLevelActor has = shaft.bcd$hasActor();
            if(has==null)return null;
            BlockEntity blockEntity = (BlockEntity) has;
            SubLevel b = Sable.HELPER.getContaining(blockEntity);
            return b==null?null: List.of(new FixedForceMapper(AbstractForceMapper.sublevelInfo(
                a,b,subLevel2Group
            )));
        }
        return null;
    }
}
