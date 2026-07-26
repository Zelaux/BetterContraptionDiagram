package processor.debugOnly;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.ArrayList;

public class AnnotatedElementsCollector extends ClassVisitor {

    @Getter
    protected final ArrayList<AnnotatedElement<FieldNode>> annotatedFields;
    @Getter
    protected final ArrayList<AnnotatedElement<MethodNode>> annotatedMethods;
    @Getter
    protected boolean isAnnotated;
    protected String annotationTypeDescriptor;
    @Getter
    protected ArrayList<AnnotationNode> ownDescriptors;

    @AllArgsConstructor
    public static class AnnotatedElement<T> {
        public final T node;
        public final ArrayList<AnnotationNode> annotations = new ArrayList<>();

    }

    public AnnotatedElementsCollector(ClassVisitor classVisitor, String annotationTypeDescriptor) {
        super(Opcodes.ASM9, classVisitor);

        annotatedFields = new ArrayList<>();
        annotatedMethods = new ArrayList<>();
        this.annotationTypeDescriptor = annotationTypeDescriptor;
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        if(!descriptor.equals(annotationTypeDescriptor)) return null;
        if(ownDescriptors == null) ownDescriptors = new ArrayList<>();
        AnnotationNode node = new AnnotationNode(descriptor);
        ownDescriptors.add(node);
        isAnnotated = true;
        return node;
    }

    @Override
    public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {

        FieldNode node = new FieldNode(access, name, descriptor, signature, value);
        return new FieldVisitor(api, node) {
            AnnotatedElement<FieldNode> myAnnotated;

            @Override
            public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
                if(isAnnotated || descriptor.equals(annotationTypeDescriptor)) {

                    if(myAnnotated == null) {
                        myAnnotated = new AnnotatedElement<>(node);
                        annotatedFields.add(myAnnotated);
                    }
                    AnnotationNode node = new AnnotationNode(descriptor);
                    myAnnotated.annotations.add(node);
                    return node;
                }
                return super.visitAnnotation(descriptor, visible);
            }

            @Override
            public void visitEnd() {
                super.visitEnd();
                if(myAnnotated!=null) return;
                if(cv != null) node.accept(cv);
            }
        };
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        var node = new MethodNode(api, access, name, descriptor, signature, exceptions);
        return new MethodVisitor(api, node) {
            AnnotatedElement<MethodNode> myAnnotated;

            @Override
            public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
                if(isAnnotated || descriptor.equals(annotationTypeDescriptor)) {
                    if(myAnnotated==null){
                        myAnnotated=new AnnotatedElement<>(node);
                        annotatedMethods.add(myAnnotated);
                    }
                    AnnotationNode anno = new AnnotationNode(descriptor);
                    myAnnotated.annotations.add(anno);
                    return anno;
                }
                return super.visitAnnotation(descriptor, visible);
            }

            @Override
            public void visitEnd() {
                super.visitEnd();
                if(myAnnotated!=null) return;
                if(cv != null) node.accept(cv);
            }
        };
    }
}
