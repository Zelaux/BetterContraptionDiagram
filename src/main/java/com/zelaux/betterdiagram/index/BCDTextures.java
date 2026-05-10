package com.zelaux.betterdiagram.index;

import com.zelaux.betterdiagram.struct.BCDTexture;

import static com.zelaux.betterdiagram.struct.BCDTexture.*;

public interface BCDTextures {
    int U = 16,U1=U,U2=U*2,U3=U*3;
    int S=16;


    BCDTexture guiDiagram = atlas("gui/diagram",512, 256);


    BCDTexture DIAGRAM_ICON_EXPECTED_COM = icon(guiDiagram, 0, 0);
    BCDTexture DIAGRAM_ICON_EXPECTED_COM_TINY =  icon(guiDiagram, 1, 0);
    BCDTexture DIAGRAM_ICON_EXPECTED_COM_ARROW =  icon(guiDiagram, 2, 0);

    BCDTexture SINGLE_POINT_FORCE = icon(guiDiagram, 0, 1);
    BCDTexture MULTIPLE_POINT_FORCE = icon(guiDiagram,  1, 1);

    BCDTexture DIAGRAM_ICON_WEIGHT= icon(guiDiagram, 0, 2);
    BCDTexture DIAGRAM_ICON_WEIGHT_SHADOW= icon(guiDiagram, 1, 2);

    BCDTexture DIAGRAM_ICON_CALCULATOR = icon(guiDiagram, 0, 3);

    BCDTexture DIAGRAM_ICON_BTN_BACKGROUND = icon(guiDiagram, 1, 3);
}
