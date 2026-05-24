package com.zelaux.betterdiagram.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.zelaux.betterdiagram.gui.screen.COM.COMScreen;
import com.zelaux.betterdiagram.util.CenterMassCache;
import com.zelaux.betterdiagram.util.CenterMassCalculator;
import com.zelaux.betterdiagram.util.StringUtil;
import com.zelaux.betterdiagram.util.VecUtil;
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

import java.util.*;
import java.util.stream.Collectors;

public class BCDCommand {

    public static final MutableComponent OBFUSCATED = Component.literal("~~").withStyle(it -> it.withObfuscated(true));

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext buildContext) {
        final LiteralArgumentBuilder<CommandSourceStack> bcdCommand = Commands.literal("better-component-diagram");
        var blocks = buildContext.lookupOrThrow(Registries.BLOCK);
        bcdCommand
            .then(Commands
                .literal("find")
                .then(Commands
                    .argument("axis", ClientEnumArgument.enumArgument(Direction.Axis.class))
                    .then(Commands
                        .argument("value", FloatArgumentType.floatArg(-2, 2))
                        .executes(context -> {
                            Direction.Axis axis = context.getArgument("axis", Direction.Axis.class);
                            float value = FloatArgumentType.getFloat(context, "value");

                            var level = context.getSource().getUnsidedLevel();
                            return findMass(axis, value, blocks, level, context.getSource());
                        })
                    )
                ))
            .then(Commands
                .literal("all")
                .then(Commands
                    .argument("axis", ClientEnumArgument.enumArgument(Direction.Axis.class))
                    .executes(context -> {
                        Direction.Axis axis = context.getArgument("axis", Direction.Axis.class);

                        var level = context.getSource().getUnsidedLevel();
                        return allMass(blocks, axis, level, context.getSource());
                    })
                )
                .executes(context -> {
                    var level = context.getSource().getUnsidedLevel();
                    return allMass(blocks, level, context.getSource());
                })
            )
            .executes(context -> {
                Minecraft minecraft = Minecraft.getInstance();
                minecraft.setScreen(new COMScreen(COMScreen.GLOBAL_CONTEXT,minecraft.player));
                return 1;
            })
        ;

        dispatcher.register(Commands.literal("bcd").redirect(
            dispatcher.register(bcdCommand)
        ));
        dispatcher.register(Commands.literal("bcdresp")
            .executes(x->{
                Minecraft.getInstance().player.respawn();
                x.getSource().sendSuccess(()->Component.literal("Ok."),true);
                return 1;
            }));

    }

    private static int allMass(HolderLookup.RegistryLookup<Block> blocks, Direction.Axis axis, Level level, CommandSourceStack source) {     //ArrayList<BlockState> states = new ArrayList<>();
        int total = 0;
        Map<Double, List<CenterMassCache.Pair>> map = CenterMassCache.getCOM2_Block2States(blocks, level)
                                                                     .values()
                                                                     .stream()
                                                                     .flatMap(it -> it.values().stream())
                                                                     .flatMap(Collection::stream)
                                                                     .collect(Collectors.groupingBy(it -> choose(axis, it.COM)));
        for(var entry : map.entrySet()) {
            var COM = toVecCompoent(entry.getKey(), axis);
            List<CenterMassCache.Pair> pairs = entry.getValue();
            source.sendSuccess(() -> COM
                    .withStyle(it -> it.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, buildPairs(pairs))))
                , true);
        }

        return total;
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

    public static double choose(Direction.Axis axis, Vector3dc com) {
        return axis.choose(com.x(), com.y(), com.z());
    }

    private static int allMass(HolderLookup.RegistryLookup<Block> blocks, Level level, CommandSourceStack source) {
        //ArrayList<BlockState> states = new ArrayList<>();
        int total = 0;
        for(var entry : CenterMassCache.getCOM2_Block2States(blocks, level).entrySet()) {
            Vector3dc COM = entry.getKey();
            var pairs = entry.getValue().keySet();
            source.sendSuccess(() -> VecUtil.vectorToFormatted(COM)
                                            .withStyle(it -> it.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, buildPairs(pairs))))
                , true);
            total++;
        }

        return total;
    }

    private static Component buildPairs(Set<Block> blocks) {
        var it = Component.empty();

        int i = 0;
        for(Block block : blocks) {
            if(i > 0) it = it.append(", ");
            it.append(block.getName());
            i++;
        }
        return it;
    }

    private static Component buildPairs(List<CenterMassCache.Pair> pairs) {
        var it = Component.empty();

        for(int i = 0; i < pairs.size(); i++) {
            CenterMassCache.Pair pair = pairs.get(i);
            if(i > 0) it = it.append(", ");
            it.append(pair.state.getBlock().getName());

        }
        return it;
    }

    private static int findMass(Direction.Axis axis, float value, HolderLookup.RegistryLookup<Block> blocks, Level level, CommandSourceStack source) {
        if(value == 0.5) {
            source.sendFailure(Component.translatable("better_contraption_diagram.command.too-many-blocks"));
            return -1;
        }
        //ArrayList<BlockState> states = new ArrayList<>();
        int total = 0;
        for(var entry : CenterMassCache.getCOM2_Block2States(blocks, level).entrySet()) {
            var vector3dc = entry.getKey();
            var blocksWithStates = entry.getValue();
            if(!CenterMassCalculator.equals(choose(axis, vector3dc), value)) continue;
            for(var block : blocksWithStates.keySet()) {
                source.sendSuccess(() -> toComponent(block), false);
                total++;
            }
        }

        return total;
    }

    private static MutableComponent toComponent(Block block) {
        MutableComponent component = block.getName().copy().withStyle(ChatFormatting.LIGHT_PURPLE);

        Item item = block.asItem();
        if(item != null) {
            component = component.withStyle(x -> x.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, new HoverEvent.ItemStackInfo(new ItemStack(item)))));
        }
        return component;
    }

    private static @NotNull MutableComponent toComponent(BlockState state) {
        MutableComponent component = Component.literal(BlockStateParser.serialize(state)).withStyle(ChatFormatting.LIGHT_PURPLE);

        Item item = getItem(state);
        if(item != null) {
            component = component.withStyle(x -> x.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, new HoverEvent.ItemStackInfo(new ItemStack(item)))));
        }
        return component;
    }

    @Nullable
    private static Item getItem(BlockState state) {
        return state.getBlock().asItem();
    }


    ;
}
