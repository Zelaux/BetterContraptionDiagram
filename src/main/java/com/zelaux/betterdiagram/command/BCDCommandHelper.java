package com.zelaux.betterdiagram.command;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import dev.ryanhcode.sable.api.command.SableCommandHelper;
import dev.ryanhcode.sable.api.sublevel.ClientSubLevelContainer;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

public class BCDCommandHelper {
    private static final SimpleCommandExceptionType MISSING_SUBLEVEL_CONTAINER
        = new SimpleCommandExceptionType(Component.translatable("commands.sable.helper.missing_sub_level_container"));
    private static final SimpleCommandExceptionType MISSING_PHYSICS_SYSTEM
        = new SimpleCommandExceptionType(Component.translatable("commands.sable.helper.missing_physics_system"));
    static ClientSubLevelContainer requireSubLevelContainer(CommandSourceStack source) throws CommandSyntaxException {
        final ClientLevel level = (ClientLevel) source.getUnsidedLevel();
        return SableCommandHelper.requireNotNull(SubLevelContainer.getContainer(level), MISSING_SUBLEVEL_CONTAINER);
    }

}
