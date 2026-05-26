package com.zelaux.betterdiagram.gui.comp;

import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;

import java.util.Optional;

public interface MockFormattedText extends FormattedText {
    @Override
    default <T> Optional<T> visit(FormattedText.ContentConsumer<T> p_130779_) {
        return Optional.empty();
    }

    @Override
    default <T> Optional<T> visit(FormattedText.StyledContentConsumer<T> p_130781_, Style p_130782_) {
        return Optional.empty();
    }
}
