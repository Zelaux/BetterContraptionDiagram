package com.zelaux.betterdiagram.mixin.leveldata;

import com.zelaux.betterdiagram.leveldata.DiagramEntityData;
import net.minecraft.client.server.IntegratedServer;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

@Mixin(IntegratedServer.class)
public abstract class IntegratedServerMixin {
    @Inject(method = "saveEverything",
        at = @At("HEAD")
    )
    public void saveEverything(boolean suppressLog, boolean flush, boolean forced, CallbackInfoReturnable<Boolean> cir) {
        IntegratedServer server = ((IntegratedServer) (Object) this);
        DiagramEntityData.saveAll(server.getAllLevels());
    }

}
