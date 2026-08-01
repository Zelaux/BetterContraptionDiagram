package com.zelaux.betterdiagram.gui.screen.blockmass;

import com.mojang.blaze3d.vertex.Tesselator;
import com.zelaux.betterdiagram.struct.math.BoundingBox2i;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.neoforged.neoforge.client.gui.widget.ScrollPanel;

import java.util.List;
import java.util.function.Consumer;

public class Grid extends ScrollPanel {
    private final ObjectArrayList<GuiEventListener> widgets = new ObjectArrayList<>();
    private final ObjectArrayList<Renderable> renderables = new ObjectArrayList<>();
    public int cellSize = 16;
    GridLayout layout = new GridLayout();
    private float partialTick;

    public int columns = 8;
    private BoundingBox2i myBB = new BoundingBox2i();
    private BoundingBox2i childBB = new BoundingBox2i();

    public Grid(Minecraft minecraft, int x, int y, int width, int height) {
        super(minecraft, width+6, height, y, x);

        layout.setPosition(x,y);
    }

    int total = 0;

    public void addWidget(AbstractWidget widget) {
        int column = total % columns;
        int row = total / columns;
        layout.addChild(new Slot(widget, cellSize, cellSize), row, column);
        total++;
        widgets.add(widget);
        renderables.add(widget);
    }

    @Override
    protected int getContentHeight() {
        return layout.getHeight();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.partialTick = partialTick;
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    protected void drawPanel(GuiGraphics guiGraphics, int entryRight, int relativeY, Tesselator tess, int mouseX, int mouseY) {
        layout.setY(relativeY);
        myBB.set(left, bottom, right, top);
        layout.arrangeElements();
        layout.visitWidgets(widget -> {
            childBB.setSized(widget.getX(),widget.getY(),widget.getWidth(),widget.getHeight());
            if(myBB.contains(childBB) || myBB.intersects(childBB)) {
                widget.render(guiGraphics, mouseX, mouseY, partialTick);
            }
        });
    }

    @Override
    public List<? extends GuiEventListener> children() {

        return widgets;
    }

    @Override
    public NarrationPriority narrationPriority() {
        return NarrationPriority.NONE;
    }

    @Override
    public void updateNarration(NarrationElementOutput narrationElementOutput) {

    }


    public record Slot(AbstractWidget widget, int width, int height) implements LayoutElement {
        @Override
        public void setX(int x) {
            widget.setX(x);
        }

        @Override
        public void setY(int y) {
            widget.setY(y);
        }

        @Override
        public int getX() {
            return widget.getX();
        }

        @Override
        public int getY() {
            return widget.getY();
        }

        @Override
        public int getWidth() {
            return width;
        }

        @Override
        public int getHeight() {
            return height;
        }

        @Override
        public void visitWidgets(Consumer<AbstractWidget> consumer) {
            consumer.accept(widget);
        }
    }

}
