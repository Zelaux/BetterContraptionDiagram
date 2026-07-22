package com.zelaux.betterdiagram.feature.balloon_volume_calculator.widget;

import com.zelaux.betterdiagram.feature.balloon_volume_calculator.BalloonVolumeCalculatorScreen;
import com.zelaux.betterdiagram.struct.BTexture;
import com.zelaux.betterdiagram.struct.math.BoundingBox2i;
import com.zelaux.betterdiagram.struct.math.BoundingBox2ic;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import org.joml.Vector2i;

public class GraphBackground extends AbstractWidget {
    public final BoundingBox2ic rawDrawableRegion;
    public final BoundingBox2i drawableRegion = new BoundingBox2i();
    public final BoundingBox2i gridDimension;
    public final Vector2i gridSpacing = new Vector2i(16, 16);

    private final BTexture texture;
    public static int gridColor = BalloonVolumeCalculatorScreen.gridColor.getRGB();

    public GraphBackground(BTexture texture, BoundingBox2ic rawDrawableRegion, BoundingBox2i gridDimension) {
        super(0, 0, 0, 0, Component.literal("message"));
        this.rawDrawableRegion = rawDrawableRegion;
        this.texture = texture;
        this.gridDimension = gridDimension;
        drawableRegion.set(rawDrawableRegion);
        setSize(texture.width, texture.height);
    }

    @Override
    public void setX(int x) {
        int wasX = getX();
        super.setX(x);
        drawableRegion.move(x - wasX, 0);
    }

    @Override
    public void setY(int y) {
        int wasY = getY();
        super.setY(y);
        drawableRegion.move(0, y - wasY);
    }


    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        texture.render(guiGraphics, getX(), getY());


        int gridSpacingX = gridSpacing.x;
        int gridSpacingY = gridSpacing.y;
        int offsetMinX = gridDimension.minX % gridSpacingX;
        int offsetMaxX = gridDimension.maxX % gridSpacingX;

        int offsetMinY = gridDimension.minY % gridSpacingY;
        int offsetMaxY = gridDimension.maxY % gridSpacingY;

        for(int x = offsetMinX; x < gridDimension.maxX; x += gridSpacingX) {
            int drawX = ((x-gridDimension.minX) * drawableRegion .width()) / gridDimension.width() + drawableRegion.minX;
            guiGraphics.vLine(drawX, drawableRegion.minY, drawableRegion.maxY, gridColor);
        }
        for(int y = offsetMinY; y < gridDimension.maxY; y += gridSpacingY) {
            int drawY= -(y-gridDimension.minY)*drawableRegion .height()/gridDimension.height() + drawableRegion.maxY;
            guiGraphics.hLine(drawableRegion.minX, drawableRegion.maxX, drawY, gridColor);
        }

    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

    }

    public GraphBackground gridSpacing(int x, int y) {
        gridSpacing.set(x,y);
        return this;
    }
}
