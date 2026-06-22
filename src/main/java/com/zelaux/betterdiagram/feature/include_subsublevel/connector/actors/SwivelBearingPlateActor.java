package com.zelaux.betterdiagram.feature.include_subsublevel.connector.actors;

import com.zelaux.betterdiagram.feature.include_subsublevel.connector.ActorMapper;
import com.zelaux.betterdiagram.feature.include_subsublevel.connector.LocalConstraint;
import com.zelaux.betterdiagram.mixin.accessors.ISwivelBearingPlateBlockEntity;
import dev.ryanhcode.sable.api.physics.constraint.FixedConstraintConfiguration;
import dev.ryanhcode.sable.api.physics.constraint.RotaryConstraintConfiguration;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.simulated_team.simulated.content.blocks.swivel_bearing.SwivelBearingBlockEntity;
import dev.simulated_team.simulated.content.blocks.swivel_bearing.link_block.SwivelBearingPlateBlockEntity;
import lombok.AllArgsConstructor;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaterniond;
import org.joml.Vector3d;

import java.util.List;


public class SwivelBearingPlateActor implements ActorMapper {
    SwivelBearingPlateBlockEntity entity;

    public SwivelBearingPlateActor(SwivelBearingPlateBlockEntity entity) {
        this.entity = entity;
    }

    SwivelBearingActor swivelBearingActor = new SwivelBearingActor(null);

    @Override
    public @Nullable Iterable<LocalConstraint> mapToConstraint(SubLevel a) {
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

        var constraints = swivelBearingActor.mapToConstraint(subLevel);
        if(constraints == null) return null;
        LocalConstraint constraint = constraints.get(0);
        return switch(constraint.configuration()) {
            case FixedConstraintConfiguration config -> List.of(LocalConstraint.from(
                new FixedConstraintConfiguration(config.pos2(), config.pos1(), config.orientation().conjugate(new Quaterniond())),
                constraint.b(), constraint.a()
            ));
            case RotaryConstraintConfiguration config -> List.of(LocalConstraint.from(
                new RotaryConstraintConfiguration(
                    config.pos2(),
                    config.pos1(),
                    config.normal2(),
                    config.normal1()
                ),
                constraint.b(), constraint.a()
            ));
            default -> null;
        };
    }
}
