package com.zelaux.betterdiagram.feature.include_subsublevel.actor_handler;

import com.zelaux.betterdiagram.feature.include_subsublevel.connector.ActorHandler;
import com.zelaux.betterdiagram.feature.include_subsublevel.connector.LocalConstraint;
import com.zelaux.betterdiagram.feature.include_subsublevel.connector.SubLevelGroup;
import com.zelaux.betterdiagram.mixin.accessors.ISwivelBearingPlateBlockEntity;
import dev.ryanhcode.sable.api.physics.constraint.FixedConstraintConfiguration;
import dev.ryanhcode.sable.api.physics.constraint.RotaryConstraintConfiguration;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.simulated_team.simulated.content.blocks.swivel_bearing.SwivelBearingBlockEntity;
import dev.simulated_team.simulated.content.blocks.swivel_bearing.link_block.SwivelBearingPlateBlockEntity;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaterniond;

import java.util.List;
import java.util.Map;


public class SwivelBearingPlateActor implements ActorHandler {
    SwivelBearingPlateBlockEntity entity;

    public SwivelBearingPlateActor(SwivelBearingPlateBlockEntity entity) {
        this.entity = entity;
    }

    SwivelBearingActor swivelBearingActor = new SwivelBearingActor(null);

    @Override
    public @Nullable Iterable<LocalConstraint<?>> mapToConstraint(SubLevel a, Map<SubLevel, SubLevelGroup> subLevel2Group) {
        ISwivelBearingPlateBlockEntity blockEntity = (ISwivelBearingPlateBlockEntity) entity;
        BlockPos pos = blockEntity.bcd$parent();
        if(pos == null) return null;
        if(!(entity.getLevel().getBlockEntity(pos) instanceof SwivelBearingBlockEntity swivelBearingBlockEntity))
            return null;
        swivelBearingActor.entity = swivelBearingBlockEntity;

        final SubLevelContainer container = SubLevelContainer.getContainer(entity.getLevel());
        var parentSubLevelId = blockEntity.bcd$parentSubLevelId();
        if(parentSubLevelId == null) return null;
        final SubLevel subLevel = container.getSubLevel(parentSubLevelId);

        var constraints = swivelBearingActor.mapToConstraint(subLevel, subLevel2Group);
        if(constraints == null) return null;
        LocalConstraint<?> constraint = constraints.get(0);
        return switch(constraint.configuration()) {
            case FixedConstraintConfiguration config -> List.of(LocalConstraint.from(
                new FixedConstraintConfiguration(config.pos2(), config.pos1(), config.orientation().conjugate(new Quaterniond())),
                constraint.toAttachSubLevel(), constraint.originSubLevel(),
                subLevel2Group));
            case RotaryConstraintConfiguration config -> List.of(LocalConstraint.from(
                new RotaryConstraintConfiguration(
                    config.pos2(),
                    config.pos1(),
                    config.normal2(),
                    config.normal1()
                ),
                constraint.toAttachSubLevel(), constraint.originSubLevel(),
                subLevel2Group));
            default -> null;
        };
    }
}
