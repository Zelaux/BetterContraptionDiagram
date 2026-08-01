package com.zelaux.betterdiagram.gui.screen.blockmass;

import com.zelaux.betterdiagram.gui.widget.CompositeWidgetLayouted;
import com.zelaux.betterdiagram.util.ui.UIUtil;
import net.createmod.catnip.theme.Color;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.layouts.LinearLayout;

class WithBackground extends CompositeWidgetLayouted {

    private final Color outline;
    private final Color background;

    public WithBackground(AbstractWidget widget, Color outline, Color background) {
        super(null,UIUtil.stackFill());
        add(widget);
        this.outline = outline;
        this.background = background;
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        int x = getX();
        int y = getY();
        int x2 = x + getWidth();
        int y2 = y + getHeight();

        graphics.fill(x, y, x2, y2, outline.getRGB());
        graphics.fill(x + 1, y + 1, x2 - 1, y2 - 1, background.getRGB());
        super.renderWidget(graphics, mouseX, mouseY, partialTicks);
    }
}
