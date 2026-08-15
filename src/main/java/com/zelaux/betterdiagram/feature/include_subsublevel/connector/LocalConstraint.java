package com.zelaux.betterdiagram.feature.include_subsublevel.connector;

import com.zelaux.betterdiagram.feature.include_subsublevel.force_mapper.AbstractForceMapper;
import com.zelaux.betterdiagram.feature.include_subsublevel.force_mapper.DummyConstraintForceMapper;
import com.zelaux.betterdiagram.feature.include_subsublevel.force_mapper.RotationConstraintForceMapper;
import dev.ryanhcode.sable.api.physics.constraint.*;
import dev.ryanhcode.sable.sublevel.SubLevel;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaterniond;

import java.util.Map;

@EqualsAndHashCode(callSuper = false)
@ToString
@FieldDefaults(level = AccessLevel.PUBLIC, makeFinal = true)
@Getter
public abstract class LocalConstraint<Config extends PhysicsConstraintConfiguration<?>> extends AbstractForceMapper {
    Config configuration;
    ConstaintKind kind;


    public LocalConstraint(Config configuration, ConstaintKind kind, SubLevel a, SubLevel b, SubLevelGroup groupA, SubLevelGroup groupB) {
        super(a, groupA, b, groupB);
        this.configuration = configuration;
        this.kind = kind;
    }

    public static @NotNull LocalConstraint<?> from(PhysicsConstraintConfiguration<?> configuration, SubLevel a, SubLevel b, Map<SubLevel, SubLevelGroup> subLevel2Group) {
        SubLevelGroup groupA = subLevel2Group.get(a);
        SubLevelGroup groupB = subLevel2Group.get(b);
        return from(configuration, a, b, groupA, groupB);
    }

    public static @NotNull LocalConstraint<?> from(PhysicsConstraintConfiguration<?> configuration, SubLevel a, SubLevel b, SubLevelGroup groupA, SubLevelGroup groupB) {
        if(configuration instanceof RotaryConstraintConfiguration rotary) {
            return new RotationConstraintForceMapper(rotary, a, b, groupA, groupB);
        }
        ConstaintKind kind = switch(configuration) {
            case FixedConstraintConfiguration ignored -> ConstaintKind.Fixed;
            case FreeConstraintConfiguration ignored -> ConstaintKind.Free;
            case GenericConstraintConfiguration ignored -> ConstaintKind.Generic;
            case RotaryConstraintConfiguration ignored ->
                throw new IllegalStateException("Unexpected value: " + configuration);
        };
        return new DummyConstraintForceMapper<>(configuration, kind, a, b, groupA, groupB);
    }

    @SuppressWarnings("unchecked")
    public LocalConstraint<Config> flip() {
        return (LocalConstraint<Config>) from(
            flipConfig(configuration()),
            toAttachSubLevel(), originSubLevel(),
            toAttachGroup(),originGroup()
        );
    }

    public static @NotNull PhysicsConstraintConfiguration<?> flipConfig(PhysicsConstraintConfiguration<?> configuration) {
        return switch(configuration) {
            case FixedConstraintConfiguration config ->
                new FixedConstraintConfiguration(config.pos2(), config.pos1(), config.orientation().conjugate(new Quaterniond()));
            case RotaryConstraintConfiguration config -> new RotaryConstraintConfiguration(
                config.pos2(),
                config.pos1(),
                config.normal2(),
                config.normal1()
            );

            case FreeConstraintConfiguration config -> new FreeConstraintConfiguration(
                config.pos2(), config.pos1(), config.orientation().conjugate(new Quaterniond())
            );
            case GenericConstraintConfiguration config -> new GenericConstraintConfiguration(
                config.pos2(),
                config.pos1(),
                config.orientation2(),
                config.orientation1(),
                config.lockedAxes()
            )
            ;
        };
    }
}
