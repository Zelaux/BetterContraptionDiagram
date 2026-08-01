package com.zelaux.betterdiagram.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.zelaux.betterdiagram.feature.client_mass_tracker.MassTrackerLevelPlotI;
import dev.ryanhcode.sable.api.command.SableCommandHelper;
import dev.ryanhcode.sable.api.physics.mass.MassTracker;
import dev.ryanhcode.sable.api.sublevel.ClientSubLevelContainer;
import dev.ryanhcode.sable.companion.math.Pose3dc;
import dev.ryanhcode.sable.sublevel.ClientSubLevel;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.*;
import org.joml.Quaterniondc;
import org.joml.Vector3dc;

import java.util.Collection;

public class BCDInfoCommand {
    public static void register(LiteralArgumentBuilder<CommandSourceStack> bcdCommand) {
        bcdCommand.then(Commands
            .literal("sublevel_info")
            .then(Commands
                .argument("sub_level", ClientSubLevelArgumentType.subLevels())
                .executes(ctx -> {
                    final CommandSourceStack source = ctx.getSource();

                    final Collection<ClientSubLevel> subLevels = ClientSubLevelArgumentType.getSubLevels(ctx, "sub_level");

                    if(subLevels.isEmpty()) {
                        throw SableCommandHelper.ERROR_NO_SUB_LEVELS_FOUND.create();
                    }

                    source.sendSuccess(() -> Component.translatable("commands.sable.info.count", subLevels.size()), false);
                    for(final var subLevel : subLevels) {
                        final Pose3dc pose = subLevel.logicalPose();
                        String uuid = subLevel.getUniqueId().toString();
                        source.sendSuccess(() -> {
                            MutableComponent component = Component.translatable("commands.sable.info.name", Component.literal(uuid)).setStyle(
                                Style.EMPTY
                                    .withColor(ChatFormatting.GREEN)
                                    .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, uuid))
                                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("chat.copy.click")))
                                    .withInsertion(uuid)
                            );
                            if(subLevel.getName() != null) {
                                component.append(" - ").append(Component.literal(subLevel.getName()).withStyle(ChatFormatting.DARK_GRAY));
                            }
                            return component;
                        }, false);
                        source.sendSuccess(() -> {
                            final Vector3dc pos = pose.position();
                            return Component.translatable("commands.sable.info.position", pos.x(), pos.y(), pos.z());
                        }, false);
                        /*source.sendSuccess(() -> {
                            final Quaterniondc orientation = pose.orientation();
                            return Component.translatable("commands.sable.info.orientation", orientation.x(), orientation.y(), orientation.z(), orientation.w());
                        }, false);*/
                       /* if(subLevel.getPlot() instanceof MassTrackerLevelPlotI massTrackerLevelPlot) {
                            MassTracker massTracker = massTrackerLevelPlot.bcd$massTracker();
                           if(massTracker!=null) {
                               source.sendSuccess(() -> Component.translatable("commands.sable.info.mass", massTracker.getMass()), false);
                           }
                        }*/
                    }
                    return subLevels.size();
                })
            ));
    }

}
