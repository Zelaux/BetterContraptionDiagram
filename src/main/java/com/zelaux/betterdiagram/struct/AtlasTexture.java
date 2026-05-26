package com.zelaux.betterdiagram.struct;

public class AtlasTexture extends BCDTexture {
    public final int gridCellWidth, gridCellHeight;

    public AtlasTexture(String location, int width, int height, int gridCellWidth, int gridCellHeight) {
        super(location, 0, 0, width, height, width, height);
        this.gridCellWidth = gridCellWidth;
        this.gridCellHeight = gridCellHeight;
    }
}
