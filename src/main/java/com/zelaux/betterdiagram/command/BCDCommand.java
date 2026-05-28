package com.zelaux.betterdiagram.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.zelaux.betterdiagram.gui.screen.COM.COMScreen;
import com.zelaux.betterdiagram.util.CenterMassCache;
import com.zelaux.betterdiagram.util.CenterMassCalculator;
import com.zelaux.betterdiagram.util.StringUtil;
import com.zelaux.betterdiagram.util.VecFormat;
import foundry.veil.impl.ClientEnumArgument;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3dc;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
