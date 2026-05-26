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
    @NotNull
    public BCDData bcdDataOrDefault() {
        return data.bcdDataOrDefault();
    }

    @NotNull
    public BCDData dataOrDefault() {return data.bcdDataOrDefault();}

    @Override
    @Nullable
    public BCDData bcdiagram$dataOrNull() {
        return data.bcdiagram$dataOrNull();
    }

    @Override
    @NotNull
    public BCDData bcdiagram$updateData(BCDData data) {
        return this.data.bcdiagram$updateData(data);
    }

    @NotNull
    public BCDData.OffCenterBlocksShowState offCenteredView() {
        return bcdDataOrDefault().offCenterBlocksShowState;
    }

    public void nextOffCenteredView() {
        BCDData data = dataOrDefault();
        bcdiagram$updateData(
            data.withOffCenterBlocksShowState(data.offCenterBlocksShowState.next())
        );
    }
    public void prevOffCenteredView() {
        BCDData data = dataOrDefault();
        bcdiagram$updateData(
            data.withOffCenterBlocksShowState(data.offCenterBlocksShowState.prev())
        );
    }
}
