package com.zelaux.betterdiagram.util.mock;

import dev.ryanhcode.sable.api.physics.constraint.ConstraintJointAxis;
import dev.ryanhcode.sable.api.physics.constraint.GenericConstraintConfiguration;
import dev.ryanhcode.sable.api.physics.constraint.GenericConstraintHandle;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import org.apache.commons.lang3.DoubleRange;
import org.joml.Quaterniond;
import org.joml.Quaterniondc;
import org.joml.Vector3d;
import org.joml.Vector3dc;

import java.util.Collections;
import java.util.EnumMap;

public class MockGenericConstraintHandle extends MockConstantHandle implements GenericConstraintHandle {
    public GenericConstraintConfiguration config;
    public ObjectSet<ConstraintJointAxis> locked;
    public void flip(){
        this.config = new GenericConstraintConfiguration(
            config.pos2(),
            config.pos1(),
            config.orientation2(),
            config.orientation1(),
            config.lockedAxes()
        );
    }

    public MockGenericConstraintHandle(GenericConstraintConfiguration config) {

        this.config = new GenericConstraintConfiguration(
            config.pos1(),
            config.pos2(),
            config.orientation1(),
            config.orientation2(),
            locked = new ObjectOpenHashSet<>(config.lockedAxes())
        );
    }

    @Override
    public void setFrame1(Vector3dc localPosition, Quaterniondc localOrientation) {
        config = new GenericConstraintConfiguration(
            new Vector3d(localPosition),
            config.pos2(),
            new Quaterniond(localOrientation),
            config.orientation2(),
            config.lockedAxes()
        );
    }

    @Override
    public void setFrame2(Vector3dc localPosition, Quaterniondc localOrientation) {
        config = new GenericConstraintConfiguration(
            config.pos1(),
            new Vector3d(localPosition),
            config.orientation1(),
            new Quaterniond(localOrientation),
            config.lockedAxes()
        );
    }

    public EnumMap<ConstraintJointAxis, DoubleRange> ranges = new EnumMap<>(ConstraintJointAxis.class);

    @Override
    public void setLimit(ConstraintJointAxis axis, double min, double max) {
        ranges.put(axis, DoubleRange.of(min, max));
    }

    @Override
    public void lockAxes(ConstraintJointAxis... axes) {
        Collections.addAll(locked, axes);
    }
}
