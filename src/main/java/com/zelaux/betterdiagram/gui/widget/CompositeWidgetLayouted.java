package com.zelaux.betterdiagram.gui.widget;

import com.zelaux.betterdiagram.struct.BTexture;
import lombok.AllArgsConstructor;
import lombok.experimental.Delegate;
import net.createmod.catnip.gui.TickableGuiEventListener;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractContainerWidget;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.*;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;

public class CompositeWidgetLayouted extends AbstractContainerWidget implements NarratableEntry, Renderable, TickableGuiEventListener {
    private final List<GuiEventListener> children = new ArrayList<>();
    private final List<Renderable> renderables = new ArrayList<>();
    public BTexture background;
    private GuiEventListener hovered;
    protected boolean dirtyLayout = true;
    public final WrappedLayout layout;

    public CompositeWidgetLayouted background(BTexture background) {
        this.background = background;
        return this;
    }

    public CompositeWidgetLayouted(Component message, LinearLayout layout) {
        this(message, layout, LinearLayout::addChild);
    }

    public CompositeWidgetLayouted(Component message, FrameLayout layout) {
        this(message, layout, FrameLayout::addChild);
    }

    public <T extends Layout> CompositeWidgetLayouted(Component message, T layout, BiConsumer<T, LayoutElement> addChild) {
        super(0, 0, 0, 0, message);
        layout.visitWidgets(this::addWithoutLayout);
        this.layout = WrappedLayout.wrap(layout, addChild);
    }

    public CompositeWidgetLayouted(Component message, EqualSpacingLayout layout) {
        this(message, layout, EqualSpacingLayout::addChild);
    }

    public CompositeWidgetLayouted(LinearLayout layout) {
        this(null, layout);
    }


    public <T extends LayoutElement> T add(T child) {
        addWithoutLayout(child);
        layout.addChild(child);

        dirtyLayout = true;
        return child;
    }

    public <T extends LayoutElement> T addWithoutLayout(T child) {
        child.visitWidgets(widget -> {
            children.add(widget);
            if(widget instanceof Renderable renderable) {
                renderables.add(renderable);
            }
        });

        return child;
    }


    @SafeVarargs
    public final <T extends AbstractWidget> CompositeWidgetLayouted add(T child0, T child1, T... children) {
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

    BackgroundAlignment xBackgroundAlign = BackgroundAlignment.MIN, yBackgroundAlign = BackgroundAlignment.MIN;

    public CompositeWidgetLayouted backgroundAlign(BackgroundAlignment x, BackgroundAlignment y) {
        xBackgroundAlign = Objects.requireNonNullElse(x, xBackgroundAlign);
        yBackgroundAlign = Objects.requireNonNullElse(y, yBackgroundAlign);
        return this;
    }

    public enum BackgroundAlignment {
        MIN, CENTER, MAX;
        public final int value = ordinal() - 1;
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
            background.render(graphics,
                locateCoordinate(getX(), getWidth(), background.width, xBackgroundAlign),
                locateCoordinate(getY(), getHeight(), background.height, yBackgroundAlign)
            );
        }
        for(Renderable renderable : this.renderables) {
            renderable.render(graphics, mouseX, mouseY, partialTicks);
        }
    }

    static int locateCoordinate(int x, int width, int innerWidth, BackgroundAlignment alignment) {
        return switch(alignment) {
            case MIN -> x;
            case CENTER -> x + width / 2 - innerWidth / 2;
            case MAX -> x + width - innerWidth;
        };
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
        width = layout.getWidth() + margin * 2;
        height = layout.getHeight() + margin * 2;
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

    @AllArgsConstructor
    public static abstract class WrappedLayout implements Layout {
        public static <T extends Layout> WrappedLayout wrap(T layout, BiConsumer<T, LayoutElement> addChild) {
            return new WrappedLayout(layout) {
                @Override
                void addChild(LayoutElement element) {
                    addChild.accept(layout, element);
                }
            };
        }

        @Delegate
        public Layout x;

        abstract void addChild(LayoutElement element);
    }
}
