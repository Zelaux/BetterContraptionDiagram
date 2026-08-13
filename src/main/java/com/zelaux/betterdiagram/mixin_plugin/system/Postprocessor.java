package com.zelaux.betterdiagram.mixin_plugin.system;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.*;

public interface Postprocessor {
    default void preMixin(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }

    default void postMixin(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }

    Type[] usedClasses();

    static Type[] types(Type... types) {
        return types;
    }

    boolean process(ClassNode targetClass);
}
