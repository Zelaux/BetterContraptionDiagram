package com.zelaux.betterdiagram.mixin.leveldata;

import com.zelaux.betterdiagram.BetterContraptionDiagramClient;
import com.zelaux.betterdiagram.data.BCDData;
import com.zelaux.betterdiagram.extend.WithClientData;
import com.zelaux.betterdiagram.feature.leveldata.DiagramEntityData;
import dev.simulated_team.simulated.content.entities.diagram.DiagramEntity;
import net.minecraft.world.entity.Entity;
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
        DiagramEntityData data = DiagramEntityData.get(self.level(), bcd$saveType);
        data.saveData(self.getUUID(), null);
    }

    @Nullable
    @Unique
    BCDData bcd$data;

    @Unique
    DiagramEntityData.SaveSubLocation bcd$saveType= DiagramEntityData.SaveSubLocation.None;

    @Override
    public DiagramEntityData.SaveSubLocation bcd$saveType() {
        return bcd$saveType;
    }

    @Override
    public void bcd$saveType(DiagramEntityData.SaveSubLocation saveType) {
        this.bcd$saveType = saveType;
    }

    @Override
    public @Nullable BCDData bcd$dataOrNull() {
        DiagramEntity self = (DiagramEntity) ((Object) this);
        if(bcd$data == null && (!BetterContraptionDiagramClient.isServerSideInstalled || bcd$saveType != DiagramEntityData.SaveSubLocation.None)) {
            bcd$data = DiagramEntityData.get(self.level(), bcd$saveType).locateDataOrNull(self.getUUID());
        }
        return bcd$data;
    }

    @Override
    public BCDData bcd$setDataSilent(BCDData data) {
        return bcd$data = data;
    }

    @Override
    public BCDData bcd$updateData(BCDData data) {
        bcd$data = data;
        DiagramEntity self = (DiagramEntity) ((Object) this);
        DiagramEntityData.get(self.level(), bcd$saveType).saveData(self.getUUID(), data);
        return data;
    }
}
