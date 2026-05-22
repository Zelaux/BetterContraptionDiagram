package com.zelaux.betterdiagram.extend;

import com.zelaux.betterdiagram.data.BCDData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface WithClientData {
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
