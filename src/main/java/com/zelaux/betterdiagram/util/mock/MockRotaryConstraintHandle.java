/*
package com.zelaux.betterdiagram.util.mock;

import dev.ryanhcode.sable.api.physics.constraint.ConstraintJointAxis;
import dev.ryanhcode.sable.api.physics.constraint.RotaryConstraintConfiguration;
import dev.ryanhcode.sable.api.physics.constraint.GenericConstraintHandle;
import dev.ryanhcode.sable.api.physics.constraint.RotaryConstraintHandle;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import org.apache.commons.lang3.DoubleRange;
import org.joml.Quaterniond;
import org.joml.Quaterniondc;
import org.joml.Vector3d;
import org.joml.Vector3dc;

import java.util.Collections;
import java.util.EnumMap;

public class MockRotaryConstraintHandle extends MockConstantHandle implements RotaryConstraintHandle {
    public RotaryConstraintConfiguration config;
    public ObjectSet<ConstraintJointAxis> locked;

    public MockRotaryConstraintHandle(RotaryConstraintConfiguration config) {

        this.config = new RotaryConstraintConfiguration(
            config.pos1(),
            config.pos2(),
            config.normal1(),
            config.normal2(),
            locked = new ObjectOpenHashSet<>(config.lockedAxes())
        );
    }

    @Override
    public void setFrame1(Vector3dc localPosition, Quaterniondc localOrientation) {
        config = new RotaryConstraintConfiguration(
            new Vector3d(localPosition),
            config.pos2(),
            new Quaterniond(localOrientation),
            config.normal2(),
            config.lockedAxes()
        );
    }

    @Override
    public void setFrame2(Vector3dc localPosition, Quaterniondc localOrientation) {
        config = new RotaryConstraintConfiguration(
            config.pos1(),
            new Vector3d(localPosition),
            config.normal1(),
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
*/
