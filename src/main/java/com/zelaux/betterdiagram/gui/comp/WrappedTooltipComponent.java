package com.zelaux.betterdiagram.gui.comp;

import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class WrappedTooltipComponent implements MockFormattedText, TooltipComponent {
    public final ClientTooltipComponent component;

    public WrappedTooltipComponent(ClientTooltipComponent component) {this.component = component;}

    public static class MockComponent implements Component {
        public final WrappedTooltipComponent component;
        public static final PlainTextContents.LiteralContents LITERAL_CONTENTS = new PlainTextContents.LiteralContents("");

        public MockComponent(WrappedTooltipComponent component) {this.component = component;}

        @Override
        public @NotNull Style getStyle() {
            return Style.EMPTY;
        }

        @Override
        public @NotNull ComponentContents getContents() {
            return LITERAL_CONTENTS;
        }

        @Override
        public @NotNull List<Component> getSiblings() {
            return List.of();
        }

        @Override
        public @NotNull FormattedCharSequence getVisualOrderText() {
            return FormattedCharSequence.EMPTY;
        }

    }

    public MockComponent wrapComponent(){
        return new MockComponent(this);
    }
    public interface Wrappable extends ClientTooltipComponent {
        default WrappedTooltipComponent wrap() {
            return new WrappedTooltipComponent(this);
        }
        default MockComponent wrapComponent(){
            return new MockComponent(wrap());
        }
    }
}
