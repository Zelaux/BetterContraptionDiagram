package com.zelaux.betterdiagram.mixin_plugin.system;

import com.zelaux.betterdiagram.DebugRuntime;
import com.zelaux.betterdiagram.mixin_plugin.postprocessors.VanilaProcessorProvider;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class BCDPostprocessors {
    private static final ObjectArrayList<Entry> postprocessors = new ObjectArrayList<>();
    private static boolean dirty = true;
    private static Map<String, List<ProcessorProvider.ClassTransform>> globalMap;

    public static Stream<ProcessorProvider> all() {
        return postprocessors.stream().map(Entry::processorProvider);
    }

    //private static Pattern globalPattern = null;
    record Entry(ProcessorProvider processorProvider,
                 Object2ObjectOpenHashMap<String, List<ProcessorProvider.ClassTransform>> providedTransformations) {}


    static {
        postprocessor(VanilaProcessorProvider.INSTANCE);
    }

    public static Stream<ProcessorProvider.ClassTransform> postprocessorsFor(String internalClassName) {
        if(!hasAnyPostprocessorFor(internalClassName)) return Stream.empty();

        return globalMap.getOrDefault(internalClassName, List.of()).stream();
    }

    public static boolean hasAnyPostprocessorFor(String internalClassName) {
        if(globalMap == null || dirty) {
            dirty = false;
            globalMap = createGlobalMap();
        }
        return globalMap.containsKey(internalClassName);
    }

    private static Map<String, List<ProcessorProvider.ClassTransform>> createGlobalMap() {
        var map = new Object2ObjectOpenHashMap<String, List<ProcessorProvider.ClassTransform>>();

        for(Entry entry : postprocessors) {
            entry.providedTransformations.forEach((s, transforms) ->
                map.computeIfAbsent(s, it->new ObjectArrayList<>()).addAll(transforms));
        }

        return map;
    }

    public static void postprocessor(ProcessorProvider processorProvider) {
        dirty = true;

        var map = new Object2ObjectOpenHashMap<String, List<ProcessorProvider.ClassTransform>>();
        ProcessorProvider.Transformations[] transformations = processorProvider.transformations();
        if(transformations != null) {
            for(var transformation : transformations) {
                for(Type type : transformation.type()) {
                    map.computeIfAbsent(type.getInternalName(),it->new ObjectArrayList<>()).add(transformation.transform());
                }
            }
        }

        postprocessors.add(new Entry(processorProvider, map));
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
