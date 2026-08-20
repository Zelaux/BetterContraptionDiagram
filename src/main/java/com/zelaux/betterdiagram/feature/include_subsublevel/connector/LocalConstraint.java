package com.zelaux.betterdiagram.feature.include_subsublevel.connector;

import com.zelaux.betterdiagram.feature.include_subsublevel.force_mapper.AbstractForceMapper;
import com.zelaux.betterdiagram.feature.include_subsublevel.force_mapper.DummyConstraintForceMapper;
import com.zelaux.betterdiagram.feature.include_subsublevel.force_mapper.DummyGenericConstantForceMapper;
import com.zelaux.betterdiagram.util.VecUtil;
import dev.ryanhcode.sable.api.physics.constraint.*;
import dev.ryanhcode.sable.sublevel.SubLevel;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaterniond;

import java.util.EnumSet;
import java.util.Map;

@EqualsAndHashCode(callSuper = false)
@ToString
@FieldDefaults(level = AccessLevel.PUBLIC, makeFinal = true)
@Getter
public abstract class LocalConstraint<Config extends PhysicsConstraintConfiguration<?>> extends AbstractForceMapper {
    private static final EnumSet<ConstraintJointAxis> ROTARY_LOCKED = EnumSet.of(
        ConstraintJointAxis.LINEAR_X,
        ConstraintJointAxis.LINEAR_Y,
        ConstraintJointAxis.LINEAR_Z,
        ConstraintJointAxis.ANGULAR_Y,
        ConstraintJointAxis.ANGULAR_Z
    );
    Config configuration;
    ConstaintKind kind;

    public LocalConstraint(SubLevelInfo info, Config configuration, ConstaintKind kind) {
        super(info);
        this.configuration = configuration;
        this.kind = kind;
    }

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
        SubLevelInfo info = new SubLevelInfo(
            a, groupA, b, groupB
        );
        if(configuration instanceof RotaryConstraintConfiguration rotary) {
            return new DummyGenericConstantForceMapper(info, new GenericConstraintConfiguration(
                rotary.pos1(),
                rotary.pos2(),
                VecUtil.X_V.rotationTo(rotary.normal1(), new Quaterniond()),
                VecUtil.X_V.rotationTo(rotary.normal2(), new Quaterniond()),
                ROTARY_LOCKED
            ), null);
            //return new RotationConstraintForceMapper(rotary, a, b, groupA, groupB);
        }
        if(configuration instanceof GenericConstraintConfiguration generic) {
            return new DummyGenericConstantForceMapper(info, generic, null);
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
            toAttachGroup(), originGroup()
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
