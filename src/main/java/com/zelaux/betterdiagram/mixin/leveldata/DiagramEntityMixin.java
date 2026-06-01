package com.zelaux.betterdiagram.mixin.leveldata;

import com.zelaux.betterdiagram.data.BCDData;
import com.zelaux.betterdiagram.extend.WithClientData;
import com.zelaux.betterdiagram.leveldata.DiagramEntityData;
import dev.simulated_team.simulated.content.entities.diagram.DiagramEntity;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

@Mixin(DiagramEntity.class)
public class DiagramEntityMixin implements WithClientData {
    @Inject(
        method = "dropItem",
        at = @At("TAIL")
    )
    private void dropItem(Entity p_110128_1_, CallbackInfo ci) {
        DiagramEntity self = (DiagramEntity) ((Object) this);
        DiagramEntityData data = DiagramEntityData.get(self.level());
        data.saveData(self.getUUID(), null);
    }

    @Nullable
    @Unique
    BCDData bcd$data;

    @Override
    public @Nullable BCDData bcdiagram$dataOrNull() {
        DiagramEntity self = (DiagramEntity) ((Object) this);
        if(bcd$data == null) {
            bcd$data = DiagramEntityData.get(self.level()).locateDataOrNull(self.getUUID());
        }
        return bcd$data;
    }

    @Override
    public @NotNull BCDData bcdiagram$updateData(BCDData data) {
        bcd$data = data;
        DiagramEntity self = (DiagramEntity) ((Object) this);
         DiagramEntityData.get(self.level()).saveData(self.getUUID(),data);
        return data;
    }
}
