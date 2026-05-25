package com.zelaux.betterdiagram.gui.screen.COM;

import com.zelaux.betterdiagram.gui.widget.ScrollBar;
import com.zelaux.betterdiagram.struct.math.BoundingBox2i;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

public class MyScrollBar extends ScrollBar {
    private final COMScreen comScreen;
    final CenterOfMassMenu menu;

    public MyScrollBar(COMScreen comScreen, int x, int y, int width, int height, Component message) {
        super(x, y, width, height, message);
        this.comScreen = comScreen;
        this.menu = comScreen.getMenu();//175 18
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
    protected void renderBackground(GuiGraphics guiGraphics) {
        //super.renderBackground(guiGraphics);
    }

    @Override
    public void setScrollAmount(double scrollAmount) {
        super.setScrollAmount(scrollAmount);
        menu.scrollTo((float) (this.scrollAmount() / getMaxScrollAmount()));
    }

    //region scroller thing
    private static final ResourceLocation SCROLLER_SPRITE = ResourceLocation.withDefaultNamespace("container/creative_inventory/scroller");
    private static final ResourceLocation SCROLLER_DISABLED_SPRITE = ResourceLocation.withDefaultNamespace("container/creative_inventory/scroller_disabled");

    @Override
    protected @NotNull ResourceLocation getScrollerSprite() {
        return active?SCROLLER_SPRITE:SCROLLER_DISABLED_SPRITE;
    }

    @Override
    public int getScrollBarHeight() {
        return 15;
    }

    @Override
    public void setupScrollBarRegion(BoundingBox2i region) {
        region.setSized(
            getX()+175,getY()+18,
            12,110
        );
    }

    @Override
    public int scrollbarWidth() {
        return 12;
    }
    //endregion
}
