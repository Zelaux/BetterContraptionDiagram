package com.zelaux.betterdiagram.command.client.arg;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.commands.SharedSuggestionProvider;
import net.neoforged.neoforge.server.command.CommandUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MapArgumentType<T> implements ArgumentType<T> {
    private static final Dynamic2CommandExceptionType INVALID_ENUM = new Dynamic2CommandExceptionType(
        (found, constants) -> CommandUtils.makeTranslatableWithFallback("commands.neoforge.arguments.enum.invalid", constants, found));
    public final Map<String, T> map;

    public MapArgumentType(Map<String, T> map) {
        this.map = map;
    }

    public static <T> MapArgumentType<T> mapArgument(Object2ObjectOpenHashMap<String, T> map) {
        return new MapArgumentType<>(map);
    }

    @Override
    public T parse(StringReader reader) throws CommandSyntaxException {
        String name = reader.readUnquotedString();

        T value = map.get(name);
        if(value != null) return value;

        throw INVALID_ENUM
            .createWithContext(reader, name, Arrays.toString(map.keySet().toArray()));
    }


    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(final CommandContext<S> context, final SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggest(map.keySet(), builder);
    }

    @Override
    public Collection<String> getExamples() {
        return map.keySet();
    }
}
