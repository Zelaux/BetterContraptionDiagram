package com.zelaux.betterdiagram.feature.include_subsublevel;


import org.jetbrains.annotations.Nullable;

public interface IDiagramScreenWithDependencies {
    void bcd$tryUpdateData();

    @Nullable
    SubLevelDependenciesData bcd$subLevelDependencies();

    void bcd$subLevelDependencies(SubLevelDependenciesData data);
}
