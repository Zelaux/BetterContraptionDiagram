package com.zelaux.betterdiagram.gui;

import com.google.common.base.Strings;
import com.mojang.blaze3d.systems.RenderSystem;
import com.zelaux.betterdiagram.data.OffCenteredBlock;
import com.zelaux.betterdiagram.gui.screen.COM.COMScreen;
import com.zelaux.betterdiagram.gui.screen.COM.Context;
import com.zelaux.betterdiagram.util.CenterMassCalculator;
import com.zelaux.betterdiagram.util.MixinCalculatorUtil;
import dev.simulated_team.simulated.content.entities.diagram.screen.DiagramScreen;
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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class OffCenteredBlockTooltipHandler {

    private static MixinCalculatorUtil.HoveredOffCenteredBlock hoveredOCBlock;


    private static final LerpedFloat progress = LerpedFloat.linear().startWithValue(0);

    private static boolean shouldTick = false;

    public static void tick() {
        shouldTick = true;
    }

    public static void addTooltip(@NotNull DiagramScreen self, @Nullable MixinCalculatorUtil.HoveredOffCenteredBlock newHovered, List<FormattedText> tooltipList) {
        if(shouldTick) delayedTick();


        var oldHover = hoveredOCBlock;
        hoveredOCBlock = null;
        boolean equals = oldHover != null && newHovered != null && newHovered.block().equals(oldHover.block());
        if(equals || oldHover == null && newHovered != null) {
            hoveredOCBlock = newHovered;
            float renderPartialTicks = AnimationTickHolder.getPartialTicksUI();
            Component component = makeProgressBar(Math.min(1, progress.getValue(renderPartialTicks) * 8 / 7f));
            tooltipList.add(newHovered.tooltipStartIndex() + 3, component);
        }
    }

    private static void delayedTick() {
        shouldTick = false;
        Minecraft instance = Minecraft.getInstance();
        Screen currentScreen = instance.screen;

        if(hoveredOCBlock == null) {
            progress.startWithValue(0);
            return;
        }

        float value = progress.getValue();

        if(RenderSystem.isOnRenderThread() && PonderKeybinds.PONDER.isDown() && currentScreen != null) {
            if(value >= 1) {
                if(currentScreen instanceof NavigatableSimiScreen)
                    ((NavigatableSimiScreen) currentScreen).centerScalingOnMouse();


                Context context = Context.newContext();
                OffCenteredBlock block = hoveredOCBlock.block();
                context.setEntries(block.COM());
                Item item = Item.BY_BLOCK.get(block.state().getBlock());
                openDialog:
                if(item != null) {
                    context.filterItem(new ItemStack(item));
                    Context.COMPair[] pairs = context.pairs();
                    int i;
                    findPair:
                    {
                        for(i = 0; i < pairs.length; i++) {
                            if(CenterMassCalculator.equals(pairs[i].center(), block.COM())) {
                                break findPair;
                            }
                        }
                        break openDialog;
                    }
                    context.selectedPair = i;
                    context.buildOnInit = true;
                    context.inverted = true;
                    Minecraft.getInstance().pushGuiLayer(new COMScreen(
                        context, Minecraft.getInstance().player
                    ));
                }
                progress.startWithValue(0);
                return;
            }
            progress.setValue(Math.min(1, value + Math.max(.25f, value) * .25f));
        } else
            progress.setValue(Math.max(0, value - .05f));

        hoveredOCBlock = null;
    }

    private static Component makeProgressBar(float progress) {
        MutableComponent holdW = Component.translatable("better_contraption_diagram.diagram.offcenter-blocks-mode.hold-to-browse",
                                              PonderKeybinds.PONDER.message().copy().withStyle(ChatFormatting.GRAY))
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

}
