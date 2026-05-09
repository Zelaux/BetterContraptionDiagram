package com.zelaux.betterdiagram.mixin;

import com.zelaux.betterdiagram.extend.WithClientData;
import dev.simulated_team.simulated.content.entities.diagram.DiagramEntity;
import org.spongepowered.asm.mixin.*;

import java.util.HashMap;
import java.util.function.Supplier;

@Mixin(DiagramEntity.class)
public class ClientDataForDiagramEntity implements WithClientData {
    @Unique
    public final HashMap<Key<?>, Object> betterContraptionDiagram$clientData = new HashMap<>();

    @Override
    public <T> T betterContraptionDiagram$getClientData(Key<T> key, Supplier<T> generator) {
        if(generator == null)
            //noinspection unchecked
            return (T) betterContraptionDiagram$clientData.get(key);
        //noinspection unchecked
        return (T) betterContraptionDiagram$clientData.computeIfAbsent(key, i -> generator.get());
    }

    @Override
    public <T> T betterContraptionDiagram$deleteClientData(Key<T> key) {
        //noinspection unchecked
        return (T) betterContraptionDiagram$clientData.remove(key);
    }

    @Override
    public <T> void betterContraptionDiagram$putClientData(Key<T> key, T data) {
        betterContraptionDiagram$clientData.put(key, data);
    }
}
