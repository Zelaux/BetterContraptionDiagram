package com.zelaux.betterdiagram.feature.include_subsublevel;


import org.jetbrains.annotations.Nullable;

public interface IDiagramScreenWithDependencies {
    @Nullable
    SubLevelDependenciesData bcd$subLevelDependencies();

    void bcd$subLevelDependencies(SubLevelDependenciesData data);
}
