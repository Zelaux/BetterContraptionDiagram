package com.zelaux.betterdiagram.mixin_plugin.system;

import com.google.common.collect.ImmutableSet;
import com.zelaux.betterdiagram.DebugRuntime;
import com.zelaux.betterdiagram.mixin_plugin.postprocessors.VanilaPostprocessor;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;

import java.util.stream.Stream;

public class BCDPostprocessors {
    private static final ObjectArrayList<Entry> postprocessors = new ObjectArrayList<>();
    private static boolean dirty = true;
    private static ImmutableSet<String> globalSet;

    public static Stream<Postprocessor> all() {
        return postprocessors.stream().map(Entry::postprocessor);
    }

    //private static Pattern globalPattern = null;
    record Entry(Postprocessor postprocessor, ImmutableSet<String> internalNames) {}


    static {
        postprocessor(VanilaPostprocessor.INSTANCE);
    }

    public static Stream<Postprocessor> postprocessorsFor(String internalClassName) {
        if(!hasAnyPostprocessorFor(internalClassName)) return Stream.empty();
        return postprocessors.stream().filter(it -> it.internalNames.contains(internalClassName)).map(Entry::postprocessor);
    }

    public static boolean hasAnyPostprocessorFor(String internalClassName) {
        if(globalSet == null || dirty) {
            dirty = false;
            globalSet = createGlobalSet();
        }
        return globalSet.contains(internalClassName);
    }

    private static ImmutableSet<String> createGlobalSet() {
        var builder = ImmutableSet.<String>builder();
        for(Entry entry : postprocessors) {
            builder.addAll(entry.internalNames);
        }
        return builder.build();
    }

    public static void postprocessor(Postprocessor postprocessor) {
        dirty = true;

        var builder = ImmutableSet.<String>builder();
        for(Type type : postprocessor.usedClasses()) {
            builder.add(type.getInternalName());
        }

        postprocessors.add(new Entry(postprocessor, builder.build()));
    }

    public static void debugSave(ClassNode targetClass) {
        if(DebugRuntime.isDebug()) {
            String fileName = targetClass.name.substring(targetClass.name.lastIndexOf("/") + 1) + ".class";
            DebugRuntime.LOGGER.info("Debug save: {}", fileName);
            ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
            targetClass.accept(writer);
            DebugRuntime.saveInFile(
                fileName,
                writer.toByteArray()
            );
        }
    }

}
