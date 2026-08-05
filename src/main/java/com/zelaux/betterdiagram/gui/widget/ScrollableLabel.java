package com.zelaux.betterdiagram.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.foundation.gui.widget.Label;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.client.extensions.IGuiGraphicsExtension;
import org.jetbrains.annotations.NotNull;

public class ScrollableLabel extends Label {
    public ScrollableLabel(int x, int y) {super(x, y, CommonComponents.EMPTY);}

    @Override
    protected void doRender(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {

        int x = getX(), y = getY();
        int x2 = getRight(), y2 = getBottom();
        //int red=0xffff0000;
        //graphics.hLine(x,x2,y,red);
        //graphics.hLine(x,x2,y2,red);
        //graphics.vLine(x,y,y2,red);
        //graphics.vLine(x2,y,y2,red);
        if(text == null || text.getString().isEmpty())
            return;

        RenderSystem.setShaderColor(1, 1, 1, 1);
        MutableComponent copy = text.plainCopy();
        if(suffix != null && !suffix.isEmpty())
            copy.append(suffix);
        int textWidth = font.width(copy);
        int renderY = (y + y2 - font.lineHeight) >> 1;
        if(textWidth < width) {
            graphics.drawString(font, copy, x, renderY, color, hasShadow);
            return;
        }
        int deltaLen = textWidth - width;
        double d0 = Util.getMillis() / 1000.0;
        double d1 = Math.max(deltaLen * 0.5, 3.0);
        double d2 = Math.sin((Math.PI / 2) * Math.cos((Math.PI * 2) * d0 / d1)) / 2.0 + 0.5;
        double d3 = Mth.lerp(d2, 0.0, deltaLen);
        graphics.enableScissor(x, y, x2, y2);
        graphics.drawString(font, copy, x - (int) d3, renderY, color, hasShadow);
        graphics.disableScissor();
    }
}
