package processor.debugOnly;

import lombok.Getter;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.ArrayList;

public class IsDebugCollector extends ClassVisitor {
    @Getter
    private final ArrayList<FieldNode> debugFields;
    @Getter
    private final ArrayList<MethodNode> debugMethods;
    @Getter
    private boolean isDebug;
    private String annotationTypeDescriptor;

    public IsDebugCollector(ClassVisitor classVisitor, String annotationTypeDescriptor) {
        super(Opcodes.ASM9, classVisitor);

        debugFields = new ArrayList<>();
        debugMethods = new ArrayList<>();
        this.annotationTypeDescriptor = annotationTypeDescriptor;
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        if(isDebug) return null;
        isDebug = descriptor.equals(annotationTypeDescriptor);
        return null;
    }

    @Override
    public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {

        FieldNode node = new FieldNode(access, name, descriptor, signature, value);
        return new FieldVisitor(api, node) {
            boolean nodeDebug = false;

            @Override
            public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
                if(isDebug || descriptor.equals(RemovedFromBuild_Transformer.ANNOTATION_TYPE_DESCRIPTOR)) {
                    if(!nodeDebug){
                        nodeDebug = true;
                        debugFields.add(node);
                    }
                }
                return super.visitAnnotation(descriptor, visible);
            }

            @Override
            public void visitEnd() {
                super.visitEnd();
                if(nodeDebug) return;
                if(cv!=null)node.accept(cv);
            }
        };
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        var node = new MethodNode(api,access,name,descriptor,signature,exceptions);
        return new MethodVisitor(api, node) {
            boolean nodeDebug = false;
            @Override
            public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
                if(isDebug || descriptor.equals(annotationTypeDescriptor)) {
                    if(!nodeDebug){
                        nodeDebug = true;
                        debugMethods.add(node);
                    }
                }
                return super.visitAnnotation(descriptor, visible);
            }

            @Override
            public void visitEnd() {
                super.visitEnd();
                if(nodeDebug) return;
                if(cv!=null)node.accept(cv);
            }
        };
    }
}
