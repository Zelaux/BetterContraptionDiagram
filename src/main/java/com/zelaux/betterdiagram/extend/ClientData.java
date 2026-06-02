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
    public BCDData bcdDataOrTryDefault() {
        return data.bcdDataOrTryDefault();
    }

    @Override
    public BCDData bcdiagram$setDataSilent(BCDData data) {
        return this.data.bcdiagram$setDataSilent(data);
    }

    @Override
    @Nullable
    public BCDData bcdiagram$dataOrNull() {
        return data.bcdiagram$dataOrNull();
    }

    @Override
    public BCDData bcdiagram$updateData(BCDData data) {
        return this.data.bcdiagram$updateData(data);
    }

    @NotNull
    public BCDData.OffCenterBlocksShowState offCenteredView() {
        return bcdDataNotNull_readOnly().offCenterBlocksShowState;
    }

    public void nextOffCenteredView() {
        BCDData data = this.data.bcdDataOrTryDefault();
        if(data == null) return;
        bcdiagram$updateData(
            data.withOffCenterBlocksShowState(data.offCenterBlocksShowState.next())
        );
    }

    public void prevOffCenteredView() {
        BCDData data = this.data.bcdDataOrTryDefault();
        if(data == null) return;
        bcdiagram$updateData(
            data.withOffCenterBlocksShowState(data.offCenterBlocksShowState.prev())
        );
    }
}
