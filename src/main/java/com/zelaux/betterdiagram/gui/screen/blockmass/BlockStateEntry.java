package com.zelaux.betterdiagram.gui.screen.blockmass;

import com.zelaux.betterdiagram.index.Colors;
import com.zelaux.betterdiagram.util.ui.InplaceBlockRenderer;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

class BlockStateEntry extends AbstractWidget {
    final BlockState state;
    private final ObjectArrayList<FormattedText> tooltipList;
    private final Level level;
    private final int scale;
    InplaceBlockRenderer.WorldContainer container;

    public BlockStateEntry(BlockState state, Level level, int cellSize, int scale, ObjectArrayList<FormattedText> tooltipList) {
        super(0, 0, 0, 0, BlockByMassScreen.LITERAL);
        this.state = state;
        container = new InplaceBlockRenderer.WorldContainer();
        setWidth(cellSize);
        setHeight(cellSize);
        this.tooltipList = tooltipList;
        this.level = level;
        this.scale = scale;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {

        int x = getX();
        int y = getY();
        int x2 = x + getWidth();
        int y2 = y + getHeight();

        InplaceBlockRenderer.renderInplace(guiGraphics, state, container, (x + x2) / 2, (y + y2) / 2, partialTick, level, scale);
        if(isHovered) {
            tooltipList.add(state.getBlock().getName());
            tooltipList.add(Component.literal("   ")
                                                       .append(state.toString())

            );
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

    }
}
