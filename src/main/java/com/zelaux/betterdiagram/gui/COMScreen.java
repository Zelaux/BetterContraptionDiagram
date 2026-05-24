package com.zelaux.betterdiagram.gui;

import com.zelaux.betterdiagram.BetterContraptionDiagram;
import com.zelaux.betterdiagram.command.BCDCommand;
import com.zelaux.betterdiagram.extend.AbstractContainerScreenAccessors;
import com.zelaux.betterdiagram.extend.EditBoxAccessors;
import com.zelaux.betterdiagram.gui.widget.PartialInteration;
import com.zelaux.betterdiagram.index.BCDTextures;
import com.zelaux.betterdiagram.struct.BCDTexture;
import com.zelaux.betterdiagram.struct.math.BoundingBox2i;
import com.zelaux.betterdiagram.util.CenterMassCalculator;
import com.zelaux.betterdiagram.util.UIUtil;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractScrollWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.SpacerElement;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
import org.joml.Vector3dc;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nullable;
import java.util.Arrays;

import static com.zelaux.betterdiagram.util.UIUtil.horizontal;
import static com.zelaux.betterdiagram.util.UIUtil.vertical;

public class COMScreen extends AbstractContainerScreen<COMScreen.COMMenu> {
    static final SimpleContainer CONTAINER = new SimpleContainer(45);
    static Entry[] entries = new Entry[]{
        makeEntry(Direction.Axis.X), makeEntry(Direction.Axis.Y), makeEntry(Direction.Axis.Z)
    };
    private final LocalPlayer player;
    private MyScrollBar scrollWidget;
    public final AbstractContainerScreenAccessors accessors = (AbstractContainerScreenAccessors) this;
    public BCDTexture inventoryBG= BCDTextures.COMScreen.atlas;
    public COMScreen(LocalPlayer player) {
        super(new COMMenu(player), player.getInventory(), CommonComponents.EMPTY);
        this.player = player;
        player.containerMenu = this.menu;
        this.imageHeight = 136;
        this.imageWidth = 195;
        //imageWidth=inventoryBG.width;
        //imageHeight=inventoryBG.height;
    }

    private static Entry makeEntry(Direction.Axis axis) {
        final var entry = new Entry(axis);
        var editBox = new EditBox(Minecraft.getInstance().font, 60, 16, null) {
            Tooltip error;
            final EditBoxAccessors accessors = (EditBoxAccessors) (EditBox) this;

            @Override
            public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {

                int x = getX(), y = getY(), x2 = x + getWidth(), y2 = y + getHeight();
                if(error != null && entry.enabled) {
                    setTooltip(error);
                    int color = ChatFormatting.RED.getColor() | 0xff00_0000;
                    guiGraphics.hLine(x - 1, x2, y - 1, color);
                    guiGraphics.hLine(x - 1, x2, y2, color);
                    guiGraphics.vLine(x - 1, y - 1, y2, color);
                    guiGraphics.vLine(x2, y - 1, y2, color);
                } else setTooltip(null);
                boolean was = this.active;
                this.active = entry.enabled && was;
                int i = accessors.bcd$highlightPos();
                if(!entry.enabled) accessors.bcd$highlightPos(getCursorPosition());
                super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
                if(!entry.enabled) {
                    guiGraphics.fill(x, y, x2, y2, 0xbb_00_00_00);
                    accessors.bcd$highlightPos(i);
                }
                this.active = was;
            }

            @Override
            protected boolean isValidClickButton(int button) {
                return button == 0 || button == GLFW.GLFW_MOUSE_BUTTON_RIGHT;
            }

            @Override
            public void onClick(double mouseX, double mouseY, int button) {
                if(button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                    entry.enabled = !entry.enabled;
                    return;
                }
                super.onClick(mouseX, mouseY, button);
            }

            @Override
            public void onClick(double mouseX, double mouseY) {
                super.onClick(mouseX, mouseY);
            }
        };
        entry.editBox = editBox;

        editBox.setFilter(txt -> entry.enabled);

        editBox.setResponder(txt -> {
            try {
                entry.value = Float.parseFloat(txt);
                editBox.error = null;
            } catch(NumberFormatException e) {
                editBox.error = Tooltip.create(Component.literal(e.getLocalizedMessage()));
            }
        });
        return entry;
    }

    public static ResourceLocation createTextureLocation(String name) {
        return ResourceLocation.withDefaultNamespace("textures/gui/container/creative_inventory/tab_" + name + ".png");
    }
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        boolean b = super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        if(b) return b;
        MyScrollBar myScrollBar = getChildAt(mouseX, mouseY).map(x -> (x instanceof PartialInteration p ? p.target : x) instanceof MyScrollBar bar ? bar : null).orElse(null);
        if(myScrollBar == scrollWidget) return false;
        return scrollWidget.isMouseOver(mouseX, mouseY) && scrollWidget.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        Slot clickedSlot = accessors.bcd$clickedSlot();
        if(clickedSlot == null && accessors.bcd$findSlot(mouseX, mouseY) == null) {
            if(this.getFocused() != null && this.isDragging() && button == 0)
                if(this.getFocused().mouseDragged(mouseX, mouseY, button, dragX, dragY)) return true;
        }
        //Slot slot = this.findSlot(mouseX, mouseY);
        //if(slot==null && clickedSlot)
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    ;

    public void handleItemDrop(ItemTossEvent event) {
        if(minecraft == null || minecraft.gameMode == null) return;
        minecraft.gameMode.handleCreativeModeItemDrop(event.getEntity().getItem());
    }

    static boolean inverted;

    @Override
    protected void init() {
        super.init();

        var layout = horizontal(4);

        layout.setPosition(leftPos, 0);
        for(Entry entry : entries) {
            layout.addChild(UIUtil.label(entry.axis.name()), it -> it.alignVerticallyMiddle());
            layout.addChild(entry.editBox);
        }
        layout.addChild(new SpacerElement(10, 0));
        layout.addChild(vertical(0,
            Button.builder(invertedTitle(), button -> {
                      inverted = !inverted;
                      button.setMessage(invertedTitle());
                  })
                  .tooltip(Tooltip.create(Component.translatable("better_contraption_diagram.com-menu.invert-move.info")))
                  .build(),
            Button.builder(Component.translatable("better_contraption_diagram.com-menu.search"), button -> {
                SearchEntry[] searchEntries = new SearchEntry[3];
                int i = 0;
                for(Entry entry : entries) {
                    if(!entry.enabled) continue;
                    searchEntries[i++] = new SearchEntry(entry.axis, inverted ? 1 - entry.value : entry.value);
                }
                doSearch(Arrays.copyOf(searchEntries, i));
            }).build()
        ));

        layout.arrangeElements();
        layout.setY(topPos - 4 - layout.getHeight());
        layout.visitWidgets(this::addRenderableWidget);
        scrollWidget = new MyScrollBar(
            leftPos, topPos, imageWidth, imageHeight,
            null);
        addRenderableOnly(scrollWidget);
        addWidget(new PartialInteration(scrollWidget, null, BoundingBox2i.box2d(
            scrollWidget.getRight(),
            scrollWidget.getY(),
            scrollWidget.getRight() + 8,
            scrollWidget.getBottom()
        )
        ));


    }

    private static Component invertedTitle() {
        return inverted ? Component.translatable("better_contraption_diagram.com-menu.invert-move.inverted") : Component.translatable("better_contraption_diagram.com-menu.invert-mode.normal");
    }

    private void doSearch(SearchEntry[] entries) {
        NonNullList<ItemStack> items = menu.items;
        items.clear();
        var lookup = player.level().registryAccess().lookup(Registries.BLOCK).orElse(null);
        if(lookup == null) return;
        var comStats = BCDCommand.collectMass(lookup, player.level());
        forLoop:
        for(var comStat : comStats.entrySet()) {
            Vector3dc key = comStat.getKey();
            for(var entry : entries) {
                if(!CenterMassCalculator.equals(BCDCommand.choose(entry.axis, key), entry.value)) continue forLoop;
            }
            for(Block block : comStat.getValue().keySet()) {
                Item item = Item.BY_BLOCK.get(block);
                if(item == null) continue;
                items.add(new ItemStack(item));
            }
        }
        this.menu.scrollTo(0);
        scrollWidget.setScrollAmount(0);
        //this.scrollOffs = this.menu.getScrollForRowIndex(i);
        //this.menu.scrollTo(this.scrollOffs);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        //super.renderLabels(guiGraphics, mouseX, mouseY);
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 4210752, false);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        com.mojang.blaze3d.systems.RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        inventoryBG.render(guiGraphics,leftPos,topPos);
    }

    /**
     * Called when the mouse is clicked over a slot or outside the gui.
     */
    @Override
    protected void slotClicked(@Nullable Slot slot, int slotId, int mouseButton, ClickType type) {
        if(slot != null) {
            slotId = slot.index;
        }
        menu.clicked(slotId, mouseButton, type, this.player);
        //this.minecraft.gameMode.handleInventoryMouseClick(this.menu.containerId, slotId, mouseButton, type, this.minecraft.player);
    }

    private boolean isCreativeSlot(@Nullable Slot slot) {
        return slot != null && slot.container == CONTAINER;
    }

    @FieldDefaults(level = AccessLevel.PUBLIC)
    @Getter
    @RequiredArgsConstructor
    static class Entry {
        final Direction.Axis axis;
        EditBox editBox;
        float value = 0.5f;
        boolean enabled = true;
    }

    static record SearchEntry(Direction.Axis axis, float value) {}

    public static class COMMenu extends AbstractContainerMenu {
        /**
         * The list of items in this container.
         */
        public final NonNullList<ItemStack> items = NonNullList.create();
        private final AbstractContainerMenu inventoryMenu;

        public COMMenu(Player player) {
            super(null, 0);
            this.inventoryMenu = player.inventoryMenu;
            Inventory inventory = player.getInventory();

            for(int i = 0; i < 5; i++) {
                for(int j = 0; j < 9; j++) {
                    this.addSlot(new CustomCreativeSlot(CONTAINER, i * 9 + j, 9 + j * 18, 18 + i * 18));
                }
            }

            for(int k = 0; k < 9; k++) {
                this.addSlot(new Slot(inventory, k, 9 + k * 18, 112));
            }

            this.scrollTo(0.0F);
        }

        /**
         * Updates the gui slot's ItemStacks based on scroll position.
         */
        public void scrollTo(float pos) {
            int i = this.getRowIndexForScroll(pos);

            for(int j = 0; j < 5; j++) {
                for(int k = 0; k < 9; k++) {
                    int l = k + (j + i) * 9;
                    if(l >= 0 && l < this.items.size()) {
                        CONTAINER.setItem(k + j * 9, this.items.get(l));
                    } else {
                        CONTAINER.setItem(k + j * 9, ItemStack.EMPTY);
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
                    slot.setByPlayer(ItemStack.EMPTY);
                }
            }

            return ItemStack.EMPTY;
        }

        /**
         * Called to determine if the current slot is valid for the stack merging (double-click) code. The stack passed in is null for the initial slot that was double-clicked.
         */
        @Override
        public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) {
            return slot.container != CONTAINER;
        }

        /**
         * Returns {@code true} if the player can "drag-spilt" items into this slot. Returns {@code true} by default. Called to check if the slot can be added to a list of Slots to split the held ItemStack across.
         */
        @Override
        public boolean canDragTo(Slot slot) {
            return slot.container != CONTAINER;
        }

        @Override
        public ItemStack getCarried() {
            return this.inventoryMenu.getCarried();
        }

        @Override
        public void setCarried(ItemStack stack) {
            this.inventoryMenu.setCarried(stack);
        }


        @OnlyIn(Dist.CLIENT)
        static class CustomCreativeSlot extends Slot {
            public CustomCreativeSlot(Container container, int slot, int x, int y) {
                super(container, slot, x, y);
            }

            @Override
            public void set(ItemStack stack) {
                int x = 0;
            }

            @Override
            public ItemStack safeTake(int count, int decrement, Player player) {
                return super.safeTake(count, decrement, player);
            }

            @Override
            public ItemStack getItem() {
                return super.getItem().copy();
            }

            @Override
            public ItemStack safeInsert(ItemStack stack, int increment) {
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
                ItemStack itemstack = this.getItem();
                return super.mayPickup(player) && !itemstack.isEmpty()
                    ? itemstack.isItemEnabled(player.level().enabledFeatures()) && !itemstack.has(DataComponents.CREATIVE_SLOT_LOCK)
                    : itemstack.isEmpty();
            }
        }
    }

    public class MyScrollBar extends AbstractScrollWidget {
        final COMMenu menu;

        public MyScrollBar(int x, int y, int width, int height, Component message) {
            super(x, y, width, height, message);
            this.menu = COMScreen.this.menu;
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

        }

        @Override
        protected int getInnerHeight() {
            return Mth.positiveCeilDiv(menu.items.size(), 9) * 16;
        }

        @Override
        protected double scrollRate() {
            return 16;
        }

        @Override
        protected void renderContents(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {

        }

        @Override
        public void setScrollAmount(double scrollAmount) {
            super.setScrollAmount(scrollAmount);
            menu.scrollTo((float) (this.scrollAmount() / getMaxScrollAmount()));
        }

    }
}