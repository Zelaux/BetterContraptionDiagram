package com.zelaux.betterdiagram.mixin.COM_view;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.simibubi.create.content.equipment.goggles.GoggleOverlayRenderer;
import com.zelaux.betterdiagram.Config;
import com.zelaux.betterdiagram.util.CenterMassCalculator;
import com.zelaux.betterdiagram.util.MixinCalculatorUtil;
import com.zelaux.betterdiagram.util.VecFormat;
import dev.ryanhcode.sable.api.physics.mass.MassTracker;
import dev.ryanhcode.sable.companion.math.JOMLConversion;
import dev.ryanhcode.sable.physics.config.block_properties.PhysicsBlockPropertyHelper;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.joml.Vector3dc;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

import java.util.List;

@Mixin(GoggleOverlayRenderer.class)
public class com_view_GoggleOverlayRenderer {

    //@WrapOperation(method = "renderOverlay",at= @At(value = "INVOKE", target =))
    @Inject(method = "renderOverlay", at = @At(
        value = "FIELD", opcode = Opcodes.PUTSTATIC, ordinal = 4,
        shift = At.Shift.BY, by = 3
    ))
    private static void x(GuiGraphics guiGraphics,
                          DeltaTracker deltaTracker,
                          CallbackInfo ci,
                          @Local(ordinal = 0) BlockHitResult result,
                          @Local(ordinal = 0) List<Component> tooltip,
                          @Local(ordinal = 0) boolean wearingGoggles,
                          @Local(ordinal = 1) boolean isShifting,
                          @Local(ordinal = 4) LocalBooleanRef goggleAddedInformation,
                          @Local(ordinal = 5) LocalBooleanRef hoverAddedInformation) {
        if(!wearingGoggles) return;
        if(!Config.COM_viewType_Goggles.get().shouldShow(isShifting)) return;
        ClientLevel level = Minecraft.getInstance().level;
        if(level == null) return;
        BlockPos blockPos = result.getBlockPos();

        BlockState blockstate = level.getBlockState(blockPos);

        Vector3dc blockCenterOfMass = MassTracker.BLOCK_CENTER_OF_MASS.apply(level, blockstate);
        if(blockCenterOfMass.equals(JOMLConversion.HALF)) return;
        double mass = PhysicsBlockPropertyHelper.getMass(level, blockPos, blockstate);
        if(CenterMassCalculator.equals(mass, 0)) return;
        hoverAddedInformation.set(true);
        goggleAddedInformation.set(true);
        if(!tooltip.isEmpty())
            tooltip.add(CommonComponents.EMPTY);


        tooltip.add(
            MixinCalculatorUtil.indented(0,
                Component.translatable("better_contraption_diagram.google-overlay.com-offset", VecFormat.Presets.gray(blockCenterOfMass))
            )
        );
    }

}
