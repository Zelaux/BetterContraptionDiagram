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

import java.io.File;
import java.util.HashSet;

public class RemovedFromBuild extends AbstractClassFileTransformer {
    public static final String ANNOTATION_CLASS_NAME = "com.zelaux.betterdiagram.annotations.DebugOnly";
    public static final Type ANNOTATION_TYPE = Type.getObjectType(ANNOTATION_CLASS_NAME.replace('.', '/'));
    public static final String ANNOTATION_TYPE_DESCRIPTOR = ANNOTATION_TYPE.getDescriptor();
    public static final boolean enabled;

    static {
        File file = new File("build/releaseMarker");
        System.out.println(file.getAbsolutePath());
        System.out.println(file.exists());
        enabled = file.exists();
    }

    final HashSet<DebugEntry> debugEntries = new HashSet<>();
    final ErrorMarker ERROR_MARKER = new ErrorMarker(debugEntries, this);
    boolean hasErrors;


    public RemovedFromBuild() {
        super(3);
    }

    @Override
    public boolean shouldAnalyze(String className, TransformationContext context) {
        return enabled;
    }

    @Override
    public void finishRound(TransformationContext context) {
        if(roundIndex==0){
            for(DebugEntry entry : debugEntries) {
                System.out.println(entry);
            }
        }
        super.finishRound(context);
    }

    @Override
    public @Nullable TransformationWriter analyze(String className, LazyByteCodeProvider byteCodeProvider, TransformationContext context) {
        ClassFileMetaData metaData = new ClassFileMetaData(byteCodeProvider.getCloned());
        if(!metaData.usesAnnotation(ANNOTATION_TYPE_DESCRIPTOR) && roundIndex==0) return null;

        ClassVisitor classVisitor = roundIndex == 1 ? ERROR_MARKER : null;
        IsDebugCollector debugCollector = new IsDebugCollector(classVisitor);
        byteCodeProvider
            .createReader()
            .accept(debugCollector, roundIndex==1?0:ClassReader.SKIP_CODE | ClassReader.SKIP_FRAMES);

        Type dataClassName = Type.getObjectType(metaData.getClassName());
        if(debugCollector.isDebug()) {
            debugEntries.add(DebugEntry.classEntry(dataClassName));
        }
        for(var node : debugCollector.debugFields()) {
            debugEntries.add(DebugEntry.fieldEntry(dataClassName, node));
        }
        for(var node : debugCollector.debugMethods()) {
            debugEntries.add(DebugEntry.methodEntry(dataClassName, node));
        }
        if(roundIndex == 2 && hasErrors) {
            System.err.println("You cannot use things marked as @DebugOnly in release build");
            System.exit(1);
            RuntimeException exception = new RuntimeException("This code has errors");
            exception.setStackTrace(new StackTraceElement[0]);
            throw exception;
        }
        return TransformationWriter.CONTINUE_WRITER;
    }


}
