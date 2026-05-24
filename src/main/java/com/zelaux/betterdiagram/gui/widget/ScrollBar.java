package com.zelaux.betterdiagram.gui.widget;

import com.zelaux.betterdiagram.struct.math.BoundingBox2i;
import com.zelaux.betterdiagram.struct.math.BoundingBox2ic;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractScrollWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.lwjgl.glfw.GLFW;

public abstract class ScrollBar extends AbstractScrollWidget {


    public ScrollBar(int x, int y, int width, int height, Component message) {
        super(x, y, width + scrollBarWidth, height, message);
    }


    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

    }

    public static final int scrollBarWidth = 8;
    public boolean scrolling;


    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        width -= scrollBarWidth;
        super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
        width += scrollBarWidth;
    }

    @Override
    public void setScrollAmount(double scrollAmount) {
        super.setScrollAmount(scrollAmount);
        updateBarBox();
    }

    public void updateBarBox() {
        int maxScrollAmount = getMaxScrollAmount();
        if(maxScrollAmount==0) {
            barBox.set(0,0,0,0);
            return;}
        int height = this.getScrollBarHeight();
        int x = this.getX() + width - scrollBarWidth;

        int scrollBarOffset = (int) scrollAmount() * (this.height - height) / maxScrollAmount;
        int y = Math.max(this.getY(), getY()+scrollBarOffset);
        barBox.set(
            x, y, x + scrollBarWidth, y + height
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

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if(button != GLFW.GLFW_MOUSE_BUTTON_LEFT) return super.mouseClicked(mouseX, mouseY, button);
        int maxScrollAmount = getMaxScrollAmount();
        if(maxScrollAmount > 0 && barBox.contains((int) mouseX, (int) mouseY)) {
            scrolling = true;
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if(scrolling) scrolling = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if(scrolling) {

            double distance = mouseY - barBox.centerY();
            setScrollAmount(distance / getHeight() * getMaxScrollAmount());
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    protected void renderContents(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {

    }
}
