package com.zelaux.betterdiagram.mixin.accessors;

import com.zelaux.betterdiagram.extend.AbstractContainerScreenAccessors;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.*;

import javax.annotation.Nullable;

@Mixin(value = AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin implements AbstractContainerScreenAccessors {
    @Shadow
    @Nullable
    private Slot clickedSlot;

    @Shadow
    @Nullable
    protected Slot hoveredSlot;

    @Shadow
    @Nullable
    private Slot lastClickSlot;

    @Shadow
    @Nullable
    protected abstract Slot findSlot(double mouseX, double mouseY);

    @Nullable
    public Slot bcd$clickedSlot() {return clickedSlot;}

    @Nullable
    public Slot bcd$hoveredSlot() {return hoveredSlot;}

    @Nullable
    public Slot bcd$lastClickSlot() {return lastClickSlot;}

    @Unique
    @Nullable
    @Override
    public Slot bcd$findSlot(double mouseX, double mouseY) {return findSlot(mouseX, mouseY);}

}
