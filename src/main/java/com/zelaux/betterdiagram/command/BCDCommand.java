package com.zelaux.betterdiagram.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.zelaux.betterdiagram.gui.screen.COM.COMScreen;
import com.zelaux.betterdiagram.gui.screen.pressure.PressureScreen;
import com.zelaux.betterdiagram.util.StringUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class BCDCommand {

    public static final MutableComponent OBFUSCATED = Component.literal("~~").withStyle(it -> it.withObfuscated(true));

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext buildContext) {
        final LiteralArgumentBuilder<CommandSourceStack> bcdCommand = Commands.literal("bcd");
        bcdCommand
            .then(Commands
                .literal("com")
                .executes(context -> {
                    Minecraft minecraft = Minecraft.getInstance();
                    minecraft.setScreen(new COMScreen(COMScreen.GLOBAL_CONTEXT, minecraft.player));
                    return 1;
                })
            )
            .then(Commands
                .literal("pressure")
                .executes(context -> {
                    Minecraft minecraft = Minecraft.getInstance();
                    PressureScreen.show(minecraft.player.level());
                    return 1;
                })
            )
        ;

        dispatcher.register(bcdCommand);
    }

    private static MutableComponent toVecCompoent(double value, Direction.Axis axis) {
        return switch(axis) {
            case X -> Component.literal("(")
                               .append(StringUtil.plainDouble(value))
                               .append(", ")
                               .append(OBFUSCATED)
                               .append(", ")
                               .append(OBFUSCATED)
                               .append(")")
            ;
            case Y -> Component.literal("(")
                               .append(OBFUSCATED)
                               .append(", ")
                               .append(StringUtil.plainDouble(value))
                               .append(", ")
                               .append(OBFUSCATED)
                               .append(")");
            case Z -> Component.literal("(")
                               .append(OBFUSCATED)
                               .append(", ")
                               .append(OBFUSCATED)
                               .append(", ")
                               .append(StringUtil.plainDouble(value))
                               .append(")");
        };
    }


}
