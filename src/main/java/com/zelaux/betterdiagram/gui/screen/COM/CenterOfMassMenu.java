package com.zelaux.betterdiagram.gui.screen.COM;

import com.google.common.collect.Lists;
import com.zelaux.betterdiagram.util.CenterMassCache;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.core.NonNullList;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.SlotItemHandler;

import java.util.ArrayList;
import java.util.List;

public class CenterOfMassMenu extends AbstractContainerMenu {
    /**
     * The list of items in this container.
     */
    public final ArrayList<ItemEntry> items = new ArrayList<>();
    private final AbstractContainerMenu inventoryMenu;
    private final IItemHandlerModifiable container;
    private final IItemHandlerModifiable filterContainer;
    @Getter
    private int rowOffset;

    public record ItemEntry(ItemStack stack, List<CenterMassCache.Pair> cachePairs){

    }

    public CenterOfMassMenu(
        IItemHandlerModifiable container, int containerX, int containerY,
        IItemHandlerModifiable filterContainer, int filterX, int filterY,
        Player player, int playerX, int playerY,
        boolean isInfinite) {
        super(null, 0);
        this.inventoryMenu = player.inventoryMenu;
        this.container=container;
        this.filterContainer=filterContainer;
        Inventory inventory = player.getInventory();

        addSlot(new SlotItemHandler(filterContainer,0,filterX,filterY){
            @Override
            public void onTake(Player player, ItemStack stack) {
                stack.setCount(0);
                this.set(ItemStack.EMPTY);
            }

            @Override
            public ItemStack safeTake(int count, int decrement, Player player) {
                this.set(ItemStack.EMPTY);
                return ItemStack.EMPTY;
            }

            @Override
            public ItemStack safeInsert(ItemStack stack, int increment) {
                trySetFilterSlot(Minecraft.getInstance().player, stack,this);
                return stack;
            }
        });
        for(int i = 0; i < 5; i++) {
            for(int j = 0; j < 9; j++) {
                COMMenu$Slot slot = new COMMenu$Slot(container, i * 9 + j, containerX + j * 18, containerY + i * 18);
                slot.mutable=isInfinite && false;
                this.addSlot(slot);
            }
        }

        for(int k = 0; k < 9; k++) {
            this.addSlot(new Slot(inventory, k, playerX + k * 18, playerY));
        }

        this.scrollTo(0.0F);
    }

    /**
     * Updates the gui slot's ItemStacks based on scroll position.
     */
    public void scrollTo(float pos) {
        int i = this.getRowIndexForScroll(pos);
        this.rowOffset=i;
        for(int j = 0; j < 5; j++) {
            for(int k = 0; k < 9; k++) {
                int l = k + (j + i) * 9;
                if(l >= 0 && l < this.items.size()) {
                    container.setStackInSlot(k + j * 9, this.items.get(l).stack);
                } else {
                    container.setStackInSlot(k + j * 9, ItemStack.EMPTY);
                }
            }
        }
    }

    protected int getRowIndexForScroll(float scrollOffs) {
        return Math.max((int) ((double) (scrollOffs * (float) this.calculateRowCount()) + 0.5), 0);
    }

    protected int calculateRowCount() {
        return Mth.positiveCeilDiv(this.items.size(), 9) - 5;
    }

    /**
     * Determines whether supplied player can use this container
     */
    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    protected float getScrollForRowIndex(int rowIndex) {
        return Mth.clamp((float) rowIndex / (float) this.calculateRowCount(), 0.0F, 1.0F);
    }

    protected float subtractInputFromScroll(float scrollOffs, double input) {
        return Mth.clamp(scrollOffs - (float) (input / (double) this.calculateRowCount()), 0.0F, 1.0F);
    }

    public boolean canScroll() {
        return this.items.size() > 45;
    }

    /**
     * Handle when the stack in slot {@code index} is shift-clicked. Normally this moves the stack between the player inventory and the other inventory(s).
     */
    @Override
    public ItemStack quickMoveStack(Player player, int index) {

        if(index >= this.slots.size() - 9 && index < this.slots.size()) {
            Slot slot = this.slots.get(index);
            if(slot != null && slot.hasItem()) {
                ItemStack item = slot.getItem();
                Slot slot1 = slots.get(0);
                trySetFilterSlot(player, item, slot1);
                //slot.setByPlayer(ItemStack.EMPTY);
            }
        }

        return ItemStack.EMPTY;
    }

    private void trySetFilterSlot(Player player, ItemStack item, Slot slot1) {
        if(item.getItem() instanceof BlockItem blockItem && !CenterMassCache.getBlock2Pairs(player).get(blockItem.getBlock()).isEmpty()){
            slot1.set(item.copyWithCount(1));
        }
    }

    /**
     * Called to determine if the current slot is valid for the stack merging (double-click) code. The stack passed in is null for the initial slot that was double-clicked.
     */
    @Override
    public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) {
        return false;
    }

    /**
     * Returns {@code true} if the player can "drag-spilt" items into this slot. Returns {@code true} by default. Called to check if the slot can be added to a list of Slots to split the held ItemStack across.
     */
    @Override
    public boolean canDragTo(Slot slot) {
        return slot.container == this.inventoryMenu;
    }

    @Override
    public ItemStack getCarried() {
        return this.inventoryMenu.getCarried();
    }

    @Override
    public void setCarried(ItemStack stack) {
        this.inventoryMenu.setCarried(stack);
    }

}
