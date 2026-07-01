package com.zelaux.betterdiagram.feature.include_subsublevel.force_mapper;

import com.zelaux.betterdiagram.feature.include_subsublevel.SubLevelDependenciesData;
import com.zelaux.betterdiagram.feature.include_subsublevel.connector.ConstaintKind;
import com.zelaux.betterdiagram.feature.include_subsublevel.connector.LocalConstraint;
import com.zelaux.betterdiagram.feature.include_subsublevel.connector.SubLevelGroup;
import com.zelaux.betterdiagram.feature.include_subsublevel.struct.Forces;
import dev.ryanhcode.sable.api.physics.constraint.PhysicsConstraintConfiguration;
import dev.ryanhcode.sable.sublevel.SubLevel;

public class DummyConstraintForceMapper<Config extends PhysicsConstraintConfiguration<?>> extends LocalConstraint<Config> {
    public DummyConstraintForceMapper(Config configuration, ConstaintKind kind, SubLevel a, SubLevel b, SubLevelGroup groupA, SubLevelGroup groupB) {
        super(configuration, kind, a, b,groupA,groupB);
    }

    @Override
    public boolean fixed() {
        return kind==ConstaintKind.Fixed;
    }

    @Override
    public void mergeForces(Forces origin, Forces toAttach, SubLevelDependenciesData subLevelDependenciesData) {
    }
}
