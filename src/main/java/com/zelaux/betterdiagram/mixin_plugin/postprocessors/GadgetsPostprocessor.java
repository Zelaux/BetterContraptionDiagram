package com.zelaux.betterdiagram.mixin_plugin.postprocessors;

import com.zelaux.betterdiagram.mixin_plugin.BCDPostprocessors;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;
import org.objectweb.asm.tree.*;

import java.util.regex.Pattern;


public class GadgetsPostprocessor implements BCDPostprocessors.Postprocessor {
    public static final GadgetsPostprocessor INSTANCE = new GadgetsPostprocessor();
    private static final Type physicsShaft = Type.getObjectType("com/rieno/gadgetsandgizmos/content/PhysicsGantryShaftBlockEntity");

    private static final Type BlockEntitySubLevelActor = Type.getObjectType("dev/ryanhcode/sable/api/block/BlockEntitySubLevelActor");
    public static final Method generatedMethod = new Method("bcd$hasActor", BlockEntitySubLevelActor, new Type[0]);
    public static final Method searchMethod = new Method("hasActiveAttachedCarriage", Type.BOOLEAN_TYPE, new Type[0]);

    @Override
    public Pattern filter() {
        return Pattern.compile(
            Pattern.quote(physicsShaft.getInternalName())
        );
    }

    public boolean process(ClassNode targetClass) {
        if(!targetClass.name.equals(physicsShaft.getInternalName())) return false;

        MethodNode found = findMethod(targetClass, generatedMethod, searchMethod);
        if(found != null) {
            addMethod(targetClass, found);
            return true;
        }

        return false;

    }

    static @Nullable MethodNode findMethod(ClassNode targetClass, Method generatedMethod, Method searchMethod) {
        MethodNode found = null;
        for(MethodNode method : targetClass.methods) {
            if(matches(method, generatedMethod)) {
                found = null;
                break;
            }
            if(matches(method, searchMethod)) found = method;

        }
        return found;
    }

    private static boolean matches(MethodNode method, Method searchMethod1) {
        return method.name.equals(searchMethod1.getName()) && method.desc.equals(searchMethod1.getDescriptor());
    }

    private static void addMethod(ClassNode targetClass, MethodNode method) {
        MethodNode node = new MethodNode(Opcodes.ACC_PUBLIC, generatedMethod.getName(), generatedMethod.getDescriptor(), null, null);

        method.accept(new MyMethodVisitor(node, method));
        targetClass.methods.add(node);

    }

    private static class MyMethodVisitor extends GeneratorAdapter {
        private final MethodNode node;
        private final InsnList instructions;
        private int carriageIndex = -1;

        public MyMethodVisitor(MethodNode node, MethodNode original) {
            super(Opcodes.ASM9, node, node.access, node.name, node.desc);
            this.node = node;
            instructions = node.instructions;
            for(LocalVariableNode localVariable : original.localVariables) {
                if(localVariable.name.equals("carriage")) {
                    this.carriageIndex = localVariable.index;
                }
            }

        }

        @Override
        public void visitInsn(int opcode) {
            if(opcode == Opcodes.IRETURN) {
                AbstractInsnNode last = instructions.getLast();
                boolean wasFalse = last.getOpcode() == Opcodes.ICONST_0;
                boolean wasTrue = last.getOpcode() == Opcodes.ICONST_1;
                if(wasTrue || wasFalse) {
                    instructions.remove(last);
                    if(wasFalse) {
                        pushNull();
                    } else {
                        loadCarriage();
                    }
                    returnValue();
                    return;
                }

                Label isTrue = new Label(), goAfter = new Label();

                ifICmp(Opcodes.IFGT, isTrue);
                pushNull();
                goTo(goAfter);

                visitLabel(isTrue);
                loadCarriage();

                visitLabel(goAfter);
                returnValue();
                return;
            }
            super.visitInsn(opcode);
        }

        private void loadCarriage() {
            loadLocal(carriageIndex + firstLocal);
        }

        private void pushNull() {
            push((String) null);
        }
    }
}
