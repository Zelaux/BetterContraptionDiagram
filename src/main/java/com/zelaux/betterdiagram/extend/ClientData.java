package com.zelaux.betterdiagram.extend;

import com.zelaux.betterdiagram.data.BCDData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ClientData implements WithClientData {
    public WithClientData data;

    public ClientData(WithClientData data) {
        this.data = data;
    }

    @Override
    public @Nullable BCDData bcdiagram$dataOrNull() {
        return data.bcdiagram$dataOrNull();
    }

    @Override
    public @NotNull BCDData bcdiagram$dataOrCreate() {
        return data.bcdiagram$dataOrCreate();
    }

    @Override
    public @NotNull BCDData bcdiagram$updateData(BCDData data) {
        return this.data.bcdiagram$updateData(data);
    }
}
