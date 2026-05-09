package com.zelaux.betterdiagram.struct;

import com.mojang.blaze3d.systems.RenderSystem;
import com.zelaux.betterdiagram.BetterContraptionDiagram;
import net.createmod.catnip.gui.UIRenderHelper;
import net.createmod.catnip.gui.element.ScreenElement;
import net.createmod.catnip.theme.Color;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class BCDTexture implements ScreenElement {
    @NotNull
    public final ResourceLocation location;

    public final int width, height;
    public final int startX, startY;
    public final int texWidth, texHeight;


    public BCDTexture(final String location, final int width, final int height) {
        this(location, 0, 0, width, height);
    }

    public BCDTexture(final String location, final int startX, final int startY, final int width, final int height) {
        this(BetterContraptionDiagram.MODID, location, startX, startY, width, height);
    }

    public BCDTexture(final String namespace, final String location, final int startX, final int startY, final int width, final int height) {
        this(namespace, location, startX, startY, width, height, 256, 256);
    }


    public BCDTexture(final String namespace, final String location, final int startX, final int startY, final int width, final int height, final int texWidth, final int texHeight) {
        final ResourceLocation loc = ResourceLocation.tryBuild(namespace, "textures/" + location + ".png");
        assert loc != null; //location should never be null here, if it is, we messed up
        this.location = loc;
        this.width = width;
        this.height = height;
        this.startX = startX;
        this.startY = startY;
        this.texWidth = texWidth;
        this.texHeight = texHeight;
    }

    public BCDTexture(final int startX, final int startY) {
        this("icons", startX * 16, startY * 16, 16, 16);
    }

    public BCDTexture(final String location, final int startX, final int startY, final int width, final int height, final int texWidth, final int texHeight) {
        this(BetterContraptionDiagram.MODID, location, startX, startY, width, height, texWidth, texHeight);
    }

    public BCDTexture(final ResourceLocation location, final int startX, final int startY, final int width, final int height, final int texWidth, final int texHeight) {
        this.location = location;
        this.width = width;
        this.height = height;
        this.startX = startX;
        this.startY = startY;
        this.texWidth = texWidth;
        this.texHeight = texHeight;
    }


    public static BCDTexture texture(final BCDTexture owner, final int startX, final int startY, final int width, final int height) {
        return new BCDTexture(owner.location, startX, startY, width, height, owner.texWidth, owner.texHeight);
    }
    public static BCDTexture icon(final BCDTexture owner, final int startX, final int startY) {
        return icon(owner,startX,startY,16);
    }
    public static BCDTexture icon(final BCDTexture owner, final int startX, final int startY,final int size) {
        return texture(owner, startX*size, startY*size,size,size);
    }

    public static BCDTexture texture(final String location, final int width, final int height) {return new BCDTexture(location, width, height);}

    public static BCDTexture atlas(final String location, final int width, final int height) {return new BCDTexture(location,0,0, width, height,width, height);}

    public static BCDTexture texture(final int startX, final int startY) {return new BCDTexture(startX, startY);}

    public static BCDTexture texture(final String location, final int startX, final int startY, final int width, final int height) {return new BCDTexture(location, startX, startY, width, height);}


    public static BCDTexture texture(final String location, final int startX, final int startY, final int width, final int height, final int texWidth, final int texHeight) {return new BCDTexture(location, startX, startY, width, height, texWidth, texHeight);}

    public static BCDTexture texture(final String namespace, final String location, final int startX, final int startY, final int width, final int height) {return new BCDTexture(namespace, location, startX, startY, width, height);}

    public static BCDTexture texture(final ResourceLocation location, final int startX, final int startY, final int width, final int height, final int texWidth, final int texHeight) {return new BCDTexture(location, startX, startY, width, height, texWidth, texHeight);}

    public static BCDTexture texture(final String namespace, final String location, final int startX, final int startY, final int width, final int height, final int texWidth, final int texHeight) {return new BCDTexture(namespace, location, startX, startY, width, height, texWidth, texHeight);}

    public void render(final GuiGraphics graphics, final int x, final int y) {
        graphics.blit(this.location, x, y, this.startX, this.startY, this.width, this.height, this.texWidth, this.texHeight);
    }

    public void render(final GuiGraphics graphics, final int x, final int y, final int width, final int height) {
        graphics.blit(this.location, x, y, this.startX, this.startY, width, height, this.texWidth, this.texHeight);
    }

    public void render(final GuiGraphics graphics, final int x, final int y, final Color c) {
        this.bind();
        UIRenderHelper.drawColoredTexture(graphics, c, x, y, 0, this.startX, this.startY, this.width, this.height, this.texWidth, this.texHeight);
    }

    public void bind() {
        RenderSystem.setShaderTexture(0, this.location);
    }
}
