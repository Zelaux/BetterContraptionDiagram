package com.zelaux.betterdiagram.mixin_plugin.postprocessors;

import com.zelaux.bcd.asm_commons.BoundMethod;
import com.zelaux.bcd.asm_commons.CodeEntry;
import com.zelaux.bcd.asm_commons.MethodUtil;
import com.zelaux.bcd.asm_commons.Renamer;
import com.zelaux.betterdiagram.annotations.CompileTool;
import com.zelaux.betterdiagram.mixin_plugin.TypeIndex;
import com.zelaux.betterdiagram.mixin_plugin.system.Postprocessor;
import com.zelaux.betterdiagram.mixin_plugin.util.ServerToUnboundRenamer;
import dev.silvergold.simulatedcoasters.track.cart.CoasterCartTrainLinkConstraint;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InnerClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodNode;

import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

public enum CoasterPostprocessor implements Postprocessor {
    INSTANCE;

    static Type CoasterCartTrainLinkConstraint = CompileTool.bakeType(CoasterCartTrainLinkConstraint.class);

    //SubLevelPhysicsSystem physics, ActiveLink link, ServerSubLevel cartA, ServerSubLevel cartB
    static Method searchMethod = new Method("updateConstraint", Type.VOID_TYPE, new Type[]{
        TypeIndex.SubLevelPhysicsSystem, TypeIndex.CoasterCartTrainLinkConstraint_ActiveLink, TypeIndex.ServerSubLevel, TypeIndex.ServerSubLevel,
    });
    static Method generatedMethod = new Method("bcd$updateConstraint", TypeIndex.GenericConstraintConfiguration, new Type[]{
        TypeIndex.Object, TypeIndex.CoasterCartTrainLinkConstraint_ActiveLink, TypeIndex.SubLevel, TypeIndex.SubLevel,
    });
    static BoundMethod localJointOrientation = BoundMethod.make(
        CoasterCartTrainLinkConstraint,
        "localJointOrientation", TypeIndex.Quaterniond,
        TypeIndex.ServerSubLevel, TypeIndex.Quaterniond
    );
    static BoundMethod localJointOrientation_2 = BoundMethod.make(
        CoasterCartTrainLinkConstraint,
        "localJointOrientation", TypeIndex.Quaterniond,
        TypeIndex.SubLevel, TypeIndex.Quaterniond
    );
    //localJointOrientation

    @Override
    public Type[] usedClasses() {
        return Postprocessor.types(
            CoasterCartTrainLinkConstraint,
            TypeIndex.CoasterCartTrainLinkConstraint_ActiveLink
        );
    }

    @Override
    public boolean process(ClassNode targetClass) {
        if(targetClass.name.equals(TypeIndex.CoasterCartTrainLinkConstraint_ActiveLink.getInternalName())) {
            int acc = Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC;
            targetClass.access = acc | Opcodes.ACC_FINAL;
            makeActiveLinkPublic(targetClass);
            for(MethodNode method : targetClass.methods) {
                method.access = Opcodes.ACC_PUBLIC;
            }
            for(var field : targetClass.fields) {
                field.access = Opcodes.ACC_PUBLIC;
            }

            return true;
        }
        MethodNode found = GadgetsPostprocessor.findMethod(targetClass, generatedMethod, searchMethod);
        if(found == null) return false;
        makeActiveLinkPublic(targetClass);
        List<MethodNode> methods = targetClass.methods;
        for(int i = 0; i < methods.size(); i++) {
            MethodNode method = methods.get(i);
            if(GadgetsPostprocessor.matches(method, localJointOrientation.method())) {
                ;
                ClassNode classNode = MethodUtil.withWrappedMethodVisitor(ServerToUnboundRenamer::make);
                method.accept(classNode);
                MethodNode clone = classNode.methods.getFirst();
                clone.desc = localJointOrientation_2.getDescriptor();
                methods.add(clone);
                break;
            }
        }

        MethodNode clone = new MethodNode(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, generatedMethod.getName(), generatedMethod.getDescriptor(), found.signature, null);

        found.accept(new MyGenerator(clone, found));
        targetClass.methods.add(clone);

        return true;

    }

    private boolean transformInner(ClassNode targetClass, Type type, Predicate<InnerClassNode> deleteVisitor) {
        for(Iterator<InnerClassNode> iterator = targetClass.innerClasses.iterator(); iterator.hasNext(); ) {
            InnerClassNode innerClass = iterator.next();
            if(innerClass.name.equals(type.getInternalName())) {
                if(deleteVisitor.test(innerClass)) {
                    iterator.remove();
                }
                return true;
            }
        }
        return false;
    }

    private void makeActiveLinkPublic(ClassNode targetClass1) {
        transformInner(targetClass1, TypeIndex.CoasterCartTrainLinkConstraint_ActiveLink, innerClass -> {
            innerClass.access = Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL;
            return true;
        });
    }


    private static class MyRenamer extends Renamer {


        public MyRenamer(MethodVisitor methodVisitor) {
            super(Opcodes.ASM9, methodVisitor);
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
            super.visitMethodInsn(opcode, transformOwner(owner), name, descriptor, isInterface);
        }

        @Override
        public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
            super.visitFieldInsn(opcode, transformOwner(owner), name, descriptor);
        }

        protected String transformOwner(String owner) {
            if(owner.equals(TypeIndex.ServerSubLevel.getInternalName())) {
                return TypeIndex.SubLevel.getInternalName();
            }
            return owner;
        }
    }

    private static class MyGenerator extends GeneratorAdapter {

        private final InsnList instructions;
        private final Renamer renamer;

        public MyGenerator(MethodNode ownNode, MethodNode found) {
            super(Opcodes.ASM9, ServerToUnboundRenamer.make(ownNode), ownNode.access, ownNode.name, ownNode.desc);
            instructions = ownNode.instructions;
            renamer = (Renamer) mv;
            Type type = CoasterCartTrainLinkConstraint;
            renamer.addMapping(CodeEntry.methodEntry(localJointOrientation),CodeEntry.methodEntry(localJointOrientation_2));

        }

        private final ObjectOpenHashSet<Label> visitedLabel = new ObjectOpenHashSet<>();
        private final ObjectOpenHashSet<Label> labelQueue = new ObjectOpenHashSet<>();

        @Override
        public void visitLabel(Label label) {
            super.visitLabel(label);
            visitedLabel.add(label);
            labelQueue.remove(label);
        }

        @Override
        public void visitJumpInsn(int opcode, Label label) {
            super.visitJumpInsn(opcode, label);
            if(visitedLabel.contains(label)) return;
            labelQueue.add(label);
        }

        @Override
        public void visitInsn(int opcode) {
            if(opcode == Opcodes.RETURN) {
                visitInsn(Opcodes.ACONST_NULL);
                visitInsn(Opcodes.ARETURN);
                return;

            }
            super.visitInsn(opcode);
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
            super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
            if(opcode == Opcodes.INVOKESPECIAL && name.equals("<init>")) {
                if(owner.equals(TypeIndex.GenericConstraintConfiguration.getInternalName())) {
                    returnValue();
                    for(Label label : labelQueue) {
                        visitLabel(label);
                    }
                    push((String) null);
                    returnValue();
                    visitEnd();
                    mv = null;
                }
            }
        }


    }

}
