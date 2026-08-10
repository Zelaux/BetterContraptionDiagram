package com.zelaux.betterdiagram.feature.include_subsublevel.force_mapper;

import com.zelaux.betterdiagram.feature.include_subsublevel.SubLevelDependenciesData;
import com.zelaux.betterdiagram.feature.include_subsublevel.connector.LocalConstraint;
import com.zelaux.betterdiagram.feature.include_subsublevel.connector.SubLevelGroup;
import com.zelaux.betterdiagram.feature.include_subsublevel.struct.Forces;
import dev.ryanhcode.sable.sublevel.SubLevel;

public class FixedForceMapper extends AbstractForceMapper {
    public FixedForceMapper(SubLevel originSubLevel, SubLevelGroup originGroup, SubLevel toAttachSubLevel, SubLevelGroup toAttachGroup) {
        super(originSubLevel, originGroup, toAttachSubLevel, toAttachGroup);
    }

    public FixedForceMapper(SubLevelInfo info) {
        super(info);
    }

    @Override
    public boolean fixed() {
        return true;
    }

    @Override
    public AbstractForceMapper tryMergeWith(AbstractForceMapper other) {
        if(!(other instanceof FixedForceMapper fixed)) return null;

        return fixed;
    }

    @Override
    public void mergeForces(Forces origin, Forces toAttach, SubLevelDependenciesData subLevelDependenciesData) {

    }
}
