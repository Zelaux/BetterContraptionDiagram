package com.zelaux.betterdiagram.struct;

import com.mojang.blaze3d.systems.RenderSystem;
import com.zelaux.betterdiagram.BetterContraptionDiagram;
import net.createmod.catnip.gui.UIRenderHelper;
import net.createmod.catnip.gui.element.ScreenElement;
import net.createmod.catnip.theme.Color;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class AtlasTexture extends BCDTexture {
    public final int gridCellWidth,gridCellHeight;
    public AtlasTexture(String location, int width, int height, int gridCellWidth, int gridCellHeight) {
        super(location,0,0, width, height,width, height);
        this.gridCellWidth = gridCellWidth;
        this.gridCellHeight = gridCellHeight;
    }
}
