package com.zelaux.betterdiagram.feature.include_subsublevel.force_mapper;

import com.zelaux.betterdiagram.feature.include_subsublevel.SubLevelDependenciesData;
import com.zelaux.betterdiagram.feature.include_subsublevel.struct.Forces;
import com.zelaux.betterdiagram.util.mock.MockGenericConstraintHandle;

public class DummyGenericConstantForceMapper extends AbstractForceMapper{
    public final MockGenericConstraintHandle handle;
    public DummyGenericConstantForceMapper(SubLevelInfo info, MockGenericConstraintHandle handle) {
        super(info);
        this.handle = handle;
    }

    @Override
    public void mergeForces(Forces origin, Forces toAttach, SubLevelDependenciesData subLevelDependenciesData) {

    }
}
