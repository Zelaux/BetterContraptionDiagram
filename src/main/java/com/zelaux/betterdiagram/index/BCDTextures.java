package com.zelaux.betterdiagram.index;

import com.zelaux.betterdiagram.struct.AtlasTexture;
import com.zelaux.betterdiagram.struct.BTexture;

import static com.zelaux.betterdiagram.struct.BTexture.*;

public interface BCDTextures {
    interface COMScreen {
        AtlasTexture atlas = atlas("gui/com_screen", 256, 256, 161, 16);
        BTexture inventory = texture(atlas, 0, 0, 226, 136);
        BTexture previewSlot = texture(atlas, 0, 136, 7 * 4, 7 * 4);
        BTexture previewSlotHovered = texture(atlas, 7 * 4, 136, 7 * 4, 7 * 4);
        BTexture previewSlotSelected = texture(atlas, 2 * 7 * 4, 136, 7 * 4, 7 * 4);

        static BTexture choosePreviewSlotTexture(boolean isHovered, boolean isSelected) {
            return isSelected ? previewSlotSelected : (isHovered ? previewSlotHovered : previewSlot);
        }
    }

    interface Diagram {
        AtlasTexture atlas = atlas("gui/diagram", 512, 256, 16, 16);

        BTexture DIAGRAM_ICON_EXPECTED_COM = gridCell(atlas, 0, 0);
        BTexture DIAGRAM_ICON_EXPECTED_COM_TINY = gridCell(atlas, 1, 0);
        BTexture DIAGRAM_ICON_EXPECTED_COM_ARROW = gridCell(atlas, 2, 0);

        BTexture ICON_TOTAL_DISPLACEMENT = gridCell(atlas, 0, 13);
        BTexture ICON_TOTAL_DISPLACEMENT_TINY = gridCell(atlas, 1, 13);
        BTexture ICON_TOTAL_DISPLACEMENT_ARROW = gridCell(atlas, 2, 13);

        BTexture ICON_BALANCE_CENTER = gridCell(atlas, 0 + 3, 13);
        BTexture ICON_BALANCE_CENTER_TINY = gridCell(atlas, 1 + 3, 13);
        BTexture ICON_BALANCE_CENTER_ARROW = gridCell(atlas, 2 + 3, 13);


        BTexture DIAGRAM_BACKGROUND_64_24 = texture(atlas, 64, 32, 64, 24);
        BTexture DIAGRAM_BACKGROUND_16_20 = texture(atlas, 64, 0, 16, 20);
        BTexture DIAGRAM_BACKGROUND_16_10 = texture(atlas, 96, 0, 16, 10);
        BTexture DIAGRAM_BACKGROUND_12_10 = texture(atlas, 112, 0, 12, 10);

        BTexture SINGLE_POINT_FORCE = gridCell(atlas, 0, 1);
        BTexture MULTIPLE_POINT_FORCE = gridCell(atlas, 1, 1);

        BTexture DIAGRAM_ICON_WEIGHT = gridCell(atlas, 0, 2);
        BTexture DIAGRAM_ICON_WEIGHT_SHADOW = gridCell(atlas, 1, 2);
        BTexture DIAGRAM_ICON_WEIGHT_CROSS = gridCell(atlas, 2, 4);

        BTexture DIAGRAM_ICON_SMALL_WEIGHT = gridCell(atlas, 2, 2);
        BTexture DIAGRAM_ICON_SMALL_WEIGHT_SHADOW = gridCell(atlas, 3, 2);


        BTexture ICON_COLORED_OFF_CENTERED_BLOCK = gridCell(atlas, 0, 11);
        BTexture ICON_COLORED_OFF_CENTERED_BLOCK_PLUS = gridCell(atlas, 1, 11);
        BTexture ICON_COLORED_OFF_CENTERED_BLOCK_CROSS = gridCell(atlas, 2, 11);
        BTexture ICON_OFF_CENTERED_BLOCK_SHADOW = gridCell(atlas, 3, 10);
        BTexture[] ICONS_COLORED_OFF_CENTERED_BLOCK = {ICON_COLORED_OFF_CENTERED_BLOCK_CROSS, ICON_COLORED_OFF_CENTERED_BLOCK, ICON_COLORED_OFF_CENTERED_BLOCK_PLUS};
        BTexture ICON_OFF_CENTERED_BLOCK = gridCell(atlas, 0, 10);
        BTexture ICON_OFF_CENTERED_BLOCK_PLUS = gridCell(atlas, 1, 10);
        BTexture ICON_OFF_CENTERED_BLOCK_CROSS = gridCell(atlas, 2, 10);
        BTexture[] ICONS_OFF_CENTERED_BLOCK = {ICON_OFF_CENTERED_BLOCK_CROSS, ICON_OFF_CENTERED_BLOCK, ICON_OFF_CENTERED_BLOCK_PLUS};


        BTexture DIAGRAM_ICON_CALCULATOR = gridCell(atlas, 0, 3);
        BTexture DIAGRAM_ICON_INFO = gridCell(atlas, 0, 4);
        BTexture ICON_SINGLE_SHIP = gridCell(atlas, 0, 14);
        BTexture ICON_MANY_SHIPS = gridCell(atlas, 1, 14);
        BTexture[] ICONS_SHIPS = {ICON_SINGLE_SHIP,ICON_MANY_SHIPS};

        BTexture ICON_X = gridCell(atlas, 0, 5);
        BTexture ICON_X_CROSS = gridCell(atlas, 1, 5);
        BTexture[] ICONS_X = {ICON_X_CROSS, ICON_X};
        BTexture ICON_Y = gridCell(atlas, 0, 6);
        BTexture ICON_Y_CROSS = gridCell(atlas, 1, 6);
        BTexture[] ICONS_Y = {ICON_Y_CROSS, ICON_Y};
        BTexture ICON_Z = gridCell(atlas, 0, 7);
        BTexture ICON_Z_CROSS = gridCell(atlas, 1, 7);
        BTexture[] ICONS_Z = {ICON_Z_CROSS, ICON_Z};

        BTexture BACKGROUND_XYZ = texture(atlas, 0, 128, 72, 24);

        BTexture DIAGRAM_ICON_BTN_BACKGROUND = gridCell(atlas, 1, 3);
    }

    interface BalloonScreen{
        AtlasTexture atlas = atlas("gui/balloon_graph", 512, 256, 16, 16);

        BTexture BACKGROUND = textureGrid(atlas,0,0,16,12);
        BTexture ICON_CIRCLE = gridCell(atlas,0,12);
        BTexture MODE_BACKGROUND = textureGrid(atlas, BACKGROUND.width_grid + 4,0,7,2);
        BTexture NUMBER_BACKGROUND = textureGrid(atlas,MODE_BACKGROUND.startX_grid,MODE_BACKGROUND.height_grid,3,1);
    }
}
