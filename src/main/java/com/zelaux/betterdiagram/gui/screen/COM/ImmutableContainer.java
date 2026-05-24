package com.zelaux.betterdiagram.gui.screen.COM;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ImmutableContainer extends SimpleContainer {
    public boolean isInfinite;

    public ImmutableContainer infinite(boolean infinite) {
        isInfinite = infinite;
        return this;
    }

    public ImmutableContainer(int size, boolean isInfinite) {
        super(size);
        this.isInfinite = isInfinite;
    }

    public ImmutableContainer(boolean isInfinite, ItemStack... items) {
        super(items);
        this.isInfinite = isInfinite;
    }


    public ImmutableContainer(int size) {
        super(size);
    }

    public ImmutableContainer(ItemStack... items) {
        super(items);
    }

    //region SimpleContainer
    @Override
    public ItemStack getItem(int index) {
        return super.getItem(index);
    }

    @Override
    public ItemStack removeItem(int index, int count) {
        if(!isInfinite)return ItemStack.EMPTY;
        return getItem(index).copyWithCount(count);
    }

    @Override
    public ItemStack removeItemType(Item item, int amount) {
        ItemStack itemStack = new ItemStack(item, 0);
        if(!isInfinite) return itemStack;
        return itemStack.copyWithCount(amount);
    }

    @Override
    public ItemStack addItem(ItemStack stack) {
        return stack;
    }

    @Override
    public boolean canAddItem(ItemStack stack) {
        return false;
    }

    @Override
    public ItemStack removeItemNoUpdate(int index) {
        return isInfinite ? getItem(index).copy() : ItemStack.EMPTY;
    }

    @Override
    public void setItem(int index, ItemStack stack) {
        //super.setItem(index, stack);
    }


    @Override
    public String toString() {
        return super.toString();
    }
    //endregion


    //region Container

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return false;
    }

    @Override
    public boolean canTakeItem(Container target, int slot, ItemStack stack) {
        return isInfinite;
    }

    //endregion


}
