package com.zelaux.betterdiagram.mixin_plugin.util;

import com.zelaux.betterdiagram.mixin_plugin.BCDExtension;
import lombok.Lombok;
import lombok.SneakyThrows;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.transformer.*;
import org.spongepowered.asm.service.IMixinService;
import org.spongepowered.asm.service.MixinService;
import org.spongepowered.asm.service.modlauncher.MixinServiceModLauncher;
import org.spongepowered.asm.service.modlauncher.MixinTransformationHandler;

import java.lang.reflect.Field;
import java.util.function.Predicate;

public class WithPostMixinTransformer extends DelegateTransformer {

    public Predicate<String> predicate;
    private BCDExtension extension;

    public WithPostMixinTransformer(IMixinTransformer transformer, BCDExtension extension, Predicate<String> predicate) {
        super(transformer);
        this.extension = extension;
        this.predicate = predicate;
    }

    public static void setCurrentTransformer(MixinTransformationHandler handler, BCDExtension extension, Predicate<String> predicate) throws NoSuchFieldException, IllegalAccessException {
        Field field = MixinTransformationHandler.class.getDeclaredField("transformer");
        field.setAccessible(true);

        if(field.get(handler) instanceof IMixinTransformer oldTransformer) {
            field.set(handler, new WithPostMixinTransformer(oldTransformer, extension, predicate));
        }
    }

    @SneakyThrows
    public static void setFactory(MixinTransformationHandler handler, BCDExtension extension, Predicate<String> predicate) {
        Field field = MixinTransformationHandler.class.getDeclaredField("transformerFactory");
        field.setAccessible(true);
        IMixinTransformerFactory oldFactory = (IMixinTransformerFactory) field.get(handler);

        field.set(handler, (IMixinTransformerFactory)
            () -> new WithPostMixinTransformer(oldFactory.createTransformer(), extension, predicate)
        );
    }

    public static void register(BCDExtension extension, Predicate<String> predicate) {
        try {
            IMixinService service = MixinService.getService();
            if(service instanceof MixinServiceModLauncher modLauncher) {
                var method = MixinServiceModLauncher.class.getDeclaredMethod("getTransformationHandler");
                method.setAccessible(true);
                MixinTransformationHandler handler = (MixinTransformationHandler) method.invoke(modLauncher);
                setFactory(handler, extension, predicate);
                setCurrentTransformer(handler, extension, predicate);
            }
        } catch(Exception e) {
            throw Lombok.sneakyThrow(e);
        }
    }

    @Override
    public boolean transformClass(MixinEnvironment environment, String name, ClassNode classNode) {

        boolean b = delegate.transformClass(environment, name, classNode);
        if(predicate.test(name.replace('.','/'))){
            b|=extension.postprocess(classNode);
        }
        return b;
    }

    @Override
    public byte[] transformClassBytes(String name, String transformedName, byte[] basicClass) {

        byte[] bytes = delegate.transformClassBytes(name, transformedName, basicClass);
        return postprocessBytes(transformedName, bytes);
    }

    private byte[] postprocessBytes(String transformedName, byte[] bytes) {
        if(predicate.test(transformedName)) {
            ClassNode classNode = readClass(transformedName, bytes);
            if(extension.postprocess(classNode)) {
                bytes = writeClass(classNode);
            }
        }
        return bytes;
    }

    @Override
    public byte[] transformClass(MixinEnvironment environment, String name, byte[] classBytes) {
        return postprocessBytes(name,delegate.transformClass(environment, name, classBytes));
        //return super.transformClass(environment, name, classBytes);
    }

    @Override
    public byte[] generateClass(MixinEnvironment environment, String name) {
        return delegate.generateClass(environment, name);
    }

    @Override
    public boolean generateClass(MixinEnvironment environment, String name, ClassNode classNode) {
        return delegate.generateClass(environment, name, classNode);
    }

}
