package com.zelaux.betterdiagram.extend;

import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public interface WithClientData {
    class Key<T> {
        private static int counter = 0;
        public final int id = counter++;

        @Override
        public int hashCode() {
            return counter;
        }
    }

    <T> T betterContraptionDiagram$getClientData(Key<T> key, @Nullable Supplier<T> generator);
    default  <T> T betterContraptionDiagram$getClientData(Key<T> key){
        return betterContraptionDiagram$getClientData(key,null);
    }

    <T> void betterContraptionDiagram$putClientData(Key<T> key, T data);

    <T> T betterContraptionDiagram$deleteClientData(Key<T> key);
}
