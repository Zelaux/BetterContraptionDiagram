package com.zelaux.betterdiagram.feature.create_contraption_mass;

import dev.ryanhcode.sable.api.sublevel.KinematicContraption;

import java.util.List;

public interface ClientLevelPlotExt {
    List<KinematicContraption> bcd$getContraptions();

    default void addContraption(KinematicContraption contraption) {
        bcd$getContraptions().add(contraption);
    }

    default void removeContraption(KinematicContraption contraption) {
        bcd$getContraptions().remove(contraption);
    }
}
