package processor;

import asmlib.transform.AbstractClassFileTransformer;
import asmlib.transform.LazyByteCodeProvider;
import asmlib.transform.TransformationWriter;
import asmlib.transform.context.TransformationContext;
import asmlib.util.ClassFileMetaData;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Type;
import com.zelaux.bcd.asm_commons.AnnotatedElementsCollector;
import com.zelaux.bcd.asm_commons.CodeEntry;

import java.util.regex.Pattern;

public class SilentDelete_Transformer extends AbstractClassFileTransformer {
    public static final Type SILENT_DELETE_ANNOTATION_TYPE = Type.getObjectType("com.zelaux.betterdiagram.annotations.SilentDelete".replace('.', '/'));
    private final AnnotatedElementsCollector collector = new AnnotatedElementsCollector(null, SILENT_DELETE_ANNOTATION_TYPE.getDescriptor(), false);
    private ObjectArrayList<String> classToVisit = new ObjectArrayList<>();
    private ObjectArrayList<String> classToDelete = new ObjectArrayList<>();

    public SilentDelete_Transformer() {
        super(2);
    }

    private Pattern patternToDelete = Pattern.compile(""), patternToVisit = Pattern.compile("");

    @Override
    public void finishRound(TransformationContext context) {
        super.finishRound(context);
        patternToDelete = constructPattern(classToDelete, ".*");
        patternToVisit = constructPattern(classToVisit, ".*");
    }

    private @NotNull Pattern constructPattern(ObjectArrayList<String> classToDelete1, @Language("RegExp") String postfix) {
        String[] array = classToDelete1.toArray(String[]::new);
        for(int i = 0; i < array.length; i++) {
            array[i] = Pattern.quote(array[i]);
        }
        Pattern compile = Pattern.compile(
            "(" + String.join("|", array) + ")" + postfix
        );
        return compile;
    }

    @Override
    public boolean shouldAnalyze(String className, TransformationContext context) {
        return roundIndex == 0 || patternToVisit.matcher(className.replace('.', '/')).matches();
    }

    ObjectOpenHashSet<CodeEntry> entries = new ObjectOpenHashSet<>();

    @Override
    public @Nullable TransformationWriter analyze(String className, LazyByteCodeProvider byteCodeProvider, TransformationContext context) {
        ClassFileMetaData metaData = new ClassFileMetaData(byteCodeProvider.getCloned());
        if(roundIndex != 0) return deletionRound(metaData.getClassName());
        if(!metaData.usesAnnotation(SILENT_DELETE_ANNOTATION_TYPE.getDescriptor())) return null;
        return registerRound(byteCodeProvider);
    }

    private TransformationWriter deletionRound(String className) {
        if(patternToDelete.matcher(className).matches())
            return TransformationWriter.DELETE_WRITER;

        return TransformationWriter.nodeTransformer(node -> {
            Type type = Type.getObjectType(node.name);
            node.fields.removeIf(t -> entries.contains(CodeEntry.fieldEntry(type, t)));
            node.methods.removeIf(t -> entries.contains(CodeEntry.methodEntry(type, t)));
            return node;
        });
    }

    private @Nullable TransformationWriter registerRound(LazyByteCodeProvider byteCodeProvider) {
        byteCodeProvider.createReader().accept(collector, 0);
        collector.registerFound(entries);
        String internalName = collector.classType().getInternalName();
        if(!collector.isEmpty()) this.classToVisit.add(internalName);
        if(collector.isAnnotated()) classToDelete.add(internalName);
        return null;
    }
}
