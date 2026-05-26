package com.zelaux.betterdiagram.mixin;

import com.zelaux.betterdiagram.data.BCDData;
import com.zelaux.betterdiagram.extend.WithClientData;
import dev.simulated_team.simulated.content.entities.diagram.DiagramEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;

@Mixin(DiagramEntity.class)
public class ClientDataForDiagramEntity implements WithClientData {
    @Nullable
    @Unique
    BCDData bcd$data;

    @Override
    public @Nullable BCDData bcdiagram$dataOrNull() {
        return bcd$data;
    }

    @Override
    public @NotNull BCDData bcdiagram$updateData(BCDData data) {
        bcd$data = data;
        return data;
    }
}
