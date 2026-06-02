package com.zelaux.betterdiagram.extend;

import com.zelaux.betterdiagram.data.BCDData;

public interface ServerSideData {

    BCDData bcd$storredData();

    void bcd$storredData(BCDData bcd$storredData);
}
