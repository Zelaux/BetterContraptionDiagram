package com.zelaux.betterdiagram.feature.include_subsublevel.connector;

import com.zelaux.betterdiagram.annotations.JavaClassName;
import dev.ryanhcode.sable.api.block.BlockEntitySubLevelActor;
import lombok.Lombok;
import lombok.SneakyThrows;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.function.Function;

public final class OptionalActorHandlers {
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T extends BlockEntitySubLevelActor> void registerOptional(
        @JavaClassName
        String blockOrEntityClass, Function<T, ActorHandler> mapper) {
        try {
            Class<?> type = Class.forName(blockOrEntityClass);
            if(BlockEntitySubLevelActor.class.isAssignableFrom(type)) {
                ActorHandler.register((Class<? extends BlockEntitySubLevelActor>) type, (Function) mapper);
            } else {
                throw new RuntimeException("Given class is not a block or an entity '" + blockOrEntityClass + "'");
            }
        } catch(ClassNotFoundException ignored) {

        }
    }

    @SneakyThrows
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T extends BlockEntitySubLevelActor> void registerOptional(
        @JavaClassName String blockOrEntityClass,
        @JavaClassName String actorHandlerClassName) {
        try {
            var type = Class.forName(blockOrEntityClass);

            Class<?> actorHandlerType0 = Class.forName(actorHandlerClassName);
            if(!ActorHandler.class.isAssignableFrom(actorHandlerType0)) {
                throw new RuntimeException("Given class is not an actor handler '" + actorHandlerClassName + "'");
            }
            var actorHandlerType = (Class<? extends ActorHandler>) actorHandlerType0;

            registerOptional(blockOrEntityClass, actorHandlerType);
        } catch(ClassNotFoundException ignored) {

        }
    }

    @SneakyThrows
    public static void registerOptional(@JavaClassName String blockOrEntityClass, Class<? extends ActorHandler> actorHandlerType) {
        try {
            var type = Class.forName(blockOrEntityClass);
            Constructor<?> found = null;
            for(Constructor<?> constructor : actorHandlerType.getConstructors()) {
                if(Modifier.isPublic(constructor.getModifiers())) {
                    if(constructor.getParameterCount() == 1) {
                        if(found == null) found = constructor;
                        Class<?> parameterType = constructor.getParameterTypes()[0];
                        if(BlockEntitySubLevelActor.class.isAssignableFrom(parameterType)) {
                            found = constructor;
                            break;
                        }
                    }
                }
            }
            if(found == null)
                throw new RuntimeException("Given class has no valid constructor '" + actorHandlerType + "'");
            MethodHandle handle = MethodHandles.lookup().unreflectConstructor(found);
            registerOptional(blockOrEntityClass, actor -> {
                try {
                    return (ActorHandler) handle.invoke(actor);
                } catch(Throwable e) {
                    throw Lombok.sneakyThrow(e);
                }
            });
        } catch(ClassNotFoundException ignored) {

        }
    }
}
