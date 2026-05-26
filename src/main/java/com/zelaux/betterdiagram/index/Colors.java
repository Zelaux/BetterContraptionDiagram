package com.zelaux.betterdiagram.index;

import net.createmod.catnip.theme.Color;
import net.minecraft.ChatFormatting;

@SuppressWarnings("DataFlowIssue")
public interface Colors {
    Color TOOLTIP_COLOR = new Color(0xffc2937d);

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
}
