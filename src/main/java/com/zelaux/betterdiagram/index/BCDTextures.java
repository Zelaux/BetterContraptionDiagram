package com.zelaux.betterdiagram.index;

import com.zelaux.betterdiagram.struct.AtlasTexture;
import com.zelaux.betterdiagram.struct.BCDTexture;

import static com.zelaux.betterdiagram.struct.BCDTexture.*;

public interface BCDTextures {
    int U = 16, U1 = U, U2 = U * 2, U3 = U * 3;
    int S = 16;

    interface COMScreen {
        AtlasTexture atlas = atlas("gui/com_screen", 256, 256, 161, 16);
        BCDTexture inventory = texture(atlas, 0, 0, 226, 136);
        BCDTexture previewSlot = texture(atlas, 0, 136, 7 * 4, 7 * 4);
        BCDTexture previewSlotHovered = texture(atlas, 7 * 4, 136, 7 * 4, 7 * 4);
        BCDTexture previewSlotSelected = texture(atlas, 2 * 7 * 4, 136, 7 * 4, 7 * 4);

        static BCDTexture choosePreviewSlotTexture(boolean isHovered, boolean isSelected) {
            return isSelected ? previewSlotSelected : (isHovered ? previewSlotHovered : previewSlot);
        }
    }

    interface Diagram {
        AtlasTexture atlas = atlas("gui/diagram", 512, 256, 16, 16);

        BCDTexture DIAGRAM_ICON_EXPECTED_COM = gridCell(atlas, 0, 0);
        BCDTexture DIAGRAM_ICON_EXPECTED_COM_TINY = gridCell(atlas, 1, 0);
        BCDTexture DIAGRAM_ICON_EXPECTED_COM_ARROW = gridCell(atlas, 2, 0);


        BCDTexture DIAGRAM_BACKGROUND_64_24 = texture(atlas, 64, 32, 64, 24);
        BCDTexture DIAGRAM_BACKGROUND_16_20 = texture(atlas, 64, 0, 16, 20);
        BCDTexture DIAGRAM_BACKGROUND_16_10 = texture(atlas, 96, 0, 16, 10);
        BCDTexture DIAGRAM_BACKGROUND_12_10 = texture(atlas, 112, 0, 12, 10);

        BCDTexture SINGLE_POINT_FORCE = gridCell(atlas, 0, 1);
        BCDTexture MULTIPLE_POINT_FORCE = gridCell(atlas, 1, 1);

        BCDTexture DIAGRAM_ICON_WEIGHT = gridCell(atlas, 0, 2);
        BCDTexture DIAGRAM_ICON_WEIGHT_SHADOW = gridCell(atlas, 1, 2);
        BCDTexture DIAGRAM_ICON_SMALL_WEIGHT = gridCell(atlas, 2, 2);
        BCDTexture DIAGRAM_ICON_SMALL_WEIGHT_SHADOW = gridCell(atlas, 3, 2);


        BCDTexture ICON_COLORED_OFF_CENTERED_BLOCK = gridCell(atlas, 0, 11);
        BCDTexture ICON_COLORED_OFF_CENTERED_BLOCK_PLUS = gridCell(atlas, 1, 11);
        BCDTexture ICON_COLORED_OFF_CENTERED_BLOCK_CROSS = gridCell(atlas, 2, 11);
        BCDTexture ICON_OFF_CENTERED_BLOCK_SHADOW = gridCell(atlas, 3, 10);
        BCDTexture[] ICONS_COLORED_OFF_CENTERED_BLOCK = {ICON_COLORED_OFF_CENTERED_BLOCK_CROSS, ICON_COLORED_OFF_CENTERED_BLOCK, ICON_COLORED_OFF_CENTERED_BLOCK_PLUS};
        BCDTexture ICON_OFF_CENTERED_BLOCK = gridCell(atlas, 0, 10);
        BCDTexture ICON_OFF_CENTERED_BLOCK_PLUS = gridCell(atlas, 1, 10);
        BCDTexture ICON_OFF_CENTERED_BLOCK_CROSS = gridCell(atlas, 2, 10);
        BCDTexture[] ICONS_OFF_CENTERED_BLOCK = {ICON_OFF_CENTERED_BLOCK_CROSS, ICON_OFF_CENTERED_BLOCK, ICON_OFF_CENTERED_BLOCK_PLUS};


        BCDTexture DIAGRAM_ICON_CALCULATOR = gridCell(atlas, 0, 3);
        BCDTexture DIAGRAM_ICON_INFO = gridCell(atlas, 0, 4);

        BCDTexture ICON_X = gridCell(atlas, 0, 5);
        BCDTexture ICON_X_CROSS = gridCell(atlas, 1, 5);
        BCDTexture[] ICONS_X = {ICON_X_CROSS, ICON_X};
        BCDTexture ICON_Y = gridCell(atlas, 0, 6);
        BCDTexture ICON_Y_CROSS = gridCell(atlas, 1, 6);
        BCDTexture[] ICONS_Y = {ICON_Y_CROSS, ICON_Y};
        BCDTexture ICON_Z = gridCell(atlas, 0, 7);
        BCDTexture ICON_Z_CROSS = gridCell(atlas, 1, 7);
        BCDTexture[] ICONS_Z = {ICON_Z_CROSS, ICON_Z};

        BCDTexture BACKGROUND_XYZ = texture(atlas, 0, 128, 72, 24);

        BCDTexture DIAGRAM_ICON_BTN_BACKGROUND = gridCell(atlas, 1, 3);
    }
}
