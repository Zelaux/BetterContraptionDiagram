package com.zelaux.betterdiagram.mixin.compat.gadgets;

import com.zelaux.betterdiagram.compat.gadgetsandgizmos.IPhysicsGantryShaft;
import com.zelaux.betterdiagram.mixin_plugin.postprocessors.GadgetsPostprocessor;
import org.spongepowered.asm.mixin.*;

/**
 * @see GadgetsPostprocessor
 * */
@Mixin(targets = "com.rieno.gadgetsandgizmos.content.PhysicsGantryShaftBlockEntity")
public abstract class Mixin_PhysicsGantryShaftBlockEntity implements IPhysicsGantryShaft {
}
