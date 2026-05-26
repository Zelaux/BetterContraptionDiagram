package com.zelaux.betterdiagram.gui.screen.COM;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.SlotItemHandler;

public class BSlotItemHandler extends SlotItemHandler {

    public boolean mutable;

    public BSlotItemHandler(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
        super(itemHandler, index, xPosition, yPosition);
        mutable = itemHandler instanceof IItemHandlerModifiable;
    }

    public BSlotItemHandler mutable(boolean mutable) {
        this.mutable = mutable;
        return this;
    }

    @Override
    public void initialize(ItemStack stack) {
        if(mutable) super.initialize(stack);
    }

    public int index() {
        return index;
    }

    @Override
    public void set(ItemStack stack) {
        if(mutable) super.set(stack);
    }
}
