package processor.debugOnly;

import lombok.Getter;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.ArrayList;

class IsDebugCollector extends ClassVisitor {
    @Getter
    private final ArrayList<FieldNode> debugFields;
    @Getter
    private final ArrayList<MethodNode> debugMethods;
    @Getter
    private boolean isDebug;

    public IsDebugCollector(ClassVisitor classVisitor) {
        super(Opcodes.ASM9, classVisitor);

        debugFields = new ArrayList<>();
        debugMethods = new ArrayList<>();
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        if(isDebug) return null;
        isDebug = descriptor.equals(RemovedFromBuild.ANNOTATION_TYPE_DESCRIPTOR);
        return null;
    }

    @Override
    public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {

        FieldNode node = new FieldNode(access, name, descriptor, signature, value);
        return new FieldVisitor(api, node) {
            boolean nodeDebug = false;

            @Override
            public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
                if(isDebug || descriptor.equals(RemovedFromBuild.ANNOTATION_TYPE_DESCRIPTOR)) {
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
                if(isDebug || descriptor.equals(RemovedFromBuild.ANNOTATION_TYPE_DESCRIPTOR)) {
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
