package com.zelaux.betterdiagram.feature.include_subsublevel.connector;

import dev.ryanhcode.sable.api.physics.constraint.*;
import dev.ryanhcode.sable.sublevel.SubLevel;
import org.jetbrains.annotations.NotNull;

public record LocalConstraint(PhysicsConstraintConfiguration<?> configuration,Kind kind,SubLevel a, SubLevel b) {
    public static @NotNull LocalConstraint from(PhysicsConstraintConfiguration<?> configuration,SubLevel a, SubLevel b) {
        Kind kind=switch(configuration) {
            case FixedConstraintConfiguration ignored -> Kind.Fixed;
            case FreeConstraintConfiguration ignored -> Kind.Free;
            case GenericConstraintConfiguration ignored -> Kind.Generic;
            case RotaryConstraintConfiguration ignored -> Kind.Rotary;
        };
        return new LocalConstraint(configuration,kind,a,b);
    }

    public enum Kind{
        Free,
        Fixed,
        Rotary,
        Generic
    }
}
