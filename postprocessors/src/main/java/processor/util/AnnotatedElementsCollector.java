package processor.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.ArrayList;
import java.util.Collection;

public class AnnotatedElementsCollector extends ClassVisitor {

    @Getter
    protected final ArrayList<AnnotatedElement<FieldNode>> annotatedFields;
    @Getter
    protected final ArrayList<AnnotatedElement<MethodNode>> annotatedMethods;
    @Getter
    protected boolean isAnnotated;
    public boolean registerAllNestedIfAnnotated =false;
    protected String annotationTypeDescriptor;
    @Getter
    protected ArrayList<AnnotationNode> ownDescriptors;
    @Getter
    protected Type classType;

    public void registerFound(Collection<CodeEntry> entries) {
        Type type = classType;
        if(isAnnotated()){
            entries.add(CodeEntry.classEntry(type));
        }
        for(var node : annotatedFields()) {
            entries.add(CodeEntry.fieldEntry(type, node.node));
        }
        for(var node : annotatedMethods()) {
            entries.add(CodeEntry.methodEntry(type, node.node));
        }
    }

    public boolean isEmpty() {
        return !isAnnotated && annotatedFields.isEmpty() && annotatedMethods.isEmpty();
    }

    @AllArgsConstructor
    public static class AnnotatedElement<T> {
        public final T node;
        public final ArrayList<AnnotationNode> annotations = new ArrayList<>();

    }

    public AnnotatedElementsCollector(ClassVisitor classVisitor, String annotationTypeDescriptor, boolean registerAllNestedIfAnnotated) {
        super(Opcodes.ASM9, classVisitor);

        this.registerAllNestedIfAnnotated =registerAllNestedIfAnnotated;
        annotatedFields = new ArrayList<>();
        annotatedMethods = new ArrayList<>();
        this.annotationTypeDescriptor = annotationTypeDescriptor;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        isAnnotated=false;
        ownDescriptors=null;
        annotatedFields.clear();
        annotatedMethods.clear();

        classType =Type.getObjectType(name);

        super.visit(version, access, name, signature, superName, interfaces);
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
                if(isAnnotated && registerAllNestedIfAnnotated){
                    AnnotatedElement<FieldNode> e = new AnnotatedElement<>(node);
                    e.annotations.add(AnnotatedElementsCollector.this.ownDescriptors.getFirst());
                    annotatedFields.add(e);
                    return;
                }
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
                if(isAnnotated && registerAllNestedIfAnnotated){
                    var e = new AnnotatedElement<>(node);
                    e.annotations.add(AnnotatedElementsCollector.this.ownDescriptors.getFirst());
                    annotatedMethods.add(e);
                    return;
                }
                if(cv != null) node.accept(cv);
            }
        };
    }
}
