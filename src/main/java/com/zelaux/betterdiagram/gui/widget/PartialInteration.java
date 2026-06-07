package com.zelaux.betterdiagram.gui.widget;

import com.zelaux.betterdiagram.extend.accessors.AbstractWidgetAccessors;
import com.zelaux.betterdiagram.struct.math.BoundingBox2i;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractContainerWidget;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;

import java.util.List;

public class PartialInteration extends AbstractContainerWidget {
    public final GuiEventListener target;
    private final BoundingBox2i[] include;
    private final BoundingBox2i bounds;
    private final Vector2i prevMouse = new Vector2i(Integer.MIN_VALUE, Integer.MIN_VALUE);
    @Nullable
    private final Drawer<?> drawer;
    private final BoundingBox2i[] exclude;


    public static PartialInteration partialInteration(GuiEventListener target, BoundingBox2i[] include, BoundingBox2i[] exclude) {
        return new PartialInteration(target, null, include, exclude);
    }

    public <T extends GuiEventListener> PartialInteration(T target, @Nullable Drawer<T> drawer, BoundingBox2i first, BoundingBox2i... extra) {
        this(target, drawer, boxes(first, extra), BoundingBox2i.EMPTY_ARRAY);
    }

    public static BoundingBox2i @NotNull [] boxes(BoundingBox2i first, BoundingBox2i... extra) {
        BoundingBox2i[] boxes = new BoundingBox2i[extra.length + 1];
        boxes[0] = first;
        System.arraycopy(extra, 0, boxes, 1, extra.length);
        return boxes;
    }

    public <T extends GuiEventListener> PartialInteration(T target, @Nullable Drawer<T> drawer, BoundingBox2i[] include, BoundingBox2i[] exclude) {
        super(0, 0, 0, 0, Component.literal(""));
        this.drawer = drawer;
        if(include.length == 0) throw new IllegalArgumentException("include boxes are empty");
        this.include = include;
        this.exclude = exclude;
        BoundingBox2i bounds = new BoundingBox2i(include[0]);
        for(int i = 1; i < include.length; i++) bounds.expandTo(include[i]);

        this.setPosition((int) Math.floor(bounds.minX), (int) Math.floor(bounds.minY));
        this.setSize((int) Math.ceil(bounds.maxX), (int) Math.ceil(bounds.maxY));
        this.bounds = bounds;
        if(target instanceof AbstractWidget a) setMessage(a.getMessage());
        //this.message = message;
        this.target = target;

    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        if(!this.active || !this.visible) return false;
        if(!bounds.contains(mouseX, mouseY)) return false;
        for(BoundingBox2i box : exclude) {
            if(box.contains(mouseX, mouseY)) return false;
        }
        for(BoundingBox2i box : include) {
            if(box.contains(mouseX, mouseY)) return true;
        }
        return false;
    }

    @Override
    public void setFocused(boolean focused) {target.setFocused(focused);}

    @Override
    public boolean isFocused() {return target.isFocused();}

    @Override
    protected boolean isValidClickButton(int button) {
        return target instanceof AbstractWidgetAccessors w ? w.bcd$isValidClickButton(button) :
            target instanceof Screen || super.isValidClickButton(button);
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
        return isMouseOver(mouseX, mouseY) && target.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        if(isMouseOver(mouseX, mouseY)) target.mouseMoved(mouseX, mouseY);
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
        if(target instanceof Renderable widget) {
            Vector2i prevMouse = this.prevMouse;
            //int mx = mouseX,my = mouseY;

            if(!isMouseOver(mouseX, mouseY)) {
                //mouseX=prevMouse.x;
                //mouseY=prevMouse.y;
                mouseX = Integer.MIN_VALUE;
                mouseY = Integer.MIN_VALUE;
            }


            if(drawer != null) {
                //noinspection rawtypes,unchecked
                ((Drawer) drawer).render(widget, guiGraphics, mouseX, mouseY, partialTick);
            } else {
                widget.render(guiGraphics, mouseX, mouseY, partialTick);

            }

            prevMouse.set(mouseX, mouseY);
        }
    }

    public interface Drawer<T> {
        void render(T object, GuiGraphics graphics, int mouseX, int mouseY, float partialTick);
    }

    @Override
    protected void updateWidgetNarration(@NotNull NarrationElementOutput narrationElementOutput) {

    }
}
