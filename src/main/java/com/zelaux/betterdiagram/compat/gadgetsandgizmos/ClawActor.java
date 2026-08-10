package com.zelaux.betterdiagram.compat.gadgetsandgizmos;

import com.zelaux.betterdiagram.feature.include_subsublevel.actor_handler.RopeActor;
import com.zelaux.betterdiagram.feature.include_subsublevel.connector.ActorHandler;
import com.zelaux.betterdiagram.feature.include_subsublevel.connector.SubLevelGroup;
import com.zelaux.betterdiagram.feature.include_subsublevel.force_mapper.AbstractForceMapper;
import com.zelaux.betterdiagram.feature.include_subsublevel.force_mapper.FixedForceMapper;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.simulated_team.simulated.content.blocks.rope.RopeStrandHolderBlockEntity;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class ClawActor implements ActorHandler {
    private final RopeActor ropeActor;
    private final RopeStrandHolderBlockEntity actor;

    public ClawActor(RopeStrandHolderBlockEntity actor) {
        this.ropeActor = new RopeActor(actor);
        this.actor = actor;
    }

    @Override
    public @Nullable Iterable<? extends AbstractForceMapper> mapToConstraint(SubLevel a, Map<SubLevel, SubLevelGroup> subLevel2Group) {
        var mappers = ropeActor.mapToConstraint(a, subLevel2Group);
        Iterable<@NotNull SubLevel> subLevels = actor.sable$getConnectionDependencies();
        if(subLevels == null || !subLevels.iterator().hasNext()) return mappers;

        mappers= mappers == null ? new ObjectArrayList<>() : new ObjectArrayList<>(mappers);

        mappers.add(new FixedForceMapper(AbstractForceMapper.sublevelInfo(
            a, subLevels.iterator().next(), subLevel2Group
        )));
        return mappers;
    }
}
