package com.zelaux.betterdiagram.extend;

import com.zelaux.betterdiagram.BetterContraptionDiagramClient;
import com.zelaux.betterdiagram.data.BCDData;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public interface WithClientData {
    default int axisStatesInt(int i) {
        return axisStates(i) ? 1 : 0;
    }

    default boolean axisStates(int i) {
        return bcdDataNotNull_readOnly().axisStateBool(i);
    }

    default boolean axisStates(int i, boolean value) {
        BCDData data = bcdDataOrTryDefault();
        if(data==null)return value;
        bcdiagram$updateData(data.withAxisStateInt(i, value ? 1 : 0));
        return value;
    }

    default boolean flipAxisStates(int i) {
        return axisStates(i, !axisStates(i));
    }

    class Key<T> {
        private static int counter = 0;
        public final int id = counter++;

        @Override
        public int hashCode() {
            return counter;
        }
    }

    @Nullable
    @Contract(pure = true)
    BCDData bcdiagram$dataOrNull();

    /**
     * @return bcdData - if exists<br>
     * null if server has installed mod and waiting for server data object
     * otherwise {@link BCDData#DEFAULT()}
     *
     * @see com.zelaux.betterdiagram.network.DiagramOpenBCDDataPacket
     * @see BetterContraptionDiagramClient#isServerSideInstalled
     */
    @Nullable
    default BCDData bcdDataOrTryDefault() {
        BCDData obj = bcdiagram$dataOrNull();
        //waiting for server packat
        if(obj == null && BetterContraptionDiagramClient.isServerSideInstalled) return null;
        return Objects.requireNonNullElse(obj, BCDData.DEFAULT_VALUE);
    }

    @NotNull
    default BCDData bcdDataNotNull_readOnly() {
        BCDData obj = bcdiagram$dataOrNull();
        return Objects.requireNonNullElse(obj, BCDData.DEFAULT_VALUE);
    }


    @Contract("_ -> param1")
    BCDData bcdiagram$setDataSilent(BCDData data);

    @Nullable
    BCDData bcdiagram$updateData(BCDData data);
}
