package com.zelaux.betterdiagram.feature.include_subsublevel.connector;

import com.google.common.collect.Iterables;
import dev.ryanhcode.sable.sublevel.SubLevel;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.SneakyThrows;
import org.jetbrains.annotations.Nullable;

import java.util.function.BooleanSupplier;

public interface ActorHandlerPollution {
    ObjectArrayList<ActorHandlerPollution> pollution = new ObjectArrayList<>();

    static void register(ActorHandlerPollution handlerPollution) {
        pollution.add(handlerPollution);
    }

    @SneakyThrows
    static void registerOptional(String handlerPollution, BooleanSupplier condition) {
        if(!condition.getAsBoolean()) return;
        try {
            Class<?> name = Class.forName(handlerPollution);
            var obj = (ActorHandlerPollution) name.getConstructor().newInstance();
            pollution.add(obj);
        } catch(ClassNotFoundException ignored) {
        }
    }

    @Nullable
    static Iterable<ActorHandler> withoutBlockEntities(SubLevel subLevel) {
        ObjectArrayList<Iterable<ActorHandler>> list = null;
        for(ActorHandlerPollution handlerPollution : pollution) {
            Iterable<ActorHandler> handlers = handlerPollution.get(subLevel);
            if(handlers == null) continue;
            if(list == null) list = new ObjectArrayList<>();
            list.add(handlers);
        }
        if(list == null) return null;
        return Iterables.concat(list);
    }

    @Nullable
    Iterable<ActorHandler> get(SubLevel subLevel);


}
