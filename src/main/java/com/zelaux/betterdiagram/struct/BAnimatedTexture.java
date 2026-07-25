package com.zelaux.betterdiagram.struct;

import net.createmod.catnip.gui.UIRenderHelper;
import net.createmod.catnip.theme.Color;
import net.minecraft.client.gui.GuiGraphics;

public class BAnimatedTexture {
    public final BTexture origin;
    public final int framesAmount;
    public final int deltaWidth;
    public final int deltaHeight;

    public BAnimatedTexture(BTexture origin, int framesAmount, int deltaWidth, int deltaHeight) {
        this.origin = origin;
        this.framesAmount = framesAmount;
        this.deltaWidth = deltaWidth;
        this.deltaHeight = deltaHeight;
    }

    public void render(GuiGraphics graphics, int x, int y, int frame) {
        frame=Math.clamp(frame,0,framesAmount-1);
        graphics.blit(origin.location,
            x, y,
            origin.startX + frame * deltaWidth, origin.startY + frame * deltaHeight,
            origin.width, origin.height,
            origin.texWidth, origin.texHeight
        );
    }

    public void render(GuiGraphics graphics, int x, int y, int width, int height, int frame) {
        frame=Math.clamp(frame,0,framesAmount-1);
        graphics.blit(origin.location,
            x, y,
            origin.startX + frame * deltaWidth, origin.startY + frame * deltaHeight,
            width, height,
            origin.texWidth, origin.texHeight
        );
    }

    public void render(GuiGraphics graphics, int x, int y, Color c, int frame) {
        origin.bind();
        frame=Math.clamp(frame,0,framesAmount-1);
        UIRenderHelper.drawColoredTexture(graphics, c, x, y, 0,
            origin.startX + frame * deltaWidth, origin.startY + frame * deltaHeight,
            origin.width, origin.height,
            origin.texWidth, origin.texHeight
        );
    }
}
