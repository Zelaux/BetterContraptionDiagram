package com.zelaux.betterdiagram.mixin.feature.force_to_mass;

import com.llamalad7.mixinextras.sugar.Local;
import com.zelaux.betterdiagram.index.Colors;
import com.zelaux.betterdiagram.index.SomeConstants;
import com.zelaux.betterdiagram.util.CenterMassCalculator;
import dev.ryanhcode.sable.api.physics.force.ForceGroup;
import dev.ryanhcode.sable.sublevel.ClientSubLevel;
import dev.simulated_team.simulated.content.entities.diagram.screen.DiagramScreen;
import net.createmod.catnip.lang.LangBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

import java.util.List;

@Mixin(DiagramScreen.class)
public abstract class DiagramScreenMixin {
    @Shadow
    @Final
    public ClientSubLevel subLevel;

    @Inject(method = "addForceArrowTooltip", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/createmod/catnip/lang/LangBuilder;color(I)Lnet/createmod/catnip/lang/LangBuilder;", ordinal = 1))
    private static void addForceArrowTooltip(
        ForceGroup forceGroup,
        int forceCount,
        double forceMagnitude,
        int color,
        List<FormattedText> tooltipLines,
        CallbackInfo ci,
        @Local(name = "forceMagnitudeText") LangBuilder forceMagnitudeText
    ) {
        if(!Screen.hasShiftDown()) return;
        double mass = forceMagnitude / SomeConstants.gravityScalar(Minecraft.getInstance().level);
        String formatted = String.format("%,.2f", mass);
        boolean isEqual = CenterMassCalculator.equals(mass, (int) (mass * 100) / 100.0);
        if(isEqual) formatted = "~" + formatted;
        MutableComponent value = Component.translatable("simulated.contraption_diagram.mass", formatted).withColor(0xffffffff);
        forceMagnitudeText.add(Component
            .empty()
            .append(" (")
            .append(value)
            .append(")")
            .withColor(Colors.LIGHT_GRAY.getRGB())
        );
    }

}
