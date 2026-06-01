package com.zelaux.betterdiagram.mixin.leveldata;

import com.zelaux.betterdiagram.leveldata.DiagramEntityData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.PauseScreen;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

import java.util.Collections;

@Mixin(PauseScreen.class)
public abstract class PauseScreenMixin {
    @Inject(
        method = "init",
        at = @At("HEAD")
    )
    protected void init(CallbackInfo ci) {

        DiagramEntityData.saveAll(Collections.singleton(Minecraft.getInstance().level));
    }

}
