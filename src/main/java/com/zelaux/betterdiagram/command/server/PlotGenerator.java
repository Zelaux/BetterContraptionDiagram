package com.zelaux.betterdiagram.command.server;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ParsedArgument;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import dev.ryanhcode.sable.util.LevelAccelerator;
import lombok.Lombok;
import lombok.SneakyThrows;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.util.Map;

public enum PlotGenerator {
    Stone_Bedrock {
        @Override
        public void generate(LevelAccelerator accelerator, int plotX, int plotZ, int radius, int depth, CommandContext<CommandSourceStack> ctx) {
            BlockState stone = Blocks.STONE.defaultBlockState();
            BlockState bedrock = Blocks.BEDROCK.defaultBlockState();
            for(BlockPos pos : plotPoses(plotX, plotZ, radius, depth)) {
                accelerator.setBlockFast(pos, stone);
            }
            BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
            for(int i = -radius; i <= radius; i++) {
                pos.set(plotX + i, 0, plotZ);
                accelerator.setBlockFast(pos, bedrock);
                pos.set(plotX, 0, plotZ + i);
                accelerator.setBlockFast(pos, bedrock);
            }
        }
    },

    Big_Ponder {
        @Override
        public void generate(LevelAccelerator accelerator, int plotX, int plotZ, int radius, int depth, CommandContext<CommandSourceStack> ctx) {
            int gridShiftX = getOrDef(ctx, "shift_x", 0);
            int gridShiftZ = getOrDef(ctx, "shift_z", 0);
            BlockState[] states = {
                Blocks.WHITE_CONCRETE.defaultBlockState(),
                Blocks.SNOW_BLOCK.defaultBlockState(),
            };
            for(BlockPos pos : PlotGenerator.plotPoses(plotX, plotZ, radius, depth)) {
                int x = pos.getX() + gridShiftX;
                int z = pos.getZ() + gridShiftZ;
                int sig = pos.getY() + x / 3 + z / 3;
                if(x % 3 == 1 && z % 3 == 1) sig++;
                accelerator.setBlockFast(pos, states[sig & 1]);
            }

        }


        private static MethodHandle methodHandle;
        static {
            try {
                Field field = CommandContext.class.getDeclaredField("arguments");
                field.setAccessible(true);
                methodHandle = MethodHandles.lookup().unreflectGetter(field);
            } catch(Exception e) {
                throw Lombok.sneakyThrow(e);
            }
        }

        @SneakyThrows
        private int getOrDef(CommandContext<CommandSourceStack> ctx, String name, int def) {
            Map<String, ParsedArgument<?, ?>> map = (Map<String, ParsedArgument<?, ?>>) methodHandle.invoke(ctx);
            if(!map.containsKey(name)) return def;
            return IntegerArgumentType.getInteger(ctx, name);
        }

        @Override
        public ArgumentBuilder<CommandSourceStack, ?> addArguments(ArgumentBuilder<CommandSourceStack, ?> command, Command<CommandSourceStack> exec) {
            IntegerArgumentType argumentType = IntegerArgumentType.integer(0, 2);
            SuggestionProvider<CommandSourceStack> suggestionProvider =
                (context, builder) ->
                SharedSuggestionProvider.suggest(new String[]{"0", "1", "2"}, builder);

            var shiftX = Commands
                .argument("shift_x", argumentType).suggests(suggestionProvider);
            var shiftZ = Commands
                .argument("shift_z", argumentType).suggests(suggestionProvider);

            return command
                .then(shiftX.then(shiftZ.executes(exec)))
                .executes(exec)
                ;
        }
    },
    Ponder {
        @Override
        public void generate(LevelAccelerator accelerator, int plotX, int plotZ, int radius, int depth, CommandContext<CommandSourceStack> ctx) {
            BlockState[] states = {
                Blocks.WHITE_CONCRETE.defaultBlockState(),
                Blocks.SNOW_BLOCK.defaultBlockState(),
            };
            for(BlockPos pos : PlotGenerator.plotPoses(plotX, plotZ, radius, depth)) {
                int sig = pos.getY() + pos.getX() + pos.getZ();
                accelerator.setBlockFast(pos, states[sig & 1]);
            }

        }
    },

    ;

    private static @NotNull Iterable<BlockPos> plotPoses(int plotX, int plotZ, int radius, int depth) {
        return BlockPos.betweenClosed(
            plotX - radius, -depth, plotZ - radius, plotX + radius, 0, plotZ + radius
        );
    }

    public ArgumentBuilder<CommandSourceStack, ?> addArguments(ArgumentBuilder<CommandSourceStack, ?> command, Command<CommandSourceStack> exec) {
        return command.executes(exec);
    }

    public abstract void generate(
        LevelAccelerator accelerator,
        int plotX, int plotZ,
        int radius, int depth,
        CommandContext<CommandSourceStack> ctx);

}
