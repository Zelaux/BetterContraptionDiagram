package com.zelaux.betterdiagram.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.zelaux.betterdiagram.gui.screen.COM.COMScreen;
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
        final LiteralArgumentBuilder<CommandSourceStack> bcdCommand = Commands.literal("better-component-diagram");
        bcdCommand
            .executes(context -> {
                Minecraft minecraft = Minecraft.getInstance();
                minecraft.setScreen(new COMScreen(COMScreen.GLOBAL_CONTEXT, minecraft.player));
                return 1;
            })
        ;

        dispatcher.register(Commands.literal("bcd").redirect(
            dispatcher.register(bcdCommand)
        ));
        dispatcher.register(Commands.literal("bcdresp")
                                    .executes(x -> {
                                        Minecraft.getInstance().player.respawn();
                                        x.getSource().sendSuccess(() -> Component.literal("Ok."), true);
                                        return 1;
                                    }));

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
