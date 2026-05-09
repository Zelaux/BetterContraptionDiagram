package com.zelaux.betterdiagram.extend;

import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class ClientData implements WithClientData {
    public WithClientData data;

    public ClientData(WithClientData data) {
        this.data = data;
    }

    public <T> T get(WithClientData.Key<T> key) {
        return data.betterContraptionDiagram$getClientData(key, null);
    }

    public <T> T get(WithClientData.Key<T> key, Supplier<T> generator) {
        return data.betterContraptionDiagram$getClientData(key, generator);
    }

    public <T> T remove(WithClientData.Key<T> key) {
        return data.betterContraptionDiagram$deleteClientData(key);
    }

    public <T> void put(WithClientData.Key<T> key, T data) {
        this.data.betterContraptionDiagram$putClientData(key, data);
    }

    @Override
    public <T> T betterContraptionDiagram$getClientData(Key<T> key, @Nullable Supplier<T> generator) {
        return data.betterContraptionDiagram$getClientData(key, generator);
    }

    @Override
    public <T> void betterContraptionDiagram$putClientData(Key<T> key, T data) {
        this.data.betterContraptionDiagram$putClientData(key, data);
    }

    @Override
    public <T> T betterContraptionDiagram$deleteClientData(Key<T> key) {
        return this.data.betterContraptionDiagram$deleteClientData(key);
    }
}
