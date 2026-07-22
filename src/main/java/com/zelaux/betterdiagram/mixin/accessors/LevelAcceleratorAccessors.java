package com.zelaux.betterdiagram.mixin.accessors;

import dev.ryanhcode.sable.util.LevelAccelerator;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.gen.*;

@Mixin(LevelAccelerator.class)
public interface LevelAcceleratorAccessors {
    @Accessor("level")
    Level bcd$level();
}
