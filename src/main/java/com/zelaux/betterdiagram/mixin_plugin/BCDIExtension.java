package com.zelaux.betterdiagram.mixin_plugin;

import lombok.SneakyThrows;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.extensibility.*;
import org.spongepowered.asm.mixin.transformer.ext.*;

import java.lang.reflect.Field;
import java.util.SortedSet;

class BCDIExtension implements IExtension {

    private static final String TARGET_CONTEXT = "org.spongepowered.asm.mixin.transformer.TargetClassContext";

    @Override
    public boolean checkActive(MixinEnvironment environment) {
        return true;
    }

    @SneakyThrows
    @Override
    public void preApply(ITargetClassContext context) {
        /*var type = context.getClass();
        ;
        if(type.getName().equals(TARGET_CONTEXT)) {
            //SortedSet<MixinInfo> mixins
            Field field = type.getDeclaredField("mixins");
            field.setAccessible(true);
            SortedSet<IMixinInfo> set = (SortedSet) field.get(context);
            //set.getFirst().getName()
        }*/
    }

    @Override
    public void postApply(ITargetClassContext context) {
        ClassNode classNode = context.getClassNode();
        boolean[] any = {false};
        BCDPostprocessors.postprocessorsFor(classNode.name).forEach(postprocessor -> any[0] |= postprocessor.process(classNode));
        if(any[0]) {
            BCDPostprocessors.debugSave(classNode);
        }
    }

    @Override
    public void export(MixinEnvironment env, String name, boolean force, ClassNode classNode) {}
}
