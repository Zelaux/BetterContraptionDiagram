package com.zelaux.betterdiagram.gui.screen.blockmass;

import com.zelaux.betterdiagram.index.Colors;
import com.zelaux.betterdiagram.util.async.Mass2State;
import com.zelaux.betterdiagram.util.ui.UIUtil;
import dev.simulated_team.simulated.content.entities.diagram.screen.DiagramScreen;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.createmod.catnip.gui.AbstractSimiScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.concurrent.ConcurrentLinkedQueue;

public class BlockByMassScreen extends AbstractSimiScreen {
    public static final MutableComponent LITERAL = Component.literal("");
    private final Level level;
    private static int scale = 16;
    float mass;
    ObjectList<Mass2State.BlockEntry> blockEntries;
    private final ObjectArrayList<FormattedText> tooltipList = new ObjectArrayList<>();

    public BlockByMassScreen(Level level, float mass) {
        HolderLookup.RegistryLookup<Block> blocks = level.registryAccess().lookup(Registries.BLOCK).orElse(null);
        this.blockEntries = Mass2State.calc(blocks, level, mass, new ConcurrentLinkedQueue<>());
        this.mass = mass;
        this.level = level;
    }

    @Override
    protected void init() {
        super.init();
        int cellSize = 32;
        int gridWidth = 10 * cellSize;
        int gridHeight = 10 * cellSize;
        Grid grid = new Grid(minecraft, width / 2 - gridWidth / 2, height / 2 - gridHeight / 2, gridWidth, gridHeight) {
            @Override
            public void addWidget(AbstractWidget widget) {
                //super.addWidget(new WithBackground(widget, Colors.GRAY, Colors.LIGHT_GRAY));
                super.addWidget(widget);
            }
        };
        grid.cellSize = cellSize;
        grid.columns = 10;

        Item.TooltipContext tooltipContext = Item.TooltipContext.of(level);
        for(var entry : blockEntries) {

            if(entry.isSingleMass) {

                Block block = entry.block;
                Item blockItem = Item.BY_BLOCK.get(block);
                if(blockItem == null && block instanceof LiquidBlock liquidBlock) {
                    blockItem = liquidBlock.fluid.getBucket();
                }
                AbstractWidget widget;
                if(blockItem != null) {
                    ItemStack stack = new ItemStack(blockItem);
                    widget = new ItemStackWidget(cellSize, stack, cellSize - 8, tooltipList, tooltipContext);
                } else {
                    widget = new BlockStateEntry(block.defaultBlockState(), level, cellSize, scale, tooltipList);
                }
                grid.addWidget(new WithBackground(
                    widget,
                    Colors.GRAY,
                    Colors.LIGHT_GRAY
                ));
            } else {
                ObjectList<BlockState> states = entry.states;
                IntSet skipIndex=new IntOpenHashSet();
                for(int i = 0; i < states.size(); i++) {
                    if(skipIndex.contains(i))continue;
                    BlockState state = states.get(i);
                    WithBackground widget = new WithBackground(
                        new BlockStateEntry(state, this.level, cellSize, scale, this.tooltipList),
                        Colors.DARK_GRAY,
                        Colors.GRAY
                    );
                    grid.addWidget(widget);
                    if(state.hasProperty(BlockStateProperties.WATERLOGGED)
                       && state.getValue(BlockStateProperties.WATERLOGGED)
                    ) {
                        GridLayout layout = new GridLayout(0, 0);
                        int bucketSize = 12;
                        int spaceSize = cellSize - bucketSize;
                        layout.addChild(UIUtil.space(spaceSize, spaceSize), 0, 0);
                        ItemStackWidget left = new ItemStackWidget(
                            bucketSize,
                            new ItemStack(Items.WATER_BUCKET),
                            bucketSize - 2,
                            it -> {
                                var blockByMassScreen = this;
                                var it2 = state;
                            },
                            tooltipContext
                        );
                        layout.defaultCellSetting().alignVerticallyBottom();
                        layout.addChild(left, 1, 1);
                        int found = states.indexOf(state.setValue(BlockStateProperties.WATERLOGGED, false));
                        if(found>=0){
                            skipIndex.add(found);
                            left.setWidth(bucketSize/3);
                            ItemStackWidget right = new ItemStackWidget(
                                bucketSize,
                                new ItemStack(Items.BUCKET),
                                bucketSize - 2,
                                tooltipContext
                            );
                            right.setWidth(bucketSize/3);
                            layout.addChild(right, 1, 2);
                        }
                        widget.add(layout);
                    }
                }
            }
        }
        addRenderableWidget(grid);


    }

    public static boolean show(Minecraft minecraft, Level level, float mass) {
        BlockByMassScreen screen = new BlockByMassScreen(level, mass);
        if(screen.blockEntries.isEmpty()) return false;
        Minecraft.getInstance().pushGuiLayer(
            screen
        );
        return true;
    }

    @Override
    protected void renderWindow(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {

    }

    @Override
    protected void renderWindowForeground(final GuiGraphics graphics, final int mouseX, final int mouseY, final float partialTicks) {
        if(!this.tooltipList.isEmpty()) {
            DiagramScreen.renderTooltip(graphics, mouseX, mouseY, this.tooltipList);
        }

        this.tooltipList.clear();

        super.renderWindowForeground(graphics, mouseX, mouseY, partialTicks);
    }

}
