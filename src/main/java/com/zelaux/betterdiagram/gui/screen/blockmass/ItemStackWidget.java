package com.zelaux.betterdiagram.gui.screen.blockmass;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zelaux.betterdiagram.util.ui.GuiGraphicsUtil;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.client.ClientTooltipFlag;

import java.util.List;
import java.util.function.Consumer;

class ItemStackWidget extends AbstractWidget {
    private final ItemStack stack;
    private final int iconSize;
    private final Font font;
    private final Consumer<List<Component>> tooltipSink;
    private final Item.TooltipContext tooltipContext;

    public ItemStackWidget(int cellSize, ItemStack stack, int iconSize, ObjectArrayList<? super FormattedText> tooltipSink, Item.TooltipContext tooltipContext) {
        this(cellSize,stack, iconSize, tooltipSink == null?null: tooltipSink::addAll,tooltipContext);
    }
    public ItemStackWidget(int cellSize, ItemStack stack, int iconSize, Item.TooltipContext tooltipContext) {
        this(cellSize,stack, iconSize, (Consumer<List<Component>>) null,tooltipContext);
    }
    public ItemStackWidget(int cellSize, ItemStack stack, int iconSize, Consumer<List<Component>> tooltipSink, Item.TooltipContext tooltipContext) {
        super(0, 0, 0, 0, BlockByMassScreen.LITERAL);
        this.stack = stack;
        this.iconSize = iconSize;
        this.tooltipSink = tooltipSink;
        this.tooltipContext = tooltipContext;

        setWidth(cellSize);
        setHeight(cellSize);
        font = Minecraft.getInstance().font;
    }
    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int x = getX();
        int y = getY();
        int x2 = x + getWidth();
        int y2 = y + getHeight();


        //guiGraphics.fill(x,y,x2,y2,0x33000000);

        GuiGraphicsUtil.renderSlotContents(
            guiGraphics, stack, font, iconSize, iconSize,
            (x+x2)/2,(y+y2)/2, true, ""
        );

        if(isHovered && tooltipSink != null){
            List<Component> lines = stack.getTooltipLines(
                tooltipContext,
                Minecraft.getInstance().player,
                ClientTooltipFlag.of(Minecraft.getInstance().options.advancedItemTooltips ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL)
            );
            tooltipSink.accept(lines);
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

    }
}
