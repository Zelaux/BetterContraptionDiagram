package com.zelaux.betterdiagram.mixin_plugin.system;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.*;

public interface ProcessorProvider {
    interface ClassTransform {
        boolean transform(ClassNode targetClass);
    }

    default void preMixin(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }

    default void postMixin(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }

    record Transformations(ClassTransform transform, Type... type) {
        public static Transformations of(ClassTransform transform, Type... type) {
            return new Transformations(transform, type);
        }

        public static Transformations of(Type type, ClassTransform transform) {
            return new Transformations(transform, type);
        }
        public static Transformations of(Type[] type, ClassTransform transform) {
            return new Transformations(transform, type);
        }
    }

    Transformations[] transformations();

    static Transformations[] types(Transformations... types) {
        return types;
    }

    default boolean process(ClassNode targetClass) {
        return false;
    }
}
