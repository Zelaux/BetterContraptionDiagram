package com.zelaux.betterdiagram.gui.tooltip;

import com.google.common.base.Strings;
import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.createmod.catnip.animation.LerpedFloat;
import net.createmod.catnip.gui.NavigatableSimiScreen;
import net.createmod.ponder.enums.PonderKeybinds;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import org.apache.commons.lang3.function.ToBooleanBiFunction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public abstract class AbstractTooltipHandler<PayloadType> {
    public static final ObjectArrayList<AbstractTooltipHandler<?>> handlers = new ObjectArrayList<>();

    private final LerpedFloat progress = LerpedFloat.linear().startWithValue(0);
    private PayloadType lastPayload;
    private boolean shouldTick = false;

    {
        handlers.add(this);
    }

    public static <PayloadType> AbstractTooltipHandler<PayloadType> create(
        Component name,
        ToBooleanBiFunction<PayloadType, PayloadType> equalizer,
        Consumer<PayloadType> onActive
        ) {

        return new AbstractTooltipHandler<PayloadType>() {
            @Override
            public boolean isSamePayload(@NotNull PayloadType newHovered, PayloadType oldHover) {
                return equalizer.applyAsBoolean(newHovered,oldHover);
            }

            @Override
            public void onActive(PayloadType lastPayload) {
                onActive.accept(lastPayload);
            }

            @Override
            protected Component getActionName() {
                return name;
            }
        };
    }

    public static void tick() {
        for(AbstractTooltipHandler<?> handler : handlers) {
            handler.shouldTick = true;
        }
    }

    public void addTooltip(@Nullable PayloadType newHovered, int tooltipInsertAfterIndex, List<? super Component> tooltipList) {
        if(shouldTick) delayedTick();


        var oldHover = lastPayload;
        lastPayload = null;
        boolean equals = oldHover != null && newHovered != null && isSamePayload(newHovered, oldHover);
        if(equals || oldHover == null && newHovered != null) {
            lastPayload = newHovered;
            float renderPartialTicks = AnimationTickHolder.getPartialTicksUI();
            Component component = makeProgressBar(Math.min(1, progress.getValue(renderPartialTicks) * 8 / 7f));
            tooltipList.add(tooltipInsertAfterIndex+1, component);
        }
    }

    public abstract boolean isSamePayload(@NotNull PayloadType newHovered, PayloadType oldHover);

    private void delayedTick() {
        shouldTick = false;
        Minecraft instance = Minecraft.getInstance();
        Screen currentScreen = instance.screen;

        if(lastPayload == null) {
            progress.startWithValue(0);
            return;
        }

        float value = progress.getValue();

        if(RenderSystem.isOnRenderThread() && PonderKeybinds.PONDER.isDown() && currentScreen != null) {
            if(value >= 1) {
                if(currentScreen instanceof NavigatableSimiScreen)
                    ((NavigatableSimiScreen) currentScreen).centerScalingOnMouse();


                onActive(lastPayload);

                progress.startWithValue(0);
                return;
            }
            progress.setValue(Math.min(1, value + Math.max(.25f, value) * .25f));
        } else
            progress.setValue(Math.max(0, value - .05f));

        lastPayload = null;
    }

    public abstract void onActive(PayloadType lastPayload);

    private Component makeProgressBar(float progress) {
        MutableComponent holdW = Component.translatable("bcd.tooltip-handler.message",
                                              PonderKeybinds.PONDER.message().copy().withStyle(ChatFormatting.GRAY), getActionName())
                                          .withStyle(ChatFormatting.DARK_GRAY);

        Font fontRenderer = Minecraft.getInstance().font;
        float charWidth = fontRenderer.width("|");
        float tipWidth = fontRenderer.width(holdW);

        int total = (int) (tipWidth / charWidth);
        int current = (int) (progress * total);

        if(progress > 0) {
            String bars = "";
            bars += ChatFormatting.GRAY + Strings.repeat("|", current);
            if(progress < 1)
                bars += ChatFormatting.DARK_GRAY + Strings.repeat("|", total - current);
            return Component.literal(bars);
        }

        return holdW;
    }

    protected abstract Component getActionName();

}
