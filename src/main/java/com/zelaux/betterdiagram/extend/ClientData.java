package com.zelaux.betterdiagram.extend;

import com.zelaux.betterdiagram.data.BCDData;
import com.zelaux.betterdiagram.data.OffCenterBlocksShowState;
import com.zelaux.betterdiagram.feature.leveldata.DiagramEntityData;
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
    public BCDData bcd$setDataSilent(BCDData data) {
        return this.data.bcd$setDataSilent(data);
    }

    @Override
    @Nullable
    public BCDData bcd$dataOrNull() {
        return data.bcd$dataOrNull();
    }

    @Override
    public BCDData bcd$updateData(BCDData data) {
        return this.data.bcd$updateData(data);
    }

    @Override
    public DiagramEntityData.SaveSubLocation bcd$saveType() {
        return data.bcd$saveType();
    }

    @Override
    public void bcd$saveType(DiagramEntityData.SaveSubLocation bcd$saveType) {
        data.bcd$saveType(bcd$saveType);
    }

    @NotNull
    public OffCenterBlocksShowState offCenteredView() {
        return bcdDataNotNull_readOnly().offCenterBlocksShowState;
    }

    public void nextOffCenteredView() {
        BCDData data = this.data.bcdDataOrTryDefault();
        if(data == null) return;
        bcd$updateData(
            data.withOffCenterBlocksShowState(data.offCenterBlocksShowState.next())
        );
    }

    public void prevOffCenteredView() {
        BCDData data = this.data.bcdDataOrTryDefault();
        if(data == null) return;
        bcd$updateData(
            data.withOffCenterBlocksShowState(data.offCenterBlocksShowState.prev())
        );
    }
}
