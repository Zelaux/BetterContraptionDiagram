package com.zelaux.betterdiagram.extend;

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
        return bcdDataOrDefault().axisStateBool(i);
    }

    default boolean axisStates(int i, boolean value) {
        BCDData data = bcdDataOrDefault();
        bcdiagram$updateData(data.withAxisStateInt(i,value?1:0));
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

    @NotNull
    @Contract(pure = true)
    default BCDData bcdDataOrDefault() {return Objects.requireNonNullElse(bcdiagram$dataOrNull(), BCDData.DEFAULT_VALUE);}

    @NotNull
    BCDData bcdiagram$updateData(BCDData data);
}
