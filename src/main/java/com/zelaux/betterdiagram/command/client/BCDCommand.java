package com.zelaux.betterdiagram.command.client;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.zelaux.betterdiagram.gui.screen.COM.COMScreen;
import com.zelaux.betterdiagram.gui.screen.blockmass.BlockByMassScreen;
import com.zelaux.betterdiagram.gui.screen.pressure.PressureScreen;
import com.zelaux.betterdiagram.util.CenterMassCache;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;

import java.util.Comparator;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class BCDCommand {

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
            .then(Commands
                .literal("mass")
                .then(Commands
                    .argument("mass", FloatArgumentType.floatArg())
                    .suggests(BCDCommand::massSuggestions)
                    .executes(ctx -> {
                        float mass = FloatArgumentType.getFloat(ctx, "mass");
                        Minecraft minecraft = Minecraft.getInstance();
                        if(BlockByMassScreen.show(minecraft, minecraft.player.level(), mass)) return 1;

                        return 0;
                    })
                )
            )
        ;

        BCDInfoCommand.register(bcdCommand);

        dispatcher.register(bcdCommand);
    }


    private static CompletableFuture<Suggestions> massSuggestions(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder suggestionsBuilder) {
        return CenterMassCache
            .massVariants
            .getClient()
            .map(CompletableFuture::completedFuture, Function.identity())
            .thenCompose(massVariants -> {
                String[] strings = new String[massVariants.variants.size()];
                for(int i = 0; i < massVariants.variants.size(); i++) {
                    strings[i] = massVariants.variants.getDouble(i) + "";
                }

                return SharedSuggestionProvider.suggest(strings, suggestionsBuilder);
            })
            .thenApply(it -> {
                it.getList().sort(Comparator.comparingDouble(suggestion -> Double.parseDouble(suggestion.getText())));
                return it;
            });
    }
}
