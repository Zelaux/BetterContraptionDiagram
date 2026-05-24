package com.zelaux.betterdiagram.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import com.zelaux.betterdiagram.extend.AbstractScrollWidgetAccessors;
import com.zelaux.betterdiagram.struct.math.BoundingBox2i;
import com.zelaux.betterdiagram.struct.math.BoundingBox2ic;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractScrollWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

public abstract class ScrollBar extends AbstractScrollWidget {

    public ScrollBar(int x, int y, int width, int height, Component message) {
        super(x, y, width, height, message);
    }

    @Override
    public void setScrollAmount(double scrollAmount) {
        super.setScrollAmount(scrollAmount);
        updateBarBox();
    }

    public void updateBarBox() {
        int maxScrollAmount = getMaxScrollAmount();
        if(maxScrollAmount == 0) {
            barBox.set(0, 0, 0, 0);
            return;
        }
        int height = this.getScrollBarHeight();
        int x = this.getX() + width - scrollbarWidth();

        int scrollBarOffset = (int) scrollAmount() * (this.height - height) / maxScrollAmount;
        int y = Math.max(this.getY(), getY() + scrollBarOffset);
        barBox.set(
            x, y, x + scrollbarWidth(), y + height
        );
    }


    public int getContentHeight() {
        return this.getInnerHeight() + 4;
    }

    public int getScrollBarHeight() {
        return Mth.clamp((int) ((float) (this.height * this.height) / (float) this.getContentHeight()), 32, this.height);
    }


    @Override
    public double scrollAmount() {return super.scrollAmount();}


    protected final BoundingBox2i barBox = new BoundingBox2i();

    public BoundingBox2ic barBox() {return barBox;}

    public boolean scrolling() {return ((AbstractScrollWidgetAccessors) this).bcd$scrolling();}

    public void scrolling(boolean scrolling) {((AbstractScrollWidgetAccessors) this).bcd$scrolling(scrolling);}

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if(!this.visible) {
            return false;
        } else {
            boolean flag = this.withinContentAreaPoint(mouseX, mouseY);
            boolean flag1 = this.scrollbarVisible()
                            && mouseX >= (double) (this.getX() + this.width)
                            && mouseX <= (double) (this.getX() + this.width + scrollbarWidth())
                            && mouseY >= (double) this.getY()
                            && mouseY < (double) (this.getY() + this.height);
            if(flag1 && button == 0) {
                this.scrolling(true);
                return true;
            } else {
                return flag || flag1;
            }
        }
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if(scrolling()) scrolling(false);
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if(scrolling()) {
            double distance = mouseY - barBox.centerY();
            setScrollAmount(distance / getHeight() * getMaxScrollAmount());
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    protected void renderDecorations(GuiGraphics guiGraphics) {
        if (this.scrollbarVisible()) {
            this.renderScrollBar(guiGraphics);
        }
    }

    private void renderScrollBar(GuiGraphics guiGraphics) {
        int i = this.getScrollBarHeight();
        int j = this.getX() + this.width;
        int k = Math.max(this.getY(), (int) this.scrollAmount() * (this.height - i) / this.getMaxScrollAmount() + this.getY());
updateBarBox();

        RenderSystem.enableBlend();
        guiGraphics.blitSprite(getScrollerSprite(), barBox.minX, barBox.maxY, barBox.width(), barBox.height());
        RenderSystem.disableBlend();
    }
    private static final ResourceLocation SCROLLER_SPRITE = ResourceLocation.withDefaultNamespace("widget/scroller");
    protected @NotNull ResourceLocation getScrollerSprite() {
        return SCROLLER_SPRITE;
    }

    @Override
    protected void renderContents(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {

    }
}
