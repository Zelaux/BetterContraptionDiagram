package com.zelaux.betterdiagram.extend;

import com.zelaux.betterdiagram.data.BCDData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface WithClientData {
    default int axisStatesInt(int i) {
        return axisStates(i)?1:0;
    }
    default boolean axisStates(int i) {
        BCDData data = bcdiagram$dataOrNull();
        if(data == null || data.axisStates == null) return true;
        return data.axisStates[i];
    }

    default boolean axisStates(int i, boolean value) {
        BCDData data = bcdiagram$dataOrCreate();
        if(data.axisStates == null)data=data.withAxisStates(new boolean[]{true,true,true});
        data.axisStates[i] = value;
        bcdiagram$updateData(data.withAxisStates(data.axisStates));
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
    BCDData bcdiagram$dataOrNull();

    @NotNull
    BCDData bcdiagram$dataOrCreate();

    @NotNull
    BCDData bcdiagram$updateData(BCDData data);
}
