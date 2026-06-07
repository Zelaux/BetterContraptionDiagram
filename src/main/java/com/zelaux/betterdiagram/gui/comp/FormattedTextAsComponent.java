package com.zelaux.betterdiagram.gui.comp;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public class FormattedTextAsComponent implements Component {
    public final FormattedText text;
    private final FormattedTextComponentContents contents;
    private Language decomposedWith;
    private FormattedCharSequence visualOrderText;

    public FormattedTextAsComponent(FormattedText text) {
        this.text = text;
        contents = new FormattedTextComponentContents(text);
    }
    public static Component create(FormattedText text){
        return new FormattedTextAsComponent(text);
    }
    @Override
    public @NotNull Style getStyle() {
        return Style.EMPTY;
    }

    @Override
    public <T> @NotNull Optional<T> visit(@NotNull StyledContentConsumer<T> acceptor, @NotNull Style p_style) {
        return text.visit(acceptor, p_style);
    }

    @Override
    public <T> @NotNull Optional<T> visit(@NotNull ContentConsumer<T> acceptor) {
        return text.visit(acceptor);
    }


    @Override
    public @NotNull String getString() {return text.getString();}


    @Override
    public @NotNull ComponentContents getContents() {
        return contents;
    }

    @Override
    public @NotNull List<Component> getSiblings() {
        return List.of();
    }

    @Override
    public FormattedCharSequence getVisualOrderText() {
        Language language = Language.getInstance();
        if(this.decomposedWith != language) {
            this.visualOrderText = language.getVisualOrder(this);
            this.decomposedWith = language;
        }

        return this.visualOrderText;
    }


    public static class FormattedTextComponentContents implements ComponentContents {
        public static final Codec<FormattedTextComponentContents> CODEC = new Codec<>() {
            @Override
            public <T> DataResult<T> encode(FormattedTextComponentContents input, DynamicOps<T> ops, T prefix) {
                return DataResult.error(() -> "You should not try to serialize this component");
            }

            @Override
            public <T> DataResult<Pair<FormattedTextComponentContents, T>> decode(DynamicOps<T> ops, T input) {
                return DataResult.error(() -> "You should not try to serialize this component");
            }
        };
        public static final ComponentContents.Type<FormattedTextComponentContents> TYPE = new Type<>(CODEC.fieldOf("__value__"), "formattedTextWrapper");


        private final FormattedText text;

        public FormattedTextComponentContents(FormattedText text) {this.text = text;}

        @Override
        public @NotNull Type<?> type() {
            return TYPE;
        }

        @Override
        public <T> @NotNull Optional<T> visit(@NotNull StyledContentConsumer<T> styledContentConsumer, @NotNull Style style) {
            return text.visit(styledContentConsumer, style);
        }

        @Override
        public <T> @NotNull Optional<T> visit(@NotNull ContentConsumer<T> contentConsumer) {
            return text.visit(contentConsumer);
        }
    }
}
