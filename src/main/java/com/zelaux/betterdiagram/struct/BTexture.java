package com.zelaux.betterdiagram.struct;

import com.mojang.blaze3d.systems.RenderSystem;
import com.zelaux.betterdiagram.BetterContraptionDiagram;
import lombok.NonNull;
import net.createmod.catnip.gui.UIRenderHelper;
import net.createmod.catnip.gui.element.ScreenElement;
import net.createmod.catnip.theme.Color;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public class BTexture implements ScreenElement {
    @NonNull
    public final ResourceLocation location;

    public final int width, height;
    public final int startX, startY;
    public final double width_grid, height_grid;
    public final double startX_grid, startY_grid;
    public final int texWidth, texHeight;


    public BTexture(final String location, final int width, final int height) {
        this(location, 0, 0, width, height);
    }

    public BTexture(final String location, final int startX, final int startY, final int width, final int height) {
        this(BetterContraptionDiagram.MODID, location, startX, startY, width, height);
    }

    public BTexture(final String namespace, final String location, final int startX, final int startY, final int width, final int height) {
        this(namespace, location, startX, startY, width, height, 256, 256);
    }


    public BTexture(final String namespace, final String location, final int startX, final int startY, final int width, final int height, final int texWidth, final int texHeight) {
        this.location = ResourceLocation.fromNamespaceAndPath(namespace, "textures/" + location + ".png");
        this.width = width;
        this.height = height;
        this.startX = startX;
        this.startY = startY;
        this.texWidth = texWidth;
        this.texHeight = texHeight;

        this.width_grid = width;
        this.height_grid = height;
        this.startX_grid = startX;
        this.startY_grid = startY;
    }

    public BTexture(final int startX, final int startY) {
        this("icons", startX * 16, startY * 16, 16, 16);
    }

    public BTexture(final String location, final int startX, final int startY, final int width, final int height, final int texWidth, final int texHeight) {
        this(BetterContraptionDiagram.MODID, location, startX, startY, width, height, texWidth, texHeight);
    }

    public BTexture(@NonNull final ResourceLocation location, final int startX, final int startY, final int width, final int height, final int texWidth, final int texHeight) {
        this(
            location,
            startX,
            startY,
            width,
            height,
            texWidth,
            texHeight,
            startX,
            startY,
            width,
            height
        );
    }

    public BTexture(@NonNull final ResourceLocation location,
                    final int startX,
                    final int startY,
                    final int width,
                    final int height,
                    final int texWidth,
                    final int texHeight,
                    double startX_grid,
                    double startY_grid,
                    double width_grid,
                    double height_grid
    ) {
        this.location = location;
        this.width = width;
        this.height = height;
        this.startX = startX;
        this.startY = startY;
        this.width_grid = width_grid;
        this.height_grid = height_grid;
        this.startX_grid = startX_grid;
        this.startY_grid = startY_grid;
        this.texWidth = texWidth;
        this.texHeight = texHeight;
    }


    public static BTexture texture(final BTexture owner, final int startX, final int startY, final int width, final int height) {
        return new BTexture(owner.location, startX, startY, width, height, owner.texWidth, owner.texHeight);
    }

    public static BTexture textureGrid(final AtlasTexture owner, final double startX_grid, final double startY_grid, final double width_grid, final double height_grid) {
        return new BTexture(owner.location,
            (int)(startX_grid * owner.gridCellWidth),
            (int)(startY_grid * owner.gridCellHeight),
            (int)(width_grid * owner.gridCellWidth),
            (int)(height_grid * owner.gridCellHeight),
            owner.texWidth,
            owner.texHeight,
            startX_grid,
            startY_grid,
            width_grid,
            height_grid
        );
    }
    public static BTexture textureGrid(final AtlasTexture owner, final int startX_grid, final int startY_grid, final int width_grid, final int height_grid) {
        return new BTexture(owner.location,
            startX_grid * owner.gridCellWidth,
            startY_grid * owner.gridCellHeight,
            width_grid * owner.gridCellWidth,
            height_grid * owner.gridCellHeight,
            owner.texWidth,
            owner.texHeight,
            startX_grid,
            startY_grid,
            width_grid,
            height_grid
        );
    }

    public static BTexture gridCell(final AtlasTexture owner, final int startX, final int startY) {
        return gridCell(owner, startX, startY, owner.gridCellWidth, owner.gridCellHeight);
    }

    public static BTexture gridCell(final BTexture owner, final int startX, final int startY, final int size) {
        return gridCell(owner, startX, startY, size, size);
    }

    public static BTexture gridCell(final BTexture owner, final int startX, final int startY, final int w, final int h) {
        return texture(owner, startX * w, startY * h, w, h);
    }

    public static BTexture texture(final String location, final int width, final int height) {return new BTexture(location, width, height);}

    public static AtlasTexture atlas(final String location, final int width, final int height, int cellWidth, int cellHeight) {return new AtlasTexture(location, width, height, cellWidth, cellHeight);}

    public static BTexture texture(final int startX, final int startY) {return new BTexture(startX, startY);}

    public static BTexture texture(final String location, final int startX, final int startY, final int width, final int height) {return new BTexture(location, startX, startY, width, height);}


    public static BTexture texture(final String location, final int startX, final int startY, final int width, final int height, final int texWidth, final int texHeight) {return new BTexture(location, startX, startY, width, height, texWidth, texHeight);}

    public static BTexture texture(final String namespace, final String location, final int startX, final int startY, final int width, final int height) {return new BTexture(namespace, location, startX, startY, width, height);}

    public static BTexture texture(final ResourceLocation location, final int startX, final int startY, final int width, final int height, final int texWidth, final int texHeight) {return new BTexture(location, startX, startY, width, height, texWidth, texHeight);}

    public static BTexture texture(final String namespace, final String location, final int startX, final int startY, final int width, final int height, final int texWidth, final int texHeight) {return new BTexture(namespace, location, startX, startY, width, height, texWidth, texHeight);}

    public BAnimatedTexture asAnimation(int framesAmount, int deltaWidth, int deltaHeight) {
        return new BAnimatedTexture(this, framesAmount, deltaWidth, deltaHeight);
    }

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
