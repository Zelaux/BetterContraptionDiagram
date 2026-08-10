package com.zelaux.betterdiagram.mixin_plugin;

import com.zelaux.betterdiagram.DebugRuntime;
import com.zelaux.betterdiagram.mixin_plugin.postprocessors.GadgetsPostprocessor;
import com.zelaux.betterdiagram.mixin_plugin.postprocessors.VanilaPostprocessor;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import java.util.regex.Pattern;
import java.util.stream.Stream;

public class BCDPostprocessors {
    private static final ObjectArrayList<Postprocessor> postprocessors = new ObjectArrayList<>();
    private static boolean dirty = true;
    private static Pattern globalPattern = null;


    static {
        postprocessor(VanilaPostprocessor.INSTANCE);
    }

    public static Stream<Postprocessor> postprocessorsFor(String internalClassName) {
        if(globalPattern == null || dirty) {
            dirty = false;
            globalPattern = createGlobalPattern();
        }
        if(!globalPattern.matcher(internalClassName).matches()) return Stream.empty();
        return postprocessors.stream().filter(it -> it.filter().matcher(internalClassName).matches());
    }

    private static Pattern createGlobalPattern() {
        StringBuilder builder = new StringBuilder();
        for(Postprocessor postprocessor : postprocessors) {
            builder.append('(').append(postprocessor.filter().pattern()).append(")|");
        }
        builder.setLength(builder.length() - 1);
        return Pattern.compile(builder.toString());
    }

    public static void postprocessor(Postprocessor postprocessor) {
        dirty = true;
        postprocessors.add(postprocessor);
    }

    public static void debugSave(ClassNode targetClass) {
        if(DebugRuntime.isDebug()) {
            ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
            targetClass.accept(writer);
            DebugRuntime.saveInFile(
                targetClass.name.substring(targetClass.name.lastIndexOf("/") + 1) + ".class",
                writer.toByteArray()
            );
        }
    }

    public interface Postprocessor {
        Pattern filter();

        static Pattern orFilter(String... parts) {
            for(int i = 0; i < parts.length; i++) {
                parts[i] = Pattern.quote(parts[i]);
            }
            return Pattern.compile(String.join("|", parts));
        }

        boolean process(ClassNode targetClass);
    }
}
