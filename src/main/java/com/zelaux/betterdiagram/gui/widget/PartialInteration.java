package com.zelaux.betterdiagram.gui.widget;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractContainerWidget;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PartialInteration extends AbstractContainerWidget {
    public final GuiEventListener target;

    public PartialInteration(int x, int y, int width, int height, GuiEventListener target) {
        super(x, y, width, height, target instanceof AbstractWidget a ? a.getMessage() : Component.literal(""));
        this.target = target;

    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return isMouseOver(mouseX, mouseY) && target.mouseClicked(mouseX, mouseY, button);

    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return isMouseOver(mouseX, mouseY) && target.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        return isMouseOver(mouseX, mouseY) && target.mouseDragged(mouseX, mouseY, button,dragX,dragY);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
if( isMouseOver(mouseX, mouseY) ) target.mouseMoved(mouseX, mouseY);
    }

    @Override
    public void onClick(double mouseX, double mouseY, int button) {

         //if(isMouseOver(mouseX, mouseY)) target.onClick(mouseX, mouseY, button);
    }

    @Override
    protected void onDrag(double mouseX, double mouseY, double dragX, double dragY) {

        //super.onDrag(mouseX, mouseY, dragX, dragY);
    }

    @Override
    public void onRelease(double mouseX, double mouseY) {
        //super.onRelease(mouseX, mouseY);
    }

    @Override
    public @NotNull List<? extends GuiEventListener> children() {
        return List.of(target);
    }

    @Override
    protected void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if(target instanceof AbstractWidget widget) {
            widget.render(guiGraphics, mouseX, mouseY, partialTick);
        }
    }

    @Override
    protected void updateWidgetNarration(@NotNull NarrationElementOutput narrationElementOutput) {

    }
}
