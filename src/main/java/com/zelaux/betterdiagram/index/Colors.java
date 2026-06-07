package com.zelaux.betterdiagram.index;

import net.createmod.catnip.theme.Color;
import net.minecraft.ChatFormatting;
import org.jetbrains.annotations.Range;

@SuppressWarnings("DataFlowIssue")
public interface Colors {
    Color TOOLTIP_COLOR = new Color(0xffc2937d);
    Color[] BLUE_GRADIENT = {
        new Color(0xff5555e7),
        new Color(0xff343498),
        new Color(0xff5f41c1),
        new Color(0xff4B3398),
        new Color(0xff505ab8),
        new Color(0xff424B98),
    };

    Color BLACK = new Color(ChatFormatting.BLACK.getColor() | (0xff_00_00_00));
    Color DARK_BLUE = new Color(ChatFormatting.DARK_BLUE.getColor() | (0xff_00_00_00));
    Color DARK_GREEN = new Color(ChatFormatting.DARK_GREEN.getColor() | (0xff_00_00_00));
    Color DARK_AQUA = new Color(ChatFormatting.DARK_AQUA.getColor() | (0xff_00_00_00));
    Color DARK_RED = new Color(ChatFormatting.DARK_RED.getColor() | (0xff_00_00_00));
    Color DARK_PURPLE = new Color(ChatFormatting.DARK_PURPLE.getColor() | (0xff_00_00_00));
    Color GOLD = new Color(ChatFormatting.GOLD.getColor() | (0xff_00_00_00));
    Color LIGHT_GRAY = new Color(ChatFormatting.GRAY.getColor() | (0xff_00_00_00)); //0xaaaaaa //170/255
    Color GRAY = new Color((0x7f7f7f) | (0xff_00_00_00)); //127/255
    Color DARK_GRAY = new Color(ChatFormatting.DARK_GRAY.getColor() | (0xff_00_00_00)); //0x555555 85/255
    Color BLUE = new Color(ChatFormatting.BLUE.getColor() | (0xff_00_00_00));
    Color GREEN = new Color(ChatFormatting.GREEN.getColor() | (0xff_00_00_00));
    Color AQUA = new Color(ChatFormatting.AQUA.getColor() | (0xff_00_00_00));
    Color RED = new Color(ChatFormatting.RED.getColor() | (0xff_00_00_00));
    Color LIGHT_PURPLE = new Color(ChatFormatting.LIGHT_PURPLE.getColor() | (0xff_00_00_00));
    Color YELLOW = new Color(ChatFormatting.YELLOW.getColor() | (0xff_00_00_00));
    Color WHITE = new Color(ChatFormatting.WHITE.getColor() | (0xff_00_00_00));
    Color[] DISPLACEMENT_GREEN_COLORS = {
        new Color(0xdef9e3 | (0xff_00_00_00)),
        new Color(0x79ac82 | (0xff_00_00_00)),
        new Color(0x487554 | (0xff_00_00_00)),
        new Color(0x4d7651 | (0xff_00_00_00)),
        new Color(0x3a5d3e | (0xff_00_00_00)),
        new Color(0x2a3d2a | (0xff_00_00_00)),
    };

    /**
     * @param progress value [0;1] 0<=x<=1
     * @param gradient colors
     *
     */
    static Color mixMany(@Range(from = 0,to = 1) float progress, Color... gradient) {
        float beforeOne = Math.nextDown(1f);
        progress*= beforeOne;//transforming into [0;1)
        float v = progress * (gradient.length-1);
        int i= (int) v;
        float p=(v-i);
        return Color.mixColors(gradient[i],gradient[i+1],p);
    }
}
