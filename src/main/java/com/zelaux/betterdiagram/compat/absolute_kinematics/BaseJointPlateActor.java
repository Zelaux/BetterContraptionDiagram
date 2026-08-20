package com.zelaux.betterdiagram.compat.absolute_kinematics;

import com.zelaux.betterdiagram.feature.include_subsublevel.connector.ActorHandler;
import com.zelaux.betterdiagram.feature.include_subsublevel.connector.SubLevelGroup;
import com.zelaux.betterdiagram.feature.include_subsublevel.force_mapper.AbstractForceMapper;
import com.zelaux.betterdiagram.feature.include_subsublevel.force_mapper.GenericConstantForceMapper;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.SubLevel;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BaseJointPlateActor implements ActorHandler {
    public IBaseJointPlateBlockEntity entity;

    public BaseJointPlateActor(IBaseJointPlateBlockEntity entity) {
        this.entity = entity;
    }

    @Override
    public @Nullable Iterable<? extends AbstractForceMapper> mapToConstraint(SubLevel a, Map<SubLevel, SubLevelGroup> subLevel2Group) {
        UUID uuid = entity.bcd$parentSubLevelId();
        if(uuid == null) return null;
        SubLevelContainer container = SubLevelContainer.getContainer(a.getLevel());
        if(container == null) return null;
        SubLevel b = container.getSubLevel(uuid);
        if(b == null) return null;

        if(b.getLevel().getBlockEntity(entity.bcd$parent()) instanceof IBaseJointBlockEntity base) {
            AbstractForceMapper constraint = BaseJointActor.createConstraint(b, subLevel2Group, base, AbstractForceMapper.SubLevelInfo::flipped);
            if(constraint == null) return null;
            if(constraint instanceof GenericConstantForceMapper handle) {
                constraint=handle.flip();
            }
            return List.of(constraint);
        }
        return null;

    }

}
