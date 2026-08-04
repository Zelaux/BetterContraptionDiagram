package com.zelaux.betterdiagram.command.server;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import dev.ryanhcode.sable.util.LevelAccelerator;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

public class BCDDebugCommand {
    public static void register(LiteralArgumentBuilder<CommandSourceStack> bcdCommand) {
        var command = Commands.literal("debug");

        command.then(Commands
            .literal("plot")
            .then(Commands
                .literal("tp")
                .then(Commands
                    .argument("x", IntegerArgumentType.integer())
                    .then(Commands
                        .argument("z", IntegerArgumentType.integer())
                        .executes(ctx -> {
                            CommandSourceStack source = ctx.getSource();
                            ServerPlayer entity = source.getPlayerOrException();
                            int z = IntegerArgumentType.getInteger(ctx, "z");
                            int x = IntegerArgumentType.getInteger(ctx, "x");
                            entity.teleportTo(x * 1000 + 0.5f, 1, z * 1000 + 0.5f);
                            return 1;
                        })
                    )
                )
            )
            .then(getGenerateCommand()
            )
        );

        bcdCommand.then(command);
    }

    private static LiteralArgumentBuilder<CommandSourceStack> getGenerateCommand() {
        var command = Commands.literal("generate");
        for(PlotGenerator type : PlotGenerator.values()) {

            Command<CommandSourceStack> exec = ctx -> {
                CommandSourceStack source = ctx.getSource();
                genPlot(
                    ctx,
                    source.getPlayerOrException(),
                    source.getLevel(),
                    IntegerArgumentType.getInteger(ctx, "radius"),
                    IntegerArgumentType.getInteger(ctx, "depth"),
                    type
                );
                source.sendSuccess(() -> Component.literal("Done."), false);
                return 1;
            };

            ArgumentBuilder<CommandSourceStack, ?> depth;

            ;
            command.then(Commands
                .literal(type.name().toLowerCase())
                .then(Commands
                    .argument("radius", IntegerArgumentType.integer())
                    .then(type.addArguments(Commands
                            .argument("depth", IntegerArgumentType.integer()),
                        exec
                        )
                    )
                ));
        }

        return command;
    }

    private static void genPlot(CommandContext<CommandSourceStack> ctx, ServerPlayer entity, Level level, int radius, int depth, PlotGenerator type) {
        BlockPos blockPos = entity.blockPosition();
        int plotX = blockPos.getX();
        int plotZ = blockPos.getZ();
        LevelAccelerator accelerator = new LevelAccelerator(level);
        type.generate(accelerator, plotX, plotZ, radius, depth, ctx);
    }


}
