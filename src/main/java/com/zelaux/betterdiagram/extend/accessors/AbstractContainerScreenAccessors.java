package com.zelaux.betterdiagram.extend.accessors;

import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.*;

import javax.annotation.Nullable;

public interface AbstractContainerScreenAccessors {
    @Unique
    @Nullable
    Slot bcd$findSlot(double mouseX, double mouseY);

    @Nullable
    Slot bcd$clickedSlot();

    @Nullable
    Slot bcd$hoveredSlot();

    @Nullable
    Slot bcd$lastClickSlot();
}
