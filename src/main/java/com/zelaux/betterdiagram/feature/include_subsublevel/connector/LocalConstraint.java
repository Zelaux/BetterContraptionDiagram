package com.zelaux.betterdiagram.feature.include_subsublevel.connector;

import com.zelaux.betterdiagram.feature.include_subsublevel.force_mapper.AbstractForceMapper;
import com.zelaux.betterdiagram.feature.include_subsublevel.force_mapper.DummyConstraintForceMapper;
import com.zelaux.betterdiagram.feature.include_subsublevel.force_mapper.RotationConstraintForceMapper;
import dev.ryanhcode.sable.api.physics.constraint.*;
import dev.ryanhcode.sable.sublevel.SubLevel;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

@EqualsAndHashCode(callSuper = false)
@ToString
@FieldDefaults(level = AccessLevel.PUBLIC, makeFinal = true)
@Getter
public abstract class LocalConstraint<Config extends PhysicsConstraintConfiguration<?>> extends AbstractForceMapper {
    Config configuration;
    ConstaintKind kind;


    public LocalConstraint(Config configuration, ConstaintKind kind, SubLevel a, SubLevel b, SubLevelGroup groupA, SubLevelGroup groupB) {
        super(a,groupA,b,groupB);
        this.configuration = configuration;
        this.kind = kind;
    }

    public static @NotNull LocalConstraint<?> from(PhysicsConstraintConfiguration<?> configuration, SubLevel a, SubLevel b, Map<SubLevel, SubLevelGroup> subLevel2Group) {
        SubLevelGroup groupA = subLevel2Group.get(a);
        SubLevelGroup groupB = subLevel2Group.get(b);
        if(configuration instanceof RotaryConstraintConfiguration rotary){
            return new RotationConstraintForceMapper(rotary,a,b,groupA,groupB);
        }
        ConstaintKind kind = switch(configuration) {
            case FixedConstraintConfiguration ignored -> ConstaintKind.Fixed;
            case FreeConstraintConfiguration ignored -> ConstaintKind.Free;
            case GenericConstraintConfiguration ignored -> ConstaintKind.Generic;
            case RotaryConstraintConfiguration ignored ->throw new IllegalStateException("Unexpected value: " + configuration);
        };
        return new DummyConstraintForceMapper<>(configuration, kind, a, b,groupA,groupB);
    }
}
