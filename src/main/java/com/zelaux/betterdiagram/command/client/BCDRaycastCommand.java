package com.zelaux.betterdiagram.command.client;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.zelaux.betterdiagram.command.client.arg.EitherArgumentType;
import com.zelaux.betterdiagram.command.client.arg.MapArgumentType;
import com.zelaux.betterdiagram.command.client.arg.RaycastDirection;
import com.zelaux.betterdiagram.util.RaycastHelper;
import com.zelaux.betterdiagram.util.format.VecFormat;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.companion.math.JOMLConversion;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.Coordinates;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;
import org.joml.Vector3dc;

import java.util.List;

public class BCDRaycastCommand {


    private static final EitherArgumentType<RaycastDirection, Coordinates> directionArgumentType = EitherArgumentType.eitherArgument(
        MapArgumentType.mapArgument(RaycastDirection.nameToDirection),
        Vec3Argument.vec3()
    );
    private static final Vector3d tmp = new Vector3d();

    public static void register(LiteralArgumentBuilder<CommandSourceStack> bcdCommand) {
        LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal("raycast");

        command.then(Commands
            .argument("direction", directionArgumentType)
            .then(Commands
                .argument("maxDistance", FloatArgumentType.floatArg(0.1f, 2000))
                .then(Commands
                    .argument("hitLiquid", BoolArgumentType.bool())
                    .then(Commands
                        .argument("hitEntity", BoolArgumentType.bool())
                        .executes(ctx -> doRaycast(
                            ctx,
                            BoolArgumentType.getBool(ctx, "hitLiquid"),
                            BoolArgumentType.getBool(ctx, "hitEntity")
                        ))

                    )
                    .executes(ctx -> doRaycast(ctx,
                        BoolArgumentType.getBool(ctx, "hitLiquid"),
                        false
                    ))
                )
                .executes(ctx -> doRaycast(ctx, false, false))
            )
        );


        bcdCommand.then(command);
    }

    private static int doRaycast(CommandContext<CommandSourceStack> ctx, boolean hitLiquid, boolean hitEntity) throws CommandSyntaxException {
        var direction = directionArgumentType.get(ctx, "direction");
        var maxDistance = FloatArgumentType.getFloat(ctx, "maxDistance");
        return doRaycast(ctx, direction.map(
            it -> it.direction().apply(ctx),
            it -> List.of(JOMLConversion.toJOML(it.getPosition(ctx.getSource())))
        ), maxDistance, hitLiquid, hitEntity);
    }

    private static int doRaycast(CommandContext<CommandSourceStack> ctx, Iterable<Vector3dc> directions, float maxDistance, boolean hitLiquid, boolean hitEntity) throws CommandSyntaxException {
        CommandSourceStack source = ctx.getSource();
        Level level = source.getUnsidedLevel();
        Entity executor = source.getEntityOrException();

        Vector3dc mainWorldStart = Sable.HELPER.projectOutOfSubLevel(level, JOMLConversion.toJOML(source.getPosition()));
        Vec3 startVec3 = JOMLConversion.toMojang(mainWorldStart);


        for(Vector3dc rawDir : directions) {
            HitResult finalHit = RaycastHelper.getHitResult(level, startVec3, rawDir, maxDistance, executor, hitLiquid, hitEntity, false);
            if(finalHit.getType() != HitResult.Type.MISS) {
                handleResult(source, finalHit, level);
            } else {
                source.sendSuccess(() -> Component.translatable("bcd.command.raycast.nothing"), false);
            }
        }
        return 1;

    }

    private static void handleResult(CommandSourceStack source, HitResult hit, Level level) {
        Vec3 pos = hit.getLocation();
        JOMLConversion.toJOML(pos, tmp);
        MutableComponent posComponent = Component.translatable("bcd.command.raycast.pos", VecFormat.Presets.lightGray(tmp));

        if(hit instanceof BlockHitResult bHit) {
            BlockPos blockPos = bHit.getBlockPos();
            BlockState state = level.getBlockState(blockPos);
            Direction side = bHit.getDirection();

            source.sendSuccess(() -> Component
                    .translatable("bcd.command.raycast.block.name", state.getBlock().getName())
                    .withStyle(
                        it -> it.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(state.toString())))
                    )
                , false);
            source.sendSuccess(() -> Component
                    .translatable("bcd.command.raycast.block.pos", blockPos.toShortString())
                , false);
            source.sendSuccess(() -> Component
                    .translatable("bcd.command.raycast.block.side", side.getName())
                , false);
        } else if(hit instanceof EntityHitResult eHit) {
            Entity entity = eHit.getEntity();

            source.sendSuccess(() -> Component
                    .translatable("bcd.command.raycast.entity.name", entity.getDisplayName())
                    .withStyle(
                        it -> it.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(entity.getType().toString())))
                    )
                , false);
            source.sendSuccess(() -> Component
                    .translatable("bcd.command.raycast.entity.uuid", entity.getStringUUID())
                , false);
        } else return;
        source.sendSuccess(() -> posComponent, false);

    }
}
