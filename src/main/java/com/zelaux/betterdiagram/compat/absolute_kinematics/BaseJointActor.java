package com.zelaux.betterdiagram.compat.absolute_kinematics;

import com.zelaux.betterdiagram.feature.include_subsublevel.connector.ActorHandler;
import com.zelaux.betterdiagram.feature.include_subsublevel.connector.SubLevelGroup;
import com.zelaux.betterdiagram.feature.include_subsublevel.force_mapper.AbstractForceMapper;
import com.zelaux.betterdiagram.feature.include_subsublevel.force_mapper.DummyGenericConstantForceMapper;
import com.zelaux.betterdiagram.feature.include_subsublevel.force_mapper.FixedForceMapper;
import com.zelaux.betterdiagram.util.mock.MockGenericConstraintHandle;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.api.physics.constraint.GenericConstraintHandle;
import dev.ryanhcode.sable.companion.math.JOMLConversion;
import dev.ryanhcode.sable.sublevel.SubLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

public class BaseJointActor implements ActorHandler {
    public IBaseJointBlockEntity entity;

    public BaseJointActor(IBaseJointBlockEntity entity) {
        this.entity = entity;
    }

    @Override
    public @Nullable Iterable<? extends AbstractForceMapper> mapToConstraint(SubLevel a, Map<SubLevel, SubLevelGroup> subLevel2Group) {
        IBaseJointBlockEntity entity = this.entity;
        AbstractForceMapper constraint = createConstraint(a, subLevel2Group, entity, subLevelInfo1 -> subLevelInfo1);
        return constraint==null?null: List.of(constraint);

    }

    static @Nullable AbstractForceMapper createConstraint(SubLevel a, Map<SubLevel, SubLevelGroup> subLevel2Group, IBaseJointBlockEntity entity, Function<AbstractForceMapper.SubLevelInfo, AbstractForceMapper.SubLevelInfo> infoMapper) {
        UUID uuid = entity.bcd$subLevelID();
        if(uuid == null) return null;
        Level level = a.getLevel();
        if(level == null) return null;
        BlockPos joint = entity.bcd$jointPlatePos();
        SubLevel b = Sable.HELPER.getContaining(level, joint);
        if(b == null) return null;

        BlockEntity blockEntity = (BlockEntity) entity;
        boolean shouldLock = entity.bcd$lockedDefaultOption().get().shouldLock(
            level.getBestNeighborSignal(blockEntity.getBlockPos())
        );
        AbstractForceMapper.SubLevelInfo info = infoMapper.apply(AbstractForceMapper.sublevelInfo(a, b, subLevel2Group));
        if(shouldLock) return new FixedForceMapper(info);

        GenericConstraintHandle was = swapHandle(null, entity);
        entity.bcd$attachConstraints(b, JOMLConversion.toJOML(joint.getCenter()));
        GenericConstraintHandle raw = swapHandle(was, entity);
        if(raw instanceof MockGenericConstraintHandle handle) {
            return new DummyGenericConstantForceMapper(info, handle);
        }
        return null;
    }

    static GenericConstraintHandle swapHandle(GenericConstraintHandle handle, IBaseJointBlockEntity entity) {
        GenericConstraintHandle was = entity.bcd$handle();
        entity.bcd$handle(handle);
        return was;
    }
}
