package com.zelaux.betterdiagram.gui.widget;

import com.zelaux.betterdiagram.struct.BCDTexture;
import net.createmod.catnip.gui.TickableGuiEventListener;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractContainerWidget;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CompositeWidgetLayouted extends AbstractContainerWidget implements NarratableEntry, Renderable, TickableGuiEventListener {
    private final List<GuiEventListener> children = new ArrayList<>();
    private final List<Renderable> renderables = new ArrayList<>();
    public BCDTexture background;
    private GuiEventListener hovered;
    protected boolean dirtyLayout = true;
    public final LinearLayout layout;

    public CompositeWidgetLayouted background(BCDTexture background) {
        this.background = background;
        return this;
    }

    public CompositeWidgetLayouted(int x, int y, int width, int height, Component message, LinearLayout layout) {
        super(x, y, width, height, message);
        this.layout = layout;
        layout.visitWidgets(this::addWithoutLayout);

    }

    public CompositeWidgetLayouted(Component message, LinearLayout layout) {
        this(0, 0, 0, 0, message, layout);
    }

    public CompositeWidgetLayouted(LinearLayout layout) {
        this(null, layout);
    }

    public <T extends AbstractWidget> T add(T child) {
        addWithoutLayout(child);
        layout.addChild(child);

        dirtyLayout = true;
        return child;
    }

    public <T extends AbstractWidget> T addWithoutLayout(T child) {
        this.children.add(child);

        if(child instanceof Renderable renderable) {
            this.renderables.add(renderable);
        }
        return child;
    }

    public <T extends AbstractWidget> CompositeWidgetLayouted add(T child0, T child1, T... children) {
        add(child0);
        add(child1);
        for(T child : children) add(child);
        return this;
    }

    public <T extends Renderable> T addRenderableOnly(T renderable) {
        this.renderables.add(renderable);
        return renderable;
    }

    public <T extends GuiEventListener> boolean remove(T child) {
        boolean removed = this.children.remove(child);

        if(child instanceof Renderable) {
            removed |= this.renderables.remove(child);
        }

        return removed;
    }

    public <T extends Renderable> boolean removeRenderableOnly(T renderable) {
        return this.renderables.remove(renderable);
    }

    public void clear() {
        this.children.clear();
        this.renderables.clear();
    }

    @Override
    public List<? extends GuiEventListener> children() {
        layout();
        return Collections.unmodifiableList(this.children);
    }


    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        this.getChildAt(mouseX, mouseY).ifPresent(hovered -> this.hovered = hovered);
        if(background != null) {
            background.render(graphics, getX(), getY());
        }
        for(Renderable renderable : this.renderables) {
            renderable.render(graphics, mouseX, mouseY, partialTicks);
        }
    }

    @Override
    public NarrationPriority narrationPriority() {
        if(this.getFocused() instanceof NarratableEntry) {
            return NarrationPriority.FOCUSED;
        } else if(this.hovered instanceof NarratableEntry) {
            return NarrationPriority.HOVERED;
        } else {
            return NarrationPriority.NONE;
        }
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput output) {
        if(this.hovered instanceof NarratableEntry narratable) {
            narratable.updateNarration(output);
        } else if(this.getFocused() instanceof NarratableEntry narratable) {
            narratable.updateNarration(output);
        }
    }

    @Override
    public void tick() {
        layout();
        for(GuiEventListener child : this.children) {
            if(child instanceof TickableGuiEventListener tickable) {
                tickable.tick();
            }
        }
    }

    @Override
    public void setX(int x) {
        super.setX(x);
        invalidateLayout();
    }

    @Override
    public void setY(int y) {
        super.setY(y);
        invalidateLayout();
    }

    public void invalidateLayout() {
        dirtyLayout = true;
    }

    public int margin;

    public CompositeWidgetLayouted margin(int margin) {
        this.margin = margin;
        return this;
    }

    private void layout() {
        if(!dirtyLayout) return;

        layout.setPosition(getX() + margin, getY() + margin);
        layout.arrangeElements();
        width = layout.getWidth() + margin;
        width = layout.getHeight() + margin;
        dirtyLayout = false;
    }


    // these aren't implemented by ContainerEventHandler for some reason

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        layout();
        for(GuiEventListener child : this.children) {
            child.mouseMoved(mouseX, mouseY);
        }
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return this.getChildAt(mouseX, mouseY).isPresent();
    }
}
