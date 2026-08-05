package com.zelaux.betterdiagram.command.client.arg;

import com.mojang.brigadier.Message;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.datafixers.util.Either;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class EitherArgumentType<A, B> implements ArgumentType<Either<A, B>> {




    public static final Dynamic2CommandExceptionType MERGED_ERROR = new Dynamic2CommandExceptionType((a, b) ->
        () -> EitherArgumentType.unpackMessage(a) + "\n" + EitherArgumentType.unpackMessage(b)
    );

    private static String unpackMessage(Object obj) {
        if(obj instanceof Message msg) return msg.getString();
        if(obj instanceof String msg) return msg;
        return null;
    }

    public final ArgumentType<A> a;
    public final ArgumentType<B> b;

    public EitherArgumentType(ArgumentType<A> a, ArgumentType<B> b) {
        this.a = a;
        this.b = b;
    }

    public static <A, B> EitherArgumentType<A, B> eitherArgument(ArgumentType<A> a, ArgumentType<B> b) {
        return new EitherArgumentType<>(a, b);
    }

    @SuppressWarnings("unchecked")
    public static <A, B> Either<A, B> getEither(CommandContext<?> ctx, String name) {
        return ctx.getArgument(name, Either.class);
    }

    @SuppressWarnings("unchecked")
    public Either<A, B> get(CommandContext<?> ctx, String name) {
        return ctx.getArgument(name, Either.class);
    }

    @Override
    public Either<A, B> parse(StringReader reader) throws CommandSyntaxException {
        try {
            return Either.left(a.parse(reader));
        } catch(CommandSyntaxException e_a) {

            try {
                return Either.right(b.parse(reader));
            } catch(CommandSyntaxException e_b) {
                throw MERGED_ERROR.create(e_a.getMessage(), e_b.getMessage());
            }
        }
    }

    @Override
    public <S> Either<A, B> parse(StringReader reader, S source) throws CommandSyntaxException {
        try {
            return Either.left(a.parse(reader, source));
        } catch(CommandSyntaxException e_a) {

            try {
                return Either.right(b.parse(reader, source));
            } catch(CommandSyntaxException e_b) {
                throw MERGED_ERROR.create(e_a.getMessage(), e_b.getMessage());
            }
        }
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        CompletableFuture<Suggestions> futureA = a.listSuggestions(context, builder);
        CompletableFuture<Suggestions> futureB = b.listSuggestions(context, builder);
        return futureA.thenCombine(futureB, (a, b) ->
            Suggestions.merge(context.getInput(), List.of(a, b)
            ));
    }

    @Override
    public Collection<String> getExamples() {
        Collection<String> examples = new ObjectArrayList<>(a.getExamples());
        examples.addAll(b.getExamples());
        return examples;
    }
}
