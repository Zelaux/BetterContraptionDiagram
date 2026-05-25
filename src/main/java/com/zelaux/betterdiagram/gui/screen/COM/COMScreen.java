package com.zelaux.betterdiagram.gui.screen.COM;

import com.zelaux.betterdiagram.BetterContraptionDiagram;
import com.zelaux.betterdiagram.command.BCDCommand;
import com.zelaux.betterdiagram.extend.AbstractContainerScreenAccessors;
import com.zelaux.betterdiagram.gui.widget.PartialInteration;
import com.zelaux.betterdiagram.index.BCDTextures;
import com.zelaux.betterdiagram.struct.BCDTexture;
import com.zelaux.betterdiagram.struct.math.BoundingBox2i;
import com.zelaux.betterdiagram.util.CenterMassCache;
import com.zelaux.betterdiagram.util.CenterMassCalculator;
import com.zelaux.betterdiagram.util.UIUtil;
import com.zelaux.betterdiagram.util.VecUtil;
import com.zelaux.betterdiagram.util.ui.InplaceBlockRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.SpacerElement;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.joml.Vector3dc;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.zelaux.betterdiagram.util.UIUtil.horizontal;
import static com.zelaux.betterdiagram.util.UIUtil.vertical;

public class COMScreen extends AbstractContainerScreen<CenterOfMassMenu> {
    static final ItemStackHandler CONTAINER = new ItemStackHandler(45);

    static final ItemStackHandler FILTER_HANDLER = new ItemStackHandler(1) {
        @Override
        public void setStackInSlot(int slot, ItemStack stack) {
            super.setStackInSlot(slot, stack);
            if(Minecraft.getInstance().screen instanceof COMScreen comScreen) {
                Context context = comScreen.context;
                context.filterItem(stack.copy());
            }
        }
    };
    public static final Context GLOBAL_CONTEXT = Context.newContext();

    public static final int PREVIEW_PANEL_OFFSET_X = 200;
    public static final int PREVIEW_PANEL_OFFSET_Y = 44;
    public static final int PREVIEW_PANEL_PER_ROW = 3;
    public static final int PREVIEW_PANEL_CELL_SIZE = BCDTextures.COMScreen.previewSlot.width;
    public static final int PREVIEW_PANEL_CELL_HALF_SIZE = PREVIEW_PANEL_CELL_SIZE / 2;
    public final Context context;


    private final LocalPlayer player;
    private MyScrollBar scrollWidget;
    public final AbstractContainerScreenAccessors accessors = (AbstractContainerScreenAccessors) this;
    public BCDTexture inventoryBG = BCDTextures.COMScreen.inventory;
    private float partialTick;

    public COMScreen(Context context, LocalPlayer player) {
        super(new CenterOfMassMenu(
            CONTAINER, 9, 18,
            FILTER_HANDLER, 203, 18,
            player, 9, 112,
            hasInfiniteResources()
        ), player.getInventory(), CommonComponents.EMPTY);
        this.context = context;
        this.player = player;
        player.containerMenu = this.menu;
        this.imageHeight = 136;
        this.imageWidth = 195;
        //imageWidth=inventoryBG.width;
        //imageHeight=inventoryBG.height;
    }

    private static boolean hasInfiniteResources() {
        return Minecraft.getInstance().gameMode != null && Minecraft.getInstance().gameMode.hasInfiniteItems();
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
        if(scrollWidget.isOverBarRegion(mouseX,mouseY))return scrollWidget.mouseClicked(mouseX,mouseY,button);
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if(scrollWidget.scrolling())return scrollWidget.mouseDragged(mouseX, mouseY, button, dragX, dragY);
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
        FILTER_HANDLER.setStackInSlot(0, context.filterItem());


        var layout = horizontal(4);

        layout.setPosition(leftPos, 0);
        var coordsLayout = horizontal(4);
        for(Entry entry : context.entries) {
            coordsLayout.addChild(UIUtil.label(entry.axis.name()), it -> it.alignVerticallyMiddle());
            coordsLayout.addChild(entry.editBox);
        }
        layout.addChild(coordsLayout);
        layout.addChild(new SpacerElement(10, 0));
        layout.addChild(vertical(0,
            Button.builder(invertedTitle(), button -> {
                      inverted = !inverted;
                      button.setMessage(invertedTitle());
                  })
                  .tooltip(Tooltip.create(Component.translatable("better_contraption_diagram.com-menu.invert-move.info")))
                  .build(),
            Button.builder(Component.translatable("better_contraption_diagram.com-menu.search"), button -> performSearch()).build()
        ));

        layout.arrangeElements();
        layout.setY(topPos - 4 - layout.getHeight());
        layout.visitWidgets(this::addRenderableWidget);
        scrollWidget = new MyScrollBar(this,
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
        addWidget(new AbstractWidget(
            PREVIEW_PANEL_OFFSET_X + leftPos, PREVIEW_PANEL_OFFSET_Y + topPos,
            PREVIEW_PANEL_PER_ROW * PREVIEW_PANEL_CELL_SIZE,
            20 * PREVIEW_PANEL_CELL_SIZE, null
        ) {
            @Override
            protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {

            }

            @Override
            protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

            }

            @Override
            public void onClick(double mouseX, double mouseY, int button) {
                Context.COMPair[] pairs = context.pairs();
                //context.selectedPair= context.selectedPair;
                if(pairs == null) return;
                if(button != 0) return;
                int col = (int) ((mouseX - getX()) / PREVIEW_PANEL_CELL_SIZE);
                int row = (int) ((mouseY - getY()) / PREVIEW_PANEL_CELL_SIZE);

                int i = row * PREVIEW_PANEL_PER_ROW + col;
                if(i >= pairs.length) return;
                if(context.selectedPair == i) {
                    context.selectedPair = -1;
                    return;
                }
                context.selectedPair = i;
                Context.COMPair pair = context.pairs()[i];
                context.entries[0].setValue(pair.center().x());
                context.entries[1].setValue(pair.center().y());
                context.entries[2].setValue(pair.center().z());
                performSearch();

            }
        });

    if(context.buildOnInit){
        performSearch();
    }
    }

    private void performSearch() {
        context.buildOnInit=true;
        SearchEntry[] searchEntries = new SearchEntry[3];
        int i = 0;
        for(Entry entry : context.entries) {
            if(!entry.enabled) continue;
            searchEntries[i++] = new SearchEntry(entry.axis, inverted ? 1 - entry.value : entry.value);
        }
        doSearch(Arrays.copyOf(searchEntries, i));
    }

    private static Component invertedTitle() {
        return inverted ? Component.translatable("better_contraption_diagram.com-menu.invert-move.inverted") : Component.translatable("better_contraption_diagram.com-menu.invert-mode.normal");
    }

    record SearchEntry(Direction.Axis axis, float value) {}

    private void doSearch(SearchEntry[] entries) {
        var items = menu.items;
        items.clear();
        items.trimToSize();

        var lookup = player.level().registryAccess().lookup(Registries.BLOCK).orElse(null);
        if(lookup == null) return;
        var comStats = CenterMassCache.getCOM2_Block2States(lookup, player.level());
        long nano=System.nanoTime();
        forLoop:
        for(var comStat : comStats.entrySet()) {
            Vector3dc key = comStat.getKey();
            for(var entry : entries) {
                if(!CenterMassCalculator.equals(BCDCommand.choose(entry.axis, key), entry.value)) continue forLoop;
            }

            for(var entry : comStat.getValue().entrySet()) {
                Block block = entry.getKey();
                Item item = Item.BY_BLOCK.get(block);
                if(item == null) continue;
                items.add(new CenterOfMassMenu.ItemEntry(
                    new ItemStack(item),
                    entry.getValue()
                ));
            }
        }
        long endNano=System.nanoTime();

        BetterContraptionDiagram.LOGGER.debug("Build search result in {}ms",(endNano-nano)/1_000_000.);
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

    boolean wasError = false;
    private final InplaceBlockRenderer.WorldContainer blockEntityWorldContainer = new InplaceBlockRenderer.WorldContainer();

    @Override
    public void removed() {
        super.removed();
        blockEntityWorldContainer.clear();
    }

    @Override
    protected void renderSlotContents(GuiGraphics guiGraphics, ItemStack itemstack, Slot slot, @org.jetbrains.annotations.Nullable String countString) {
        super.renderSlotContents(guiGraphics, itemstack, slot, countString);

    }

    private boolean try3dRender(GuiGraphics guiGraphics, Slot slot) {
        if(!(slot instanceof COMMenu$Slot mySlot) || slot.getItem().isEmpty()) return false;
        if(slot!=hoveredSlot)return false;
        int rowOffset = menu.rowOffset();
        int rawIndex = mySlot.index();
        int rawRow = rawIndex / 9, rawCol = rawIndex % 9;
        int index = rawCol + (rawRow + rowOffset) * 9;

        if(index >= menu.items.size()) return false;
        CenterOfMassMenu.ItemEntry entry = menu.items.get(index);
        if(entry.cachePairs() == null || entry.cachePairs().isEmpty()) return false;

        var pairs = entry.cachePairs();

        renderLeftSide(guiGraphics, pairs, mySlot.entityWorldContainer);

        return false;
    }

    private void renderLeftSide(GuiGraphics guiGraphics, List<CenterMassCache.Pair> pairs, InplaceBlockRenderer.WorldContainer worldContainer) {
        int perRow = PREVIEW_PANEL_PER_ROW;
        int size = PREVIEW_PANEL_CELL_SIZE;

        int ox = leftPos-perRow*size;
        int oy = topPos;
        for(int i = 0; i < pairs.size(); i++) {

            var pair = pairs.get(i);
            int row = i / perRow, col = i % perRow;
            int x = ox + col * size, y = oy + row * size;
            boolean isSelected =false&&  context.selectedPair == i;
            boolean isHovered =false;

            worldContainer.updateBlockstateIfMatch(pair.state);
            BCDTextures.COMScreen.choosePreviewSlotTexture(isHovered, isSelected).render(
                guiGraphics, x, y
            );
            renderBlockState(guiGraphics, pair.state, x, y, partialTick, PREVIEW_PANEL_CELL_HALF_SIZE, 12, worldContainer);
            if(isHovered) {

                guiGraphics.renderTooltip(this.font, List.of(
                    Component.literal(BlockStateParser.serialize(pair.state)),
                    VecUtil.vectorToFormatted(pair.COM)
                ), Optional.empty(), 0, 0);
            }
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if(wasError)return;
        try {
            this.partialTick = partialTick;
            super.render(guiGraphics, mouseX, mouseY, partialTick);

            if(hoveredSlot!=null )try3dRender(guiGraphics, hoveredSlot);
            Slot slot = menu.slots.get(0);
            if(context.blockItem() != null) {
                int ox = leftPos + PREVIEW_PANEL_OFFSET_X, oy = topPos + PREVIEW_PANEL_OFFSET_Y;


                try {
                    int perRow = PREVIEW_PANEL_PER_ROW;
                    int size = PREVIEW_PANEL_CELL_SIZE;
                    Context.COMPair[] pairs = context.pairs();
                    for(int i = 0; i < pairs.length; i++) {
                        Context.COMPair pair = pairs[i];
                        int row = i / perRow, col = i % perRow;
                        int x = ox + col * size, y = oy + row * size;
                        boolean isSelected = context.selectedPair == i;
                        boolean isHovered =
                            x <= mouseX && mouseX <= x + size &&
                            y <= mouseY && mouseY <= y + size;
                        BCDTextures.COMScreen.choosePreviewSlotTexture(isHovered, isSelected).render(
                            guiGraphics, x, y
                        );
                        CenterMassCache.Pair first = pair.pairs().getFirst();
                        renderBlockState(guiGraphics, first.state, x, y, partialTick, PREVIEW_PANEL_CELL_HALF_SIZE, 12, blockEntityWorldContainer);
                        if(isHovered) {

                            guiGraphics.renderTooltip(this.font, List.of(
                                Component.literal(BlockStateParser.serialize(first.state)),
                                VecUtil.vectorToFormatted(first.COM)
                            ), Optional.empty(), mouseX, mouseY);
                        }
                    }

                } catch(Exception e) {
                    e.printStackTrace(System.err);
                    wasError = true;
                }
            }
            com.mojang.blaze3d.systems.RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            this.renderTooltip(guiGraphics, mouseX, mouseY);
        } catch(Exception e) {
            e.printStackTrace(System.err);
            wasError=true;
        }

    }

    private void renderBlockState(GuiGraphics guiGraphics, BlockState state, int x, int y, float partialTick, int size, int scale, InplaceBlockRenderer.WorldContainer blockEntityWorldContainer1) {
        InplaceBlockRenderer.renderInplace(
            guiGraphics,
            state,
            blockEntityWorldContainer1,
            x + size, y + size,
            partialTick,
            Minecraft.getInstance().level,
            scale
        );
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        inventoryBG.render(guiGraphics, leftPos, topPos);
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

}