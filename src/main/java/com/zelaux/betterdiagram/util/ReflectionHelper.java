package com.zelaux.betterdiagram.util;

import com.zelaux.betterdiagram.annotations.JavaClassName;
import lombok.Lombok;
import lombok.SneakyThrows;

import java.lang.reflect.Field;
import java.util.function.Function;

public class ReflectionHelper {
    @SneakyThrows
    public static <T, F> Function<T, F> locateGetter(
        @JavaClassName
        String typeName, String fieldName) {
        Class<?> type = Class.forName(typeName);
        Field field = type.getDeclaredField(fieldName);
        field.setAccessible(true);
        return it -> {
            try {
                return (F) field.get(it);
            } catch(IllegalAccessException e) {
                throw Lombok.sneakyThrow(e);
            }
        };
    }
}
