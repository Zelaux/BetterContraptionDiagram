package com.zelaux.betterdiagram.mixin;


import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import com.simibubi.create.foundation.gui.RemovedGuiUtils;
import com.zelaux.betterdiagram.gui.comp.ListenerClientTooltipComponent;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

import java.util.ArrayList;
import java.util.List;

@Mixin(RemovedGuiUtils.class)
public class RemovedGuiUtilsMixin {
    @Inject(method = "drawHoveringText(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/client/gui/GuiGraphics;Ljava/util/List;IIIIIIIILnet/minecraft/client/gui/Font;)V",
        at = @At(
            value = "INVOKE",
            shift = At.Shift.BEFORE,
            ordinal = 0,
            target = "Lnet/minecraft/client/gui/GuiGraphics;fillGradient(IIIIIII)V"
        )
    )
    private static void fixHeight(
        @NotNull ItemStack stack,
        GuiGraphics graphics,
        List<? extends FormattedText> textLines,
        int mouseX,
        int mouseY,
        int screenWidth,
        int screenHeight,
        int maxTextWidth,
        int backgroundColor,
        int borderColorStart,
        int borderColorEnd,
        Font font,
        CallbackInfo ci,
        @Local(name = "tooltipHeight") LocalIntRef tooltipHeight,
        @Local(name = "tooltipTextWidth") LocalIntRef tooltipTextWidth,
        @Local(name = "list") List<ClientTooltipComponent> list,
        @Local(name = "tooltipX") int tooltipX,
        @Local(name = "tooltipY") int tooltipY
    ) {
        int height = tooltipHeight.get();
        int width = tooltipTextWidth.get();
        ArrayList<ListenerClientTooltipComponent> listeners = new ArrayList<>();
        for(ClientTooltipComponent component : list) {
            if(component instanceof ListenerClientTooltipComponent listener) listeners.add(listener);
            height += component.getHeight() - 10;
            width = Math.max(component.getWidth(font), width);
        }
        tooltipHeight.set(height);
        width = maxTextWidth > 0 ? Math.min(maxTextWidth, width) : width;
        tooltipTextWidth.set(width);
        for(ListenerClientTooltipComponent listener : listeners) {
            listener.beforeRender(
                graphics, tooltipX, tooltipY, width, height, borderColorStart, borderColorEnd, backgroundColor
            );
        }
    }
}
