package com.zelaux.betterdiagram.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.suggestion.Suggestions;
import com.zelaux.betterdiagram.gui.screen.COM.COMScreen;
import com.zelaux.betterdiagram.gui.screen.blockmass.BlockByMassScreen;
import com.zelaux.betterdiagram.gui.screen.pressure.PressureScreen;
import com.zelaux.betterdiagram.util.CenterMassCache;
import com.zelaux.betterdiagram.util.StringUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.Comparator;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class BCDCommand {

    public static final MutableComponent OBFUSCATED = Component.literal("~~").withStyle(it -> it.withObfuscated(true));

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext buildContext) {
        final LiteralArgumentBuilder<CommandSourceStack> bcdCommand = Commands.literal("bcd");
        String[] DEAULT_MASS = {"0", "0.25", "0.5", "0.75", "1", "2", "4", "1000"};
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
            .then(Commands
                .literal("mass")
                .then(Commands
                    .argument("mass", FloatArgumentType.floatArg())
                    .suggests((ctx, suggestionsBuilder) -> CenterMassCache
                        .massVariants
                        .getClient()
                        .map(CompletableFuture::completedFuture, Function.identity())
                        .thenCompose(massVariants -> {
                            String[] strings = new String[massVariants.variants.size()];
                            for(int i = 0; i < massVariants.variants.size(); i++) {
                                strings[i] = massVariants.variants.getDouble(i) + "";
                            }

                            return SharedSuggestionProvider.suggest(strings, suggestionsBuilder)                                ;
                        })
                        .thenApply(it->{
                            it.getList().sort(Comparator.comparingDouble(suggestion->Double.parseDouble(suggestion.getText())));
                            return it;
                        })
                    )
                    .executes(ctx -> {
                        float mass = FloatArgumentType.getFloat(ctx, "mass");
                        Minecraft minecraft = Minecraft.getInstance();
                        if(BlockByMassScreen.show(minecraft, minecraft.player.level(), mass)) return 1;

                        return 0;
                    })


                )
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
