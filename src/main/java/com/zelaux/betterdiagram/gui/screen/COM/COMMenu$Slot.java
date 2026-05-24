package com.zelaux.betterdiagram.gui.screen.COM;

import com.zelaux.betterdiagram.util.ui.InplaceBlockRenderer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

@OnlyIn(Dist.CLIENT)
class COMMenu$Slot extends BSlotItemHandler {
    public COMMenu$Slot(IItemHandler container, int slot, int x, int y) {
        super(container, slot, x, y);
    }

    @Override
    public void set(ItemStack stack) {
        int x = 0;
    }

    @Override
    public ItemStack safeTake(int count, int decrement, Player player) {
        if(!this.mutable)return ItemStack.EMPTY;
        return super.safeTake(count, decrement, player);
    }
public final InplaceBlockRenderer.Container entityContainer=new InplaceBlockRenderer.Container();
    @Override
    public ItemStack getItem() {
        return super.getItem().copy();
    }

    @Override
    public ItemStack safeInsert(ItemStack stack, int increment) {
        if(!this.mutable)return stack;
        ItemStack item = getItem();
        if(item.getItem() != stack.getItem()) {
            stack.setCount(0);
            return stack;
        }
        stack.shrink(-increment);
        return super.safeInsert(stack, increment);
    }

    @Override
    public ItemStack remove(int amount) {
        return getItem().copyWithCount(amount);
    }

    /**
     * Return whether this slot's stack can be taken from this slot.
     */
    @Override
    public boolean mayPickup(Player player) {
        if(!this.mutable)return false;
        ItemStack itemstack = this.getItem();
        return super.mayPickup(player) && !itemstack.isEmpty()
            ? itemstack.isItemEnabled(player.level().enabledFeatures()) && !itemstack.has(DataComponents.CREATIVE_SLOT_LOCK)
            : itemstack.isEmpty();
    }
}
