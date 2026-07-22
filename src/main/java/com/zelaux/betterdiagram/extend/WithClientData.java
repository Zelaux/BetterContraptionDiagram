package com.zelaux.betterdiagram.extend;
1
import com.zelaux.betterdiagram.BetterContraptionDiagramClient;
import com.zelaux.betterdiagram.data.BCDData;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Function;

public interface WithClientData {
    default int axisStatesInt(int i) {
        return axisStates(i) ? 1 : 0;
    }

    default boolean axisStates(int i) {
        return bcdDataNotNull_readOnly().axisStateBool(i);
    }

    default boolean axisStates(int i, boolean value) {
        BCDData data = bcdDataOrTryDefault();
        if(data == null) return value;
        bcd$updateData(data.withAxisStateInt(i, value ? 1 : 0));
        return value;
    }

    default boolean flipAxisStates(int i) {
        return axisStates(i, !axisStates(i));
    }

    @Nullable
    @Contract(pure = true)
    BCDData bcd$dataOrNull();

    /**
     * @return bcdData - if exists<br>
     * null if server has installed mod and waiting for server data object
     * otherwise {@link BCDData#DEFAULT()}
     * @see com.zelaux.betterdiagram.network.DiagramOpenBCDDataPacket
     * @see BetterContraptionDiagramClient#isServerSideInstalled
     */
    @Nullable
    default BCDData bcdDataOrTryDefault() {
        BCDData obj = bcd$dataOrNull();
        //waiting for server packat
        if(obj == null && BetterContraptionDiagramClient.isServerSideInstalled) return null;
        return Objects.requireNonNullElse(obj, BCDData.DEFAULT_VALUE);
    }

    @NotNull
    default BCDData bcdDataNotNull_readOnly() {
        BCDData obj = bcd$dataOrNull();
        return Objects.requireNonNullElse(obj, BCDData.DEFAULT_VALUE);
    }


    @Contract("_ -> param1")
    BCDData bcd$setDataSilent(BCDData data);

    @Nullable
    BCDData bcd$updateData(BCDData data);
    default BCDData bcd$updateData(Function<BCDData,BCDData> updater){
        BCDData data1 = bcdDataOrTryDefault();
        if(data1==null)return null;
        return bcd$updateData(updater.apply(data1));
    }
}
