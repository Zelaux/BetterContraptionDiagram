package com.zelaux.betterdiagram.feature.include_subsublevel.actor_handler;

import com.zelaux.betterdiagram.feature.include_subsublevel.connector.ActorHandler;
import com.zelaux.betterdiagram.feature.include_subsublevel.connector.LocalConstraint;
import com.zelaux.betterdiagram.feature.include_subsublevel.connector.SubLevelGroup;
import com.zelaux.betterdiagram.mixin.accessors.IDockingConnectorBlockEntity;
import com.zelaux.betterdiagram.mixin.accessors.IDockingConnectorPair;
import dev.ryanhcode.sable.api.physics.constraint.FixedConstraintConfiguration;
import dev.ryanhcode.sable.companion.math.JOMLConversion;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.simulated_team.simulated.content.blocks.docking_connector.DockingConnectorBlockEntity;
import dev.simulated_team.simulated.content.blocks.docking_connector.DockingConnectorPair;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaterniond;
import org.joml.Vector3d;

import java.util.List;
import java.util.Map;

@AllArgsConstructor
public class DockingConnectorActor implements ActorHandler {
    public static final Vector3d staticTmp1 = new Vector3d();
    DockingConnectorBlockEntity entity;
    private static final DockingConnectorPair DUMMY_PAIR = new DockingConnectorPair(null, null, null);
    private static final IDockingConnectorPair IDUMMY_PAIR = ((IDockingConnectorPair) DUMMY_PAIR);

    @Override
    public @Nullable Iterable<LocalConstraint<?>> mapToConstraint(SubLevel a, Map<SubLevel, SubLevelGroup> subLevel2Group) {
        Iterable<@NotNull SubLevel> levels = entity.sable$getConnectionDependencies();
        if(levels == null) return null;
        SubLevel b = levels.iterator().next();
        IDockingConnectorBlockEntity blockEntity = (IDockingConnectorBlockEntity) entity;
        int x=0;
        //if(blockEntity.bcd$state() != DockingConnectorBlockEntity.DockingConnectorState.LOCKING) return null;
        DockingConnectorBlockEntity dock1 = entity, dock2 = entity.getOtherConnector();
        if(dock2 == null) return null;

        final Quaterniond blockOrientation1 = new Quaterniond(dock1.getOrientation());
        final Quaterniond blockOrientation2 = new Quaterniond(dock2.getOrientation());
        IDUMMY_PAIR.bcd$getRelativeOrientationDifference(dock1, dock2, staticTmp1);
        Quaterniond target = IDUMMY_PAIR.bcd$target();
        final Quaterniond orientation = target.mul(blockOrientation1, new Quaterniond()).mul(blockOrientation2.conjugate());


        return List.of(LocalConstraint.from(new FixedConstraintConfiguration(
            JOMLConversion.toJOML(entity.getTipPosition()),
            JOMLConversion.toJOML(dock2.getTipPosition()),
            orientation
        ), a, b, subLevel2Group));
    }
}
