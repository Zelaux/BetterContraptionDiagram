package com.zelaux.betterdiagram.extend;

import com.zelaux.betterdiagram.struct.MassStack;
import com.zelaux.betterdiagram.struct.Weights;
import org.joml.Vector3d;

import java.util.ArrayList;

public class DataKeys {
    public static final WithClientData.Key<Vector3d> EXPECTED_CENTER_OF_MASS = new WithClientData.Key<>();
    public static final WithClientData.Key<Weights[]> MASS_STACKS = new WithClientData.Key<>();
}
