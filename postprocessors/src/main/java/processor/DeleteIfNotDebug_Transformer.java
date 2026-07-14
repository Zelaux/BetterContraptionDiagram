package processor;

import asmlib.transform.AbstractClassFileTransformer;
import asmlib.transform.LazyByteCodeProvider;
import asmlib.transform.TransformationWriter;
import asmlib.transform.context.TransformationContext;
import asmlib.util.ClassFileMetaData;
import asmlib.util.NodeUtil;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import processor.debugOnly.IsDebugCollector;

import java.util.List;

public class DeleteIfNotDebug_Transformer extends AbstractClassFileTransformer {
    public static final String ANNOTATION_CLASS_NAME = "com.zelaux.betterdiagram.annotations.DeleteInRelease";
    public static final String ANNOTATION_CLASS_NAME2 = "com.zelaux.betterdiagram.annotations.EmptyInRelease";
    public static final Type ANNOTATION_TYPE = Type.getObjectType(ANNOTATION_CLASS_NAME.replace('.', '/'));
    public static final Type ANNOTATION_TYPE2 = Type.getObjectType(ANNOTATION_CLASS_NAME2.replace('.', '/'));
    public static final String ANNOTATION_TYPE_DESCRIPTOR = ANNOTATION_TYPE.getDescriptor();
    public static final String ANNOTATION_TYPE2_DESCRIPTOR = ANNOTATION_TYPE2.getDescriptor();
    public final boolean isDebug;


    public DeleteIfNotDebug_Transformer(TransformationContext context) {
        super(1);
        isDebug = context.extraArguments().isDebug();
    }

    @Override
    public boolean shouldAnalyze(String className, TransformationContext context) {
        return !isDebug;
    }


    @Override
    public @Nullable TransformationWriter analyze(String className, LazyByteCodeProvider byteCodeProvider, TransformationContext context) {
        ClassFileMetaData metaData = new ClassFileMetaData(byteCodeProvider.getCloned());
        if(!metaData.usesAnnotation(ANNOTATION_TYPE_DESCRIPTOR) && !metaData.usesAnnotation(ANNOTATION_TYPE2_DESCRIPTOR)) return null;

        ClassNode classNode = byteCodeProvider.createClassNode();
        IsDebugCollector debugCollector = new IsDebugCollector(null, ANNOTATION_TYPE_DESCRIPTOR);
        classNode.accept(debugCollector);
        if(!debugCollector.isDebug() && NodeUtil.findAnnotation(
            ANNOTATION_TYPE2_DESCRIPTOR, classNode.invisibleAnnotations, classNode.visibleAnnotations
        ) != null) {
            return TransformationWriter.nodeTransformer(x -> {
                x.fields.clear();
                List<MethodNode> methods = x.methods;
                for(int i = 0; i < methods.size(); i++) {
                    MethodNode methodNode = methods.get(i);
                    if(methodNode.name.endsWith("init>")) continue;
                    methods.remove(i--);
                }
                return x;
            });
        }
        return lastRound(className, debugCollector);
    }

    private TransformationWriter lastRound(String className, IsDebugCollector debugCollector) {

        if(debugCollector.isDebug()) return TransformationWriter.DELETE_WRITER;

        var set = new ObjectOpenHashSet<String>();
        debugCollector.debugFields().forEach(x -> set.add(x.name));
        debugCollector.debugMethods().forEach(x -> set.add(x.name + "#" + x.desc));
        return TransformationWriter.nodeTransformer(classNode -> {
            var fields = classNode.fields;

            for(int i = 0; i < fields.size(); i++) {
                var node = fields.get(i);
                if(set.contains(node.name)) fields.remove(i--);
            }
            var methods = classNode.methods;
            for(int i = 0; i < methods.size(); i++) {
                var node = methods.get(i);
                if(set.contains(node.name + "#" + node.desc)) methods.remove(i--);
            }
            return classNode;
        });
    }


}
