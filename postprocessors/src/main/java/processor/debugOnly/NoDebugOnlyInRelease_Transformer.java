package processor.debugOnly;

import asmlib.transform.AbstractClassFileTransformer;
import asmlib.transform.LazyByteCodeProvider;
import asmlib.transform.TransformationWriter;
import asmlib.transform.context.TransformationContext;
import asmlib.util.ClassFileMetaData;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Type;
import processor.Vars;
import processor.util.AnnotatedElementsCollector;
import processor.util.CodeEntry;

import java.util.HashSet;

public class NoDebugOnlyInRelease_Transformer extends AbstractClassFileTransformer {
    public static final String ANNOTATION_CLASS_NAME = "com.zelaux.betterdiagram.annotations.DebugOnly";
    public static final Type ANNOTATION_TYPE = Type.getObjectType(ANNOTATION_CLASS_NAME.replace('.', '/'));
    public static final String ANNOTATION_TYPE_DESCRIPTOR = ANNOTATION_TYPE.getDescriptor();
    public final boolean isDebug;

public   static   final HashSet<CodeEntry> debugEntries = new HashSet<>();
    final ErrorMarker ERROR_MARKER = new ErrorMarker(debugEntries, this);
    boolean hasErrors;


    public NoDebugOnlyInRelease_Transformer(TransformationContext context) {
        super(Vars.currentMaxRounds+1);
        isDebug = context.extraArguments().isDebug();
    }

    @Override
    public boolean shouldAnalyze(String className, TransformationContext context) {
        return !isDebug;
    }

    @Override
    public void finishRound(TransformationContext context) {
        if(roundIndex == 0) {
            System.out.println("debugEntries:");
            for(CodeEntry entry : debugEntries) {
                System.out.print("    ");
                System.out.println(entry);
            }
        }
        super.finishRound(context);
    }

    @Override
    public @Nullable TransformationWriter analyze(String className, LazyByteCodeProvider byteCodeProvider, TransformationContext context) {
        ClassFileMetaData metaData = new ClassFileMetaData(byteCodeProvider.getCloned());
        if(!metaData.usesAnnotation(ANNOTATION_TYPE_DESCRIPTOR) && roundIndex == 0) return null;

        ClassVisitor classVisitor = roundLeft == 2 ? ERROR_MARKER : null;
        AnnotatedElementsCollector debugCollector = new AnnotatedElementsCollector(classVisitor, ANNOTATION_TYPE_DESCRIPTOR, false);
        byteCodeProvider
            .createReader()
            .accept(debugCollector, roundIndex == 1 ? 0 : ClassReader.SKIP_CODE | ClassReader.SKIP_FRAMES);

        String typeInternalName = metaData.getClassName();
        Type dataClassName = Type.getObjectType(typeInternalName);
        if(debugCollector.isAnnotated()) {
            debugEntries.add(CodeEntry.classEntry(dataClassName));
        }
        for(var node : debugCollector.annotatedFields()) {
            debugEntries.add(CodeEntry.fieldEntry(dataClassName, node.node));
        }
        for(var node : debugCollector.annotatedMethods()) {
            debugEntries.add(CodeEntry.methodEntry(dataClassName, node.node));
        }
        if(roundLeft == 1 && hasErrors) {
            System.err.println("You cannot use things marked as @DebugOnly in release build");
            System.exit(1);
            RuntimeException exception = new RuntimeException("This code has errors");
            exception.setStackTrace(new StackTraceElement[0]);
            throw exception;
        }
        return TransformationWriter.CONTINUE_WRITER;
    }


}
